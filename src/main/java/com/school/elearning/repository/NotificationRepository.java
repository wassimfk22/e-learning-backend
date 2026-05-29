package com.school.elearning.repository;

import com.school.elearning.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Toutes les notifs d'un utilisateur, triées par date décroissante
    List<Notification> findByDestinataireIdOrderByDateEnvoiDesc(Long destinataireId);

    // Notifs non lues d'un utilisateur
    List<Notification> findByDestinataireIdAndEstLueFalse(Long destinataireId);

    // Compter les non lues (utile pour le badge front)
    long countByDestinataireIdAndEstLueFalse(Long destinataireId);
    
    
    
}