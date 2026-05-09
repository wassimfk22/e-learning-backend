package com.school.elearning.repository;

import com.school.elearning.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // Tous les messages RACINES d'une communauté (pas de réponses), triés par date
    List<Message> findByCommunauteIdAndParentIsNullOrderByDateEnvoiAsc(Long communauteId);

    // Tous les messages d'une communauté (pour comptage, etc.)
    List<Message> findByCommunauteId(Long communauteId);
    
    
    
}