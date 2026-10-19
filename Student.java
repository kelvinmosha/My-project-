package com.university.hostel.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "students")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id")
    private Long studentId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "student_reg_number", nullable = false, unique = true)
    private String studentRegNumber;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "email_address", nullable = false, unique = true)
    private String emailAddress;

    @Column(name = "gender")
    private String gender;

    @Column(name = "national_id_number", unique = true)
    private String nationalIdNumber;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "program_of_study")
    private String programOfStudy;

    @Column(name = "guardian_name")
    private String guardianName;

    @Column(name = "guardian_phone")
    private String guardianPhone;

    @Column(name = "is_active")
    @Builder.Default
    private boolean isActive = true;
}
