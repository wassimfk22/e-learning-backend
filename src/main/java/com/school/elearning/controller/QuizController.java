package com.school.elearning.controller;

import com.school.elearning.dto.*;
import com.school.elearning.service.QuizService;

import io.jsonwebtoken.io.IOException;
import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════╗
 * ║  QUIZ CONTROLLER — Côté Enseignant                       ║
 * ║  Accès : ENSEIGNANT uniquement                           ║
 * ║  Base  : /api/quiz                                       ║
 * ╠══════════════════════════════════════════════════════════╣
 * ║  POST   /api/quiz                    → créer un quiz     ║
 * ║  PUT    /api/quiz/{id}               → modifier          ║
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

    // POST /api/quiz
    // Body:
    // {
    //   "titre": "Quiz POO",
    //   "dureeMinutes": 60,
    //   "nombreQuestions": 5,
    //   "coursId": 1,
    //   "questions": [
    //     {
    //       "enonce": "Qu'est-ce que l'héritage ?",
    //       "choixPossibles": ["Répétition", "Transmission", "Encapsulation", "Abstraction"],
    //       "bonneReponse": "Transmission",
    //       "points": 2.0
    //     }
    //   ]
    // }
    @PostMapping
    public ResponseEntity<QuizDetailEnseignantResponse> creer(
            @RequestBody QuizRequest request, Authentication auth) {
        return ResponseEntity.ok(quizService.creerQuiz(request, auth));
    }

    // PUT /api/quiz/{id}
    @PutMapping("/{id}")
    public ResponseEntity<QuizDetailEnseignantResponse> modifier(
            @PathVariable Long id,
            @RequestBody QuizRequest request,
            Authentication auth) {
        return ResponseEntity.ok(quizService.modifierQuiz(id, request, auth));
    }

    // GET /api/quiz/mes-quizzes
    @GetMapping("/mes-quizzes")
    public ResponseEntity<List<QuizResponse>> getMesQuizzes(Authentication auth) {
        return ResponseEntity.ok(quizService.getMesQuizzes(auth));
    }

    // GET /api/quiz/{id}
    @GetMapping("/{id}")
    public ResponseEntity<QuizDetailEnseignantResponse> getQuiz(
            @PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(quizService.getQuizEnseignant(id, auth));
    }

    // GET /api/quiz/cours/{coursId}
    @GetMapping("/cours/{coursId}")
    public ResponseEntity<List<QuizResponse>> getQuizzesByCours(
            @PathVariable Long coursId, Authentication auth) {
        return ResponseEntity.ok(quizService.getQuizzesByCours(coursId, auth));
    }

    // DELETE /api/quiz/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<String> supprimer(
            @PathVariable Long id, Authentication auth) {
        quizService.supprimerQuiz(id, auth);
        return ResponseEntity.ok("Quiz supprimé avec succès");
    }
    
 // POST /api/quiz/ia
    @PostMapping(value = "/ia", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<QuizDetailEnseignantResponse> creerQuizParIA(
            @RequestParam("titre") String titre,
            @RequestParam("dureeMinutes") Integer dureeMinutes,
            @RequestParam("coursId") Long coursId,
            @RequestParam("fichier") MultipartFile fichier,
            Authentication auth) throws IOException, java.io.IOException {
        return ResponseEntity.ok(quizService.creerQuizDepuisFichier(titre, dureeMinutes, coursId, fichier, auth));
    }
    
    
    
}