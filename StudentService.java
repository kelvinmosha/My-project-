package com.university.hostel.service;

import com.university.hostel.dto.StudentDto;
import com.university.hostel.entity.Student;
import com.university.hostel.exception.ResourceNotFoundException;
import com.university.hostel.repository.StudentRepository;
import com.university.hostel.util.FilterSpecifications;
import com.university.hostel.util.PatchMapper;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class StudentService {
    private final StudentRepository studentRepository;
    private final PatchMapper patchMapper;

    public StudentService(StudentRepository studentRepository, PatchMapper patchMapper) {
        this.studentRepository = studentRepository;
        this.patchMapper = patchMapper;
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    /** Filtered listing. Any combination of params may be omitted (null). */
    public List<Student> search(Boolean isActive, String programOfStudy, String nameContains,
                                String email, String regNumber) {
        List<Specification<Student>> filters = new ArrayList<>();
        if (isActive != null) filters.add(FilterSpecifications.equal("isActive", isActive));
        if (programOfStudy != null && !programOfStudy.isBlank()) {
            filters.add(FilterSpecifications.exactIgnoreCase("programOfStudy", programOfStudy));
        }
        if (nameContains != null && !nameContains.isBlank()) {
            filters.add(FilterSpecifications.containsIgnoreCase("fullName", nameContains));
        }
        if (email != null && !email.isBlank()) {
            filters.add(FilterSpecifications.containsIgnoreCase("emailAddress", email));
        }
        if (regNumber != null && !regNumber.isBlank()) {
            filters.add(FilterSpecifications.exactIgnoreCase("studentRegNumber", regNumber));
        }
        return studentRepository.findAll(Specification.allOf(filters));
    }

    public Student getStudentById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
    }

    public Student createStudent(StudentDto dto) {
        Student student = Student.builder()
                .fullName(dto.getFullName())
                .studentRegNumber(dto.getStudentRegNumber())
                .phoneNumber(dto.getPhoneNumber())
                .emailAddress(dto.getEmailAddress())
                .gender(dto.getGender())
                .nationalIdNumber(dto.getNationalIdNumber())
                .dateOfBirth(dto.getDateOfBirth())
                .programOfStudy(dto.getProgramOfStudy())
                .guardianName(dto.getGuardianName())
                .guardianPhone(dto.getGuardianPhone())
                .isActive(dto.isActive())
                .build();
        return studentRepository.save(student);
    }

    public Student updateStudent(Long id, StudentDto dto) {
        Student student = getStudentById(id);
        student.setFullName(dto.getFullName());
        student.setStudentRegNumber(dto.getStudentRegNumber());
        student.setPhoneNumber(dto.getPhoneNumber());
        student.setEmailAddress(dto.getEmailAddress());
        student.setGender(dto.getGender());
        student.setNationalIdNumber(dto.getNationalIdNumber());
        student.setDateOfBirth(dto.getDateOfBirth());
        student.setProgramOfStudy(dto.getProgramOfStudy());
        student.setGuardianName(dto.getGuardianName());
        student.setGuardianPhone(dto.getGuardianPhone());
        student.setActive(dto.isActive());
        return studentRepository.save(student);
    }

    /** Partial update: only the fields present in {@code updates} are changed. */
    public Student patchStudent(Long id, Map<String, Object> updates) {
        Student existing = getStudentById(id);
        StudentDto snapshot = StudentDto.builder()
                .studentId(existing.getStudentId())
                .fullName(existing.getFullName())
                .studentRegNumber(existing.getStudentRegNumber())
                .phoneNumber(existing.getPhoneNumber())
                .emailAddress(existing.getEmailAddress())
                .gender(existing.getGender())
                .nationalIdNumber(existing.getNationalIdNumber())
                .dateOfBirth(existing.getDateOfBirth())
                .programOfStudy(existing.getProgramOfStudy())
                .guardianName(existing.getGuardianName())
                .guardianPhone(existing.getGuardianPhone())
                .isActive(existing.isActive())
                .build();
        StudentDto merged = patchMapper.merge(snapshot, updates);
        return updateStudent(id, merged);
    }

    public void deleteStudent(Long id) {
        studentRepository.delete(getStudentById(id));
    }
}
