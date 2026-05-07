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
@Transactional(readOnly = true)
public class QuizService {

    private final QuizRepository quizRepository;
    private final CoursRepository coursRepository;
    private final EnseignantRepository enseignantRepository;

    // ── CRÉER ────────────────────────────────────────────────────────

    @Transactional
    public QuizDetailEnseignantResponse creerQuiz(QuizRequest request, Authentication auth) {
        Enseignant enseignant = getEnseignantConnecte(auth);

        Cours cours = coursRepository.findById(request.getCoursId())
                .orElseThrow(() -> new RuntimeException("Cours introuvable : " + request.getCoursId()));

        if (!cours.getModule().getEnseignant().getId().equals(enseignant.getId())) {
            throw new RuntimeException("Accès refusé : ce cours appartient au module d'un autre enseignant");
        }

        if (quizRepository.existsByTitre(request.getTitre())) {
            throw new RuntimeException("Un quiz avec ce titre existe déjà !");
        }

        // Validation : nombreQuestions ne peut pas dépasser le nombre de questions saisies
        if (request.getNombreQuestions() > request.getQuestions().size()) {
            throw new RuntimeException(
                "Le nombre de questions à afficher (" + request.getNombreQuestions()
                + ") ne peut pas dépasser le nombre de questions saisies ("
                + request.getQuestions().size() + ")"
            );
        }

        validerQuestions(request.getQuestions());

        Quiz quiz = new Quiz();
        quiz.setTitre(request.getTitre());
        quiz.setDureeMinutes(request.getDureeMinutes());
        quiz.setNombreQuestions(request.getNombreQuestions());
        quiz.setCours(cours);
        quiz.setModule(cours.getModule());

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
        return toDetailEnseignantResponse(quizRepository.save(quiz));
    }

    // ── MODIFIER ─────────────────────────────────────────────────────

    @Transactional
    public QuizDetailEnseignantResponse modifierQuiz(Long quizId, QuizRequest request, Authentication auth) {
        Enseignant enseignant = getEnseignantConnecte(auth);
        Quiz quiz = findQuiz(quizId);
        verifierProprietaire(quiz, enseignant);

        if (request.getNombreQuestions() > request.getQuestions().size()) {
            throw new RuntimeException(
                "Le nombre de questions à afficher (" + request.getNombreQuestions()
                + ") ne peut pas dépasser le nombre de questions saisies ("
                + request.getQuestions().size() + ")"
            );
        }

        validerQuestions(request.getQuestions());

        quiz.setTitre(request.getTitre());
        quiz.setDureeMinutes(request.getDureeMinutes());
        quiz.setNombreQuestions(request.getNombreQuestions());

        if (request.getCoursId() != null) {
            Cours nouveauCours = coursRepository.findById(request.getCoursId())
                    .orElseThrow(() -> new RuntimeException("Cours introuvable"));
            if (!nouveauCours.getModule().getEnseignant().getId().equals(enseignant.getId())) {
                throw new RuntimeException("Accès refusé : ce cours ne vous appartient pas");
            }
            quiz.setCours(nouveauCours);
            quiz.setModule(nouveauCours.getModule());
        }

        quiz.getQuestions().clear();
        request.getQuestions().forEach(req -> {
            QuestionQuiz q = new QuestionQuiz();
            q.setEnonce(req.getEnonce());
            q.setChoixPossibles(req.getChoixPossibles());
            q.setBonneReponse(req.getBonneReponse());
            q.setPoints(req.getPoints());
            q.setQuiz(quiz);
            quiz.getQuestions().add(q);
        });

        return toDetailEnseignantResponse(quizRepository.save(quiz));
    }

    // ── READ ─────────────────────────────────────────────────────────

    public QuizDetailEnseignantResponse getQuizEnseignant(Long quizId, Authentication auth) {
        Enseignant enseignant = getEnseignantConnecte(auth);
        Quiz quiz = findQuiz(quizId);
        verifierProprietaire(quiz, enseignant);
        return toDetailEnseignantResponse(quiz);
    }

    public List<QuizResponse> getMesQuizzes(Authentication auth) {
        return quizRepository.findByEnseignant(getEnseignantConnecte(auth))
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<QuizResponse> getQuizzesByCours(Long coursId, Authentication auth) {
        Enseignant enseignant = getEnseignantConnecte(auth);
        Cours cours = coursRepository.findById(coursId)
                .orElseThrow(() -> new RuntimeException("Cours introuvable : " + coursId));
        if (!cours.getModule().getEnseignant().getId().equals(enseignant.getId())) {
            throw new RuntimeException("Accès refusé : ce cours ne vous appartient pas");
        }
        return quizRepository.findByCoursId(coursId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    // ── SUPPRIMER ────────────────────────────────────────────────────

    @Transactional
    public void supprimerQuiz(Long quizId, Authentication auth) {
        Enseignant enseignant = getEnseignantConnecte(auth);
        Quiz quiz = findQuiz(quizId);
        verifierProprietaire(quiz, enseignant);
        quizRepository.deleteById(quizId);
    }

    // ── HELPERS ──────────────────────────────────────────────────────

    private void validerQuestions(List<QuestionQuizRequest> questions) {
        if (questions == null || questions.isEmpty()) {
            throw new RuntimeException("Un quiz doit contenir au moins une question");
        }
        for (QuestionQuizRequest q : questions) {
            if (q.getChoixPossibles() == null || q.getChoixPossibles().size() < 2) {
                throw new RuntimeException("Chaque question doit avoir au moins 2 choix possibles");
            }
            if (!q.getChoixPossibles().contains(q.getBonneReponse())) {
                throw new RuntimeException(
                    "La bonne réponse '" + q.getBonneReponse() + "' doit être dans les choix possibles"
                );
            }
        }
    }

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

    // ── MAPPERS ──────────────────────────────────────────────────────

    public QuizResponse toResponse(Quiz q) {
        QuizResponse r = new QuizResponse();
        r.setId(q.getId());
        r.setTitre(q.getTitre());
        r.setDureeMinutes(q.getDureeMinutes());
        r.setNombreQuestions(q.getNombreQuestions());
        r.setTotalQuestions(q.getQuestions() != null ? q.getQuestions().size() : 0);
        r.setPointsTotal(q.getQuestions() != null
                ? q.getQuestions().stream().mapToDouble(QuestionQuiz::getPoints).sum() : 0);
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

    private QuizDetailEnseignantResponse toDetailEnseignantResponse(Quiz q) {
        QuizDetailEnseignantResponse r = new QuizDetailEnseignantResponse();
        r.setId(q.getId());
        r.setTitre(q.getTitre());
        r.setDureeMinutes(q.getDureeMinutes());
        r.setNombreQuestions(q.getNombreQuestions());
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
            qr.setBonneReponse(qq.getBonneReponse());
            qr.setPoints(qq.getPoints());
            return qr;
        }).collect(Collectors.toList()));
        return r;
    }
    
    
    
}