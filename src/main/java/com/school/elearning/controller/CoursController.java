// ════════════════════════════════════════════════════
// CoursController.java
// ════════════════════════════════════════════════════
package com.school.elearning.controller;
 
import com.school.elearning.model.Cours;
import com.school.elearning.service.CoursService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
 
import java.util.List;
import java.util.Map;
 
@RestController
@RequestMapping("/api/cours")
@RequiredArgsConstructor
public class CoursController {
 
    private final CoursService coursService;
 
    // GET /api/cours
    @GetMapping
    public ResponseEntity<List<Cours>> getTousCours() {
        return ResponseEntity.ok(coursService.getTousCours());
    }
 
    // GET /api/cours/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Cours> getCours(@PathVariable Long id) {
        return ResponseEntity.ok(coursService.getCoursById(id));
    }
 
    // GET /api/cours/module/{moduleId}
    @GetMapping("/module/{moduleId}")
    public ResponseEntity<List<Cours>> getByModule(@PathVariable Long moduleId) {
        return ResponseEntity.ok(coursService.getCoursByModule(moduleId));
    }
 
    // POST /api/cours
    // Body: { "titre": "Introduction à Java", "moduleId": 1 }
    // Rôle: ENSEIGNANT
    @PostMapping
    public ResponseEntity<Cours> creer(@RequestBody Map<String, Object> body, Authentication auth) {
        String titre = (String) body.get("titre");
        Long moduleId = Long.valueOf(body.get("moduleId").toString());
        return ResponseEntity.ok(coursService.creerCours(titre, moduleId, auth));
    }
 
    // PUT /api/cours/{id}
    // Body: { "titre": "Nouveau titre" }
    // Rôle: ENSEIGNANT
    @PutMapping("/{id}")
    public ResponseEntity<Cours> modifier(@PathVariable Long id,
                                           @RequestBody Map<String, String> body,
                                           Authentication auth) {
        return ResponseEntity.ok(coursService.modifierCours(id, body.get("titre"), auth));
    }
 
    // DELETE /api/cours/{id}
    // Rôle: ENSEIGNANT
    @DeleteMapping("/{id}")
    public ResponseEntity<String> supprimer(@PathVariable Long id, Authentication auth) {
        coursService.supprimerCours(id, auth);
        return ResponseEntity.ok("Cours supprimé avec succès");
    }
}
 