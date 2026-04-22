package com.school.elearning.repository;

import com.school.elearning.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    java.util.List<Message> findByCommunauteIdAndEstChatTrue(Long communauteId);

}
