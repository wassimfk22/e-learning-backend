package com.school.elearning.repository;

import com.school.elearning.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    java.util.List<Note> findByTentativeEtudiantId(Long etudiantId);

}
