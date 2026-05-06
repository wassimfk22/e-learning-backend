package com.school.elearning.controller;

import com.school.elearning.dto.ModuleRequest;
import com.school.elearning.dto.ModuleResponse;
import com.school.elearning.service.ModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/modules")
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleService moduleService;

    // ── GET ALL ─────────────────────────────────────────────────
    // Accès : tous les authentifiés (SecurityConfig)
    @GetMapping
    public ResponseEntity<List<ModuleResponse>> getTousModules() {
        return ResponseEntity.ok(moduleService.getTousModules());
    }

    // ── GET ONE ─────────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<ModuleResponse> getModule(@PathVariable Long id) {
        return ResponseEntity.ok(moduleService.getModuleById(id));
    }

    // ── GET BY NIVEAU ────────────────────────────────────────────
    @GetMapping("/niveau/{niveauId}")
    public ResponseEntity<List<ModuleResponse>> getByNiveau(@PathVariable Long niveauId) {
        return ResponseEntity.ok(moduleService.getModulesByNiveau(niveauId));
    }
    
    // ── GET BY ENSEIGNANT ────────────────────────────────────────────
    @GetMapping("/enseignant/{enseignantId}")
    public ResponseEntity<List<ModuleResponse>> getByEnseignant(@PathVariable Long enseignantId) {
    	return ResponseEntity.ok(moduleService.getModulesByEnseignant(enseignantId));
    }
    
    // ── MES MODULES ──────────────────────────────────────────────
    // Accès : ENSEIGNANT uniquement (SecurityConfig)
    // L'enseignant connecté voit uniquement ses modules
    @GetMapping("/mes-modules")
    public ResponseEntity<List<ModuleResponse>> getMesModules(Authentication auth) {
        return ResponseEntity.ok(moduleService.getMesModules(auth));
    }

    // ── CREATE ───────────────────────────────────────────────────
    // Accès : MODERATEUR + ADMIN (SecurityConfig)
    // Body (tous requis) :
    // {
    //   "titre": "Programmation Java",
    //   "description": "Introduction à Java et Spring Boot",
    //   "duree": "30h",
    //   "niveauId": 1,
    //   "enseignantId": 2
    // }
    @PostMapping
    public ResponseEntity<ModuleResponse> creer(@RequestBody ModuleRequest request) {
        return ResponseEntity.ok(moduleService.creerModule(request));
    }

    // ── UPDATE COMPLET — PUT ─────────────────────────────────────
    // Accès : MODERATEUR + ADMIN (SecurityConfig)
    // Remplace TOUT l'objet → tous les champs obligatoires
    // Body :
    // {
    //   "titre": "Java Avancé",
    //   "description": "Design Patterns",
    //   "duree": "40h",
    //   "niveauId": 1,
    //   "enseignantId": 2
    // }
    @PutMapping("/{id}")
    public ResponseEntity<ModuleResponse> modifier(@PathVariable Long id,
                                                    @RequestBody ModuleRequest request) {
        return ResponseEntity.ok(moduleService.modifierModule(id, request));
    }

    // ── UPDATE PARTIEL — PATCH ───────────────────────────────────
    // Accès : MODERATEUR + ADMIN (SecurityConfig)
    // Modifie UNIQUEMENT les champs présents dans le body
    // Exemples valides :
    //   { "titre": "Nouveau titre" }                        → modifie seulement le titre
    //   { "duree": "50h", "niveauId": 2 }                  → modifie durée + niveau
    //   { "description": "...", "enseignantId": 3 }         → modifie description + enseignant
    @PatchMapping("/{id}")
    public ResponseEntity<ModuleResponse> modifierPartiellement(@PathVariable Long id,
                                                                  @RequestBody ModuleRequest request) {
        return ResponseEntity.ok(moduleService.modifierModulePartiellement(id, request));
    }

    // ── DELETE ───────────────────────────────────────────────────
    // Accès : MODERATEUR + ADMIN (SecurityConfig)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> supprimer(@PathVariable Long id) {
        moduleService.supprimerModule(id);
        return ResponseEntity.ok("Module supprimé avec succès");
    }
}