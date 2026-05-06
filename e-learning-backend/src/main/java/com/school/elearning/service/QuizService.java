package com.school.elearning.service;

import com.school.elearning.dto.*;
import com.school.elearning.model.*;
import com.school.elearning.repository.*;
import com.school.elearning.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // Par défaut, tout est en lecture seule (performant)
public class QuizService {

    private final QuizRepository quizRepository;
    private final CoursRepository coursRepository;
    private final EnseignantRepository enseignantRepository;

    // ── CRÉER UN QUIZ ────────────────────────────────────────────────
    // L'enseignant peut seulement créer un quiz pour un cours dont
    // le module lui est affecté → vérification cours → module → enseignant
    @Transactional
    public QuizDetailEnseignantResponse creerQuiz(QuizRequest request, Authentication auth) {
        Enseignant enseignant = getEnseignantConnecte(auth);

        // 1. Récupérer le cours
        Cours cours = coursRepository.findById(request.getCoursId())
                .orElseThrow(() -> new RuntimeException("Cours introuvable : " + request.getCoursId()));

        // 2. Vérifier que le cours appartient à un module de CET enseignant
        if (!cours.getModule().getEnseignant().getId().equals(enseignant.getId())) {
            throw new RuntimeException(
                "Accès refusé : ce cours appartient au module d'un autre enseignant"
            );
        }
        
        if ( this.quizRepository.existsByTitre(request.getTitre() ) ) {
        	throw new RuntimeException("Cette quiz existe déjà !");
        }
        
        // 3. Valider les questions
        if (request.getQuestions() == null || request.getQuestions().isEmpty()) {
            throw new RuntimeException("Un quiz doit contenir au moins une question");
        }
        for (QuestionQuizRequest q : request.getQuestions()) {
            if (q.getChoixPossibles() == null || q.getChoixPossibles().size() < 2) {
                throw new RuntimeException(
                    "Chaque question doit avoir au moins 2 choix possibles"
                );
            }
            if (!q.getChoixPossibles().contains(q.getBonneReponse())) {
                throw new RuntimeException(
                    "La bonne réponse '" + q.getBonneReponse()
                    + "' doit être dans les choix possibles"
                );
            }
        }

        // 4. Créer le Quiz
        Quiz quiz = new Quiz();
        quiz.setTitre(request.getTitre());
        quiz.setDateDebut(request.getDateDebut());
        quiz.setDateFin(request.getDateFin());
        quiz.setNombreTentativesMax(request.getNombreTentativesMax());
        quiz.setCours(cours);
        // Le module est hérité de Evaluation — on le set via le module du cours
        quiz.setModule(cours.getModule());

        // 5. Créer les QuestionQuiz et les associer
        List<QuestionQuiz> questions = request.getQuestions().stream().map(req -> {
            QuestionQuiz q = new QuestionQuiz();
            q.setEnonce(req.getEnonce());
            q.setChoixPossibles(req.getChoixPossibles());
            q.setBonneReponse(req.getBonneReponse());
            q.setPoints(req.getPoints());
            q.setQuiz(quiz);
            return q;
        }).collect(Collectors.toList());

        quiz.setQuestions(questions);
        Quiz saved = quizRepository.save(quiz);
        return toDetailEnseignantResponse(saved);
    }

    // ── CONSULTER UN QUIZ (enseignant — avec bonneReponse) ───────────
    public QuizDetailEnseignantResponse getQuizEnseignant(Long quizId, Authentication auth) {
        Enseignant enseignant = getEnseignantConnecte(auth);
        Quiz quiz = findQuiz(quizId);
        verifierProprietaire(quiz, enseignant);
        return toDetailEnseignantResponse(quiz);
    }

    // ── MES QUIZ (tous les quiz de l'enseignant connecté) ────────────
    public List<QuizResponse> getMesQuizzes(Authentication auth) {
        Enseignant enseignant = getEnseignantConnecte(auth);
        return quizRepository.findByEnseignant(enseignant)
                .stream().map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── QUIZ PAR COURS ───────────────────────────────────────────────
    // Vérifie que le cours appartient à l'enseignant connecté
    public List<QuizResponse> getQuizzesByCours(Long coursId, Authentication auth) {
        Enseignant enseignant = getEnseignantConnecte(auth);
        Cours cours = coursRepository.findById(coursId)
                .orElseThrow(() -> new RuntimeException("Cours introuvable : " + coursId));

        if (!cours.getModule().getEnseignant().getId().equals(enseignant.getId())) {
            throw new RuntimeException("Accès refusé : ce cours ne vous appartient pas");
        }

        return quizRepository.findByCoursId(coursId)
                .stream().map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── SUPPRIMER UN QUIZ ────────────────────────────────────────────
    @Transactional
    public void supprimerQuiz(Long quizId, Authentication auth) {
        Enseignant enseignant = getEnseignantConnecte(auth);
        Quiz quiz = findQuiz(quizId);
        verifierProprietaire(quiz, enseignant);
        quizRepository.deleteById(quizId);
    }
    
    @Transactional
    public QuizDetailEnseignantResponse modifierQuiz(Long quizId, QuizRequest request, Authentication auth) {
        Enseignant enseignant = getEnseignantConnecte(auth);
        
        // 1. Récupérer le quiz existant
        Quiz quiz = findQuiz(quizId);
        
        // 2. Vérifier que l'enseignant est bien le propriétaire
        verifierProprietaire(quiz, enseignant);

        // 3. Mettre à jour les infos de base
        quiz.setTitre(request.getTitre());
        quiz.setDateDebut(request.getDateDebut());
        quiz.setDateFin(request.getDateFin());
        quiz.setNombreTentativesMax(request.getNombreTentativesMax());
        
     // Dans ta méthode modifierQuiz, ajoute ceci avant la gestion des questions :
        if (request.getCoursId() != null) {
            Cours nouveauCours = coursRepository.findById(request.getCoursId())
                    .orElseThrow(() -> new RuntimeException("Cours introuvable"));
                    
            // Sécurité : Vérifier que ce nouveau cours appartient aussi à l'enseignant
            if (!nouveauCours.getModule().getEnseignant().getId().equals(enseignant.getId())) {
                throw new RuntimeException("Accès refusé : ce cours ne vous appartient pas");
            }
            
            quiz.setCours(nouveauCours);
            quiz.setModule(nouveauCours.getModule()); // Mise à jour du module héritée
        }

        // 4. Gérer les questions : La méthode la plus propre est de vider les anciennes 
        // et de remettre les nouvelles (si tu ne veux pas gérer d'IDs spécifiques par question)
        quiz.getQuestions().clear(); 

        if (request.getQuestions() == null || request.getQuestions().isEmpty()) {
            throw new RuntimeException("Un quiz doit contenir au moins une question");
        }

        for (QuestionQuizRequest qReq : request.getQuestions()) {
            // Validation (Tu peux extraire ça dans une méthode privée pour ne pas répéter le code du creerQuiz)
            if (qReq.getChoixPossibles() == null || qReq.getChoixPossibles().size() < 2) {
                throw new RuntimeException("Chaque question doit avoir au moins 2 choix");
            }
            if (!qReq.getChoixPossibles().contains(qReq.getBonneReponse())) {
                throw new RuntimeException("La bonne réponse doit être dans les choix possibles");
            }

            QuestionQuiz nouvelleQuestion = new QuestionQuiz();
            nouvelleQuestion.setEnonce(qReq.getEnonce());
            nouvelleQuestion.setChoixPossibles(qReq.getChoixPossibles());
            nouvelleQuestion.setBonneReponse(qReq.getBonneReponse());
            nouvelleQuestion.setPoints(qReq.getPoints());
            nouvelleQuestion.setQuiz(quiz); // Lier au quiz
            
            quiz.getQuestions().add(nouvelleQuestion);
        }

        // 5. Sauvegarder (cascade s'occupera des questions si configuré dans l'entité)
        Quiz updated = quizRepository.save(quiz);
        return toDetailEnseignantResponse(updated);
    }

    // ══════════════════════════════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════════════════════════════

    private Quiz findQuiz(Long id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quiz introuvable : " + id));
    }

    private void verifierProprietaire(Quiz quiz, Enseignant enseignant) {
        if (!quiz.getCours().getModule().getEnseignant().getId().equals(enseignant.getId())) {
            throw new RuntimeException("Accès refusé : ce quiz ne vous appartient pas");
        }
    }

    private Enseignant getEnseignantConnecte(Authentication auth) {
        CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();
        return enseignantRepository.findById(details.getUtilisateur().getId())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé ou pas un enseignant"));
    }

    // ══════════════════════════════════════════════════════════════════
    // MAPPERS
    // ══════════════════════════════════════════════════════════════════

    // Vue liste — sans questions détaillées
    public QuizResponse toResponse(Quiz q) {
        QuizResponse r = new QuizResponse();
        r.setId(q.getId());
        r.setTitre(q.getTitre());
        r.setDateDebut(q.getDateDebut());
        r.setDateFin(q.getDateFin());
        r.setNombreTentativesMax(q.getNombreTentativesMax());
        r.setNombreQuestions(q.getQuestions() != null ? q.getQuestions().size() : 0);
        r.setPointsTotal(q.getQuestions() != null
            ? q.getQuestions().stream().mapToDouble(QuestionQuiz::getPoints).sum()
            : 0);
        if (q.getCours() != null) {
            r.setCoursId(q.getCours().getId());
            r.setCoursTitre(q.getCours().getTitre());
            if (q.getCours().getModule() != null) {
                r.setModuleId(q.getCours().getModule().getId());
                r.setModuleTitre(q.getCours().getModule().getTitre());
                Enseignant e = q.getCours().getModule().getEnseignant();
                if (e != null) {
                    r.setEnseignantNom(e.getNom());
                    r.setEnseignantPrenom(e.getPrenom());
                }
            }
        }
        return r;
    }

    // Vue détaillée ENSEIGNANT — avec bonneReponse
    private QuizDetailEnseignantResponse toDetailEnseignantResponse(Quiz q) {
        QuizDetailEnseignantResponse r = new QuizDetailEnseignantResponse();
        r.setId(q.getId());
        r.setTitre(q.getTitre());
        r.setDateDebut(q.getDateDebut());
        r.setDateFin(q.getDateFin());
        r.setNombreTentativesMax(q.getNombreTentativesMax());
        if (q.getCours() != null) {
            r.setCoursId(q.getCours().getId());
            r.setCoursTitre(q.getCours().getTitre());
            if (q.getCours().getModule() != null) {
                r.setModuleTitre(q.getCours().getModule().getTitre());
            }
        }
        r.setQuestions(q.getQuestions().stream().map(qq -> {
            QuestionQuizDetailEnseignantResponse qr = new QuestionQuizDetailEnseignantResponse();
            qr.setId(qq.getId());
            qr.setEnonce(qq.getEnonce());
            qr.setChoixPossibles(qq.getChoixPossibles());
            qr.setBonneReponse(qq.getBonneReponse()); // ✅ visible enseignant
            qr.setPoints(qq.getPoints());
            return qr;
        }).collect(Collectors.toList()));
        return r;
    }
    
    
    
}