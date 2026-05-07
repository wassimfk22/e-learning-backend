package com.school.elearning.repository;

import com.school.elearning.model.PassageExamen;
import com.school.elearning.model.ReponseExamenEtudiant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReponseExamenEtudiantRepository extends JpaRepository<ReponseExamenEtudiant, Long> {

    List<ReponseExamenEtudiant> findByPassage(PassageExamen passage);

    Optional<ReponseExamenEtudiant> findByPassageIdAndQuestionId(Long passageId, Long questionId);

    // Nombre de réponses déjà corrigées dans un passage
    long countByPassageAndEstJusteIsNotNull(PassageExamen passage);

    // Somme des points obtenus après correction
    @Query("SELECT COALESCE(SUM(r.pointsObtenus), 0) FROM ReponseExamenEtudiant r WHERE r.passage = :passage")
    double sumPointsByPassage(@Param("passage") PassageExamen passage);

    // Toutes les réponses non encore corrigées d'un passage
    List<ReponseExamenEtudiant> findByPassageAndEstJusteIsNull(PassageExamen passage);
    
    
    
}