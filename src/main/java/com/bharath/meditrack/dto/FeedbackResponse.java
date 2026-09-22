package com.bharath.meditrack.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackResponse {
    private Long id;
    private Long appointmentId;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;
}
