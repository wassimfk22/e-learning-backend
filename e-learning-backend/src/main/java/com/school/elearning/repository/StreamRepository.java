package com.school.elearning.repository;

import com.school.elearning.model.Enseignant;
import com.school.elearning.model.LiveSession;
import com.school.elearning.model.enums.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StreamRepository extends JpaRepository<LiveSession, Long> {

    List<LiveSession> findByEnseignant(Enseignant enseignant);

    List<LiveSession> findByStatus(SessionStatus status);

    List<LiveSession> findByEnseignantAndStatus(Enseignant enseignant, SessionStatus status);
    
}