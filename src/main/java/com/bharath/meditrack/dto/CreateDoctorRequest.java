package com.bharath.meditrack.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDoctorRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String licenseNo;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal consultationFee;

    @Min(1)
    private int dailySlotCapacity;

    private Boolean active;

    @NotNull
    private Long specialtyId;
}
