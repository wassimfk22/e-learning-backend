package com.school.elearning.repository;

import com.school.elearning.model.BoiteReception;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoiteReceptionRepository extends JpaRepository<BoiteReception, Long> {
    java.util.Optional<BoiteReception> findByUtilisateurId(Long utilisateurId);

}
