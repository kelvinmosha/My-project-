package com.university.hostel.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long roleId;

    @Column(name = "role_name", nullable = false, unique = true)
    private String roleName;

    @Column(name = "permission_set", columnDefinition = "TEXT")
    private String permissionSet;

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL)
    @Builder.Default
    @JsonIgnore
    private List<SystemUser> users = new ArrayList<>();
}
