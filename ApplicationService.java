package com.university.hostel.service;

import com.university.hostel.dto.ApplicationDto;
import com.university.hostel.entity.*;
import com.university.hostel.exception.InvalidStateException;
import com.university.hostel.exception.ResourceNotFoundException;
import com.university.hostel.repository.ApplicationRepository;
import com.university.hostel.repository.AcademicTermRepository;
import com.university.hostel.repository.RoomTypeRepository;
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
public class ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final StudentRepository studentRepository;
    private final AcademicTermRepository academicTermRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final SystemUserRepository systemUserRepository;
    private final PatchMapper patchMapper;

    public ApplicationService(ApplicationRepository applicationRepository,
                             StudentRepository studentRepository,
                             AcademicTermRepository academicTermRepository,
                             RoomTypeRepository roomTypeRepository,
                             SystemUserRepository systemUserRepository,
                             PatchMapper patchMapper) {
        this.applicationRepository = applicationRepository;
        this.studentRepository = studentRepository;
        this.academicTermRepository = academicTermRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.systemUserRepository = systemUserRepository;
        this.patchMapper = patchMapper;
    }

    public List<Application> getAll() {
        return applicationRepository.findAll();
    }

    /** Filtered listing. Any combination of params may be omitted (null). */
    public List<Application> search(Long studentId, Long termId, ApplicationStatus status) {
        List<Specification<Application>> filters = new ArrayList<>();
        if (studentId != null) filters.add((root, query, cb) -> cb.equal(root.get("student").get("studentId"), studentId));
        if (termId != null) filters.add((root, query, cb) -> cb.equal(root.get("term").get("termId"), termId));
        if (status != null) filters.add(FilterSpecifications.equal("applicationStatus", status));
        return applicationRepository.findAll(Specification.allOf(filters));
    }

    public Application getById(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));
    }

    public Application create(ApplicationDto dto) {
        Student student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + dto.getStudentId()));
        AcademicTerm term = academicTermRepository.findById(dto.getTermId())
                .orElseThrow(() -> new ResourceNotFoundException("Academic term not found with id: " + dto.getTermId()));
        RoomType roomType = roomTypeRepository.findById(dto.getPreferredRoomTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Room type not found with id: " + dto.getPreferredRoomTypeId()));

        Application app = Application.builder()
                .student(student)
                .term(term)
                .preferredRoomType(roomType)
                .applicationDate(dto.getApplicationDate() != null ? dto.getApplicationDate() : LocalDate.now())
                .applicationStatus(dto.getApplicationStatus() != null ? dto.getApplicationStatus() : ApplicationStatus.PENDING)
                .build();

        return applicationRepository.save(app);
    }

    public Application update(Long id, ApplicationDto dto) {
        Application app = getById(id);
        app.setStudent(studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + dto.getStudentId())));
        app.setTerm(academicTermRepository.findById(dto.getTermId())
                .orElseThrow(() -> new ResourceNotFoundException("Academic term not found with id: " + dto.getTermId())));
        app.setPreferredRoomType(roomTypeRepository.findById(dto.getPreferredRoomTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Room type not found with id: " + dto.getPreferredRoomTypeId())));
        app.setApplicationDate(dto.getApplicationDate() != null ? dto.getApplicationDate() : LocalDate.now());
        if (dto.getApplicationStatus() != null) {
            app.setApplicationStatus(dto.getApplicationStatus());
        }
        if (dto.getReviewedByUserId() != null) {
            app.setReviewedByUser(systemUserRepository.findById(dto.getReviewedByUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("System user not found with id: " + dto.getReviewedByUserId())));
        }
        app.setReviewDate(dto.getReviewDate());
        return applicationRepository.save(app);
    }

    /** Partial update: only the fields present in {@code updates} are changed. */
    public Application patch(Long id, Map<String, Object> updates) {
        Application existing = getById(id);
        ApplicationDto snapshot = ApplicationDto.builder()
                .applicationId(existing.getApplicationId())
                .studentId(existing.getStudent().getStudentId())
                .termId(existing.getTerm().getTermId())
                .preferredRoomTypeId(existing.getPreferredRoomType().getRoomTypeId())
                .applicationDate(existing.getApplicationDate())
                .applicationStatus(existing.getApplicationStatus())
                .reviewedByUserId(existing.getReviewedByUser() != null ? existing.getReviewedByUser().getUserId() : null)
                .reviewDate(existing.getReviewDate())
                .build();
        ApplicationDto merged = patchMapper.merge(snapshot, updates);
        return update(id, merged);
    }

    public Application approveApplication(Long applicationId, Long reviewedByUserId) {
        Application application = getById(applicationId);
        if (application.getApplicationStatus() != ApplicationStatus.PENDING) {
            throw new InvalidStateException("Only pending applications can be approved");
        }
        application.setApplicationStatus(ApplicationStatus.APPROVED);
        application.setReviewDate(LocalDate.now());
        application.setReviewedByUser(systemUserRepository.findById(reviewedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("System user not found with id: " + reviewedByUserId)));
        return applicationRepository.save(application);
    }

    public void delete(Long id) {
        applicationRepository.delete(getById(id));
    }
}
