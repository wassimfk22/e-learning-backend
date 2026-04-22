package com.school.elearning.controller;

import com.school.elearning.model.*;
import com.school.elearning.service.EvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/evaluations")
@RequiredArgsConstructor
public class EvaluationController {
    private final EvaluationService evaluationService;

    @PostMapping("/quiz")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATEUR', 'ENSEIGNANT')")
    public ResponseEntity<Quiz> createQuiz(@RequestBody Quiz quiz) {
        return ResponseEntity.ok(evaluationService.createQuiz(quiz));
    }

    @PostMapping("/examen")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATEUR', 'ENSEIGNANT')")
    public ResponseEntity<Examen> createExamen(@RequestBody Examen examen) {
        return ResponseEntity.ok(evaluationService.createExamen(examen));
    }

    @PostMapping("/questions")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATEUR', 'ENSEIGNANT')")
    public ResponseEntity<Question> addQuestion(@RequestBody Question question) {
        return ResponseEntity.ok(evaluationService.addQuestion(question));
    }

    @GetMapping("/quiz/cours/{coursId}")
    public ResponseEntity<List<Quiz>> getQuizzesByCours(@PathVariable Long coursId) {
        return ResponseEntity.ok(evaluationService.getQuizzesByCours(coursId));
    }

    @GetMapping("/{evalId}/questions")
    public ResponseEntity<List<Question>> getQuestions(@PathVariable Long evalId) {
        return ResponseEntity.ok(evaluationService.getQuestionsByEvaluation(evalId));
    }

    @PostMapping("/tentative/{tentativeId}/submit")
    @PreAuthorize("hasRole('ETUDIANT')")
    public ResponseEntity<Tentative> submitTentative(@PathVariable Long tentativeId) {
        return ResponseEntity.ok(evaluationService.submitTentative(tentativeId));
    }
}
