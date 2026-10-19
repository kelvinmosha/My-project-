package com.university.hostel.controller;

import com.university.hostel.dto.RoomDto;
import com.university.hostel.entity.Room;
import com.university.hostel.entity.RoomStatus;
import com.university.hostel.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/staff/rooms")
public class RoomController {
    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','HOSTEL_STAFF')")
    @GetMapping("")
    public ResponseEntity<List<Room>> getAll(
            @RequestParam(required = false) RoomStatus roomStatus,
            @RequestParam(required = false) Long roomTypeId,
            @RequestParam(required = false) String buildingBlock,
            @RequestParam(required = false) Boolean isActive) {
        return ResponseEntity.ok(roomService.search(roomStatus, roomTypeId, buildingBlock, isActive));
    }

    @PreAuthorize("hasAnyRole('ADMIN','HOSTEL_STAFF')")
    @GetMapping("/{id}")
    public ResponseEntity<Room> getById(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getById(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN','HOSTEL_STAFF')")
    @PostMapping("")
    public ResponseEntity<Room> create(@Valid @RequestBody RoomDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.create(dto));
    }

    @PreAuthorize("hasAnyRole('ADMIN','HOSTEL_STAFF')")
    @PutMapping("/{id}")
    public ResponseEntity<Room> update(@PathVariable Long id, @Valid @RequestBody RoomDto dto) {
        return ResponseEntity.ok(roomService.update(id, dto));
    }

    @PreAuthorize("hasAnyRole('ADMIN','HOSTEL_STAFF')")
    @PatchMapping("/{id}")
    public ResponseEntity<Room> patch(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        return ResponseEntity.ok(roomService.patch(id, updates));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        roomService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
