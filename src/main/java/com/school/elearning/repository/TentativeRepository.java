package com.school.elearning.repository;

import com.school.elearning.model.Tentative;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TentativeRepository extends JpaRepository<Tentative, Long> {
    java.util.List<Tentative> findByEtudiantIdAndEvaluationId(Long etudiantId, Long evaluationId);
    java.util.List<Tentative> findByEtudiantId(Long etudiantId);

}
