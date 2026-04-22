package com.school.elearning.repository;

import com.school.elearning.model.Stream;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StreamRepository extends JpaRepository<Stream, Long> {
    java.util.List<Stream> findByEnseignantId(Long enseignantId);

}
