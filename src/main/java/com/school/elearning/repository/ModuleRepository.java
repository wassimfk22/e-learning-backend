package com.school.elearning.repository;

import com.school.elearning.model.Enseignant;
import com.school.elearning.model.Module;
import com.school.elearning.model.Niveau;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {
    List<Module> findByNiveau(Niveau niveau);
    List<Module> findByEnseignant(Enseignant enseignant);
}
