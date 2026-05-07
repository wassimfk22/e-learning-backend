package com.school.elearning.repository;

import com.school.elearning.model.Etudiant;
import com.school.elearning.model.Quiz;
import com.school.elearning.model.TentativeQuiz;
import com.school.elearning.model.enums.StatutTentative;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TentativeQuizRepository extends JpaRepository<TentativeQuiz, Long> {

    // LA tentative unique d'un étudiant pour un quiz (s'il en a une)
    Optional<TentativeQuiz> findByEtudiantAndQuiz(Etudiant etudiant, Quiz quiz);

    // Vérifie si l'étudiant a déjà passé ce quiz
    boolean existsByEtudiantAndQuiz(Etudiant etudiant, Quiz quiz);

    // Toutes les tentatives d'un étudiant (pour l'historique / dashboard)
    List<TentativeQuiz> findByEtudiantOrderByDateDebutDesc(Etudiant etudiant);

    // Les 5 dernières pour le dashboard
    List<TentativeQuiz> findTop5ByEtudiantOrderByDateDebutDesc(Etudiant etudiant);

    // Toutes les tentatives SOUMISES d'un étudiant pour un module (calcul moyenne)
    @Query("SELECT t FROM TentativeQuiz t WHERE t.etudiant = :etudiant AND t.quiz.cours.module.id = :moduleId AND t.statut = 'SOUMISE'")
    List<TentativeQuiz> findByEtudiantAndModuleId(@Param("etudiant") Etudiant etudiant, @Param("moduleId") Long moduleId);

    // Tentative EN_COURS (pour vérifier si l'étudiant a une session active)
    Optional<TentativeQuiz> findByEtudiantAndQuizAndStatut(Etudiant etudiant, Quiz quiz, StatutTentative statut);
    
    
    
}