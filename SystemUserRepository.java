package com.university.hostel.repository;

import com.university.hostel.entity.SystemUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface SystemUserRepository extends JpaRepository<SystemUser, Long>, JpaSpecificationExecutor<SystemUser> {
    Optional<SystemUser> findByUsername(String username);
    boolean existsByUsername(String username);
}
