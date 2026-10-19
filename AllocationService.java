package com.university.hostel.service;

import com.university.hostel.dto.AllocationDto;
import com.university.hostel.entity.*;
import com.university.hostel.exception.InvalidStateException;
import com.university.hostel.exception.ResourceNotFoundException;
import com.university.hostel.repository.*;
import com.university.hostel.util.FilterSpecifications;
import com.university.hostel.util.PatchMapper;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Service
public class AllocationService {
    private final AllocationRepository allocationRepository;
    private final ApplicationRepository applicationRepository;
    private final BedRepository bedRepository;
    private final StudentRepository studentRepository;
    private final AcademicTermRepository academicTermRepository;
    private final SystemUserRepository systemUserRepository;
    private final RoomRepository roomRepository;
    private final PatchMapper patchMapper;

    public AllocationService(AllocationRepository allocationRepository,
                            ApplicationRepository applicationRepository,
                            BedRepository bedRepository,
                            StudentRepository studentRepository,
                            AcademicTermRepository academicTermRepository,
                            SystemUserRepository systemUserRepository,
                            RoomRepository roomRepository,
                            PatchMapper patchMapper) {
        this.allocationRepository = allocationRepository;
        this.applicationRepository = applicationRepository;
        this.bedRepository = bedRepository;
        this.studentRepository = studentRepository;
        this.academicTermRepository = academicTermRepository;
        this.systemUserRepository = systemUserRepository;
        this.roomRepository = roomRepository;
        this.patchMapper = patchMapper;
    }

    public List<Allocation> getAll() {
        return allocationRepository.findAll();
    }

    /** Filtered listing. Any combination of params may be omitted (null). */
    public List<Allocation> search(Long studentId, Long termId, Long roomId, AllocationStatus status,
                                   Long applicationId) {
        List<Specification<Allocation>> filters = new ArrayList<>();
        if (studentId != null) filters.add((root, query, cb) -> cb.equal(root.get("student").get("studentId"), studentId));
        if (termId != null) filters.add((root, query, cb) -> cb.equal(root.get("term").get("termId"), termId));
        if (roomId != null) filters.add((root, query, cb) -> cb.equal(root.get("bed").get("room").get("roomId"), roomId));
        if (status != null) filters.add(FilterSpecifications.equal("allocationStatus", status));
        if (applicationId != null) filters.add((root, query, cb) -> cb.equal(root.get("application").get("applicationId"), applicationId));
        return allocationRepository.findAll(Specification.allOf(filters));
    }

    public Allocation getById(Long id) {
        return allocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Allocation not found with id: " + id));
    }

    @Transactional
    public Allocation create(AllocationDto dto) {
        Application application = applicationRepository.findById(dto.getApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + dto.getApplicationId()));

        if (application.getApplicationStatus() != ApplicationStatus.APPROVED) {
            throw new InvalidStateException("Allocation can only be created for an approved application");
        }

        if (!allocationRepository.findByApplication_ApplicationId(dto.getApplicationId()).isEmpty()) {
            throw new InvalidStateException("Application already has an allocation");
        }

        Bed bed = bedRepository.findById(dto.getBedId())
                .orElseThrow(() -> new ResourceNotFoundException("Bed not found with id: " + dto.getBedId()));

        if (bed.getBedStatus() == BedStatus.OCCUPIED) {
            throw new InvalidStateException("Selected bed is already occupied");
        }

        Student student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + dto.getStudentId()));
        AcademicTerm term = academicTermRepository.findById(dto.getTermId())
                .orElseThrow(() -> new ResourceNotFoundException("Academic term not found with id: " + dto.getTermId()));
        SystemUser createdByUser = systemUserRepository.findById(dto.getCreatedByUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + dto.getCreatedByUserId()));

        Allocation allocation = Allocation.builder()
                .application(application)
                .student(student)
                .bed(bed)
                .term(term)
                .checkinDate(dto.getCheckinDate())
                .allocationStatus(dto.getAllocationStatus() != null ? dto.getAllocationStatus() : AllocationStatus.ACTIVE)
                .feeAmount(dto.getFeeAmount())
                .createdByUser(createdByUser)
                .build();

        Allocation saved = allocationRepository.save(allocation);

        bed.setBedStatus(BedStatus.OCCUPIED);
        bedRepository.save(bed);

        List<Bed> freeBeds = bedRepository.findByRoom_RoomIdAndBedStatus(bed.getRoom().getRoomId(), BedStatus.AVAILABLE);
        Room room = bed.getRoom();
        room.setRoomStatus(freeBeds.isEmpty() ? RoomStatus.FULL : RoomStatus.AVAILABLE);
        roomRepository.save(room);

        return saved;
    }

    /**
     * Edits the mutable record fields (checkin/checkout date, status, fee amount).
     * Does not move the allocation to a different bed/application; use dedicated
     * endpoints (e.g. checkout) for state transitions that affect bed occupancy.
     */
    public Allocation update(Long id, AllocationDto dto) {
        Allocation allocation = getById(id);
        AcademicTerm term = academicTermRepository.findById(dto.getTermId())
                .orElseThrow(() -> new ResourceNotFoundException("Academic term not found with id: " + dto.getTermId()));
        allocation.setTerm(term);
        allocation.setCheckinDate(dto.getCheckinDate());
        allocation.setCheckoutDate(dto.getCheckoutDate());
        if (dto.getAllocationStatus() != null) {
            allocation.setAllocationStatus(dto.getAllocationStatus());
        }
        allocation.setFeeAmount(dto.getFeeAmount());
        return allocationRepository.save(allocation);
    }

    /** Partial update: only the fields present in {@code updates} are changed. */
    public Allocation patch(Long id, Map<String, Object> updates) {
        Allocation existing = getById(id);
        AllocationDto snapshot = AllocationDto.builder()
                .allocationId(existing.getAllocationId())
                .applicationId(existing.getApplication().getApplicationId())
                .studentId(existing.getStudent().getStudentId())
                .bedId(existing.getBed().getBedId())
                .termId(existing.getTerm().getTermId())
                .checkinDate(existing.getCheckinDate())
                .checkoutDate(existing.getCheckoutDate())
                .allocationStatus(existing.getAllocationStatus())
                .feeAmount(existing.getFeeAmount())
                .createdByUserId(existing.getCreatedByUser().getUserId())
                .build();
        AllocationDto merged = patchMapper.merge(snapshot, updates);
        return update(id, merged);
    }

    @Transactional
    public Allocation checkout(Long allocationId) {
        Allocation allocation = getById(allocationId);
        if (allocation.getAllocationStatus() == AllocationStatus.CHECKED_OUT) {
            throw new InvalidStateException("Allocation already checked out");
        }

        allocation.setCheckoutDate(java.time.LocalDate.now());
        allocation.setAllocationStatus(AllocationStatus.CHECKED_OUT);
        allocationRepository.save(allocation);

        Bed bed = allocation.getBed();
        bed.setBedStatus(BedStatus.AVAILABLE);
        bedRepository.save(bed);

        List<Bed> occupiedBeds = bedRepository.findByRoom_RoomIdAndBedStatus(bed.getRoom().getRoomId(), BedStatus.OCCUPIED);
        Room room = bed.getRoom();
        room.setRoomStatus(occupiedBeds.isEmpty() ? RoomStatus.AVAILABLE : RoomStatus.FULL);
        roomRepository.save(room);

        return allocation;
    }

    public void delete(Long id) {
        allocationRepository.delete(getById(id));
    }
}
