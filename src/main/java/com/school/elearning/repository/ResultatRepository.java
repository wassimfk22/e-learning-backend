package com.school.elearning.repository;

import com.school.elearning.model.Resultat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResultatRepository extends JpaRepository<Resultat, Long> {
    java.util.List<Resultat> findByEtudiantId(Long etudiantId);
    java.util.Optional<Resultat> findByEtudiantIdAndModuleId(Long etudiantId, Long moduleId);

}
