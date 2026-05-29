package com.school.elearning.controller;

import com.school.elearning.dto.NotificationResponse;
import com.school.elearning.model.Notification;
import com.school.elearning.repository.NotificationRepository;
import com.school.elearning.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;

    // GET /api/notifications/mes-notifications
    @GetMapping("/mes-notifications")
    public ResponseEntity<List<NotificationResponse>> getMesNotifications(Authentication auth) {
        return ResponseEntity.ok(
                notificationRepository.findByDestinataireIdOrderByDateEnvoiDesc(getIdConnecte(auth))
                        .stream().map(this::toResponse).collect(Collectors.toList()));
    }

    // GET /api/notifications/mes-notifications/non-lues
    @GetMapping("/mes-notifications/non-lues")
    public ResponseEntity<List<NotificationResponse>> getMesNonLues(Authentication auth) {
        return ResponseEntity.ok(
                notificationRepository.findByDestinataireIdAndEstLueFalse(getIdConnecte(auth))
                        .stream().map(this::toResponse).collect(Collectors.toList()));
    }

    // GET /api/notifications/mes-notifications/count
    @GetMapping("/mes-notifications/count")
    public ResponseEntity<Map<String, Long>> getCount(Authentication auth) {
        long count = notificationRepository.countByDestinataireIdAndEstLueFalse(getIdConnecte(auth));
        return ResponseEntity.ok(Map.of("nonLues", count));
    }

    // PUT /api/notifications/{id}/lue
    @PutMapping("/{id}/lue")
    public ResponseEntity<NotificationResponse> marquerLue(@PathVariable Long id) {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification non trouvée"));
        n.setEstLue(true);
        return ResponseEntity.ok(toResponse(notificationRepository.save(n)));
    }

    // PUT /api/notifications/tout-lire
    @PutMapping("/tout-lire")
    public ResponseEntity<Map<String, String>> toutLire(Authentication auth) {
        List<Notification> nonLues =
                notificationRepository.findByDestinataireIdAndEstLueFalse(getIdConnecte(auth));
        nonLues.forEach(n -> n.setEstLue(true));
        notificationRepository.saveAll(nonLues);
        return ResponseEntity.ok(Map.of("message", nonLues.size() + " notification(s) marquée(s) comme lue(s)"));
    }

    // ── HELPERS ──────────────────────────────────────────────────

    private Long getIdConnecte(Authentication auth) {
        return ((CustomUserDetails) auth.getPrincipal()).getUtilisateur().getId();
    }

    private NotificationResponse toResponse(Notification n) {
        NotificationResponse r = new NotificationResponse();
        r.setId(n.getId());
        r.setContenu(n.getContenu());
        r.setDateEnvoi(n.getDateEnvoi());
        r.setEstLue(n.isEstLue());
        if (n.getAnnonce() != null) {
            r.setAnnonceId(n.getAnnonce().getId());
            r.setAnnonceTitre(n.getAnnonce().getTitre());
        }
        return r;
    }
}