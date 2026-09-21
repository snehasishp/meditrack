package com.bharath.meditrack.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "doctors")
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @Column(name = "license_no")
    private String licenseNo;

    @Column(name = "consultation_fee")
    private BigDecimal consultationFee;

    @Column(name = "daily_slot_capacity")
    private int dailySlotCapacity;

    private boolean active;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialty_id")
    private Specialty specialty;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
