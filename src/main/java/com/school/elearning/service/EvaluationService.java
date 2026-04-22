package com.school.elearning.service;

import com.school.elearning.model.*;
import com.school.elearning.model.enums.*;
import com.school.elearning.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EvaluationService {
    private final QuizRepository quizRepository;
    private final ExamenRepository examenRepository;
    private final QuestionRepository questionRepository;
    private final TentativeRepository tentativeRepository;
    private final ReponseEtudiantRepository reponseEtudiantRepository;
    private final NoteRepository noteRepository;
    private final CorrectionRepository correctionRepository;

    public Quiz createQuiz(Quiz quiz) { return quizRepository.save(quiz); }
    public Examen createExamen(Examen examen) { return examenRepository.save(examen); }
    public Question addQuestion(Question question) { return questionRepository.save(question); }

    public List<Quiz> getQuizzesByCours(Long coursId) { return quizRepository.findByCoursId(coursId); }
    public List<Question> getQuestionsByEvaluation(Long evalId) { return questionRepository.findByEvaluationId(evalId); }

    public Tentative startTentative(Etudiant etudiant, Long evaluationId) {
        Tentative t = new Tentative();
        t.setEtudiant(etudiant);
        t.setDateTentative(new Date());
        t.setStatut(StatutTentative.EN_COURS);
        // Set evaluation reference
        Quiz quiz = quizRepository.findById(evaluationId).orElse(null);
        if (quiz != null) {
            // Check max attempts
            List<Tentative> existing = tentativeRepository.findByEtudiantIdAndEvaluationId(etudiant.getId(), evaluationId);
            if (existing.size() >= quiz.getNombreTentativesMax()) {
                throw new RuntimeException("Nombre maximum de tentatives atteint");
            }
            t.setEvaluation(quiz);
        } else {
            Examen examen = examenRepository.findById(evaluationId)
                    .orElseThrow(() -> new RuntimeException("Evaluation non trouvée"));
            t.setEvaluation(examen);
        }
        return tentativeRepository.save(t);
    }

    public Tentative submitTentative(Long tentativeId) {
        Tentative t = tentativeRepository.findById(tentativeId)
                .orElseThrow(() -> new RuntimeException("Tentative non trouvée"));
        t.setStatut(StatutTentative.SOUMISE);

        // Auto-correct if quiz
        if (t.getEvaluation() instanceof Quiz) {
            float score = 0;
            List<ReponseEtudiant> reponses = t.getReponses();
            if (reponses != null && !reponses.isEmpty()) {
                for (ReponseEtudiant r : reponses) {
                    if (r.isEstCorrecte()) score++;
                }
                score = (score / reponses.size()) * 20;
            }
            t.setScoreObtenu(score);
            t.setStatut(StatutTentative.CORRIGEE);

            // Create Note
            Note note = new Note();
            note.setValeur(score);
            note.setDateObtention(new Date());
            note.setType(TypeNote.QUIZ);
            note.setTentative(t);
            noteRepository.save(note);
        }
        return tentativeRepository.save(t);
    }

    public Correction corrigerExamen(Long tentativeId, Long enseignantId, float noteVal, String commentaire, Enseignant enseignant) {
        Tentative t = tentativeRepository.findById(tentativeId)
                .orElseThrow(() -> new RuntimeException("Tentative non trouvée"));

        Correction c = new Correction();
        c.setNote(noteVal);
        c.setCommentaire(commentaire);
        c.setDateCorrection(new Date());
        c.setTentative(t);
        c.setEnseignant(enseignant);
        correctionRepository.save(c);

        t.setScoreObtenu(noteVal);
        t.setStatut(StatutTentative.CORRIGEE);
        tentativeRepository.save(t);

        Note note = new Note();
        note.setValeur(noteVal);
        note.setDateObtention(new Date());
        note.setType(TypeNote.EXAMEN);
        note.setTentative(t);
        noteRepository.save(note);

        return c;
    }
}
