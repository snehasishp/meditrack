package com.bharath.meditrack.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSpecialtyRequest {
    @NotBlank
    private String name;

    @NotBlank
    @Pattern(regexp = "^[a-z0-9-]+$")
    private String slug;

    private String description;
}
