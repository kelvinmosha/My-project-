package com.university.hostel.service;

import com.university.hostel.dto.RoomDto;
import com.university.hostel.entity.Room;
import com.university.hostel.entity.RoomStatus;
import com.university.hostel.entity.RoomType;
import com.university.hostel.exception.ResourceNotFoundException;
import com.university.hostel.repository.RoomRepository;
import com.university.hostel.repository.RoomTypeRepository;
import com.university.hostel.util.PatchMapper;
import com.university.hostel.util.FilterSpecifications;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RoomService {
    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final PatchMapper patchMapper;

    public RoomService(RoomRepository roomRepository, RoomTypeRepository roomTypeRepository, PatchMapper patchMapper) {
        this.roomRepository = roomRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.patchMapper = patchMapper;
    }

    public List<Room> getAll() {
        return roomRepository.findAll();
    }

    /** Filtered listing. Any combination of params may be omitted (null). */
    public List<Room> search(RoomStatus roomStatus, Long roomTypeId, String buildingBlock, Boolean isActive) {
        List<Specification<Room>> filters = new ArrayList<>();
        if (roomStatus != null) filters.add(FilterSpecifications.equal("roomStatus", roomStatus));
        if (roomTypeId != null) filters.add((root, query, cb) -> cb.equal(root.get("roomType").get("roomTypeId"), roomTypeId));
        if (buildingBlock != null && !buildingBlock.isBlank()) {
            filters.add(FilterSpecifications.exactIgnoreCase("buildingBlock", buildingBlock));
        }
        if (isActive != null) filters.add(FilterSpecifications.equal("isActive", isActive));
        return roomRepository.findAll(Specification.allOf(filters));
    }

    public Room getById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));
    }

    public Room create(RoomDto dto) {
        RoomType roomType = roomTypeRepository.findById(dto.getRoomTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Room type not found with id: " + dto.getRoomTypeId()));

        Room room = Room.builder()
                .roomNumber(dto.getRoomNumber())
                .buildingBlock(dto.getBuildingBlock())
                .floorNumber(dto.getFloorNumber())
                .roomType(roomType)
                .roomStatus(dto.getRoomStatus() != null ? dto.getRoomStatus() : RoomStatus.AVAILABLE)
                .isActive(dto.isActive())
                .build();
        return roomRepository.save(room);
    }

    public Room update(Long id, RoomDto dto) {
        Room room = getById(id);
        RoomType roomType = roomTypeRepository.findById(dto.getRoomTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Room type not found with id: " + dto.getRoomTypeId()));
        room.setRoomNumber(dto.getRoomNumber());
        room.setBuildingBlock(dto.getBuildingBlock());
        room.setFloorNumber(dto.getFloorNumber());
        room.setRoomType(roomType);
        room.setRoomStatus(dto.getRoomStatus());
        room.setActive(dto.isActive());
        return roomRepository.save(room);
    }

    /** Partial update: only the fields present in {@code updates} are changed. */
    public Room patch(Long id, Map<String, Object> updates) {
        Room existing = getById(id);
        RoomDto snapshot = RoomDto.builder()
                .roomId(existing.getRoomId())
                .roomNumber(existing.getRoomNumber())
                .buildingBlock(existing.getBuildingBlock())
                .floorNumber(existing.getFloorNumber())
                .roomTypeId(existing.getRoomType().getRoomTypeId())
                .roomStatus(existing.getRoomStatus())
                .isActive(existing.isActive())
                .build();
        RoomDto merged = patchMapper.merge(snapshot, updates);
        return update(id, merged);
    }

    public void delete(Long id) {
        roomRepository.delete(getById(id));
    }
}
