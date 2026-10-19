package com.university.hostel.repository;

import com.university.hostel.entity.Bed;
import com.university.hostel.entity.BedStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface BedRepository extends JpaRepository<Bed, Long>, JpaSpecificationExecutor<Bed> {
    List<Bed> findByRoom_RoomId(Long roomId);
    List<Bed> findByRoom_RoomIdAndBedStatus(Long roomId, BedStatus bedStatus);
    List<Bed> findByBedStatus(BedStatus bedStatus);
}
