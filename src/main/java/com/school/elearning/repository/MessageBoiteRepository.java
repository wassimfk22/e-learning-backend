package com.school.elearning.repository;

import com.school.elearning.model.MessageBoite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageBoiteRepository extends JpaRepository<MessageBoite, Long> {

    // Tous les messages d'une boite de réception, triés par date décroissante
    List<MessageBoite> findByBoiteReceptionIdOrderByDateEnvoiDesc(Long boiteReceptionId);
    
    
    
}