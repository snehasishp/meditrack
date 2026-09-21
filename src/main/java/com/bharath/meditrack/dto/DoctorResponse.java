package com.bharath.meditrack.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorResponse {
    private Long id;
    private String name;
    private String licenseNo;
    private BigDecimal consultationFee;
    private int dailySlotCapacity;
    private Boolean active;
    private String specialtyName;
    private LocalDateTime createdAt;
}
