package com.school.elearning.controller;

import com.school.elearning.dto.*;
import com.school.elearning.service.ExamenEnseignantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║  EXAMEN ENSEIGNANT CONTROLLER                                ║
 * ║  Accès : ENSEIGNANT uniquement                               ║
 * ║  Base  : /api/examens                                        ║
 * ╠══════════════════════════════════════════════════════════════╣
 * ║  POST   /                          → créer examen            ║
 * ║  PUT    /{id}                      → modifier                ║
 * ║  GET    /mes-examens               → mes examens             ║
 * ║  GET    /module/{moduleId}         → examens d'un module     ║
 * ║  DELETE /{id}                      → supprimer               ║
 * ║  GET    /{id}/copies               → copies soumises         ║
 * ║  GET    /{id}/copies/toutes        → toutes les copies       ║
 * ║  POST   /passages/{passageId}/corriger → corriger 1 réponse  ║
 * ╚══════════════════════════════════════════════════════════════╝
 */
@RestController
@RequestMapping("/api/examens")
@PreAuthorize("hasRole('ENSEIGNANT')")
@RequiredArgsConstructor
public class ExamenEnseignantController {

    private final ExamenEnseignantService examenService;

    // POST /api/examens
    // Body: { "titre":"...", "description":"...", "dureeMinutes":90, "moduleId":1,
    //         "questions":[{"enonce":"...","points":4.0},...] }
    @PostMapping
    public ResponseEntity<ExamenResponse> creer(
            @RequestBody ExamenRequest request, Authentication auth) {
        return ResponseEntity.ok(examenService.creerExamen(request, auth));
    }

    // PUT /api/examens/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ExamenResponse> modifier(
            @PathVariable Long id,
            @RequestBody ExamenRequest request,
            Authentication auth) {
        return ResponseEntity.ok(examenService.modifierExamen(id, request, auth));
    }

    // GET /api/examens/mes-examens
    @GetMapping("/mes-examens")
    public ResponseEntity<List<ExamenResponse>> getMesExamens(Authentication auth) {
        return ResponseEntity.ok(examenService.getMesExamens(auth));
    }

    // GET /api/examens/module/{moduleId}
    @GetMapping("/module/{moduleId}")
    public ResponseEntity<List<ExamenResponse>> getParModule(
            @PathVariable Long moduleId, Authentication auth) {
        return ResponseEntity.ok(examenService.getExamensParModule(moduleId, auth));
    }

    // DELETE /api/examens/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<String> supprimer(
            @PathVariable Long id, Authentication auth) {
        examenService.supprimerExamen(id, auth);
        return ResponseEntity.ok("Examen supprimé avec succès");
    }

    // GET /api/examens/{id}/copies  → copies SOUMISES à corriger
    @GetMapping("/{id}/copies")
    public ResponseEntity<List<PassageExamenResponse>> getCopiesACorreiger(
            @PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(examenService.getCopiesACorreiger(id, auth));
    }

    // GET /api/examens/{id}/copies/toutes  → SOUMISES + CORRIGÉES
    @GetMapping("/{id}/copies/toutes")
    public ResponseEntity<List<PassageExamenResponse>> getToutesCopies(
            @PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(examenService.getToutesCopies(id, auth));
    }

    // POST /api/examens/passages/{passageId}/corriger
    // Body: { "reponseId": 5, "estJuste": true }
    // Appeler une fois par réponse — la note se recalcule automatiquement
    // quand toutes les réponses sont corrigées
    @PostMapping("/passages/{passageId}/corriger")
    public ResponseEntity<PassageExamenResponse> corrigerReponse(
            @PathVariable Long passageId,
            @RequestBody CorrectionReponseRequest request,
            Authentication auth) {
        return ResponseEntity.ok(examenService.corrigerReponse(passageId, request, auth));
    }
    
    
    
}