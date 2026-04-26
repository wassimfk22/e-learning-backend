package com.school.elearning.controller;

import com.school.elearning.dto.EtudiantRequest;
import com.school.elearning.dto.UtilisateurResponse;
import com.school.elearning.service.EtudiantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════╗
 * ║  MODERATION CONTROLLER                               ║
 * ║  Accès : ADMIN + MODERATEUR                          ║
 * ║  Base  : /api/moderation                             ║
 * ╠══════════════════════════════════════════════════════╣
 * ║  GET    /etudiants          → liste tous             ║
 * ║  GET    /etudiants/{id}     → un seul                ║
 * ║  POST   /etudiants          → créer                  ║
 * ║  PUT    /etudiants/{id}     → modifier               ║
 * ║  POST   /etudiants/{id}/photo → upload photo         ║
 * ║  DELETE /etudiants/{id}     → supprimer              ║
 * ╚══════════════════════════════════════════════════════╝
 */
@RestController
@RequestMapping("/api/moderation")
@PreAuthorize("hasAnyRole('ADMIN', 'MODERATEUR')")
@RequiredArgsConstructor
public class ModerationController {

    private final EtudiantService etudiantService;

    // ── GET ALL ─────────────────────────────────────────
    @GetMapping("/etudiants")
    public ResponseEntity<List<UtilisateurResponse>> getTousEtudiants() {
        return ResponseEntity.ok(etudiantService.getTousEtudiants());
    }

    // ── GET ONE ─────────────────────────────────────────
    @GetMapping("/etudiants/{id}")
    public ResponseEntity<UtilisateurResponse> getEtudiant(@PathVariable Long id) {
        return ResponseEntity.ok(etudiantService.getEtudiantById(id));
    }

    // ── CREATE ──────────────────────────────────────────
    // Content-Type: application/json
    // Body: { "nom":"Ali", "prenom":"Sara", "email":"sara@school.com",
    //         "motDePasse":"pass123", "telephone":"0600000099" }
    @PostMapping("/etudiants")
    public ResponseEntity<UtilisateurResponse> creerEtudiant(
            @RequestBody EtudiantRequest request) {
        return ResponseEntity.ok(etudiantService.creerEtudiant(request));
    }

    // ── UPDATE ──────────────────────────────────────────
    // motDePasse optionnel : si absent ou vide → non modifié
    @PutMapping("/etudiants/{id}")
    public ResponseEntity<UtilisateurResponse> modifierEtudiant(
            @PathVariable Long id,
            @RequestBody EtudiantRequest request) {
        return ResponseEntity.ok(etudiantService.modifierEtudiant(id, request));
    }

    // ── UPLOAD PHOTO ────────────────────────────────────
    // Content-Type: multipart/form-data
    // Champ : "fichier" → fichier image (jpg/jpeg/png/webp, max 5Mo)
    @PostMapping(value = "/etudiants/{id}/photo",
                 consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UtilisateurResponse> uploadPhoto(
            @PathVariable Long id,
            @RequestParam("fichier") MultipartFile fichier) {
        return ResponseEntity.ok(etudiantService.uploadPhoto(id, fichier));
    }

    // ── DELETE ──────────────────────────────────────────
    @DeleteMapping("/etudiants/{id}")
    public ResponseEntity<String> supprimerEtudiant(@PathVariable Long id) {
        etudiantService.supprimerEtudiant(id);
        return ResponseEntity.ok("Étudiant supprimé avec succès");
    }
}