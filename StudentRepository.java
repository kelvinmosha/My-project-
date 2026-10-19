package com.university.hostel.repository;

import com.university.hostel.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long>, JpaSpecificationExecutor<Student> {
    Optional<Student> findByEmailAddress(String emailAddress);
    Optional<Student> findByStudentRegNumber(String studentRegNumber);
    boolean existsByEmailAddress(String emailAddress);
    boolean existsByStudentRegNumber(String studentRegNumber);
    List<Student> findByIsActive(boolean isActive);
    List<Student> findByProgramOfStudy(String programOfStudy);
    List<Student> findByFullNameContainingIgnoreCase(String fullName);
}
