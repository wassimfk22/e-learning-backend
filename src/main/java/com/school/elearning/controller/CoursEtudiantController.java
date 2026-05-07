package com.school.elearning.controller;

import com.school.elearning.dto.CoursProgressionResponse;
import com.school.elearning.dto.CoursResponse;
import com.school.elearning.service.CoursEtudiantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════╗
 * ║  COURS ÉTUDIANT CONTROLLER                               ║
 * ║  Accès : ETUDIANT uniquement                             ║
 * ║  Base  : /api/etudiant/cours                             ║
 * ╠══════════════════════════════════════════════════════════╣
 * ║  GET  /module/{moduleId}        → cours du module        ║
 * ║  GET  /{coursId}/acceder        → accéder (suivi auto)   ║
 * ║  POST /{coursId}/terminer       → marquer terminé        ║
 * ║  GET  /ma-progression           → tous mes cours suivis  ║
 * ╚══════════════════════════════════════════════════════════╝
 */
@RestController
@RequestMapping("/api/etudiant/cours")
@PreAuthorize("hasRole('ETUDIANT')")
@RequiredArgsConstructor
public class CoursEtudiantController {

    private final CoursEtudiantService coursEtudiantService;

    // Cours d'un module avec statut (jamais consulté / en cours / terminé)
    @GetMapping("/module/{moduleId}")
    public ResponseEntity<List<CoursEtudiantService.CoursAvecStatutResponse>> getCoursDuModule(
            @PathVariable Long moduleId, Authentication auth) {
        return ResponseEntity.ok(coursEtudiantService.getCoursModuleAvecStatut(moduleId, auth));
    }

    // Accéder à un cours → crée/met à jour le suivi automatiquement
    @GetMapping("/{coursId}/acceder")
    public ResponseEntity<CoursResponse> accederCours(
            @PathVariable Long coursId, Authentication auth) {
        return ResponseEntity.ok(coursEtudiantService.accederCours(coursId, auth));
    }

    // Marquer un cours comme terminé
    @PostMapping("/{coursId}/terminer")
    public ResponseEntity<CoursProgressionResponse> marquerTermine(
            @PathVariable Long coursId, Authentication auth) {
        return ResponseEntity.ok(coursEtudiantService.marquerTermine(coursId, auth));
    }

    // Toute ma progression cours
    @GetMapping("/ma-progression")
    public ResponseEntity<List<CoursProgressionResponse>> getMaProgressionCours(Authentication auth) {
        return ResponseEntity.ok(coursEtudiantService.getMaProgressionCours(auth));
    }
    
    
    
}