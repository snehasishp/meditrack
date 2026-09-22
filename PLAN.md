# Plan: Patient Feedback Feature

## Overview

Add a feedback system where patients can rate completed appointments (1-5 stars + optional comment), with a computed average rating per doctor.

## Files to Create

### 1. Model: `Feedback.java`
```
@Entity
@Table(name = "feedbacks")
- id (PK)
- appointment_id (FK, UNIQUE — one feedback per appointment)
- rating (1-5, NOT NULL)
- comment (TEXT, nullable)
- created_at
```
Relationship: `@OneToOne` to `Appointment`

### 2. Repository: `FeedbackRepository.java`
- `boolean existsByAppointmentId(Long appointmentId)`
- `Optional<Feedback> findByAppointmentId(Long appointmentId)`

### 3. Repository: `DoctorRepository.java` (extend)
Add JPQL query:
```java
@Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.appointment.doctor.id = :doctorId")
Double getAverageRatingByDoctorId(@Param("doctorId") Long doctorId);
```
Returns `null` if no feedback exists.

### 4. DTO: `CreateFeedbackRequest.java`
```java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
- @Min(1) @Max(5) int rating  (NOT NULL)
- String comment              (optional)
```

### 5. DTO: `FeedbackResponse.java`
```java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
- Long id
- Long appointmentId
- int rating
- String comment
- LocalDateTime createdAt
```

### 6. DTO: `DoctorResponse.java` (extend)
Add `Double averageRating` field.

## Files to Modify

### 7. Schema: `db/meditrack_schema.sql`
Add feedbacks table after payments:
```sql
CREATE TABLE feedbacks (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    appointment_id BIGINT        NOT NULL,
    rating         TINYINT       NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment        TEXT,
    created_at     DATETIME,
    PRIMARY KEY (id),
    UNIQUE KEY uk_feedbacks_appointment (appointment_id),
    CONSTRAINT fk_feedbacks_appointment FOREIGN KEY (appointment_id) REFERENCES appointments (appointment_id)
);
CREATE INDEX idx_feedbacks_appointment ON feedbacks (appointment_id);
CREATE INDEX idx_feedbacks_doctor ON feedbacks (doctor_id) -- via join in query
```

### 8. Model: `Appointment.java`
Add `@OneToOne(mappedBy = "appointment") private Feedback feedback;`

### 9. Service: `AppointmentService.java`
Add method:
```java
public FeedbackResponse submitFeedback(Long appointmentId, CreateFeedbackRequest request)
```
Business rules:
- Appointment must exist → `ResourceNotFoundException`
- Appointment must be `COMPLETED` → `BusinessRuleException("Appointment must be completed to submit feedback")`
- Feedback already exists → `DuplicateResourceException("Feedback already submitted for this appointment")`

### 10. Service: `DoctorService.java`
Add method:
```java
public Double getAverageRating(Long doctorId)
```
Calls `doctorRepository.getAverageRatingByDoctorId()`. Returns `null` if no ratings.

Update `toResponse()` to include `averageRating`.

### 11. Controller: `AppointmentController.java`
Add endpoint:
```java
@PostMapping("/{id}/feedback")
public ResponseEntity<FeedbackResponse> submitFeedback(
        @PathVariable Long id,
        @Valid @RequestBody CreateFeedbackRequest request)
```
Returns `201 Created`.

### 12. Controller: `DoctorController.java`
Existing `GET /api/v1/doctors/{id}` returns updated `DoctorResponse` with `averageRating` embedded.

### 13. GlobalExceptionHandler.java
No new handlers needed — uses existing exceptions.

## Tests to Add

### 14. `AppointmentServiceTest.java`
- `testSubmitFeedbackSuccess`
- `testSubmitFeedbackThrowsWhenAppointmentNotFound`
- `testSubmitFeedbackThrowsWhenNotCompleted`
- `testSubmitFeedbackThrowsWhenAlreadySubmitted`

### 15. `AppointmentControllerTest.java`
- `testSubmitFeedbackReturnsCreated`
- `testSubmitFeedbackOnNonCompletedReturnsConflict`
- `testSubmitFeedbackAlreadyExistsReturnsConflict`

### 16. `DoctorServiceTest.java` (extend)
- `testGetAverageRatingReturnsValue`
- `testGetAverageRatingReturnsNullWhenNoFeedback`

## Implementation Order

1. Schema SQL
2. Model (Feedback.java)
3. Repositories
4. DTOs
5. AppointmentService
6. DoctorService
7. Controllers
8. Tests
