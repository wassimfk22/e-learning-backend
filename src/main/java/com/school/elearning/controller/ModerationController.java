package com.school.elearning.controller;

import com.school.elearning.dto.EtudiantRequest;
import com.school.elearning.dto.UtilisateurResponse;
import com.school.elearning.service.EtudiantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Accessible par : ADMIN + MODERATEUR
 * Route de base  : /api/moderation
 *
 * Fonctionnalités :
 *   GET    /api/moderation/etudiants          → liste tous les étudiants
 *   GET    /api/moderation/etudiants/{id}     → détail d'un étudiant
 *   POST   /api/moderation/etudiants          → créer un étudiant
 *   PUT    /api/moderation/etudiants/{id}     → modifier un étudiant
 *   DELETE /api/moderation/etudiants/{id}     → supprimer un étudiant
 */
@RestController
@RequestMapping("/api/moderation")
@PreAuthorize("hasAnyRole('ADMIN', 'MODERATEUR')")
@RequiredArgsConstructor
public class ModerationController {

    private final EtudiantService etudiantService;

    // ── GET ALL ────────────────────────────────────────────────────
    @GetMapping("/etudiants")
    public ResponseEntity<List<UtilisateurResponse>> getTousEtudiants() {
        return ResponseEntity.ok(etudiantService.getTousEtudiants());
    }

    // ── GET ONE ────────────────────────────────────────────────────
    @GetMapping("/etudiants/{id}")
    public ResponseEntity<UtilisateurResponse> getEtudiant(@PathVariable Long id) {
        return ResponseEntity.ok(etudiantService.getEtudiantById(id));
    }

    // ── CREATE ─────────────────────────────────────────────────────
    // Body: { "nom":"Ali", "prenom":"Sara", "email":"sara@school.com",
    //         "motDePasse":"pass123", "telephone":"0600000099" }
    @PostMapping("/etudiants")
    public ResponseEntity<UtilisateurResponse> creerEtudiant(@RequestBody EtudiantRequest request) {
        return ResponseEntity.ok(etudiantService.creerEtudiant(request));
    }

    // ── UPDATE ─────────────────────────────────────────────────────
    // Body: mêmes champs (motDePasse optionnel : si absent ou vide → non modifié)
    @PutMapping("/etudiants/{id}")
    public ResponseEntity<UtilisateurResponse> modifierEtudiant(@PathVariable Long id,
                                                                  @RequestBody EtudiantRequest request) {
        return ResponseEntity.ok(etudiantService.modifierEtudiant(id, request));
    }

    // ── DELETE ─────────────────────────────────────────────────────
    @DeleteMapping("/etudiants/{id}")
    public ResponseEntity<String> supprimerEtudiant(@PathVariable Long id) {
        etudiantService.supprimerEtudiant(id);
        return ResponseEntity.ok("Étudiant supprimé avec succès");
    }
    
    
    
}