package com.university.hostel.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "room_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_type_id")
    private Long roomTypeId;

    @Column(name = "type_name", nullable = false, unique = true)
    private String typeName;

    @Column(name = "bed_capacity", nullable = false)
    private Integer bedCapacity;

    @Column(name = "base_fee_per_term", nullable = false)
    private BigDecimal baseFeePerTerm;

    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "roomType", cascade = CascadeType.ALL)
    @Builder.Default
    @JsonIgnore 
    private List<Room> rooms = new ArrayList<>();
}
