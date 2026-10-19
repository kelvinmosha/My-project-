package com.university.hostel.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "beds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bed_id")
    private Long bedId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "bed_label", nullable = false)
    private String bedLabel;

    @Enumerated(EnumType.STRING)
    @Column(name = "bed_status", nullable = false)
    private BedStatus bedStatus;
}
