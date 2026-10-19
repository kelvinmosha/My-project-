package com.university.hostel.repository;

import com.university.hostel.entity.Room;
import com.university.hostel.entity.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long>, JpaSpecificationExecutor<Room> {
    List<Room> findByRoomStatus(RoomStatus roomStatus);
    List<Room> findByBuildingBlockAndIsActive(String buildingBlock, boolean isActive);
    List<Room> findByRoomType_RoomTypeId(Long roomTypeId);
}
