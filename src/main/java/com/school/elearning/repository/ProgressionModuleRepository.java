package com.school.elearning.repository;

import com.school.elearning.model.Etudiant;
import com.school.elearning.model.Module;
import com.school.elearning.model.ProgressionModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgressionModuleRepository extends JpaRepository<ProgressionModule, Long> {

    List<ProgressionModule> findByEtudiant(Etudiant etudiant);

    boolean existsByEtudiantAndModule(Etudiant etudiant, Module module);

    // NOUVEAU
    Optional<ProgressionModule> findByEtudiantAndModule(Etudiant etudiant, Module module);

    // NOUVEAU — pour admin/mod/enseignant
    List<ProgressionModule> findByModule(Module module);

    // NOUVEAU — tous les étudiants d'un niveau
    @Query("SELECT p FROM ProgressionModule p WHERE p.module.niveau.id = :niveauId")
    List<ProgressionModule> findByNiveauId(@Param("niveauId") Long niveauId);

    // NOUVEAU — progression d'un étudiant pour un module spécifique
    @Query("SELECT p FROM ProgressionModule p WHERE p.etudiant.id = :etudiantId AND p.module.id = :moduleId")
    Optional<ProgressionModule> findByEtudiantIdAndModuleId(
            @Param("etudiantId") Long etudiantId,
            @Param("moduleId") Long moduleId);
    
    
    
}