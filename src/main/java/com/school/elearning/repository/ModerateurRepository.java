package com.school.elearning.repository;

import com.school.elearning.model.Moderateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ModerateurRepository extends JpaRepository<Moderateur, Long> {
	 Optional<Moderateur> findByEmail(String email);
	 boolean existsByEmail(String email);
}

