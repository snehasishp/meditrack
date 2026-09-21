package com.bharath.meditrack.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {
    private Long appointmentId;
    private String appointmentNo;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private String status;
    private LocalDate scheduledDate;
    private BigDecimal totalAmount;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
