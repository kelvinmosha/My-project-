package com.university.hostel.repository;

import com.university.hostel.entity.Notification;
import com.university.hostel.entity.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long>, JpaSpecificationExecutor<Notification> {
    List<Notification> findByRecipientStudent_StudentId(Long studentId);
    List<Notification> findByRecipientUser_UserId(Long userId);
    List<Notification> findByIsRead(boolean isRead);
    List<Notification> findByNotificationType(NotificationType notificationType);
}
