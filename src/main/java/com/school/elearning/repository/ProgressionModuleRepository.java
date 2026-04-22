package com.school.elearning.repository;

import com.school.elearning.model.ProgressionModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProgressionModuleRepository extends JpaRepository<ProgressionModule, Long> {
    java.util.List<ProgressionModule> findByEtudiantId(Long etudiantId);
    java.util.Optional<ProgressionModule> findByEtudiantIdAndModuleId(Long etudiantId, Long moduleId);

}
