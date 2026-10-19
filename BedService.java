package com.university.hostel.service;

import com.university.hostel.dto.BedDto;
import com.university.hostel.entity.Bed;
import com.university.hostel.entity.BedStatus;
import com.university.hostel.entity.Room;
import com.university.hostel.exception.ResourceNotFoundException;
import com.university.hostel.repository.BedRepository;
import com.university.hostel.repository.RoomRepository;
import com.university.hostel.util.PatchMapper;
import com.university.hostel.util.FilterSpecifications;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class BedService {
    private final BedRepository bedRepository;
    private final RoomRepository roomRepository;
    private final PatchMapper patchMapper;

    public BedService(BedRepository bedRepository, RoomRepository roomRepository, PatchMapper patchMapper) {
        this.bedRepository = bedRepository;
        this.roomRepository = roomRepository;
        this.patchMapper = patchMapper;
    }

    public List<Bed> getAll() {
        return bedRepository.findAll();
    }

    /** Filtered listing. Any combination of params may be omitted (null). */
    public List<Bed> search(Long roomId, BedStatus bedStatus) {
        List<Specification<Bed>> filters = new ArrayList<>();
        if (roomId != null) filters.add((root, query, cb) -> cb.equal(root.get("room").get("roomId"), roomId));
        if (bedStatus != null) filters.add(FilterSpecifications.equal("bedStatus", bedStatus));
        return bedRepository.findAll(Specification.allOf(filters));
    }

    public Bed getById(Long id) {
        return bedRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bed not found with id: " + id));
    }

    public Bed create(BedDto dto) {
        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + dto.getRoomId()));
        Bed bed = Bed.builder()
                .room(room)
                .bedLabel(dto.getBedLabel())
                .bedStatus(dto.getBedStatus())
                .build();
        return bedRepository.save(bed);
    }

    public Bed update(Long id, BedDto dto) {
        Bed bed = getById(id);
        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + dto.getRoomId()));
        bed.setRoom(room);
        bed.setBedLabel(dto.getBedLabel());
        bed.setBedStatus(dto.getBedStatus());
        return bedRepository.save(bed);
    }

    /** Partial update: only the fields present in {@code updates} are changed. */
    public Bed patch(Long id, Map<String, Object> updates) {
        Bed existing = getById(id);
        BedDto snapshot = BedDto.builder()
                .bedId(existing.getBedId())
                .roomId(existing.getRoom().getRoomId())
                .bedLabel(existing.getBedLabel())
                .bedStatus(existing.getBedStatus())
                .build();
        BedDto merged = patchMapper.merge(snapshot, updates);
        return update(id, merged);
    }

    public void delete(Long id) {
        bedRepository.delete(getById(id));
    }
}
