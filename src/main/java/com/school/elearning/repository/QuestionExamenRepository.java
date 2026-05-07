package com.school.elearning.repository;

import com.school.elearning.model.QuestionExamen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionExamenRepository extends JpaRepository<QuestionExamen, Long> {
	
    List<QuestionExamen> findByExamenId(Long examenId);
    
}