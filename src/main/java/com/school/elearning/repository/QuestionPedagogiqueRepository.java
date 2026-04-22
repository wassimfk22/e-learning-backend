package com.school.elearning.repository;

import com.school.elearning.model.QuestionPedagogique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionPedagogiqueRepository extends JpaRepository<QuestionPedagogique, Long> {
    java.util.List<QuestionPedagogique> findByCoursId(Long coursId);

}
