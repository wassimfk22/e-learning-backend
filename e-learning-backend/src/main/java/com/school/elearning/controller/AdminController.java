package com.school.elearning.controller;

import com.school.elearning.dto.*;
import com.school.elearning.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║  ADMIN CONTROLLER                                            ║
 * ║  Accès : ADMIN uniquement                                    ║
 * ║  Base  : /api/admin                                          ║
 * ╠══════════════════════════════════════════════════════════════╣
 * ║  GET  /utilisateurs               → tous les utilisateurs    ║
 * ║  GET  /utilisateurs/{id}          → un utilisateur           ║
 * ║  GET  /utilisateurs/etudiants     → tous les étudiants       ║
 * ║  GET  /utilisateurs/enseignants   → tous les enseignants     ║
 * ║  GET  /utilisateurs/moderateurs   → tous les modérateurs     ║
 * ║                                                              ║
 * ║  POST /etudiants                  → créer étudiant           ║
 * ║  POST /enseignants                → créer enseignant         ║
 * ║  POST /moderateurs                → créer modérateur         ║
 * ║                                                              ║
 * ║  PUT  /etudiants/{id}             → modifier étudiant        ║
 * ║  PUT  /enseignants/{id}           → modifier enseignant      ║
 * ║  PUT  /moderateurs/{id}           → modifier modérateur      ║
 * ║                                                              ║
 * ║  POST /utilisateurs/{id}/photo    → upload photo             ║
 * ║  DELETE /utilisateurs/{id}        → supprimer                ║
 * ╚══════════════════════════════════════════════════════════════╝
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UtilisateurService utilisateurService;

    // ══════════════════════════════════════════════
    // READ
    // ══════════════════════════════════════════════

    @GetMapping("/utilisateurs")
    public ResponseEntity<List<UtilisateurResponse>> getTous() {
        return ResponseEntity.ok(utilisateurService.getTousUtilisateurs());
    }

    @GetMapping("/utilisateurs/{id}")
    public ResponseEntity<UtilisateurResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(utilisateurService.getUtilisateurById(id));
    }

    @GetMapping("/utilisateurs/etudiants")
    public ResponseEntity<List<UtilisateurResponse>> getEtudiants() {
        return ResponseEntity.ok(utilisateurService.getTousEtudiants());
    }

    @GetMapping("/utilisateurs/enseignants")
    public ResponseEntity<List<UtilisateurResponse>> getEnseignants() {
        return ResponseEntity.ok(utilisateurService.getTousEnseignants());
    }

    @GetMapping("/utilisateurs/moderateurs")
    public ResponseEntity<List<UtilisateurResponse>> getModerateurs() {
        return ResponseEntity.ok(utilisateurService.getTousModerateurs());
    }

    // ══════════════════════════════════════════════
    // CREATE
    // ══════════════════════════════════════════════

    // POST /api/admin/etudiants
    // Body: { "nom":"Ali", "prenom":"Sara", "email":"sara@school.com",
    //         "motDePasse":"pass123", "telephone":"0600000099" }
    @PostMapping("/etudiants")
    public ResponseEntity<UtilisateurResponse> creerEtudiant(
            @RequestBody EtudiantRequest request) {
        return ResponseEntity.ok(utilisateurService.creerEtudiant(request));
    }

    // POST /api/admin/enseignants
    // Body: { "nom":"...", "prenom":"...", "email":"...", "motDePasse":"...",
    //         "telephone":"...", "specialite":"Mathématiques" }
    @PostMapping("/enseignants")
    public ResponseEntity<UtilisateurResponse> creerEnseignant(
            @RequestBody EnseignantRequest request) {
        return ResponseEntity.ok(utilisateurService.creerEnseignant(request));
    }

    // POST /api/admin/moderateurs
    // Body: { "nom":"...", "prenom":"...", "email":"...", "motDePasse":"...",
    //         "telephone":"...", "type":"INGENIEURIE" }
    @PostMapping("/moderateurs")
    public ResponseEntity<UtilisateurResponse> creerModerateur(
            @RequestBody ModerateurRequest request) {
        return ResponseEntity.ok(utilisateurService.creerModerateur(request));
    }

    // ══════════════════════════════════════════════
    // UPDATE
    // ══════════════════════════════════════════════

    // PUT /api/admin/etudiants/{id}
    // motDePasse optionnel : si vide ou absent → non modifié
    @PutMapping("/etudiants/{id}")
    public ResponseEntity<UtilisateurResponse> modifierEtudiant(
            @PathVariable Long id,
            @RequestBody EtudiantRequest request) {
        return ResponseEntity.ok(utilisateurService.modifierEtudiant(id, request));
    }

    // PUT /api/admin/enseignants/{id}
    @PutMapping("/enseignants/{id}")
    public ResponseEntity<UtilisateurResponse> modifierEnseignant(
            @PathVariable Long id,
            @RequestBody EnseignantRequest request) {
        return ResponseEntity.ok(utilisateurService.modifierEnseignant(id, request));
    }

    // PUT /api/admin/moderateurs/{id}
    @PutMapping("/moderateurs/{id}")
    public ResponseEntity<UtilisateurResponse> modifierModerateur(
            @PathVariable Long id,
            @RequestBody ModerateurRequest request) {
        return ResponseEntity.ok(utilisateurService.modifierModerateur(id, request));
    }

    // ══════════════════════════════════════════════
    // UPLOAD PHOTO
    // ══════════════════════════════════════════════

    // POST /api/admin/utilisateurs/{id}/photo
    // Content-Type: multipart/form-data
    // Champ : "fichier" → image (jpg/jpeg/png/webp, max 5 Mo)
    @PostMapping(value = "/utilisateurs/{id}/photo",
                 consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UtilisateurResponse> uploadPhoto(
            @PathVariable Long id,
            @RequestParam("fichier") MultipartFile fichier) {
        return ResponseEntity.ok(utilisateurService.uploadPhoto(id, fichier));
    }

    // ══════════════════════════════════════════════
    // DELETE
    // ══════════════════════════════════════════════

    // DELETE /api/admin/utilisateurs/{id}
    // Supprime l'utilisateur ET sa photo du disque
    @DeleteMapping("/utilisateurs/{id}")
    public ResponseEntity<String> supprimerUtilisateur(@PathVariable Long id) {
        utilisateurService.supprimerUtilisateur(id);
        return ResponseEntity.ok("Utilisateur supprimé avec succès");
    }
}