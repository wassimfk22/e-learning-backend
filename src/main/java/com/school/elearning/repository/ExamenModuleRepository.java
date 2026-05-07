// ── ExamenModuleRepository.java ──────────────────────────────────
package com.school.elearning.repository;

import com.school.elearning.model.Enseignant;
import com.school.elearning.model.ExamenModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamenModuleRepository extends JpaRepository<ExamenModule, Long> {

    List<ExamenModule> findByModuleId(Long moduleId);

    List<ExamenModule> findByEnseignant(Enseignant enseignant);

    boolean existsByTitreAndModuleId(String titre, Long moduleId);
    
    
    
}