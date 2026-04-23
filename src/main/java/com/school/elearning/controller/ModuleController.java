// ════════════════════════════════════════════════════
// ModuleController.java
// ════════════════════════════════════════════════════
package com.school.elearning.controller;
 
import com.school.elearning.model.Module;
import com.school.elearning.service.ModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
 
@RestController
@RequestMapping("/api/modules")
@RequiredArgsConstructor
public class ModuleController {
 
    private final ModuleService moduleService;
 
    // GET /api/modules
    @GetMapping
    public ResponseEntity<List<Module>> getTousModules() {
        return ResponseEntity.ok(moduleService.getTousModules());
    }
 
    // GET /api/modules/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Module> getModule(@PathVariable Long id) {
        return ResponseEntity.ok(moduleService.getModuleById(id));
    }
 
    // GET /api/modules/niveau/{niveauId}
    @GetMapping("/niveau/{niveauId}")
    public ResponseEntity<List<Module>> getByNiveau(@PathVariable Long niveauId) {
        return ResponseEntity.ok(moduleService.getModulesByNiveau(niveauId));
    }
 
    // GET /api/modules/mes-modules — Rôle: ENSEIGNANT
    @GetMapping("/mes-modules")
    public ResponseEntity<List<Module>> getMesModules(Authentication auth) {
        return ResponseEntity.ok(moduleService.getMesModules(auth));
    }
 
    // POST /api/modules
    // Body: { "titre": "Spring Boot", "description": "...", "duree": 20, "niveauId": 1 }
    // Rôle: ENSEIGNANT
    @PostMapping
    public ResponseEntity<Module> creer(@RequestBody Map<String, Object> body, Authentication auth) {
        String titre = (String) body.get("titre");
        String description = (String) body.get("description");
        int duree = Integer.parseInt(body.get("duree").toString());
        Long niveauId = Long.valueOf(body.get("niveauId").toString());
        return ResponseEntity.ok(moduleService.creerModule(titre, description, duree, niveauId, auth));
    }
 
    // PUT /api/modules/{id}
    // Body: { "titre": "...", "description": "...", "duree": 25 }
    // Rôle: ENSEIGNANT
    @PutMapping("/{id}")
    public ResponseEntity<Module> modifier(@PathVariable Long id,
                                            @RequestBody Map<String, Object> body,
                                            Authentication auth) {
        String titre = (String) body.get("titre");
        String description = (String) body.get("description");
        int duree = Integer.parseInt(body.get("duree").toString());
        return ResponseEntity.ok(moduleService.modifierModule(id, titre, description, duree, auth));
    }
    
    
    
}