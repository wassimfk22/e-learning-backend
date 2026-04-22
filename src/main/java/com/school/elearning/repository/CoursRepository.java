package com.school.elearning.repository;

import com.school.elearning.model.Cours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoursRepository extends JpaRepository<Cours, Long> {
    java.util.List<Cours> findByModuleId(Long moduleId);

}
