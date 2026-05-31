package com.school.elearning.controller;

import com.school.elearning.dto.CalendrierResponse;
import com.school.elearning.dto.EvenementRequest;
import com.school.elearning.dto.EvenementResponse;
import com.school.elearning.service.CalendrierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║  CALENDRIER CONTROLLER                                       ║
 * ║  Base : /api/calendrier                                      ║
 * ╠══════════════════════════════════════════════════════════════╣
 * ║  LECTURE (tous authentifiés)                                 ║
 * ║  GET  /mon-calendrier          → calendrier de mon niveau    ║
 * ║  GET  /niveau/{niveauId}       → calendrier d'un niveau      ║
 * ║  GET  /tous                    → tous (admin)                ║
 * ║  GET  /evenements/{id}         → détail d'un événement       ║
 * ║                                                              ║
 * ║  CRUD ÉVÉNEMENTS (ADMIN + MODERATEUR)                        ║
 * ║  POST   /evenements            → créer un événement          ║
 * ║  PUT    /evenements/{id}       → modifier                    ║
 * ║  DELETE /evenements/{id}       → supprimer                   ║
 * ╚══════════════════════════════════════════════════════════════╝
 */
@RestController
@RequestMapping("/api/calendrier")
@RequiredArgsConstructor
public class CalendrierController {

    private final CalendrierService calendrierService;

    // ── LECTURE ──────────────────────────────────────────────────

    /**
     * GET /api/calendrier/mon-calendrier
     * Calendrier du niveau de l'utilisateur connecté
     * Accès : ETUDIANT, ENSEIGNANT
     */
    @GetMapping("/mon-calendrier")
    @PreAuthorize("hasAnyRole('ETUDIANT', 'ENSEIGNANT')")
    public ResponseEntity<CalendrierResponse> getMonCalendrier(Authentication auth) {
        return ResponseEntity.ok(calendrierService.getMonCalendrier(auth));
    }

    /**
     * GET /api/calendrier/niveau/{niveauId}
     * Calendrier d'un niveau spécifique
     * Accès : tous les authentifiés
     */
    @GetMapping("/niveau/{niveauId}")
    public ResponseEntity<CalendrierResponse> getCalendrierParNiveau(@PathVariable Long niveauId) {
        return ResponseEntity.ok(calendrierService.getCalendrierParNiveau(niveauId));
    }

    /**
     * GET /api/calendrier/tous
     * Tous les calendriers
     * Accès : ADMIN, MODERATEUR
     */
    @GetMapping("/tous")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATEUR')")
    public ResponseEntity<List<CalendrierResponse>> getTousLesCalendriers() {
        return ResponseEntity.ok(calendrierService.getTousLesCalendriers());
    }

    /**
     * GET /api/calendrier/evenements/{id}
     * Détail d'un événement
     * Accès : tous les authentifiés
     */
    @GetMapping("/evenements/{id}")
    public ResponseEntity<EvenementResponse> getEvenement(@PathVariable Long id) {
        return ResponseEntity.ok(calendrierService.getEvenement(id));
    }

    // ── CRUD ÉVÉNEMENTS (MODERATEUR / ADMIN) ─────────────────────

    /**
     * POST /api/calendrier/evenements
     * Body:
     * {
     *   "titre": "Examen final Java",
     *   "description": "Salle B - 2ème étage",
     *   "dateDebut": "2026-06-15T09:00:00",
     *   "dateFin": "2026-06-15T12:00:00",
     *   "type": "EXAMEN",
     *   "calendrierId": 1
     * }
     * Accès : ADMIN, MODERATEUR
     */
    @PostMapping("/evenements")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATEUR')")
    public ResponseEntity<EvenementResponse> creerEvenement(
            @Valid @RequestBody EvenementRequest request,
            Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(calendrierService.creerEvenement(request, auth));
    }

    /**
     * PUT /api/calendrier/evenements/{id}
     * Accès : ADMIN, MODERATEUR
     */
    @PutMapping("/evenements/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATEUR')")
    public ResponseEntity<EvenementResponse> modifierEvenement(
            @PathVariable Long id,
            @Valid @RequestBody EvenementRequest request,
            Authentication auth) {
        return ResponseEntity.ok(calendrierService.modifierEvenement(id, request, auth));
    }

    /**
     * DELETE /api/calendrier/evenements/{id}
     * Accès : ADMIN, MODERATEUR
     */
    @DeleteMapping("/evenements/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATEUR')")
    public ResponseEntity<Map<String, String>> supprimerEvenement(
            @PathVariable Long id,
            Authentication auth) {
        calendrierService.supprimerEvenement(id, auth);
        return ResponseEntity.ok(Map.of("message", "Événement supprimé avec succès"));
    }
    
    
    
}