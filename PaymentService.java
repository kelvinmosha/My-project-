package com.university.hostel.service;

import com.university.hostel.dto.PaymentDto;
import com.university.hostel.entity.*;
import com.university.hostel.exception.InvalidStateException;
import com.university.hostel.exception.ResourceNotFoundException;
import com.university.hostel.repository.InvoiceRepository;
import com.university.hostel.repository.PaymentRepository;
import com.university.hostel.repository.SystemUserRepository;
import com.university.hostel.util.PatchMapper;
import com.university.hostel.util.FilterSpecifications;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final SystemUserRepository systemUserRepository;
    private final InvoiceService invoiceService;
    private final PatchMapper patchMapper;

    public PaymentService(PaymentRepository paymentRepository,
                         InvoiceRepository invoiceRepository,
                         SystemUserRepository systemUserRepository,
                         InvoiceService invoiceService,
                         PatchMapper patchMapper) {
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
        this.systemUserRepository = systemUserRepository;
        this.invoiceService = invoiceService;
        this.patchMapper = patchMapper;
    }

    public List<Payment> getAll() {
        return paymentRepository.findAll();
    }

    /** Filtered listing. Any combination of params may be omitted (null). */
    public List<Payment> search(Long invoiceId, Long studentId, PaymentStatus status, String transactionReference) {
        List<Specification<Payment>> filters = new ArrayList<>();
        if (invoiceId != null) filters.add((root, query, cb) -> cb.equal(root.get("invoice").get("invoiceId"), invoiceId));
        if (studentId != null) filters.add((root, query, cb) -> cb.equal(
                root.get("invoice").get("allocation").get("student").get("studentId"), studentId));
        if (status != null) filters.add(FilterSpecifications.equal("paymentStatus", status));
        if (transactionReference != null && !transactionReference.isBlank()) {
            filters.add(FilterSpecifications.exactIgnoreCase("transactionReference", transactionReference));
        }
        return paymentRepository.findAll(Specification.allOf(filters));
    }

    public Payment getById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
    }

    @Transactional
    public Payment create(PaymentDto dto) {
        Invoice invoice = invoiceRepository.findById(dto.getInvoiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + dto.getInvoiceId()));

        if (paymentRepository.existsByTransactionReference(dto.getTransactionReference())) {
            throw new InvalidStateException("Transaction reference already exists");
        }

        SystemUser recordedBy = systemUserRepository.findById(dto.getRecordedByUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + dto.getRecordedByUserId()));

        BigDecimal paymentAmount = dto.getAmountPaid();
        BigDecimal newAmountPaid = invoice.getAmountPaid().add(paymentAmount);
        if (newAmountPaid.compareTo(invoice.getAmountDue()) > 0) {
            throw new InvalidStateException("Payment exceeds amount due");
        }

        Payment payment = Payment.builder()
                .invoice(invoice)
                .amountPaid(paymentAmount)
                .paymentMethod(dto.getPaymentMethod())
                .transactionReference(dto.getTransactionReference())
                .paymentDate(dto.getPaymentDate())
                .recordedByUser(recordedBy)
                .paymentStatus(dto.getPaymentStatus() != null ? dto.getPaymentStatus() : PaymentStatus.SUCCESSFUL)
                .build();

        Payment saved = paymentRepository.save(payment);
        invoiceService.updateInvoiceTotals(invoice.getInvoiceId(), paymentAmount);
        return saved;
    }

    /**
     * Edits a payment's correctable fields (method, transaction reference, date,
     * status). Does not re-run the invoice-total recalculation that create()
     * performs; use this for corrections (e.g. voiding a payment record), not
     * for changing the paid amount after the fact.
     */
    public Payment update(Long id, PaymentDto dto) {
        Payment payment = getById(id);
        if (dto.getTransactionReference() != null
                && !dto.getTransactionReference().equals(payment.getTransactionReference())
                && paymentRepository.existsByTransactionReference(dto.getTransactionReference())) {
            throw new InvalidStateException("Transaction reference already exists");
        }
        payment.setPaymentMethod(dto.getPaymentMethod());
        payment.setTransactionReference(dto.getTransactionReference());
        payment.setPaymentDate(dto.getPaymentDate());
        if (dto.getPaymentStatus() != null) {
            payment.setPaymentStatus(dto.getPaymentStatus());
        }
        return paymentRepository.save(payment);
    }

    /** Partial update: only the fields present in {@code updates} are changed. */
    public Payment patch(Long id, Map<String, Object> updates) {
        Payment existing = getById(id);
        PaymentDto snapshot = PaymentDto.builder()
                .paymentId(existing.getPaymentId())
                .invoiceId(existing.getInvoice().getInvoiceId())
                .amountPaid(existing.getAmountPaid())
                .paymentMethod(existing.getPaymentMethod())
                .transactionReference(existing.getTransactionReference())
                .paymentDate(existing.getPaymentDate())
                .recordedByUserId(existing.getRecordedByUser().getUserId())
                .paymentStatus(existing.getPaymentStatus())
                .build();
        PaymentDto merged = patchMapper.merge(snapshot, updates);
        return update(id, merged);
    }

    public void delete(Long id) {
        paymentRepository.delete(getById(id));
    }
}
