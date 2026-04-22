package com.school.elearning.repository;

import com.school.elearning.model.Evenement;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EvenementRepository extends JpaRepository<Evenement, Long> {
//    List<Evenement> findByCalendrierId(Long calendrierId);

}
