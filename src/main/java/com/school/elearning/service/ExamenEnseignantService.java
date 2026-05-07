package com.school.elearning.service;

import com.school.elearning.dto.*;
import com.school.elearning.model.*;
import com.school.elearning.model.Module;
import com.school.elearning.model.enums.StatutPassageExamen;
import com.school.elearning.repository.*;
import com.school.elearning.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamenEnseignantService {

    private final ExamenModuleRepository examenRepository;
    private final QuestionExamenRepository questionRepository;
    private final PassageExamenRepository passageRepository;
    private final ReponseExamenEtudiantRepository reponseRepository;
    private final EnseignantRepository enseignantRepository;
    private final ModuleRepository moduleRepository;
    private final ResultatRepository resultatRepository;
    private final ProgressionModuleRepository progressionRepository;

    // ══════════════════════════════════════════════════════════════
    // 1. CRÉER UN EXAMEN
    // ══════════════════════════════════════════════════════════════

    @Transactional
    public ExamenResponse creerExamen(ExamenRequest request, Authentication auth) {
        Enseignant enseignant = getEnseignantConnecte(auth);
        Module module = moduleRepository.findById(request.getModuleId())
                .orElseThrow(() -> new RuntimeException("Module introuvable : " + request.getModuleId()));

        if (!module.getEnseignant().getId().equals(enseignant.getId())) {
            throw new RuntimeException("Accès refusé : ce module ne vous appartient pas.");
        }
        if (request.getQuestions() == null || request.getQuestions().isEmpty()) {
            throw new RuntimeException("Un examen doit avoir au moins une question.");
        }

        ExamenModule examen = new ExamenModule();
        examen.setTitre(request.getTitre());
        examen.setDescription(request.getDescription());
        examen.setDureeMinutes(request.getDureeMinutes());
        examen.setModule(module);
        examen.setEnseignant(enseignant);

        List<QuestionExamen> questions = request.getQuestions().stream().map(req -> {
            QuestionExamen q = new QuestionExamen();
            q.setEnonce(req.getEnonce());
            q.setPoints(req.getPoints());
            q.setExamen(examen);
            return q;
        }).collect(Collectors.toList());

        examen.setQuestions(questions);
        return toExamenResponse(examenRepository.save(examen));
    }

    // ══════════════════════════════════════════════════════════════
    // 2. MODIFIER UN EXAMEN
    // ══════════════════════════════════════════════════════════════

    @Transactional
    public ExamenResponse modifierExamen(Long examenId, ExamenRequest request, Authentication auth) {
        Enseignant enseignant = getEnseignantConnecte(auth);
        ExamenModule examen = findExamen(examenId);
        verifierProprietaire(examen, enseignant);

        examen.setTitre(request.getTitre());
        examen.setDescription(request.getDescription());
        examen.setDureeMinutes(request.getDureeMinutes());

        examen.getQuestions().clear();
        request.getQuestions().forEach(req -> {
            QuestionExamen q = new QuestionExamen();
            q.setEnonce(req.getEnonce());
            q.setPoints(req.getPoints());
            q.setExamen(examen);
            examen.getQuestions().add(q);
        });

        return toExamenResponse(examenRepository.save(examen));
    }

    // ══════════════════════════════════════════════════════════════
    // 3. MES EXAMENS
    // ══════════════════════════════════════════════════════════════

    public List<ExamenResponse> getMesExamens(Authentication auth) {
        Enseignant enseignant = getEnseignantConnecte(auth);
        return examenRepository.findByEnseignant(enseignant)
                .stream().map(this::toExamenResponse)
                .collect(Collectors.toList());
    }

    // ══════════════════════════════════════════════════════════════
    // 4. EXAMENS PAR MODULE
    // ══════════════════════════════════════════════════════════════

    public List<ExamenResponse> getExamensParModule(Long moduleId, Authentication auth) {
        Enseignant enseignant = getEnseignantConnecte(auth);
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module introuvable"));
        if (!module.getEnseignant().getId().equals(enseignant.getId())) {
            throw new RuntimeException("Accès refusé.");
        }
        return examenRepository.findByModuleId(moduleId)
                .stream().map(this::toExamenResponse)
                .collect(Collectors.toList());
    }

    // ══════════════════════════════════════════════════════════════
    // 5. VOIR LES COPIES SOUMISES (pour corriger)
    // ══════════════════════════════════════════════════════════════

    public List<PassageExamenResponse> getCopiesACorreiger(Long examenId, Authentication auth) {
        Enseignant enseignant = getEnseignantConnecte(auth);
        ExamenModule examen = findExamen(examenId);
        verifierProprietaire(examen, enseignant);

        return passageRepository.findByExamenAndStatut(examen, StatutPassageExamen.SOUMIS)
                .stream().map(this::toPassageResponse)
                .collect(Collectors.toList());
    }

    public List<PassageExamenResponse> getToutesCopies(Long examenId, Authentication auth) {
        Enseignant enseignant = getEnseignantConnecte(auth);
        ExamenModule examen = findExamen(examenId);
        verifierProprietaire(examen, enseignant);

        // SOUMIS + CORRIGE
        List<PassageExamen> copies = passageRepository.findByExamenAndStatut(examen, StatutPassageExamen.SOUMIS);
        copies.addAll(passageRepository.findByExamenAndStatut(examen, StatutPassageExamen.CORRIGE));
        return copies.stream().map(this::toPassageResponse).collect(Collectors.toList());
    }

    // ══════════════════════════════════════════════════════════════
    // 6. CORRIGER UNE RÉPONSE (juste ou faux)
    //    Le prof corrige réponse par réponse.
    //    Quand toutes les réponses sont corrigées → noteFinale calculée automatiquement
    // ══════════════════════════════════════════════════════════════

    @Transactional
    public PassageExamenResponse corrigerReponse(Long passageId,
                                                  CorrectionReponseRequest request,
                                                  Authentication auth) {
        Enseignant enseignant = getEnseignantConnecte(auth);

        PassageExamen passage = passageRepository.findById(passageId)
                .orElseThrow(() -> new RuntimeException("Passage introuvable : " + passageId));

        verifierProprietaire(passage.getExamen(), enseignant);

        if (passage.getStatut() == StatutPassageExamen.CORRIGE) {
            throw new RuntimeException("Ce passage est déjà entièrement corrigé.");
        }

        // Trouver et corriger la réponse
        ReponseExamenEtudiant reponse = reponseRepository.findById(request.getReponseId())
                .orElseThrow(() -> new RuntimeException("Réponse introuvable"));

        if (!reponse.getPassage().getId().equals(passageId)) {
            throw new RuntimeException("Cette réponse n'appartient pas à ce passage.");
        }

        reponse.setEstJuste(request.isEstJuste());
        reponse.setPointsObtenus(request.isEstJuste() ? reponse.getQuestion().getPoints() : 0);
        reponseRepository.save(reponse);

        // Vérifier si toutes les réponses sont corrigées
        long totalReponses = passage.getReponses().size();
        long reponsesCorrigees = reponseRepository.countByPassageAndEstJusteIsNotNull(passage);

        if (reponsesCorrigees >= totalReponses) {
            // Calcul de la note finale
            double scoreObtenu = reponseRepository.sumPointsByPassage(passage);
            double scoreMax = passage.getScoreMax();
            double noteFinale = scoreMax > 0
                    ? Math.round((scoreObtenu / scoreMax) * 20 * 100.0) / 100.0
                    : 0;

            passage.setNoteObtenue(scoreObtenu);
            passage.setNoteFinale(noteFinale);
            passage.setStatut(StatutPassageExamen.CORRIGE);
            passage.setDateCorrection(LocalDateTime.now());
            passageRepository.save(passage);

            // Mettre à jour le Resultat de l'étudiant
            mettreAJourResultatEtudiant(passage);
        }

        return toPassageResponse(passage);
    }

    // ══════════════════════════════════════════════════════════════
    // 7. SUPPRIMER UN EXAMEN
    // ══════════════════════════════════════════════════════════════

    @Transactional
    public void supprimerExamen(Long examenId, Authentication auth) {
        Enseignant enseignant = getEnseignantConnecte(auth);
        ExamenModule examen = findExamen(examenId);
        verifierProprietaire(examen, enseignant);
        examenRepository.deleteById(examenId);
    }

    // ══════════════════════════════════════════════════════════════
    // MISE À JOUR RÉSULTAT APRÈS CORRECTION COMPLÈTE
    // ══════════════════════════════════════════════════════════════

    @Transactional
    private void mettreAJourResultatEtudiant(PassageExamen passage) {
        Etudiant etudiant = passage.getEtudiant();
        Module module = passage.getExamen().getModule();

        // Récupérer ou créer le Resultat
        Resultat resultat = resultatRepository
                .findByEtudiantIdAndModuleId(etudiant.getId(), module.getId())
                .orElseGet(() -> {
                    Resultat r = new Resultat();
                    r.setEtudiant(etudiant);
                    r.setModule(module);
                    r.setMoyenneQuizs(0f);
                    r.setMoyenneExamens(0f);
                    r.setMoyenneGenerale(0f);
                    return resultatRepository.save(r);
                });

        // Recalculer moyenne des examens corrigés pour ce module
        List<PassageExamen> examensCoriges = passageRepository
                .findCorigesParModuleId(etudiant, module.getId());

        float nouvelleMoyenne = examensCoriges.isEmpty() ? 0f :
                (float) examensCoriges.stream()
                        .mapToDouble(PassageExamen::getNoteFinale)
                        .average().orElse(0);

        resultat.setMoyenneExamens(nouvelleMoyenne);
        resultat.setMoyenneGenerale((resultat.getMoyenneQuizs() + nouvelleMoyenne) / 2);
        resultatRepository.save(resultat);

        // Mettre à jour la progression du module
        progressionRepository.findByEtudiant(etudiant).stream()
                .filter(p -> p.getModule().getId().equals(module.getId()))
                .findFirst()
                .ifPresent(progression -> {
                    // On boost légèrement la complétion si l'examen est corrigé
                    if (progression.getPourcentageCompletude() < 100f) {
                        float actuel = progression.getPourcentageCompletude();
                        progression.setPourcentageCompletude(Math.min(100f, actuel + 10f));
                    }
                    progressionRepository.save(progression);
                });
    }

    // ══════════════════════════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════════════════════════

    private ExamenModule findExamen(Long id) {
        return examenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Examen introuvable : " + id));
    }

    private void verifierProprietaire(ExamenModule examen, Enseignant enseignant) {
        if (!examen.getEnseignant().getId().equals(enseignant.getId())) {
            throw new RuntimeException("Accès refusé : cet examen ne vous appartient pas.");
        }
    }

    private Enseignant getEnseignantConnecte(Authentication auth) {
        CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();
        return enseignantRepository.findById(details.getUtilisateur().getId())
                .orElseThrow(() -> new RuntimeException("Pas un enseignant"));
    }

    // ══════════════════════════════════════════════════════════════
    // MAPPERS
    // ══════════════════════════════════════════════════════════════

    public ExamenResponse toExamenResponse(ExamenModule e) {
        ExamenResponse r = new ExamenResponse();
        r.setId(e.getId());
        r.setTitre(e.getTitre());
        r.setDescription(e.getDescription());
        r.setDureeMinutes(e.getDureeMinutes());
        r.setDateCreation(e.getDateCreation());
        r.setModuleId(e.getModule().getId());
        r.setModuleTitre(e.getModule().getTitre());
        r.setEnseignantNom(e.getEnseignant().getNom());
        r.setEnseignantPrenom(e.getEnseignant().getPrenom());
        r.setNombreQuestions(e.getQuestions().size());
        r.setScoreMax(e.getQuestions().stream().mapToDouble(QuestionExamen::getPoints).sum());
        r.setQuestions(e.getQuestions().stream().map(q -> {
            ExamenResponse.QuestionExamenResponse qr = new ExamenResponse.QuestionExamenResponse();
            qr.setId(q.getId());
            qr.setEnonce(q.getEnonce());
            qr.setPoints(q.getPoints());
            return qr;
        }).collect(Collectors.toList()));
        return r;
    }

    public PassageExamenResponse toPassageResponse(PassageExamen p) {
        PassageExamenResponse r = new PassageExamenResponse();
        r.setId(p.getId());
        r.setExamenId(p.getExamen().getId());
        r.setExamenTitre(p.getExamen().getTitre());
        r.setStatut(p.getStatut());
        r.setDateDebut(p.getDateDebut());
        r.setDateExpiration(p.getDateExpiration());
        r.setDateSoumission(p.getDateSoumission());
        r.setDateCorrection(p.getDateCorrection());
        r.setNoteObtenue(p.getNoteObtenue());
        r.setScoreMax(p.getScoreMax());
        r.setNoteFinale(p.getNoteFinale());

        long secondesRestantes = -1;
        if (p.getStatut() == StatutPassageExamen.EN_COURS && p.getDateExpiration() != null) {
            secondesRestantes = Math.max(0,
                java.time.temporal.ChronoUnit.SECONDS.between(LocalDateTime.now(), p.getDateExpiration()));
        }
        r.setSecondesRestantes(secondesRestantes);

        r.setReponses(p.getReponses().stream().map(rep -> {
            PassageExamenResponse.ReponseExamenResponse rr = new PassageExamenResponse.ReponseExamenResponse();
            rr.setId(rep.getId());
            rr.setQuestionId(rep.getQuestion().getId());
            rr.setQuestionEnonce(rep.getQuestion().getEnonce());
            rr.setQuestionPoints(rep.getQuestion().getPoints());
            rr.setReponseTexte(rep.getReponseTexte());
            rr.setEstJuste(rep.getEstJuste());
            rr.setPointsObtenus(rep.getPointsObtenus());
            return rr;
        }).collect(Collectors.toList()));

        return r;
    }
}