# Refactor Plan — MediTrack

## Scope
Refactor the meditrack-starter to conform to CLAUDE.md conventions and rules.

---

## Phase 1 — Infrastructure

### 1.1 `pom.xml`
Add dependencies:
- `spring-boot-starter-validation` (Bean Validation)
- `spring-boot-starter-test` (JUnit 5, AssertJ, MockMvc)
- `spring-boot-starter-web` already present

### 1.2 `application.properties`
- Change `ddl-auto=update` → `ddl-auto=validate`

### 1.3 Package structure
Create missing packages under `com.bharath.meditrack`:
- `dto/` — request/response DTOs
- `service/` — business logic
- `exception/` — `@ControllerAdvice`, custom exceptions, `ErrorResponse`
- `mapper/` — optional, entity↔DTO mapping (inline in service is acceptable for this size)

---

## Phase 2 — Entities (fix all `@Data` → individual Lombok annotations + BigDecimal)

| Entity | Changes |
|--------|---------|
| `Appointment` | `@Data` → `@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor`; `double totalAmount` → `BigDecimal`; `String status` → `AppointmentStatus` enum |
| `Doctor` | `@Data` → individual annotations; `double consultationFee` → `BigDecimal` |
| `Patient` | `@Data` → individual annotations |
| `Payment` | `@Data` → individual annotations; `double amount` → `BigDecimal` |
| `Specialty` | `@Data` → individual annotations |
| `AppointmentService` | `@Data` → individual annotations; `double unitPrice`/`subtotal` → `BigDecimal` |

### New enum: `AppointmentStatus`
Statuses: `REQUESTED`, `CONFIRMED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`, `NO_SHOW`

---

## Phase 3 — Exception handling

### 3.1 `ErrorResponse`
```java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ErrorResponse {
    private int status;
    private String error;
    private String message;
    private String path;
    private LocalDateTime timestamp;
}
```

### 3.2 Custom exceptions
- `ResourceNotFoundException` (runtime, → 404)
- `BusinessRuleException` (runtime, → 409)
- `ValidationException` (runtime, → 400)

### 3.3 `GlobalExceptionHandler`
`@ControllerAdvice` handling all three + `MethodArgumentNotValidException` → 400, generic `Exception` → 500.

---

## Phase 4 — DTOs

Each entity gets a request DTO and response DTO:

| Entity | Request DTO | Response DTO |
|--------|-------------|--------------|
| Patient | `CreatePatientRequest` | `PatientResponse` |
| Doctor | `CreateDoctorRequest` | `DoctorResponse` |
| Appointment | `BookAppointmentRequest` | `AppointmentResponse` |
| Specialty | `CreateSpecialtyRequest` | `SpecialtyResponse` |

All request DTOs have Bean Validation (`@NotBlank`, `@Email`, etc.).

---

## Phase 5 — Service layer

Extract business logic from controllers into `@Service` classes:

| Service | Responsibilities |
|---------|-----------------|
| `PatientService` | `findAll`, `findById`, `create` (with email duplicate check) |
| `DoctorService` | `findAll`, `findById`, `create` |
| `SpecialtyService` | `findAll`, `create` |
| `AppointmentService` | `findAll`, `findById`, `book` (with slot capacity check, status transition), `cancel` (status guard, no slot restore/refund in v1) |

All services use `@RequiredArgsConstructor` + `final` fields — no `@Autowired`.

---

## Phase 6 — Thin Controllers

Replace current controllers with thin ones that delegate entirely to services and use DTOs.

### 6.1 New `AppointmentController`
- `GET /api/v1/appointments` → delegate to `AppointmentService.findAll()`
- `GET /api/v1/appointments/{id}` → delegate to `AppointmentService.findById()`
- `POST /api/v1/appointments/book` → delegate to `AppointmentService.book(request)` (request body DTO)
- `POST /api/v1/appointments/{id}/cancel` → delegate to `AppointmentService.cancel(id)`

### 6.2 New `PatientController`
- `GET /api/v1/patients`
- `POST /api/v1/patients` (request body DTO, `@Valid`)

### 6.3 New `CatalogController` (or split into `DoctorController` + `SpecialtyController`)
- `GET /api/v1/specialties`, `POST /api/v1/specialties`
- `GET /api/v1/doctors`, `POST /api/v1/doctors`

All paths prefixed with `/api/v1`. All methods take/return DTOs.

---

## Phase 7 — Delete

- `CatalogController` (replaced)
- `model/AppointmentService.java` — rename to `AppointmentServiceEntity` or move under model package if needed; currently it's a JPA entity in `model/`, not to be confused with the new service layer class

> The `model/AppointmentService.java` is a JPA entity (`AppointmentService` — the join table entity). The new service layer will also be named `AppointmentService`. Rename the JPA entity to `AppointmentServiceItem` to avoid the conflict.

---

## Phase 8 — Tests

- Add `spring-boot-starter-test` dependency (done in Phase 1)
- `AppointmentServiceTest.java`
- `AppointmentControllerTest.java`
- `PatientServiceTest.java`
- `PatientControllerTest.java`

---

## Execution Order

```
1. pom.xml        — add dependencies
2. application.properties — ddl-auto
3. Entities      — fix annotations + BigDecimal + enum
4. Exception      — ErrorResponse + custom exceptions + GlobalExceptionHandler
5. DTOs           — create all request/response DTOs
6. Services      — extract business logic
7. Controllers   — thin, delegating, /api/v1
8. Rename         — JPA entity AppointmentService → AppointmentServiceItem
9. Tests          — add tests for all new services + controllers
10. Delete        — old controllers
```
