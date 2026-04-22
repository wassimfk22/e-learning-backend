package com.school.elearning.repository;

import com.school.elearning.model.Communaute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommunauteRepository extends JpaRepository<Communaute, Long> {
    java.util.Optional<Communaute> findByNiveauId(Long niveauId);

}
