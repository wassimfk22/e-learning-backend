package com.school.elearning.repository;

import com.school.elearning.model.QuestionQuiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionQuizRepository extends JpaRepository<QuestionQuiz, Long> {
	
    List<QuestionQuiz> findByQuizId(Long quizId);
    

    
}