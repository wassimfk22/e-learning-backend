// ════════════════════════════════════════════════════
// ProgressionController.java
// ════════════════════════════════════════════════════
package com.school.elearning.controller;
 
import com.school.elearning.model.ProgressionModule;
import com.school.elearning.service.ProgressionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
 
@RestController
@RequestMapping("/api/progression")
@RequiredArgsConstructor
public class ProgressionController {
 
    private final ProgressionService progressionService;
 
    // GET /api/progression/ma-progression — Rôle: ETUDIANT
    @GetMapping("/ma-progression")
    public ResponseEntity<List<ProgressionModule>> getMaProgression(Authentication auth) {
        return ResponseEntity.ok(progressionService.getMaProgression(auth));
    }
 
    // POST /api/progression/inscrire/{moduleId} — Rôle: ETUDIANT
    @PostMapping("/inscrire/{moduleId}")
    public ResponseEntity<ProgressionModule> inscrire(@PathVariable Long moduleId,
                                                       Authentication auth) {
        return ResponseEntity.ok(progressionService.inscrireModule(moduleId, auth));
    }
 
    // PATCH /api/progression/{id}/avancement
    // Body: { "pourcentage": 45.5 }
    // Rôle: ETUDIANT
    @PatchMapping("/{id}/avancement")
    public ResponseEntity<ProgressionModule> mettreAJour(@PathVariable Long id,
                                                           @RequestBody Map<String, Object> body,
                                                           Authentication auth) {
        float pourcentage = Float.parseFloat(body.get("pourcentage").toString());
        return ResponseEntity.ok(progressionService.mettreAJour(id, pourcentage, auth));
    }
}