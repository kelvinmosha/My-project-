package com.university.hostel.service;

import com.university.hostel.dto.AcademicTermDto;
import com.university.hostel.entity.AcademicTerm;
import com.university.hostel.exception.ResourceNotFoundException;
import com.university.hostel.repository.AcademicTermRepository;
import com.university.hostel.util.FilterSpecifications;
import com.university.hostel.util.PatchMapper;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AcademicTermService {
    private final AcademicTermRepository repository;
    private final PatchMapper patchMapper;

    public AcademicTermService(AcademicTermRepository repository, PatchMapper patchMapper) {
        this.repository = repository;
        this.patchMapper = patchMapper;
    }

    public List<AcademicTerm> getAllTerms() {
        return repository.findAll();
    }

    /** Filtered listing. Any combination of params may be omitted (null). */
    public List<AcademicTerm> search(Boolean isCurrent, String academicYear) {
        List<Specification<AcademicTerm>> filters = new ArrayList<>();
        if (isCurrent != null) filters.add(FilterSpecifications.equal("isCurrent", isCurrent));
        if (academicYear != null && !academicYear.isBlank()) {
            filters.add(FilterSpecifications.exactIgnoreCase("academicYear", academicYear));
        }
        return repository.findAll(Specification.allOf(filters));
    }

    public AcademicTerm getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Academic term not found with id: " + id));
    }

    public AcademicTerm create(AcademicTermDto dto) {
        AcademicTerm term = AcademicTerm.builder()
                .termName(dto.getTermName())
                .academicYear(dto.getAcademicYear())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .isCurrent(dto.isCurrent())
                .build();
        return repository.save(term);
    }

    public AcademicTerm update(Long id, AcademicTermDto dto) {
        AcademicTerm term = getById(id);
        term.setTermName(dto.getTermName());
        term.setAcademicYear(dto.getAcademicYear());
        term.setStartDate(dto.getStartDate());
        term.setEndDate(dto.getEndDate());
        term.setCurrent(dto.isCurrent());
        return repository.save(term);
    }

    /** Partial update: only the fields present in {@code updates} are changed. */
    public AcademicTerm patch(Long id, Map<String, Object> updates) {
        AcademicTerm existing = getById(id);
        AcademicTermDto snapshot = AcademicTermDto.builder()
                .termId(existing.getTermId())
                .termName(existing.getTermName())
                .academicYear(existing.getAcademicYear())
                .startDate(existing.getStartDate())
                .endDate(existing.getEndDate())
                .isCurrent(existing.isCurrent())
                .build();
        AcademicTermDto merged = patchMapper.merge(snapshot, updates);
        return update(id, merged);
    }

    public void delete(Long id) {
        AcademicTerm term = getById(id);
        repository.delete(term);
    }
}
