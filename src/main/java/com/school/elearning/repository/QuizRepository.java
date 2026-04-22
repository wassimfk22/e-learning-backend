package com.school.elearning.repository;

import com.school.elearning.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    java.util.List<Quiz> findByCoursId(Long coursId);

}
