package com.school.elearning.repository;

import com.school.elearning.model.Moderateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModerateurRepository extends JpaRepository<Moderateur, Long> {
}
