package com.university.hostel.service;

import com.university.hostel.entity.SystemUser;
import com.university.hostel.repository.SystemUserRepository;
import com.university.hostel.util.FilterSpecifications;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SystemUserService {
    private final SystemUserRepository systemUserRepository;

    public SystemUserService(SystemUserRepository systemUserRepository) {
        this.systemUserRepository = systemUserRepository;
    }

    public List<SystemUser> search(String username, String email, String roleName, Boolean isActive) {
        List<Specification<SystemUser>> filters = new ArrayList<>();
        if (username != null && !username.isBlank()) {
            filters.add(FilterSpecifications.exactIgnoreCase("username", username));
        }
        if (email != null && !email.isBlank()) {
            filters.add(FilterSpecifications.containsIgnoreCase("emailAddress", email));
        }
        if (roleName != null && !roleName.isBlank()) {
            filters.add((root, query, cb) -> cb.equal(
                    cb.lower(root.get("role").get("roleName")), roleName.trim().toLowerCase(java.util.Locale.ROOT)));
        }
        if (isActive != null) filters.add(FilterSpecifications.equal("isActive", isActive));
        return systemUserRepository.findAll(Specification.allOf(filters));
    }
}