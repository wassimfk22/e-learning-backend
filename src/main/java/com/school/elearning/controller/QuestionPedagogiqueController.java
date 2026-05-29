package com.school.elearning.controller;

import com.school.elearning.dto.*;
import com.school.elearning.service.QuestionPedagogiqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║  QUESTION PÉDAGOGIQUE CONTROLLER                                 ║
 * ║  Base : /api/questions-pedagogiques                              ║
 * ╠══════════════════════════════════════════════════════════════════╣
 * ║  ÉTUDIANT                                                        ║
 * ║  POST /              → poser une question (liée à un cours)      ║
 * ║  GET  /mes-questions → toutes mes questions avec réponses        ║
 * ║                                                                  ║
 * ║  ENSEIGNANT                                                      ║
 * ║  GET  /cours/{coursId}            → questions du cours           ║
 * ║  GET  /cours/{coursId}/sans-reponse → questions sans réponse     ║
 * ║  POST /repondre                   → répondre à une question      ║
 * ╚══════════════════════════════════════════════════════════════════╝
 *
 * Body POST / (poser une question) :
 * { "enonce": "Comment fonctionne @Transactional ?", "coursId": 2 }
 *
 * Body POST /repondre :
 * { "contenu": "L'annotation @Transactional...", "questionId": 5 }
 */
@RestController
@RequestMapping("/api/questions-pedagogiques")
@RequiredArgsConstructor
public class QuestionPedagogiqueController {

    private final QuestionPedagogiqueService questionService;

    // ── ÉTUDIANT : Poser une question ────────────────────────────
    @PostMapping
    @PreAuthorize("hasRole('ETUDIANT')")
    public ResponseEntity<QuestionPedagogiqueResponse> poserQuestion(
            @RequestBody QuestionPedagogiqueRequest request,
            Authentication auth) {
        return ResponseEntity.ok(questionService.poserQuestion(request, auth));
    }

    // ── ÉTUDIANT : Mes questions (avec réponses) ─────────────────
    @GetMapping("/mes-questions")
    @PreAuthorize("hasRole('ETUDIANT')")
    public ResponseEntity<List<QuestionPedagogiqueResponse>> getMesQuestions(Authentication auth) {
        return ResponseEntity.ok(questionService.getMesQuestions(auth));
    }

    // ── ENSEIGNANT : Questions d'un cours (toutes) ───────────────
    @GetMapping("/cours/{coursId}")
    @PreAuthorize("hasRole('ENSEIGNANT')")
    public ResponseEntity<List<QuestionPedagogiqueResponse>> getQuestionsByCours(
            @PathVariable Long coursId,
            Authentication auth) {
        return ResponseEntity.ok(questionService.getQuestionsByCours(coursId, false, auth));
    }

    // ── ENSEIGNANT : Questions d'un cours SANS réponse ───────────
    @GetMapping("/cours/{coursId}/sans-reponse")
    @PreAuthorize("hasRole('ENSEIGNANT')")
    public ResponseEntity<List<QuestionPedagogiqueResponse>> getQuestionsSansReponse(
            @PathVariable Long coursId,
            Authentication auth) {
        return ResponseEntity.ok(questionService.getQuestionsByCours(coursId, true, auth));
    }

    // ── ENSEIGNANT : Répondre à une question ─────────────────────
    @PostMapping("/repondre")
    @PreAuthorize("hasRole('ENSEIGNANT')")
    public ResponseEntity<QuestionPedagogiqueResponse> repondre(
            @RequestBody ReponsePedagogiqueRequest request,
            Authentication auth) {
        return ResponseEntity.ok(questionService.repondreQuestion(request, auth));
    }
    
    
    
}