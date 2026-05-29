package com.school.elearning.repository;

import com.school.elearning.model.Cours;
import com.school.elearning.model.Etudiant;
import com.school.elearning.model.QuestionPedagogique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionPedagogiqueRepository extends JpaRepository<QuestionPedagogique, Long> {

    // Questions d'un cours (pour l'enseignant)
    List<QuestionPedagogique> findByCours(Cours cours);

    // Questions d'un étudiant pour un cours (pour éviter les doublons si besoin)
    List<QuestionPedagogique> findByEtudiantAndCours(Etudiant etudiant, Cours cours);

    // Questions sans réponse d'un cours (utile pour l'enseignant)
    List<QuestionPedagogique> findByCoursAndReponseIsNull(Cours cours);

    // Toutes les questions d'un étudiant
    List<QuestionPedagogique> findByEtudiant(Etudiant etudiant);
    
    
    
}