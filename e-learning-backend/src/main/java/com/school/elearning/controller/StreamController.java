package com.school.elearning.controller;

import com.school.elearning.model.LiveSession;
import com.school.elearning.model.enums.SessionStatus;
import com.school.elearning.service.StreamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stream")
@RequiredArgsConstructor
public class StreamController {

    private final StreamService streamService;

    // POST /api/stream/sessions
    // Body: { "titre": "Cours Java", "description": "...", "debut": "2025-06-01T10:00:00", "fin": "2025-06-01T12:00:00" }
    // Rôle requis: ENSEIGNANT
    @PostMapping("/sessions")
    public ResponseEntity<LiveSession> creerSession(@RequestBody Map<String, Object> body,
                                                     Authentication auth) {
        String titre = (String) body.get("titre");
        String description = (String) body.get("description");
        LocalDateTime debut = LocalDateTime.parse((String) body.get("debut"));
        LocalDateTime fin = LocalDateTime.parse((String) body.get("fin"));

        LiveSession session = streamService.creerSession(titre, description, debut, fin, auth);
        return ResponseEntity.ok(session);
    }

    // PATCH /api/stream/sessions/{id}/status
    // Body: { "status": "EN_COURS" }
    // Rôle requis: ENSEIGNANT (propriétaire)
    @PatchMapping("/sessions/{id}/status")
    public ResponseEntity<LiveSession> changerStatus(@PathVariable Long id,
                                                      @RequestBody Map<String, String> body,
                                                      Authentication auth) {
        SessionStatus status = SessionStatus.valueOf(body.get("status"));
        LiveSession session = streamService.changerStatus(id, status, auth);
        return ResponseEntity.ok(session);
    }

    // GET /api/stream/sessions/mes-sessions
    // Rôle requis: ENSEIGNANT
    @GetMapping("/sessions/mes-sessions")
    public ResponseEntity<List<LiveSession>> getMesSessions(Authentication auth) {
        return ResponseEntity.ok(streamService.getMesSessions(auth));
    }

    // GET /api/stream/sessions/actives
    // Rôle requis: tous authentifiés
    @GetMapping("/sessions/actives")
    public ResponseEntity<List<LiveSession>> getSessionsActives() {
        return ResponseEntity.ok(streamService.getSessionsActives());
    }

    // GET /api/stream/sessions/planifiees
    // Rôle requis: tous authentifiés
    @GetMapping("/sessions/planifiees")
    public ResponseEntity<List<LiveSession>> getSessionsPlanifiees() {
        return ResponseEntity.ok(streamService.getSessionsPlanifiees());
    }
}