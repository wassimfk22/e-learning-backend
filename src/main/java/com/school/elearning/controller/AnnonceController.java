package com.school.elearning.controller;

import com.school.elearning.dto.AnnonceRequest;
import com.school.elearning.dto.AnnonceResponse;
import com.school.elearning.service.AnnonceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║  ANNONCE CONTROLLER                                          ║
 * ║  Base : /api/annonces                                        ║
 * ╠══════════════════════════════════════════════════════════════╣
 * ║  GET  /            → toutes les annonces (tous auth.)        ║
 * ║  GET  /{id}        → détail d'une annonce (tous auth.)       ║
 * ║  POST /            → publier (ADMIN + MODERATEUR)            ║
 * ║  PUT  /{id}        → modifier (auteur uniquement)            ║
 * ║  DELETE /{id}      → supprimer (auteur uniquement)           ║
 * ╚══════════════════════════════════════════════════════════════╝
 *
 * Body POST/PUT :
 * {
 *   "titre": "Examen final",
 *   "contenu": "L'examen final aura lieu le 15 juin.",
 *   "evenementTitre": "Examen final S2",          // optionnel
 *   "evenementDescription": "Salle A, 3ème étage",// optionnel
 *   "evenementDateDebut": "2025-06-15T09:00:00",  // optionnel
 *   "evenementDateFin": "2025-06-15T12:00:00",    // optionnel
 *   "evenementType": "EXAMEN"                      // optionnel : COURS|EXAMEN|QUIZ|STREAM|AUTRE
 * }
 */
@RestController
@RequestMapping("/api/annonces")
@RequiredArgsConstructor
public class AnnonceController {

    private final AnnonceService annonceService;

    // ── LECTURE (tous les authentifiés) ─────────────────────────
    @GetMapping
    public ResponseEntity<List<AnnonceResponse>> getToutesAnnonces() {
        return ResponseEntity.ok(annonceService.getToutesAnnonces());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnnonceResponse> getAnnonce(@PathVariable Long id) {
        return ResponseEntity.ok(annonceService.getAnnonceById(id));
    }

    // ── PUBLIER (ADMIN + MODERATEUR) ─────────────────────────────
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATEUR')")
    public ResponseEntity<AnnonceResponse> publier(
            @RequestBody AnnonceRequest request,
            Authentication auth) {
        return ResponseEntity.ok(annonceService.publierAnnonce(request, auth));
    }

    // ── MODIFIER (auteur uniquement — vérifié dans le service) ───
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATEUR')")
    public ResponseEntity<AnnonceResponse> modifier(
            @PathVariable Long id,
            @RequestBody AnnonceRequest request,
            Authentication auth) {
        return ResponseEntity.ok(annonceService.modifierAnnonce(id, request, auth));
    }

    // ── SUPPRIMER (auteur uniquement — vérifié dans le service) ──
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATEUR')")
    public ResponseEntity<String> supprimer(
            @PathVariable Long id,
            Authentication auth) {
        annonceService.supprimerAnnonce(id, auth);
        return ResponseEntity.ok("Annonce supprimée avec succès");
    }
    
    
    
}