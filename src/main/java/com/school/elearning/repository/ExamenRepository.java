package com.school.elearning.repository;

import com.school.elearning.model.Examen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamenRepository extends JpaRepository<Examen, Long> {
    java.util.Optional<Examen> findByModuleId(Long moduleId);

}
