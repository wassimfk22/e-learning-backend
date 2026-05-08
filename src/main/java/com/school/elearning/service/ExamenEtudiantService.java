package com.school.elearning.service;

import com.school.elearning.dto.*;
import com.school.elearning.model.*;
import com.school.elearning.model.enums.StatutPassageExamen;
import com.school.elearning.repository.*;
import com.school.elearning.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamenEtudiantService {

    private final ExamenModuleRepository examenRepository;
    private final PassageExamenRepository passageRepository;
    private final ReponseExamenEtudiantRepository reponseRepository;
    private final QuestionExamenRepository questionRepository;
    private final EtudiantRepository etudiantRepository;
    private final CoursRepository coursRepository;
    private final ExamenEnseignantService examenEnseignantService; // réutilise les mappers

    // ══════════════════════════════════════════════════════════════
    // 1. LISTER LES EXAMENS D'UN MODULE (avec statut étudiant)
    // ══════════════════════════════════════════════════════════════

    public List<ExamenAvecStatutResponse> getExamensModuleAvecStatut(Long moduleId, Authentication auth) {
        Etudiant etudiant = getEtudiantConnecte(auth);
        verifierAccesModule(etudiant, moduleId);

        return examenRepository.findByModuleId(moduleId).stream().map(examen -> {
            ExamenAvecStatutResponse r = new ExamenAvecStatutResponse();
            r.setExamen(examenEnseignantService.toExamenResponse(examen));

            passageRepository.findByEtudiantAndExamen(etudiant, examen).ifPresentOrElse(
                passage -> {
                    r.setStatut(passage.getStatut());
                    r.setPassageId(passage.getId());
                    r.setNoteFinale(passage.getNoteFinale());
                    r.setDejaPasse(true);
                },
                () -> {
                    r.setDejaPasse(false);
                    r.setStatut(null);
                }
            );
            return r;
        }).collect(Collectors.toList());
    }

    // ══════════════════════════════════════════════════════════════
    // 2. DÉMARRER UN EXAMEN (passage unique)
    // ══════════════════════════════════════════════════════════════

    @Transactional
    public PassageExamenResponse demarrerExamen(Long examenId, Authentication auth) {
        Etudiant etudiant = getEtudiantConnecte(auth);
        ExamenModule examen = findExamen(examenId);

        verifierAccesModule(etudiant, examen.getModule().getId());

        // Passage unique
        Optional<PassageExamen> existant = passageRepository.findByEtudiantAndExamen(etudiant, examen);
        if (existant.isPresent()) {
            PassageExamen p = existant.get();
            if (p.getStatut() == StatutPassageExamen.SOUMIS || p.getStatut() == StatutPassageExamen.CORRIGE) {
                throw new RuntimeException("Vous avez déjà passé cet examen.");
            }
            // EN_COURS encore valide
            if (!p.estExpire()) return examenEnseignantService.toPassageResponse(p);
            // Expiré → soumettre automatiquement
            return examenEnseignantService.toPassageResponse(soumettreAutomatiquement(p));
        }

        double scoreMax = examen.getQuestions().stream()
                .mapToDouble(QuestionExamen::getPoints).sum();

        LocalDateTime maintenant = LocalDateTime.now();
        PassageExamen passage = new PassageExamen();
        passage.setEtudiant(etudiant);
        passage.setExamen(examen);
        passage.setStatut(StatutPassageExamen.EN_COURS);
        passage.setScoreMax(scoreMax);
        passage.setDateDebut(maintenant);
        passage.setDateExpiration(maintenant.plusMinutes(examen.getDureeMinutes()));

        return examenEnseignantService.toPassageResponse(passageRepository.save(passage));
    }

    // ══════════════════════════════════════════════════════════════
    // 3. SOUMETTRE L'EXAMEN (toutes les réponses d'un coup)
    // ══════════════════════════════════════════════════════════════

    @Transactional
    public PassageExamenResponse soumettreExamen(Long passageId,
                                                  SoumissionExamenRequest request,
                                                  Authentication auth) {
        Etudiant etudiant = getEtudiantConnecte(auth);
        PassageExamen passage = findPassage(passageId, etudiant);

        if (passage.getStatut() != StatutPassageExamen.EN_COURS) {
            throw new RuntimeException("Cet examen a déjà été soumis.");
        }

        // ── NOUVEAU : vérifier si le temps est écoulé ──────────────
        if (passage.estExpire()) {
            // Soumettre automatiquement avec ce qui a été saisi, puis rejeter
            soumettreAutomatiquement(passage);
            throw new RuntimeException("⏰ Temps écoulé ! L'examen a été soumis automatiquement avec vos réponses actuelles.");
        }
        // ───────────────────────────────────────────────────────────

        // Enregistrer les réponses
        for (SoumissionExamenRequest.ReponseItem item : request.getReponses()) {
            QuestionExamen question = questionRepository.findById(item.getQuestionId())
                    .orElseThrow(() -> new RuntimeException("Question introuvable : " + item.getQuestionId()));

            if (reponseRepository.findByPassageIdAndQuestionId(passageId, question.getId()).isPresent()) {
                continue;
            }

            ReponseExamenEtudiant reponse = new ReponseExamenEtudiant();
            reponse.setPassage(passage);
            reponse.setQuestion(question);
            reponse.setReponseTexte(item.getReponseTexte());
            reponse.setEstJuste(null);
            reponse.setPointsObtenus(0);
            reponseRepository.save(reponse);
        }

        passage.setStatut(StatutPassageExamen.SOUMIS);
        passage.setDateSoumission(LocalDateTime.now());
        passageRepository.save(passage);

        return examenEnseignantService.toPassageResponse(passage);
    }

    // ══════════════════════════════════════════════════════════════
    // 4. SOUMISSION PAR EXPIRATION
    // ══════════════════════════════════════════════════════════════

    @Transactional
    public PassageExamenResponse soumettreParExpiration(Long passageId, Authentication auth) {
        Etudiant etudiant = getEtudiantConnecte(auth);
        PassageExamen passage = findPassage(passageId, etudiant);

        if (passage.getStatut() != StatutPassageExamen.EN_COURS) {
            throw new RuntimeException("Cet examen a déjà été soumis.");
        }
        if (!passage.estExpire()) {
            throw new RuntimeException("Le temps n'est pas encore écoulé.");
        }

        return examenEnseignantService.toPassageResponse(soumettreAutomatiquement(passage));
    }

    // ══════════════════════════════════════════════════════════════
    // 5. MON RÉSULTAT D'UN EXAMEN
    // ══════════════════════════════════════════════════════════════

    public PassageExamenResponse getMonResultat(Long examenId, Authentication auth) {
        Etudiant etudiant = getEtudiantConnecte(auth);
        ExamenModule examen = findExamen(examenId);

        PassageExamen passage = passageRepository.findByEtudiantAndExamen(etudiant, examen)
                .orElseThrow(() -> new RuntimeException("Vous n'avez pas encore passé cet examen."));

        return examenEnseignantService.toPassageResponse(passage);
    }
    
	 // ══════════════════════════════════════════════════════════════
	 // 6. LISTER TOUS LES EXAMENS DU NIVEAU DE L'ÉTUDIANT
	 // ══════════════════════════════════════════════════════════════
	
	 public List<ExamenAvecStatutResponse> getAllExamensMonNiveau(Authentication auth) {
	     Etudiant etudiant = getEtudiantConnecte(auth);
	
	     // 1. Récupérer le niveau de l'étudiant (via sa communauté)
	     if (etudiant.getCommunaute() == null || etudiant.getCommunaute().getNiveau() == null) {
	         throw new RuntimeException("Vous n'êtes affecté à aucun niveau.");
	     }
	     Long niveauId = etudiant.getCommunaute().getNiveau().getId();
	
	     // 2. Récupérer tous les examens qui appartiennent aux modules de ce niveau
	     // Supposons que tu as une méthode findByModule_Niveau_Id dans ton repository
	     List<ExamenModule> examensNiveau = examenRepository.findByModule_Niveau_Id(niveauId);
	
	     // 3. Mapper vers la réponse avec le statut de l'étudiant
	     return examensNiveau.stream().map(examen -> {
	         ExamenAvecStatutResponse r = new ExamenAvecStatutResponse();
	         r.setExamen(examenEnseignantService.toExamenResponse(examen));
	
	         passageRepository.findByEtudiantAndExamen(etudiant, examen).ifPresentOrElse(
	             passage -> {
	                 r.setStatut(passage.getStatut());
	                 r.setPassageId(passage.getId());
	                 r.setNoteFinale(passage.getNoteFinale());
	                 r.setDejaPasse(true);
	             },
	             () -> {
	                 r.setDejaPasse(false);
	                 r.setStatut(null);
	             }
	         );
	         return r;
	     }).collect(Collectors.toList());
	 }

    // ══════════════════════════════════════════════════════════════
    // SOUMISSION AUTOMATIQUE (interne)
    // ══════════════════════════════════════════════════════════════

    @Transactional
    private PassageExamen soumettreAutomatiquement(PassageExamen passage) {
        passage.setStatut(StatutPassageExamen.SOUMIS);
        passage.setDateSoumission(passage.getDateExpiration());
        return passageRepository.save(passage);
    }

    // ══════════════════════════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════════════════════════

    private void verifierAccesModule(Etudiant etudiant, Long moduleId) {
        if (etudiant.getCommunaute() == null || etudiant.getCommunaute().getNiveau() == null) {
            throw new RuntimeException("Vous n'êtes affecté à aucun niveau.");
        }
        Long niveauId = etudiant.getCommunaute().getNiveau().getId();
        boolean accesOk = examenRepository.findByModuleId(moduleId).stream()
                .findFirst()
                .map(e -> e.getModule().getNiveau().getId().equals(niveauId))
                .orElse(true); // si pas d'examen, on laisse passer (liste vide)

        // Vérification directe sur le module
        if (!accesOk) {
            throw new RuntimeException("Accès refusé : ce module n'appartient pas à votre niveau.");
        }
    }

    private Etudiant getEtudiantConnecte(Authentication auth) {
        CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();
        return etudiantRepository.findById(details.getUtilisateur().getId())
                .orElseThrow(() -> new RuntimeException("Pas un étudiant"));
    }

    private ExamenModule findExamen(Long id) {
        return examenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Examen introuvable : " + id));
    }

    private PassageExamen findPassage(Long passageId, Etudiant etudiant) {
        PassageExamen passage = passageRepository.findById(passageId)
                .orElseThrow(() -> new RuntimeException("Passage introuvable"));
        if (!passage.getEtudiant().getId().equals(etudiant.getId())) {
            throw new RuntimeException("Accès refusé.");
        }
        return passage;
    }

    // DTO interne
    @lombok.Data
    public static class ExamenAvecStatutResponse {
        private ExamenResponse examen;
        private StatutPassageExamen statut;
        private Long passageId;
        private double noteFinale;
        private boolean dejaPasse;
    }
    
    
    
}