package com.school.elearning.repository;

import com.school.elearning.model.Etudiant;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EtudiantRepository extends JpaRepository<Etudiant, Long> {
	
	 Optional<Etudiant> findByEmail(String email);
	 boolean existsByEmail(String email);
	 boolean existsBytelephone(String telephone);
	
}
