// ════════════════════════════════════════════════════
// TentativeQuizRepository.java  →  repository/
// ════════════════════════════════════════════════════
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
 
    // Toutes les tentatives d'un étudiant pour un quiz
    List<TentativeQuiz> findByEtudiantAndQuizOrderByNumeroTentativeAsc(Etudiant etudiant, Quiz quiz);
 
    // Nombre de tentatives déjà effectuées (SOUMISE uniquement)
    int countByEtudiantAndQuizAndStatut(Etudiant etudiant, Quiz quiz, StatutTentative statut);
 
    // Toutes les tentatives d'un étudiant (pour historique)
    List<TentativeQuiz> findByEtudiantOrderByDateDebutDesc(Etudiant etudiant);
 
    // Les 5 dernières tentatives d'un étudiant
    List<TentativeQuiz> findTop5ByEtudiantOrderByDateDebutDesc(Etudiant etudiant);
 
    // Tentative EN_COURS pour un étudiant et un quiz (éviter doublons)
    Optional<TentativeQuiz> findByEtudiantAndQuizAndStatut(Etudiant etudiant, Quiz quiz, StatutTentative statut);
 
    // Meilleur score d'un étudiant pour un quiz
    @Query("SELECT MAX(t.scoreObtenu) FROM TentativeQuiz t WHERE t.etudiant = :etudiant AND t.quiz = :quiz AND t.statut = 'SOUMISE'")
    Double findMeilleurScoreByEtudiantAndQuiz(@Param("etudiant") Etudiant etudiant, @Param("quiz") Quiz quiz);
 
    // Toutes les tentatives soumises d'un étudiant pour un module (pour calcul moyenne)
    @Query("SELECT t FROM TentativeQuiz t WHERE t.etudiant = :etudiant AND t.quiz.cours.module.id = :moduleId AND t.statut = 'SOUMISE'")
    List<TentativeQuiz> findByEtudiantAndModuleId(@Param("etudiant") Etudiant etudiant, @Param("moduleId") Long moduleId);
    
    
    
}