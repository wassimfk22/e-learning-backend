package com.school.elearning.repository;

import com.school.elearning.model.Enseignant;
import com.school.elearning.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

    // Tous les quiz d'un cours
    List<Quiz> findByCoursId(Long coursId);

    boolean existsByTitre(String titre);
    
    // Tous les quiz d'un enseignant (via cours → module → enseignant)
    @Query("SELECT q FROM Quiz q WHERE q.cours.module.enseignant = :enseignant")
    List<Quiz> findByEnseignant(@Param("enseignant") Enseignant enseignant);

    // Vérifier qu'un quiz appartient bien à un enseignant donné
    @Query("SELECT COUNT(q) > 0 FROM Quiz q WHERE q.id = :quizId AND q.cours.module.enseignant = :enseignant")
    boolean appartientAEnseignant(@Param("quizId") Long quizId, @Param("enseignant") Enseignant enseignant);
}