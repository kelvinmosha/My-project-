package com.university.hostel.service;

import com.university.hostel.dto.MaintenanceRequestDto;
import com.university.hostel.entity.*;
import com.university.hostel.exception.ResourceNotFoundException;
import com.university.hostel.repository.MaintenanceRequestRepository;
import com.university.hostel.repository.RoomRepository;
import com.university.hostel.repository.StudentRepository;
import com.university.hostel.repository.SystemUserRepository;
import com.university.hostel.util.PatchMapper;
import com.university.hostel.util.FilterSpecifications;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Service
public class MaintenanceRequestService {
    private final MaintenanceRequestRepository maintenanceRequestRepository;
    private final RoomRepository roomRepository;
    private final StudentRepository studentRepository;
    private final SystemUserRepository systemUserRepository;
    private final PatchMapper patchMapper;

    public MaintenanceRequestService(MaintenanceRequestRepository maintenanceRequestRepository,
                                     RoomRepository roomRepository,
                                     StudentRepository studentRepository,
                                     SystemUserRepository systemUserRepository,
                                     PatchMapper patchMapper) {
        this.maintenanceRequestRepository = maintenanceRequestRepository;
        this.roomRepository = roomRepository;
        this.studentRepository = studentRepository;
        this.systemUserRepository = systemUserRepository;
        this.patchMapper = patchMapper;
    }

    public List<MaintenanceRequest> getAll() {
        return maintenanceRequestRepository.findAll();
    }

    /** Filtered listing. Any combination of params may be omitted (null). */
    public List<MaintenanceRequest> search(Long studentId, Long roomId, MaintenanceStatus status, Long assignedUserId) {
        List<Specification<MaintenanceRequest>> filters = new ArrayList<>();
        if (studentId != null) filters.add((root, query, cb) -> cb.equal(root.get("reportedByStudent").get("studentId"), studentId));
        if (roomId != null) filters.add((root, query, cb) -> cb.equal(root.get("room").get("roomId"), roomId));
        if (status != null) filters.add(FilterSpecifications.equal("status", status));
        if (assignedUserId != null) filters.add((root, query, cb) -> cb.equal(root.get("assignedUser").get("userId"), assignedUserId));
        return maintenanceRequestRepository.findAll(Specification.allOf(filters));
    }

    public MaintenanceRequest getById(Long id) {
        return maintenanceRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance request not found with id: " + id));
    }

    public MaintenanceRequest create(MaintenanceRequestDto dto) {
        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + dto.getRoomId()));
        Student student = studentRepository.findById(dto.getReportedByStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + dto.getReportedByStudentId()));

        MaintenanceRequest request = MaintenanceRequest.builder()
                .room(room)
                .reportedByStudent(student)
                .description(dto.getDescription())
                .priority(dto.getPriority())
                .status(dto.getStatus() != null ? dto.getStatus() : MaintenanceStatus.OPEN)
                .loggedDate(dto.getLoggedDate() != null ? dto.getLoggedDate() : LocalDate.now())
                .build();

        return maintenanceRequestRepository.save(request);
    }

    public MaintenanceRequest update(Long id, MaintenanceRequestDto dto) {
        MaintenanceRequest request = getById(id);
        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + dto.getRoomId()));
        Student student = studentRepository.findById(dto.getReportedByStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + dto.getReportedByStudentId()));
        request.setRoom(room);
        request.setReportedByStudent(student);
        request.setDescription(dto.getDescription());
        request.setPriority(dto.getPriority());
        if (dto.getStatus() != null) {
            request.setStatus(dto.getStatus());
        }
        if (dto.getLoggedDate() != null) {
            request.setLoggedDate(dto.getLoggedDate());
        }
        if (dto.getAssignedUserId() != null) {
            SystemUser assignedUser = systemUserRepository.findById(dto.getAssignedUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + dto.getAssignedUserId()));
            request.setAssignedUser(assignedUser);
        }
        request.setResolvedDate(dto.getResolvedDate());
        return maintenanceRequestRepository.save(request);
    }

    /** Partial update: only the fields present in {@code updates} are changed. */
    public MaintenanceRequest patch(Long id, Map<String, Object> updates) {
        MaintenanceRequest existing = getById(id);
        MaintenanceRequestDto snapshot = MaintenanceRequestDto.builder()
                .requestId(existing.getRequestId())
                .roomId(existing.getRoom().getRoomId())
                .reportedByStudentId(existing.getReportedByStudent().getStudentId())
                .description(existing.getDescription())
                .priority(existing.getPriority())
                .status(existing.getStatus())
                .loggedDate(existing.getLoggedDate())
                .assignedUserId(existing.getAssignedUser() != null ? existing.getAssignedUser().getUserId() : null)
                .resolvedDate(existing.getResolvedDate())
                .build();
        MaintenanceRequestDto merged = patchMapper.merge(snapshot, updates);
        return update(id, merged);
    }

    public MaintenanceRequest assign(Long requestId, Long assignedUserId) {
        MaintenanceRequest request = getById(requestId);
        SystemUser assignedUser = systemUserRepository.findById(assignedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + assignedUserId));
        request.setAssignedUser(assignedUser);
        request.setStatus(MaintenanceStatus.IN_PROGRESS);
        return maintenanceRequestRepository.save(request);
    }

    public MaintenanceRequest resolve(Long requestId) {
        MaintenanceRequest request = getById(requestId);
        request.setStatus(MaintenanceStatus.RESOLVED);
        request.setResolvedDate(LocalDate.now());
        return maintenanceRequestRepository.save(request);
    }

    public void delete(Long id) {
        maintenanceRequestRepository.delete(getById(id));
    }
}
