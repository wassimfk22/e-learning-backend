package com.school.elearning.repository;

import com.school.elearning.model.ReponseEtudiant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReponseEtudiantRepository extends JpaRepository<ReponseEtudiant, Long> {
}
