package com.school.elearning.repository;

import com.school.elearning.model.Etudiant;
import com.school.elearning.model.ExamenModule;
import com.school.elearning.model.PassageExamen;
import com.school.elearning.model.enums.StatutPassageExamen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PassageExamenRepository extends JpaRepository<PassageExamen, Long> {

    Optional<PassageExamen> findByEtudiantAndExamen(Etudiant etudiant, ExamenModule examen);

    boolean existsByEtudiantAndExamen(Etudiant etudiant, ExamenModule examen);

    // Tous les passages soumis d'un module (pour le prof)
    @Query("SELECT p FROM PassageExamen p WHERE p.examen.module.id = :moduleId " +
           "AND p.statut IN ('SOUMIS', 'CORRIGE')")
    List<PassageExamen> findByModuleId(@Param("moduleId") Long moduleId);

    // Passages à corriger (SOUMIS) pour un examen donné
    List<PassageExamen> findByExamenAndStatut(ExamenModule examen, StatutPassageExamen statut);

    // Pour le calcul de moyenne d'un étudiant dans un module
    @Query("SELECT p FROM PassageExamen p WHERE p.etudiant = :etudiant " +
           "AND p.examen.module.id = :moduleId AND p.statut = 'CORRIGE'")
    List<PassageExamen> findCorigesParModuleId(
        @Param("etudiant") Etudiant etudiant,
        @Param("moduleId") Long moduleId
    );

    List<PassageExamen> findTop5ByEtudiantOrderByDateDebutDesc(Etudiant etudiant);
    
    
    
}