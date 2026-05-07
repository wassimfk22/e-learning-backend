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
 * ║  GET  /quiz/cours/{coursId}         → quiz du cours           ║
 * ║  POST /quiz/{quizId}/demarrer       → démarrer (chrono)       ║
 * ║  GET  /quiz/tentative/{id}/questions→ questions en cours      ║
 * ║  POST /quiz/tentative/{id}/soumettre→ soumettre les réponses  ║
 * ║  POST /quiz/tentative/{id}/expirer  → soumission par timeout  ║
 * ║  GET  /quiz/{quizId}/resultat       → résultat d'un quiz passé║
 * ║  GET  /dashboard                    → tableau de bord         ║
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
    // Retourne les quiz avec : dejaPasse, enCours, dateExpiration, questions
    @GetMapping("/quiz/cours/{coursId}")
    public ResponseEntity<List<QuizEtudiantResponse>> getQuizzesDuCours(
            @PathVariable Long coursId, Authentication auth) {
        return ResponseEntity.ok(etudiantQuizService.getQuizzesDuCours(coursId, auth));
    }

    // ── DÉMARRER LE QUIZ ─────────────────────────────────────────
    // POST /api/etudiant/quiz/{quizId}/demarrer
    // Crée la tentative unique et démarre le chrono
    // ❌ Erreur si l'étudiant a déjà passé ce quiz
    @PostMapping("/quiz/{quizId}/demarrer")
    public ResponseEntity<TentativeResponse> demarrerQuiz(
            @PathVariable Long quizId, Authentication auth) {
        return ResponseEntity.ok(etudiantQuizService.demarrerQuiz(quizId, auth));
    }

    // ── QUESTIONS DE LA TENTATIVE EN COURS ───────────────────────
    // GET /api/etudiant/quiz/tentative/{tentativeId}/questions
    // Retourne les questions (tirage déterministe basé sur l'id tentative)
    // ❌ Erreur si tentative expirée ou déjà soumise
    @GetMapping("/quiz/tentative/{tentativeId}/questions")
    public ResponseEntity<List<QuestionEtudiantResponse>> getQuestions(
            @PathVariable Long tentativeId, Authentication auth) {
        return ResponseEntity.ok(etudiantQuizService.getQuestionsDeTentative(tentativeId, auth));
    }

    // ── SOUMETTRE LES RÉPONSES ────────────────────────────────────
    // POST /api/etudiant/quiz/tentative/{tentativeId}/soumettre
    // Body: [ { "tentativeId": 1, "questionId": 5, "reponseChoisie": "Paris" }, ... ]
    // L'étudiant envoie toutes ses réponses en une seule fois
    @PostMapping("/quiz/tentative/{tentativeId}/soumettre")
    public ResponseEntity<ScoreFinaleResponse> soumettreQuiz(
            @PathVariable Long tentativeId,
            @RequestBody List<ReponseQuizRequest> reponses,
            Authentication auth) {
        return ResponseEntity.ok(etudiantQuizService.soumettreQuiz(tentativeId, reponses, auth));
    }

    // ── SOUMISSION PAR EXPIRATION ─────────────────────────────────
    // POST /api/etudiant/quiz/tentative/{tentativeId}/expirer
    // Appelé par le frontend quand le chrono atteint 0
    // Calcule le score sur les réponses déjà enregistrées
    @PostMapping("/quiz/tentative/{tentativeId}/expirer")
    public ResponseEntity<ScoreFinaleResponse> soumettreParExpiration(
            @PathVariable Long tentativeId, Authentication auth) {
        return ResponseEntity.ok(etudiantQuizService.soumettreParExpiration(tentativeId, auth));
    }

    // ── RÉSULTAT D'UN QUIZ ────────────────────────────────────────
    // GET /api/etudiant/quiz/{quizId}/resultat
    // Consulter le résultat après soumission
    @GetMapping("/quiz/{quizId}/resultat")
    public ResponseEntity<ScoreFinaleResponse> getResultat(
            @PathVariable Long quizId, Authentication auth) {
        return ResponseEntity.ok(etudiantQuizService.getResultat(quizId, auth));
    }

    // ── DASHBOARD ─────────────────────────────────────────────────
    // GET /api/etudiant/dashboard
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardEtudiantResponse> getDashboard(Authentication auth) {
        return ResponseEntity.ok(etudiantQuizService.getDashboard(auth));
    }
    
    
    
}