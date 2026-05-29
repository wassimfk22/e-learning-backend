package com.school.elearning.controller;

import com.school.elearning.dto.ProgressionModuleResponse;
import com.school.elearning.service.ProgressionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/progression")
@RequiredArgsConstructor
public class ProgressionController {

    private final ProgressionService progressionService;

    // ── ÉTUDIANT ─────────────────────────────────────────────────

    @GetMapping("/ma-progression")
    @PreAuthorize("hasRole('ETUDIANT')")
    public ResponseEntity<List<ProgressionModuleResponse>> getMaProgression(Authentication auth) {
        return ResponseEntity.ok(progressionService.getMaProgression(auth));
    }

    @PostMapping("/inscrire/{moduleId}")
    @PreAuthorize("hasRole('ETUDIANT')")
    public ResponseEntity<ProgressionModuleResponse> inscrire(
            @PathVariable Long moduleId, Authentication auth) {
        return ResponseEntity.ok(progressionService.inscrireModule(moduleId, auth));
    }

    // ── ADMIN / MODERATEUR / ENSEIGNANT ──────────────────────────

    @GetMapping("/etudiant/{etudiantId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATEUR', 'ENSEIGNANT')")
    public ResponseEntity<List<ProgressionModuleResponse>> getProgressionEtudiant(
            @PathVariable Long etudiantId) {
        return ResponseEntity.ok(progressionService.getProgressionEtudiant(etudiantId));
    }

    @GetMapping("/module/{moduleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATEUR', 'ENSEIGNANT')")
    public ResponseEntity<List<ProgressionModuleResponse>> getProgressionParModule(
            @PathVariable Long moduleId) {
        return ResponseEntity.ok(progressionService.getProgressionParModule(moduleId));
    }
    
    
    
}