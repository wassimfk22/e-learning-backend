// ════════════════════════════════════════════════════
// AnnonceController.java
// ════════════════════════════════════════════════════
package com.school.elearning.controller;
 
import com.school.elearning.model.Annonce;
import com.school.elearning.service.AnnonceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
 
@RestController
@RequestMapping("/api/annonces")
@RequiredArgsConstructor
public class AnnonceController {
 
    private final AnnonceService annonceService;
 
    // GET /api/annonces — public (authentifié)
    @GetMapping
    public ResponseEntity<List<Annonce>> getToutesAnnonces() {
        return ResponseEntity.ok(annonceService.getToutesAnnonces());
    }
 
    // POST /api/annonces — Rôle: MODERATEUR
    // Body: { "titre": "Maintenance prévue", "contenu": "Le système sera indisponible..." }
    @PostMapping
    public ResponseEntity<Annonce> publier(@RequestBody Map<String, String> body,
                                            Authentication auth) {
        return ResponseEntity.ok(annonceService.publierAnnonce(
                body.get("titre"), body.get("contenu"), auth));
    }
    
    
    
}
 