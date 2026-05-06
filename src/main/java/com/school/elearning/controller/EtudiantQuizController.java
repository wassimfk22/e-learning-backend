// ════════════════════════════════════════════════════
// EtudiantQuizController.java  →  controller/
// ════════════════════════════════════════════════════
package com.school.elearning.controller;
 
import com.school.elearning.dto.*;
import com.school.elearning.service.EtudiantQuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
 
import java.util.List;
 
/**
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║  ÉTUDIANT QUIZ CONTROLLER                                     ║
 * ║  Accès : ETUDIANT uniquement                                  ║
 * ║  Base  : /api/etudiant                                        ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║  GET  /quiz/cours/{coursId}         → quiz accessibles        ║
 * ║  POST /quiz/{quizId}/demarrer       → démarrer une tentative  ║
 * ║  POST /quiz/repondre                → répondre à une question ║
 * ║  GET  /quiz/{quizId}/historique     → mes tentatives          ║
 * ║  GET  /dashboard                    → mon tableau de bord     ║
 * ╚═══════════════════════════════════════════════════════════════╝
 */
@RestController
@RequestMapping("/api/etudiant")
@PreAuthorize("hasRole('ETUDIANT')")
@RequiredArgsConstructor
public class EtudiantQuizController {
 
    private final EtudiantQuizService etudiantQuizService;
 
    // ── QUIZ D'UN COURS ──────────────────────────────────────────
    // GET /api/etudiant/quiz/cours/{coursId}
    // Retourne les quiz du cours avec : peutPasser, tentativesRestantes, questions (sans bonneReponse)
    // ❌ Erreur si l'étudiant n'appartient pas au bon niveau
    @GetMapping("/quiz/cours/{coursId}")
    public ResponseEntity<List<QuizEtudiantResponse>> getQuizzesDuCours(
            @PathVariable Long coursId, Authentication auth) {
        return ResponseEntity.ok(etudiantQuizService.getQuizzesDuCours(coursId, auth));
    }
 
    // ── DÉMARRER UNE TENTATIVE ───────────────────────────────────
    // POST /api/etudiant/quiz/{quizId}/demarrer
    // Crée ou retourne la tentative EN_COURS
    // ❌ Erreur si tentatives épuisées ou quiz hors dates
    @PostMapping("/quiz/{quizId}/demarrer")
    public ResponseEntity<TentativeResponse> demarrerTentative(
            @PathVariable Long quizId, Authentication auth) {
        return ResponseEntity.ok(etudiantQuizService.demarrerTentative(quizId, auth));
    }
 
    // ── RÉPONDRE À UNE QUESTION ──────────────────────────────────
    // POST /api/etudiant/quiz/repondre
    @PostMapping("/quiz/repondre")
    public ResponseEntity<List<ReponseQuizResultat>> repondreQuestions(
            @RequestBody List<ReponseQuizRequest> requests, Authentication auth) {
        return ResponseEntity.ok(etudiantQuizService.repondreQuestions(requests, auth));
    }
 
    // ── HISTORIQUE DES TENTATIVES ────────────────────────────────
    // GET /api/etudiant/quiz/{quizId}/historique
    // Retourne toutes les tentatives (EN_COURS et SOUMISE) pour ce quiz
    @GetMapping("/quiz/{quizId}/historique")
    public ResponseEntity<List<TentativeResponse>> getHistorique(
            @PathVariable Long quizId, Authentication auth) {
        return ResponseEntity.ok(etudiantQuizService.getHistoriqueTentatives(quizId, auth));
    }
 
    // ── DASHBOARD ────────────────────────────────────────────────
    // GET /api/etudiant/dashboard
    // Retourne : progressions, moyennes quiz/examens, 5 dernières tentatives
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardEtudiantResponse> getDashboard(Authentication auth) {
        return ResponseEntity.ok(etudiantQuizService.getDashboard(auth));
    }
    
    
    
}