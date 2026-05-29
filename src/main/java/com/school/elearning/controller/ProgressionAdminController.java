package com.school.elearning.controller;

import com.school.elearning.dto.ProgressionEtudiantDetailResponse;
import com.school.elearning.service.ProgressionAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║  PROGRESSION ADMIN CONTROLLER                                ║
 * ║  Base : /api/admin/progressions                              ║
 * ╠══════════════════════════════════════════════════════════════╣
 * ║  GET /etudiant/{id}         → progression détaillée          ║
 * ║  GET /module/{id}           → tous les étudiants du module   ║
 * ║  GET /niveau/{id}           → tous les étudiants du niveau   ║
 * ╚══════════════════════════════════════════════════════════════╝
 */
@RestController
@RequestMapping("/api/admin/progressions")
@PreAuthorize("hasAnyRole('ADMIN', 'MODERATEUR', 'ENSEIGNANT')")
@RequiredArgsConstructor
public class ProgressionAdminController {

    private final ProgressionAdminService progressionAdminService;

    /** Progression complète et détaillée d'un étudiant */
    @GetMapping("/etudiant/{etudiantId}")
    public ResponseEntity<ProgressionEtudiantDetailResponse> getProgressionEtudiant(
            @PathVariable Long etudiantId) {
        return ResponseEntity.ok(progressionAdminService.getProgressionDetailleeEtudiant(etudiantId));
    }

    /** Tous les étudiants d'un module avec leur progression */
    @GetMapping("/module/{moduleId}")
    public ResponseEntity<List<ProgressionEtudiantDetailResponse>> getProgressionParModule(
            @PathVariable Long moduleId) {
        return ResponseEntity.ok(progressionAdminService.getProgressionParModule(moduleId));
    }

    /** Tous les étudiants d'un niveau avec leur progression globale */
    @GetMapping("/niveau/{niveauId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATEUR')")
    public ResponseEntity<List<ProgressionEtudiantDetailResponse>> getProgressionParNiveau(
            @PathVariable Long niveauId) {
        return ResponseEntity.ok(progressionAdminService.getProgressionParNiveau(niveauId));
    }
    
    
    
}