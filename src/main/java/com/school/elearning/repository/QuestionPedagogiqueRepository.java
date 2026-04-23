package com.school.elearning.repository;

import com.school.elearning.model.Cours;
import com.school.elearning.model.QuestionPedagogique;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionPedagogiqueRepository extends JpaRepository<QuestionPedagogique, Long> {
    List<QuestionPedagogique> findByCours(Cours cours);
}
