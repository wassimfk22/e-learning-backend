package com.school.elearning.repository;

import com.school.elearning.model.Analytique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalytiqueRepository extends JpaRepository<Analytique, Long> {
    java.util.List<Analytique> findByEtudiantId(Long etudiantId);

}
