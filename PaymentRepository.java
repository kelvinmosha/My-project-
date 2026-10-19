package com.university.hostel.repository;

import com.university.hostel.entity.Payment;
import com.university.hostel.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long>, JpaSpecificationExecutor<Payment> {
    List<Payment> findByTransactionReference(String transactionReference);
    List<Payment> findByInvoice_InvoiceId(Long invoiceId);
    List<Payment> findByInvoice_Allocation_Student_StudentId(Long studentId);
    List<Payment> findByPaymentStatus(PaymentStatus paymentStatus);
    boolean existsByTransactionReference(String transactionReference);
}
