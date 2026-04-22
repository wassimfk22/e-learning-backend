package com.school.elearning.repository;

import com.school.elearning.model.Enregistrement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnregistrementRepository extends JpaRepository<Enregistrement, Long> {
    java.util.List<Enregistrement> findByEtudiantId(Long etudiantId);

}
