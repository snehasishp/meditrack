# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
# Run schema first (MySQL Workbench) → db/meditrack_schema.sql
mvn spring-boot:run
# Run a single test
mvn test -Dtest=ClassName
```

## Project Overview

**MediTrack** — a clinic appointment & billing REST API built with Spring Boot 4.1.0, Java 21, Maven, and MySQL.

Package root: `com.bharath.meditrack`

```
src/main/java/com/bharath/meditrack/
  model/          JPA entities
  repo/           Spring Data JPA repositories
  service/        Business logic (@Service classes)
  controller/     REST controllers (thin, delegate to services)
  dto/             Request/response DTOs
  exception/      Centralised exception handling
  MediTrackApplication.java
src/main/resources/application.properties
db/meditrack_schema.sql   MySQL schema + seed data
```

## Database

- MySQL, database `meditrack_db` (created by `db/meditrack_schema.sql`)
- Tables: `specialties`, `doctors`, `patients`, `appointments`, `appointment_services`, `payments`
- Hibernate `ddl-auto=validate` — schema is managed by `db/meditrack_schema.sql`, not Hibernate

## Conventions

All rules under `.claude/rules/` are mandatory and must be applied on every change:

- [rules/testing.md](.claude/rules/testing.md) — test structure and assertions
- [rules/api-design.md](.claude/rules/api-design.md) — REST API conventions

Additional project-level rules:

- **Constructor injection** — always `@RequiredArgsConstructor` with `final` fields. Never `@Autowired` on fields or constructors.
- **Money** — always `BigDecimal`. Never `double` or `float`.
- **DTOs** — controllers receive DTOs as input and return DTOs as output. Never expose JPA entities directly in controller methods.
- **Thin controllers** — all business logic lives in `@Service` classes. Controllers only handle HTTP concerns (params, response mapping, HTTP status).
- **API versioning** — all endpoints are under `/api/v1`.
- **Exception handling** — use `@ControllerAdvice` with a consistent `ErrorResponse` JSON body. Never let `RuntimeException` propagate to a default 500.
- **Entities** — annotate with `@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor`. Never `@Data` on JPA entities.

## Intentional Issues (do not fix silently — treat as part of the assignment)

This is a deliberately rough starter. The refactoring tasks include:

- **Money as `double`** — `Doctor.consultationFee`, `Appointment.totalAmount`, `Payment.amount`, `AppointmentService.unitPrice`/`subtotal` should all be `BigDecimal`
- **`@Data` on JPA entities** — entities expose all fields via setters; return DTOs from controllers instead
- **Business logic in controllers** — extract to a proper service layer with `@Service`
- **`@Autowired` field injection** — replace with constructor injection via `@RequiredArgsConstructor` and `final` fields
- **No tests** — `spring-boot-starter-test` is absent; add it and write JUnit 5 / AssertJ / MockMvc tests
- **Status as free-text `String`** — no enum, no transition rules
- **`cancel` endpoint** — does not restore doctor's slot capacity or refund a payment
- **No validation** — no Bean Validation constraints on entities or DTOs
- **No exception handling** — missing rows return `null`, throwing `RuntimeException` (→ HTTP 500)
- **Plain-text DB password** in `application.properties`
- **`ddl-auto=update`** — should be `ddl-auto=validate`
- **No `/api/v1` prefix** — all paths need the version prefix added
