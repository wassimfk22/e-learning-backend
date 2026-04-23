// ════════════════════════════════════════════════════
// NiveauController.java
// ════════════════════════════════════════════════════
package com.school.elearning.controller;
 
import com.school.elearning.model.Niveau;
import com.school.elearning.service.NiveauService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
 
@RestController
@RequestMapping("/api/niveaux")
@RequiredArgsConstructor
public class NiveauController {
 
    private final NiveauService niveauService;
 
    // GET /api/niveaux
    @GetMapping
    public ResponseEntity<List<Niveau>> getTousNiveaux() {
        return ResponseEntity.ok(niveauService.getTousNiveaux());
    }
 
    // GET /api/niveaux/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Niveau> getNiveau(@PathVariable Long id) {
        return ResponseEntity.ok(niveauService.getNiveauById(id));
    }
 
    // POST /api/niveaux — Rôle: ADMIN ou MODERATEUR
    // Body: { "nom": "Licence 3", "filiere": "Informatique", "annee": "2024-2025" }
    @PostMapping
    public ResponseEntity<Niveau> creer(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(niveauService.creerNiveau(
                body.get("nom"), body.get("filiere"), body.get("annee")));
    }
 
    // PUT /api/niveaux/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Niveau> modifier(@PathVariable Long id,
                                            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(niveauService.modifierNiveau(
                id, body.get("nom"), body.get("filiere"), body.get("annee")));
    }
 
    // DELETE /api/niveaux/{id} — Rôle: ADMIN
    @DeleteMapping("/{id}")
    public ResponseEntity<String> supprimer(@PathVariable Long id) {
        niveauService.supprimerNiveau(id);
        return ResponseEntity.ok("Niveau supprimé");
    }
}