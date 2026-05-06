package com.school.elearning.service;

import com.school.elearning.model.Notification;
import com.school.elearning.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public List<Notification> findAll() { return notificationRepository.findAll(); }
    public Notification findById(Long id) { return notificationRepository.findById(id).orElseThrow(() -> new RuntimeException("Notification non trouvé")); }
    public Notification save(Notification entity) { return notificationRepository.save(entity); }
    public void delete(Long id) { notificationRepository.deleteById(id); }
}
