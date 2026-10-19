package com.university.hostel.controller;

import com.university.hostel.dto.RoomTypeDto;
import com.university.hostel.entity.RoomType;
import com.university.hostel.service.RoomTypeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/staff/room-types")
public class RoomTypeController {
    private final RoomTypeService roomTypeService;

    public RoomTypeController(RoomTypeService roomTypeService) {
        this.roomTypeService = roomTypeService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','HOSTEL_STAFF')")
    @GetMapping("")
    public ResponseEntity<List<RoomType>> getAll(
            @RequestParam(required = false) String typeName,
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(required = false) Integer maxCapacity,
            @RequestParam(required = false) BigDecimal maxFee) {
        return ResponseEntity.ok(roomTypeService.search(typeName, minCapacity, maxCapacity, maxFee));
    }

    @PreAuthorize("hasAnyRole('ADMIN','HOSTEL_STAFF')")
    @GetMapping("/{id}")
    public ResponseEntity<RoomType> getById(@PathVariable Long id) {
        return ResponseEntity.ok(roomTypeService.getById(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN','HOSTEL_STAFF')")
    @PostMapping("")
    public ResponseEntity<RoomType> create(@Valid @RequestBody RoomTypeDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomTypeService.create(dto));
    }

    @PreAuthorize("hasAnyRole('ADMIN','HOSTEL_STAFF')")
    @PutMapping("/{id}")
    public ResponseEntity<RoomType> update(@PathVariable Long id, @Valid @RequestBody RoomTypeDto dto) {
        return ResponseEntity.ok(roomTypeService.update(id, dto));
    }

    @PreAuthorize("hasAnyRole('ADMIN','HOSTEL_STAFF')")
    @PatchMapping("/{id}")
    public ResponseEntity<RoomType> patch(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        return ResponseEntity.ok(roomTypeService.patch(id, updates));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        roomTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
