package com.school.elearning.repository;

import com.school.elearning.model.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {
    java.util.List<Module> findByEnseignantId(Long enseignantId);
    java.util.List<Module> findByNiveauId(Long niveauId);

}
