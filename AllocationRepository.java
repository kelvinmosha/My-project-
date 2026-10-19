package com.university.hostel.repository;

import com.university.hostel.entity.Allocation;
import com.university.hostel.entity.AllocationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface AllocationRepository extends JpaRepository<Allocation, Long>, JpaSpecificationExecutor<Allocation> {
    List<Allocation> findByStudent_StudentId(Long studentId);
    List<Allocation> findByTerm_TermId(Long termId);
    List<Allocation> findByAllocationStatus(AllocationStatus status);
    List<Allocation> findByBed_Room_RoomId(Long roomId);
    List<Allocation> findByApplication_ApplicationId(Long applicationId);
}
