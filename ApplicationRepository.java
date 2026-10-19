package com.university.hostel.repository;

import com.university.hostel.entity.Application;
import com.university.hostel.entity.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long>, JpaSpecificationExecutor<Application> {
    List<Application> findByStudent_StudentId(Long studentId);
    List<Application> findByTerm_TermId(Long termId);
    List<Application> findByApplicationStatus(ApplicationStatus status);
    List<Application> findByStudent_StudentIdAndTerm_TermId(Long studentId, Long termId);
}
