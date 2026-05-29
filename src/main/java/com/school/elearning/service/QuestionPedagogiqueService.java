package com.school.elearning.service;

import com.school.elearning.dto.*;
import com.school.elearning.model.*;
import com.school.elearning.repository.*;
import com.school.elearning.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionPedagogiqueService {

    private final QuestionPedagogiqueRepository questionRepo;
    private final ReponsePedagogiqueRepository reponseRepo;
    private final CoursRepository coursRepository;
    private final EtudiantRepository etudiantRepository;
    private final EnseignantRepository enseignantRepository;
    private final MessageBoiteRepository messageBoiteRepository;

    // ══════════════════════════════════════════════════════════════
    // 1. ÉTUDIANT : POSER UNE QUESTION (pendant la visite d'un cours)
    //    → Enregistre la question
    //    → Envoie un message dans la boite de réception du prof
    // ══════════════════════════════════════════════════════════════

    @Transactional
    public QuestionPedagogiqueResponse poserQuestion(QuestionPedagogiqueRequest request,
                                                      Authentication auth) {
        CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();
        Etudiant etudiant = etudiantRepository.findById(details.getUtilisateur().getId())
                .orElseThrow(() -> new RuntimeException("Pas un étudiant"));

        Cours cours = coursRepository.findById(request.getCoursId())
                .orElseThrow(() -> new RuntimeException("Cours introuvable : " + request.getCoursId()));

        if (request.getEnonce() == null || request.getEnonce().isBlank())
            throw new RuntimeException("L'énoncé de la question est obligatoire.");

        // Créer la question
        QuestionPedagogique question = new QuestionPedagogique();
        question.setEnonce(request.getEnonce().trim());
        question.setDatePost(new Date());
        question.setEtudiant(etudiant);
        question.setCours(cours);

        QuestionPedagogique saved = questionRepo.save(question);

        // Envoyer dans la boite de réception de l'enseignant du cours
        Enseignant enseignant = cours.getModule().getEnseignant();
        if (enseignant != null && enseignant.getBoiteReception() != null) {
            String messageContenu = "❓ Question de " + etudiant.getNom() + " "
                    + etudiant.getPrenom() + " sur le cours « " + cours.getTitre() + " » :\n\n"
                    + request.getEnonce();

            MessageBoite messageBoite = new MessageBoite();
            messageBoite.setContenu(messageContenu);
            messageBoite.setExpediteur(etudiant);
            messageBoite.setBoiteReception(enseignant.getBoiteReception());
            messageBoiteRepository.save(messageBoite);
        }

        return toResponse(saved);
    }

    // ══════════════════════════════════════════════════════════════
    // 2. ENSEIGNANT : LISTER LES QUESTIONS D'UN COURS
    //    → Filtre optionnel : non répondues seulement
    // ══════════════════════════════════════════════════════════════

    public List<QuestionPedagogiqueResponse> getQuestionsByCours(Long coursId,
                                                                   boolean sansReponse,
                                                                   Authentication auth) {
        Enseignant enseignant = getEnseignantConnecte(auth);
        Cours cours = coursRepository.findById(coursId)
                .orElseThrow(() -> new RuntimeException("Cours introuvable : " + coursId));

        if (!cours.getModule().getEnseignant().getId().equals(enseignant.getId())) {
            throw new RuntimeException("Accès refusé : ce cours ne vous appartient pas.");
        }

        List<QuestionPedagogique> questions = sansReponse
                ? questionRepo.findByCoursAndReponseIsNull(cours)
                : questionRepo.findByCours(cours);

        return questions.stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ══════════════════════════════════════════════════════════════
    // 3. ENSEIGNANT : RÉPONDRE À UNE QUESTION
    //    → Enregistre la réponse
    //    → Lie la réponse à la question
    //    → Envoie un message dans la boite de réception de l'étudiant
    // ══════════════════════════════════════════════════════════════

    @Transactional
    public QuestionPedagogiqueResponse repondreQuestion(ReponsePedagogiqueRequest request,
                                                         Authentication auth) {
        Enseignant enseignant = getEnseignantConnecte(auth);

        QuestionPedagogique question = questionRepo.findById(request.getQuestionId())
                .orElseThrow(() -> new RuntimeException("Question introuvable : " + request.getQuestionId()));

        // Vérifier que le cours appartient bien à cet enseignant
        if (!question.getCours().getModule().getEnseignant().getId().equals(enseignant.getId())) {
            throw new RuntimeException("Accès refusé : cette question ne concerne pas un de vos cours.");
        }

        if (question.getReponse() != null) {
            throw new RuntimeException("Cette question a déjà une réponse. Utilisez la modification.");
        }

        if (request.getContenu() == null || request.getContenu().isBlank())
            throw new RuntimeException("Le contenu de la réponse est obligatoire.");

        // Créer la réponse
        ReponsePedagogique reponse = new ReponsePedagogique();
        reponse.setContenu(request.getContenu().trim());
        reponse.setDateReponse(new Date());
        reponse.setEnseignant(enseignant);
        reponse.setQuestionPedagogique(question);

        reponseRepo.save(reponse);

        // Lier la réponse à la question
        question.setReponse(reponse);
        QuestionPedagogique updated = questionRepo.save(question);

        // Envoyer dans la boite de réception de l'étudiant
        Etudiant etudiant = question.getEtudiant();
        if (etudiant != null && etudiant.getBoiteReception() != null) {
            String messageContenu = "✅ Réponse de " + enseignant.getNom() + " "
                    + enseignant.getPrenom() + " à votre question sur « "
                    + question.getCours().getTitre() + " » :\n\n"
                    + "📋 Votre question : " + question.getEnonce() + "\n\n"
                    + "💬 Réponse : " + request.getContenu();

            MessageBoite messageBoite = new MessageBoite();
            messageBoite.setContenu(messageContenu);
            messageBoite.setExpediteur(enseignant);
            messageBoite.setBoiteReception(etudiant.getBoiteReception());
            messageBoiteRepository.save(messageBoite);
        }

        return toResponse(updated);
    }

    // ══════════════════════════════════════════════════════════════
    // 4. ÉTUDIANT : MES QUESTIONS (avec leurs réponses)
    // ══════════════════════════════════════════════════════════════

    public List<QuestionPedagogiqueResponse> getMesQuestions(Authentication auth) {
        CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();
        Etudiant etudiant = etudiantRepository.findById(details.getUtilisateur().getId())
                .orElseThrow(() -> new RuntimeException("Pas un étudiant"));

        return questionRepo.findByEtudiant(etudiant)
                .stream().map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ══════════════════════════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════════════════════════

    private Enseignant getEnseignantConnecte(Authentication auth) {
        CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();
        return enseignantRepository.findById(details.getUtilisateur().getId())
                .orElseThrow(() -> new RuntimeException("Pas un enseignant"));
    }

    // ══════════════════════════════════════════════════════════════
    // MAPPER
    // ══════════════════════════════════════════════════════════════

    private QuestionPedagogiqueResponse toResponse(QuestionPedagogique q) {
        QuestionPedagogiqueResponse r = new QuestionPedagogiqueResponse();
        r.setId(q.getId());
        r.setEnonce(q.getEnonce());
        r.setDatePost(q.getDatePost());

        r.setEtudiantId(q.getEtudiant().getId());
        r.setEtudiantNom(q.getEtudiant().getNom());
        r.setEtudiantPrenom(q.getEtudiant().getPrenom());
        r.setEtudiantPhoto(q.getEtudiant().getPhoto());

        r.setCoursId(q.getCours().getId());
        r.setCoursTitre(q.getCours().getTitre());

        if (q.getReponse() != null) {
            ReponsePedagogique rep = q.getReponse();
            QuestionPedagogiqueResponse.ReponsePedagogiqueInfo info =
                    new QuestionPedagogiqueResponse.ReponsePedagogiqueInfo();
            info.setId(rep.getId());
            info.setContenu(rep.getContenu());
            info.setDateReponse(rep.getDateReponse());
            info.setEnseignantId(rep.getEnseignant().getId());
            info.setEnseignantNom(rep.getEnseignant().getNom());
            info.setEnseignantPrenom(rep.getEnseignant().getPrenom());
            r.setReponse(info);
        }

        return r;
    }
}