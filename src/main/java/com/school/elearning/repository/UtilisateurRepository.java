package com.school.elearning.repository;

import com.school.elearning.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    java.util.Optional<Utilisateur> findByEmail(String email);
    boolean existsByEmail(String email);

}
