package com.school.elearning.controller;

import com.school.elearning.model.Notification;
import com.school.elearning.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationRepository notificationRepository;

    @GetMapping("/{utilisateurId}")
    public ResponseEntity<List<Notification>> getAll(@PathVariable Long utilisateurId) {
        return ResponseEntity.ok(notificationRepository.findByDestinataire_Id(utilisateurId));
    }

    @GetMapping("/{utilisateurId}/unread")
    public ResponseEntity<List<Notification>> getUnread(@PathVariable Long utilisateurId) {
        return ResponseEntity.ok(notificationRepository.findByDestinataire_IdAndEstLueFalse(utilisateurId));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable Long id) {
        Notification n = notificationRepository.findById(id).orElseThrow(() -> new RuntimeException("Notification non trouvée"));
        n.setEstLue(true);
        return ResponseEntity.ok(notificationRepository.save(n));
    }
}
