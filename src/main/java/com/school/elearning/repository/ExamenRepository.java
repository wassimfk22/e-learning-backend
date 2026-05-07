package com.school.elearning.repository;

import com.school.elearning.model.ExamenModule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamenRepository extends JpaRepository<ExamenModule, Long> {
	
    java.util.Optional<ExamenModule> findByModuleId(Long moduleId);

}
