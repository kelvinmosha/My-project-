package com.university.hostel.repository;

import com.university.hostel.entity.Invoice;
import com.university.hostel.entity.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long>, JpaSpecificationExecutor<Invoice> {
    List<Invoice> findByAllocation_AllocationId(Long allocationId);
    List<Invoice> findByInvoiceStatus(InvoiceStatus status);
    List<Invoice> findByAllocation_Student_StudentId(Long studentId);
}
