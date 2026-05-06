package com.school.elearning.controller;

import com.school.elearning.dto.*;
import com.school.elearning.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════╗
 * ║  QUIZ CONTROLLER — Côté Enseignant                       ║
 * ║  Accès : ENSEIGNANT uniquement                           ║
 * ║  Base  : /api/quiz                                       ║
 * ╠══════════════════════════════════════════════════════════╣
 * ║  POST   /api/quiz                    → créer un quiz     ║
 * ║  GET    /api/quiz/mes-quizzes        → mes quiz          ║
 * ║  GET    /api/quiz/{id}               → détail quiz       ║
 * ║  GET    /api/quiz/cours/{coursId}    → quiz d'un cours   ║
 * ║  DELETE /api/quiz/{id}               → supprimer         ║
 * ╚══════════════════════════════════════════════════════════╝
 */
@RestController
@RequestMapping("/api/quiz")
@PreAuthorize("hasRole('ENSEIGNANT')")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    // ── CRÉER ────────────────────────────────────────────────────
    // POST /api/quiz
    // Body:
    // {
    //   "titre": "Quiz POO",
    //   "dateDebut": "2025-06-01",
    //   "dateFin": "2025-06-30",
    //   "nombreTentativesMax": 3,
    //   "coursId": 1,
    //   "questions": [
    //     {
    //       "enonce": "Qu'est-ce que l'héritage ?",
    //       "choixPossibles": ["Répétition", "Transmission", "Encapsulation", "Abstraction"],
    //       "bonneReponse": "Transmission",
    //       "points": 2.0
    //     },
    //     {
    //       "enonce": "Quel mot-clé pour hériter en Java ?",
    //       "choixPossibles": ["implements", "extends", "inherits", "super"],
    //       "bonneReponse": "extends",
    //       "points": 1.5
    //     }
    //   ]
    // }
    @PostMapping
    public ResponseEntity<QuizDetailEnseignantResponse> creer(
            @RequestBody QuizRequest request,
            Authentication auth) {
        return ResponseEntity.ok(quizService.creerQuiz(request, auth));
    }

    // ── MES QUIZ ─────────────────────────────────────────────────
    // GET /api/quiz/mes-quizzes
    // Retourne tous les quiz des cours dont les modules appartiennent à l'enseignant
    @GetMapping("/mes-quizzes")
    public ResponseEntity<List<QuizResponse>> getMesQuizzes(Authentication auth) {
        return ResponseEntity.ok(quizService.getMesQuizzes(auth));
    }

    // ── DÉTAIL D'UN QUIZ ─────────────────────────────────────────
    // GET /api/quiz/{id}
    // Retourne les questions avec les bonnes réponses (vue enseignant)
    @GetMapping("/{id}")
    public ResponseEntity<QuizDetailEnseignantResponse> getQuiz(
            @PathVariable Long id,
            Authentication auth) {
        return ResponseEntity.ok(quizService.getQuizEnseignant(id, auth));
    }

    // ── QUIZ PAR COURS ───────────────────────────────────────────
    // GET /api/quiz/cours/{coursId}
    // Retourne les quiz d'un cours spécifique (le cours doit appartenir à l'enseignant)
    @GetMapping("/cours/{coursId}")
    public ResponseEntity<List<QuizResponse>> getQuizzesByCours(
            @PathVariable Long coursId,
            Authentication auth) {
        return ResponseEntity.ok(quizService.getQuizzesByCours(coursId, auth));
    }

    // ── SUPPRIMER ────────────────────────────────────────────────
    // DELETE /api/quiz/{id}
    // Supprime le quiz ET toutes ses questions (cascade)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> supprimer(
            @PathVariable Long id,
            Authentication auth) {
        quizService.supprimerQuiz(id, auth);
        return ResponseEntity.ok("Quiz supprimé avec succès");
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<QuizDetailEnseignantResponse> modifierQuiz(
            @PathVariable Long id,
            @RequestBody QuizRequest request,
            Authentication auth) {
        
        QuizDetailEnseignantResponse response = quizService.modifierQuiz(id, request, auth);
        return ResponseEntity.ok(response);
    }
    
}