package com.university.hostel.repository;

import com.university.hostel.entity.AcademicTerm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface AcademicTermRepository extends JpaRepository<AcademicTerm, Long>, JpaSpecificationExecutor<AcademicTerm> {
    List<AcademicTerm> findByIsCurrent(boolean isCurrent);
    List<AcademicTerm> findByAcademicYear(String academicYear);
}
