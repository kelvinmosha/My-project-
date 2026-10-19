package com.university.hostel.repository;

import com.university.hostel.entity.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RoomTypeRepository extends JpaRepository<RoomType, Long>, JpaSpecificationExecutor<RoomType> {
}
