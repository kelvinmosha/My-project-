package com.university.hostel.service;

import com.university.hostel.dto.RoomTypeDto;
import com.university.hostel.entity.RoomType;
import com.university.hostel.exception.ResourceNotFoundException;
import com.university.hostel.repository.RoomTypeRepository;
import com.university.hostel.util.FilterSpecifications;
import com.university.hostel.util.PatchMapper;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RoomTypeService {
    private final RoomTypeRepository repository;
    private final PatchMapper patchMapper;

    public RoomTypeService(RoomTypeRepository repository, PatchMapper patchMapper) {
        this.repository = repository;
        this.patchMapper = patchMapper;
    }

    public List<RoomType> search(String typeName, Integer minCapacity, Integer maxCapacity, BigDecimal maxFee) {
        List<Specification<RoomType>> filters = new ArrayList<>();
        if (typeName != null && !typeName.isBlank()) {
            filters.add(FilterSpecifications.containsIgnoreCase("typeName", typeName));
        }
        if (minCapacity != null) filters.add(FilterSpecifications.greaterThanOrEqualTo("bedCapacity", minCapacity));
        if (maxCapacity != null) filters.add(FilterSpecifications.lessThanOrEqualTo("bedCapacity", maxCapacity));
        if (maxFee != null) filters.add(FilterSpecifications.lessThanOrEqualTo("baseFeePerTerm", maxFee));
        return repository.findAll(Specification.allOf(filters));
    }

    public RoomType getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room type not found with id: " + id));
    }

    public RoomType create(RoomTypeDto dto) {
        RoomType roomType = RoomType.builder()
                .typeName(dto.getTypeName())
                .bedCapacity(dto.getBedCapacity())
                .baseFeePerTerm(dto.getBaseFeePerTerm())
                .description(dto.getDescription())
                .build();
        return repository.save(roomType);
    }

    public RoomType update(Long id, RoomTypeDto dto) {
        RoomType roomType = getById(id);
        roomType.setTypeName(dto.getTypeName());
        roomType.setBedCapacity(dto.getBedCapacity());
        roomType.setBaseFeePerTerm(dto.getBaseFeePerTerm());
        roomType.setDescription(dto.getDescription());
        return repository.save(roomType);
    }

    /** Partial update: only the fields present in {@code updates} are changed. */
    public RoomType patch(Long id, Map<String, Object> updates) {
        RoomType existing = getById(id);
        RoomTypeDto snapshot = RoomTypeDto.builder()
                .roomTypeId(existing.getRoomTypeId())
                .typeName(existing.getTypeName())
                .bedCapacity(existing.getBedCapacity())
                .baseFeePerTerm(existing.getBaseFeePerTerm())
                .description(existing.getDescription())
                .build();
        RoomTypeDto merged = patchMapper.merge(snapshot, updates);
        return update(id, merged);
    }

    public void delete(Long id) {
        repository.delete(getById(id));
    }
}
