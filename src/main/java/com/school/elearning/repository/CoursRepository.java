package com.school.elearning.repository;

import com.school.elearning.model.Cours;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoursRepository extends JpaRepository<Cours, Long> {
    List<Cours> findByModule(com.school.elearning.model.Module module);
    
    // OPTIMISATION : Vérifie si un cours appartient à un niveau spécifique
    // Spring va chercher : cours.module.niveau.id
    boolean existsByIdAndModule_Niveau_Id(Long coursId, Long niveauId);

    // Pratique : Récupérer tous les cours d'un niveau (pour l'affichage étudiant)
    List<Cours> findByModule_Niveau_Id(Long niveauId);
    
    
}
