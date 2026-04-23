package com.school.elearning.repository;

import com.school.elearning.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    java.util.List<Question> findByExamenId(Long examenId);

}
