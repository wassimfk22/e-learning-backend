package com.school.elearning.controller;

import com.school.elearning.dto.EtudiantRequest;
import com.school.elearning.dto.UtilisateurResponse;
import com.school.elearning.model.Utilisateur;
import com.school.elearning.repository.UtilisateurRepository;
import com.school.elearning.service.EtudiantService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

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
@RequiredArgsConstructor @Validated
public class ModerationController {

    private final EtudiantService etudiantService;
    private final UtilisateurRepository utilisateurRepository;

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
            @RequestBody @Valid EtudiantRequest request) {
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
    
	 // ── AFFECTER NIVEAU ──────────────────────────────────
	 // PUT /api/moderation/etudiants/{id}/niveau/{niveauId}
	 @PutMapping("/etudiants/{id}/niveau/{niveauId}")
	 public ResponseEntity<UtilisateurResponse> affecterNiveau(
	         @PathVariable Long id,
	         @PathVariable Long niveauId) {
	     return ResponseEntity.ok(etudiantService.affecterNiveau(id, niveauId));
	 }
	 
	 @PostMapping ("/boite/{idUser}")
	 public ResponseEntity <String> initialiserBoiteReception ( @PathVariable Long idUser ){
		 Optional <Utilisateur> u = this.utilisateurRepository.findById(idUser);
		 if ( u.isPresent() ) {
			 Utilisateur user = u.get();
			 this.etudiantService.initialiserBoiteReception(user);
			 this.utilisateurRepository.save(user);
			 return new ResponseEntity<String>("Boite de réception créée avec succés !", HttpStatus.OK);
		 } else {
			 return new ResponseEntity<String>("Introuvable !", HttpStatus.NOT_FOUND);
		 }
	 }
    
}