package com.school.elearning.repository;

import com.school.elearning.model.CoursProgression;
import com.school.elearning.model.Etudiant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CoursProgressionRepository extends JpaRepository<CoursProgression, Long> {

    Optional<CoursProgression> findByEtudiantIdAndCoursId(Long etudiantId, Long coursId);

    List<CoursProgression> findByEtudiant(Etudiant etudiant);

    // Cours terminés d'un étudiant dans un module donné
    @Query("SELECT cp FROM CoursProgression cp WHERE cp.etudiant = :etudiant " +
           "AND cp.cours.module.id = :moduleId AND cp.statut = 'TERMINE'")
    List<CoursProgression> findTerminesParModuleId(
        @Param("etudiant") Etudiant etudiant,
        @Param("moduleId") Long moduleId
    );

    // Total cours consultés par l'étudiant dans un module
    @Query("SELECT COUNT(cp) FROM CoursProgression cp WHERE cp.etudiant = :etudiant " +
           "AND cp.cours.module.id = :moduleId")
    long countByEtudiantAndModuleId(
        @Param("etudiant") Etudiant etudiant,
        @Param("moduleId") Long moduleId
    );

    boolean existsByEtudiantIdAndCoursId(Long etudiantId, Long coursId);
    
    
    
}