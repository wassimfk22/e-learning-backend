package com.school.elearning.repository;

import com.school.elearning.model.Etudiant;
import com.school.elearning.model.ProgressionModule;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProgressionModuleRepository extends JpaRepository<ProgressionModule, Long> {
    List<ProgressionModule> findByEtudiant(Etudiant etudiant);
    boolean existsByEtudiantAndModule(Etudiant etudiant, com.school.elearning.model.Module module);
}
