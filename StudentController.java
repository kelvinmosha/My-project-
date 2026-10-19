package com.university.hostel.controller;

import com.university.hostel.dto.StudentDto;
import com.university.hostel.entity.Student;
import com.university.hostel.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class StudentController {
    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','HOSTEL_STAFF','STUDENT')")
    @GetMapping("/students")
    public ResponseEntity<List<Student>> getAll(
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String programOfStudy,
            @RequestParam(required = false) String nameContains,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String regNumber) {
        return ResponseEntity.ok(studentService.search(isActive, programOfStudy, nameContains, email, regNumber));
    }

    @PreAuthorize("hasAnyRole('ADMIN','HOSTEL_STAFF','STUDENT')")
    @GetMapping("/students/{id}")
    public ResponseEntity<Student> getById(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getStudentById(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN','HOSTEL_STAFF')")
    @PostMapping("/staff/students")
    public ResponseEntity<Student> create(@Valid @RequestBody StudentDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.createStudent(dto));
    }

    @PreAuthorize("hasAnyRole('ADMIN','HOSTEL_STAFF')")
    @PutMapping("/staff/students/{id}")
    public ResponseEntity<Student> update(@PathVariable Long id, @Valid @RequestBody StudentDto dto) {
        return ResponseEntity.ok(studentService.updateStudent(id, dto));
    }

    @PreAuthorize("hasAnyRole('ADMIN','HOSTEL_STAFF')")
    @PatchMapping("/staff/students/{id}")
    public ResponseEntity<Student> patch(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        return ResponseEntity.ok(studentService.patchStudent(id, updates));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/staff/students/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }
}
