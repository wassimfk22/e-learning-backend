package com.school.elearning.repository;

import com.school.elearning.model.ReponsePedagogique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReponsePedagogiqueRepository extends JpaRepository<ReponsePedagogique, Long> {
}
