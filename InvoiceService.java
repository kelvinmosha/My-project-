package com.university.hostel.service;

import com.university.hostel.dto.InvoiceDto;
import com.university.hostel.entity.Allocation;
import com.university.hostel.entity.Invoice;
import com.university.hostel.entity.InvoiceStatus;
import com.university.hostel.exception.InvalidStateException;
import com.university.hostel.exception.ResourceNotFoundException;
import com.university.hostel.repository.AllocationRepository;
import com.university.hostel.repository.InvoiceRepository;
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
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final AllocationRepository allocationRepository;
    private final PatchMapper patchMapper;

    public InvoiceService(InvoiceRepository invoiceRepository, AllocationRepository allocationRepository, PatchMapper patchMapper) {
        this.invoiceRepository = invoiceRepository;
        this.allocationRepository = allocationRepository;
        this.patchMapper = patchMapper;
    }

    public List<Invoice> getAll() {
        return invoiceRepository.findAll();
    }

    /** Filtered listing. Any combination of params may be omitted (null). */
    public List<Invoice> search(Long allocationId, Long studentId, InvoiceStatus status) {
        List<Specification<Invoice>> filters = new ArrayList<>();
        if (allocationId != null) filters.add((root, query, cb) -> cb.equal(root.get("allocation").get("allocationId"), allocationId));
        if (studentId != null) filters.add((root, query, cb) -> cb.equal(root.get("allocation").get("student").get("studentId"), studentId));
        if (status != null) filters.add(FilterSpecifications.equal("invoiceStatus", status));
        return invoiceRepository.findAll(Specification.allOf(filters));
    }

    public Invoice getById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));
    }

    public Invoice create(InvoiceDto dto) {
        Allocation allocation = allocationRepository.findById(dto.getAllocationId())
                .orElseThrow(() -> new ResourceNotFoundException("Allocation not found with id: " + dto.getAllocationId()));

        Invoice invoice = Invoice.builder()
                .allocation(allocation)
                .issueDate(dto.getIssueDate())
                .dueDate(dto.getDueDate())
                .amountDue(dto.getAmountDue())
                .amountPaid(dto.getAmountPaid() != null ? dto.getAmountPaid() : BigDecimal.ZERO)
                .balance(dto.getBalance() != null ? dto.getBalance() : dto.getAmountDue().subtract(dto.getAmountPaid() != null ? dto.getAmountPaid() : BigDecimal.ZERO))
                .invoiceStatus(dto.getInvoiceStatus() != null ? dto.getInvoiceStatus() : InvoiceStatus.OPEN)
                .build();

        return invoiceRepository.save(invoice);
    }

    /** Full replace of the editable invoice fields (dates, amounts, status). */
    public Invoice update(Long id, InvoiceDto dto) {
        Invoice invoice = getById(id);
        if (dto.getAllocationId() != null && !dto.getAllocationId().equals(invoice.getAllocation().getAllocationId())) {
            Allocation allocation = allocationRepository.findById(dto.getAllocationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Allocation not found with id: " + dto.getAllocationId()));
            invoice.setAllocation(allocation);
        }
        invoice.setIssueDate(dto.getIssueDate());
        invoice.setDueDate(dto.getDueDate());
        invoice.setAmountDue(dto.getAmountDue());
        BigDecimal amountPaid = dto.getAmountPaid() != null ? dto.getAmountPaid() : invoice.getAmountPaid();
        invoice.setAmountPaid(amountPaid);
        invoice.setBalance(dto.getBalance() != null ? dto.getBalance() : dto.getAmountDue().subtract(amountPaid));
        if (dto.getInvoiceStatus() != null) {
            invoice.setInvoiceStatus(dto.getInvoiceStatus());
        }
        return invoiceRepository.save(invoice);
    }

    /** Partial update: only the fields present in {@code updates} are changed. */
    public Invoice patch(Long id, Map<String, Object> updates) {
        Invoice existing = getById(id);
        InvoiceDto snapshot = InvoiceDto.builder()
                .invoiceId(existing.getInvoiceId())
                .allocationId(existing.getAllocation().getAllocationId())
                .issueDate(existing.getIssueDate())
                .dueDate(existing.getDueDate())
                .amountDue(existing.getAmountDue())
                .amountPaid(existing.getAmountPaid())
                .balance(existing.getBalance())
                .invoiceStatus(existing.getInvoiceStatus())
                .build();
        InvoiceDto merged = patchMapper.merge(snapshot, updates);
        return update(id, merged);
    }

    @Transactional
    public Invoice updateInvoiceTotals(Long invoiceId, BigDecimal paymentAmount) {
        Invoice invoice = getById(invoiceId);
        if (paymentAmount == null || paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidStateException("Payment amount must be positive");
        }

        BigDecimal newAmountPaid = invoice.getAmountPaid().add(paymentAmount);
        BigDecimal newBalance = invoice.getAmountDue().subtract(newAmountPaid);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidStateException("Payment exceeds invoice amount due");
        }

        invoice.setAmountPaid(newAmountPaid);
        invoice.setBalance(newBalance);
        if (newBalance.compareTo(BigDecimal.ZERO) == 0) {
            invoice.setInvoiceStatus(InvoiceStatus.PAID);
        } else if (newAmountPaid.compareTo(BigDecimal.ZERO) > 0) {
            invoice.setInvoiceStatus(InvoiceStatus.PARTIAL);
        } else {
            invoice.setInvoiceStatus(InvoiceStatus.OPEN);
        }
        return invoiceRepository.save(invoice);
    }

    public void delete(Long id) {
        invoiceRepository.delete(getById(id));
    }
}
