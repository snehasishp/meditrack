package com.bharath.meditrack.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecialtyResponse {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private LocalDateTime createdAt;
}
