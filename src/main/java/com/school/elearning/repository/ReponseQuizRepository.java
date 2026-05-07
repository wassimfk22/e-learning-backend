package com.school.elearning.repository;

import com.school.elearning.model.QuestionQuiz;
import com.school.elearning.model.ReponseQuiz;
import com.school.elearning.model.TentativeQuiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReponseQuizRepository extends JpaRepository<ReponseQuiz, Long> {

    List<ReponseQuiz> findByTentative(TentativeQuiz tentative);

    Optional<ReponseQuiz> findByTentativeAndQuestion(TentativeQuiz tentative, QuestionQuiz question);

    int countByTentative(TentativeQuiz tentative);

    // Vérifie si une réponse existe déjà pour cette tentative + question
    boolean existsByTentativeAndQuestion(TentativeQuiz tentative, QuestionQuiz question);

    // Somme des points obtenus pour une tentative (utilisé dans la finalisation)
    @Query("SELECT COALESCE(SUM(r.pointsObtenus), 0) FROM ReponseQuiz r WHERE r.tentative = :tentative")
    double sumPointsByTentative(@Param("tentative") TentativeQuiz tentative);
    
    
    
}