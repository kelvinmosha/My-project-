package com.university.hostel.repository;

import com.university.hostel.entity.MaintenanceRequest;
import com.university.hostel.entity.MaintenanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface MaintenanceRequestRepository extends JpaRepository<MaintenanceRequest, Long>, JpaSpecificationExecutor<MaintenanceRequest> {
    List<MaintenanceRequest> findByReportedByStudent_StudentId(Long studentId);
    List<MaintenanceRequest> findByRoom_RoomId(Long roomId);
    List<MaintenanceRequest> findByStatus(MaintenanceStatus status);
    List<MaintenanceRequest> findByAssignedUser_UserId(Long userId);
}
