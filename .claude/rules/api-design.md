# API Design Rules

## Versioning

All endpoints are under `/api/v1`. No exceptions.

```
GET    /api/v1/doctors
GET    /api/v1/doctors/{id}
POST   /api/v1/doctors
GET    /api/v1/patients
POST   /api/v1/patients
GET    /api/v1/appointments
POST   /api/v1/appointments/book
POST   /api/v1/appointments/{id}/cancel
GET    /api/v1/specialties
POST   /api/v1/specialties
```

## Request & Response DTOs

Every controller method accepts a DTO (not a JPA entity) and returns a DTO. Map between entity and DTO in the service layer or a dedicated mapper.

```java
// Request DTO — validated
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CreatePatientRequest {
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    @NotBlank @Email
    private String email;
    @Pattern(regexp = "^\\+.*")
    private String phone;
}

// Response DTO — includes id, no setters needed
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PatientResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
}
```

## HTTP Status Codes

| Situation | Status |
|-----------|--------|
| Resource created | `201 Created` |
| Resource found | `200 OK` |
| No content (DELETE) | `204 No Content` |
| Validation failure | `400 Bad Request` |
| Resource not found | `404 Not Found` |
| Business rule violation | `409 Conflict` |

## Centralised Exception Handling

Use `@ControllerAdvice` with a single `ErrorResponse` body. Never let exceptions propagate to Spring's default error page.

```java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ErrorResponse {
    private int status;
    private String error;
    private String message;
    private String path;
    private LocalDateTime timestamp;
}

@ExceptionHandler(EntityNotFoundException.class)
@ResponseStatus(HttpStatus.NOT_FOUND)
public ErrorResponse handleNotFound(EntityNotFoundException ex, HttpServletRequest req) {
    return ErrorResponse.builder()
        .status(404)
        .error("Not Found")
        .message(ex.getMessage())
        .path(req.getRequestURI())
        .timestamp(LocalDateTime.now())
        .build();
}
```

## Exception Types

- `EntityNotFoundException` → `404`
- `ValidationException` / `MethodArgumentNotValidException` → `400`
- Business rule violations (e.g. no slot available) → `409`
- Generic/unexpected → `500` with a safe, user-friendly message (no stack trace in body)

## Response Body Shape

All responses are JSON. Never return a raw string or `null`. Empty list returns `[]`, not `null`.

```json
// Single resource
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com"
}

// Error
{
  "status": 404,
  "error": "Not Found",
  "message": "Patient with id 99 not found",
  "path": "/api/v1/patients/99",
  "timestamp": "2026-09-21T10:30:00"
}
```

## Request Validation

Every request body DTO must have at least basic Bean Validation annotations (`@NotBlank`, `@NotNull`, `@Email`, `@Pattern`, etc.). Controller methods take `@Valid @RequestBody Dto dto` — never skip `@Valid`.
