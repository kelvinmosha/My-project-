package com.university.hostel.service;

import com.university.hostel.dto.NotificationDto;
import com.university.hostel.entity.Notification;
import com.university.hostel.entity.NotificationType;
import com.university.hostel.entity.Student;
import com.university.hostel.entity.SystemUser;
import com.university.hostel.exception.ResourceNotFoundException;
import com.university.hostel.repository.NotificationRepository;
import com.university.hostel.repository.StudentRepository;
import com.university.hostel.repository.SystemUserRepository;
import com.university.hostel.util.PatchMapper;
import com.university.hostel.util.FilterSpecifications;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final StudentRepository studentRepository;
    private final SystemUserRepository systemUserRepository;
    private final PatchMapper patchMapper;

    public NotificationService(NotificationRepository notificationRepository,
                              StudentRepository studentRepository,
                              SystemUserRepository systemUserRepository,
                              PatchMapper patchMapper) {
        this.notificationRepository = notificationRepository;
        this.studentRepository = studentRepository;
        this.systemUserRepository = systemUserRepository;
        this.patchMapper = patchMapper;
    }

    public List<Notification> getAll() {
        return notificationRepository.findAll();
    }

    /** Filtered listing. Any combination of params may be omitted (null). */
    public List<Notification> search(Long recipientStudentId, Long recipientUserId, Boolean isRead, NotificationType type) {
        List<Specification<Notification>> filters = new ArrayList<>();
        if (recipientStudentId != null) filters.add((root, query, cb) -> cb.equal(root.get("recipientStudent").get("studentId"), recipientStudentId));
        if (recipientUserId != null) filters.add((root, query, cb) -> cb.equal(root.get("recipientUser").get("userId"), recipientUserId));
        if (isRead != null) filters.add(FilterSpecifications.equal("isRead", isRead));
        if (type != null) filters.add(FilterSpecifications.equal("notificationType", type));
        return notificationRepository.findAll(Specification.allOf(filters));
    }

    public Notification getById(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
    }

    public Notification create(NotificationDto dto) {
        Student student = null;
        SystemUser user = null;
        if (dto.getRecipientStudentId() != null) {
            student = studentRepository.findById(dto.getRecipientStudentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + dto.getRecipientStudentId()));
        }
        if (dto.getRecipientUserId() != null) {
            user = systemUserRepository.findById(dto.getRecipientUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + dto.getRecipientUserId()));
        }

        Notification notification = Notification.builder()
                .recipientStudent(student)
                .recipientUser(user)
                .title(dto.getTitle())
                .message(dto.getMessage())
                .notificationType(dto.getNotificationType() != null ? dto.getNotificationType() : NotificationType.GENERAL)
                .isRead(false)
                .createdDate(LocalDateTime.now())
                .build();
        return notificationRepository.save(notification);
    }

    public Notification update(Long id, NotificationDto dto) {
        Notification notification = getById(id);
        if (dto.getRecipientStudentId() != null) {
            notification.setRecipientStudent(studentRepository.findById(dto.getRecipientStudentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + dto.getRecipientStudentId())));
        } else {
            notification.setRecipientStudent(null);
        }
        if (dto.getRecipientUserId() != null) {
            notification.setRecipientUser(systemUserRepository.findById(dto.getRecipientUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + dto.getRecipientUserId())));
        } else {
            notification.setRecipientUser(null);
        }
        notification.setTitle(dto.getTitle());
        notification.setMessage(dto.getMessage());
        if (dto.getNotificationType() != null) {
            notification.setNotificationType(dto.getNotificationType());
        }
        notification.setRead(dto.isRead());
        return notificationRepository.save(notification);
    }

    /** Partial update: only the fields present in {@code updates} are changed. */
    public Notification patch(Long id, Map<String, Object> updates) {
        Notification existing = getById(id);
        NotificationDto snapshot = NotificationDto.builder()
                .notificationId(existing.getNotificationId())
                .recipientStudentId(existing.getRecipientStudent() != null ? existing.getRecipientStudent().getStudentId() : null)
                .recipientUserId(existing.getRecipientUser() != null ? existing.getRecipientUser().getUserId() : null)
                .title(existing.getTitle())
                .message(existing.getMessage())
                .notificationType(existing.getNotificationType())
                .isRead(existing.isRead())
                .createdDate(existing.getCreatedDate())
                .build();
        NotificationDto merged = patchMapper.merge(snapshot, updates);
        return update(id, merged);
    }

    public Notification markAsRead(Long id) {
        Notification notification = getById(id);
        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    public void delete(Long id) {
        notificationRepository.delete(getById(id));
    }
}
