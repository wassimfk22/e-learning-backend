package com.school.elearning.controller;

import com.school.elearning.dto.*;
import com.school.elearning.service.ExamenEtudiantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║  EXAMEN ÉTUDIANT CONTROLLER                                  ║
 * ║  Accès : ETUDIANT uniquement                                 ║
 * ║  Base  : /api/etudiant/examens                               ║
 * ╠══════════════════════════════════════════════════════════════╣
 * ║  GET  /module/{moduleId}           → examens du module       ║
 * ║  POST /{examenId}/demarrer         → démarrer (chrono)       ║
 * ║  POST /passages/{passageId}/soumettre → soumettre réponses   ║
 * ║  POST /passages/{passageId}/expirer   → soumission timeout   ║
 * ║  GET  /{examenId}/mon-resultat     → consulter résultat      ║
 * ╚══════════════════════════════════════════════════════════════╝
 */
@RestController
@RequestMapping("/api/etudiant/examens")
@PreAuthorize("hasRole('ETUDIANT')")
@RequiredArgsConstructor
public class ExamenEtudiantController {

    private final ExamenEtudiantService examenEtudiantService;

    // GET /api/etudiant/examens/module/{moduleId}
    // Retourne les examens avec statut : dejaPasse, noteFinale si corrigé
    @GetMapping("/module/{moduleId}")
    public ResponseEntity<List<ExamenEtudiantService.ExamenAvecStatutResponse>> getExamensDuModule(
            @PathVariable Long moduleId, Authentication auth) {
        return ResponseEntity.ok(examenEtudiantService.getExamensModuleAvecStatut(moduleId, auth));
    }

    // POST /api/etudiant/examens/{examenId}/demarrer
    // Crée le passage unique + démarre le chrono
    @PostMapping("/{examenId}/demarrer")
    public ResponseEntity<PassageExamenResponse> demarrerExamen(
            @PathVariable Long examenId, Authentication auth) {
        return ResponseEntity.ok(examenEtudiantService.demarrerExamen(examenId, auth));
    }

    // POST /api/etudiant/examens/passages/{passageId}/soumettre
    // Body: { "reponses": [ { "questionId": 1, "reponseTexte": "La réponse de l'étudiant" }, ... ] }
    @PostMapping("/passages/{passageId}/soumettre")
    public ResponseEntity<PassageExamenResponse> soumettreExamen(
            @PathVariable Long passageId,
            @RequestBody SoumissionExamenRequest request,
            Authentication auth) {
        return ResponseEntity.ok(examenEtudiantService.soumettreExamen(passageId, request, auth));
    }

    // POST /api/etudiant/examens/passages/{passageId}/expirer
    // Appelé par le frontend quand le chrono atteint 0
    @PostMapping("/passages/{passageId}/expirer")
    public ResponseEntity<PassageExamenResponse> soumettreParExpiration(
            @PathVariable Long passageId, Authentication auth) {
        return ResponseEntity.ok(examenEtudiantService.soumettreParExpiration(passageId, auth));
    }

    // GET /api/etudiant/examens/{examenId}/mon-resultat
    // Consulter son résultat (SOUMIS = en attente, CORRIGE = note disponible)
    @GetMapping("/{examenId}/mon-resultat")
    public ResponseEntity<PassageExamenResponse> getMonResultat(
            @PathVariable Long examenId, Authentication auth) {
        return ResponseEntity.ok(examenEtudiantService.getMonResultat(examenId, auth));
    }
    
    
    
}