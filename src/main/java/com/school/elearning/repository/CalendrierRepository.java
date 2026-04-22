package com.school.elearning.repository;

import com.school.elearning.model.Calendrier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CalendrierRepository extends JpaRepository<Calendrier, Long> {
}
