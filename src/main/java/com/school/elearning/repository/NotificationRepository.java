package com.school.elearning.repository;

import com.school.elearning.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
//    java.util.List<Notification> findByDestinataire_IdAndEstLueFalse(Long destinataireId);
//    java.util.List<Notification> findByDestinataire_Id(Long destinataireId);

}
