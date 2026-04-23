package com.school.elearning.repository;

import com.school.elearning.model.Cours;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoursRepository extends JpaRepository<Cours, Long> {
    List<Cours> findByModule(com.school.elearning.model.Module module);
}
