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
  model/          JPA entities + AppointmentStatus enum
  repo/           Spring Data JPA repositories
  service/        Business logic (@Service classes)
  controller/     Thin REST controllers (delegate to services)
  dto/            Request/response DTOs
  exception/      ErrorResponse + custom exceptions + GlobalExceptionHandler
  mapper/         (entity↔DTO mapping inline in service layer)
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

## API Endpoints

| Method | Path | Status |
|--------|------|--------|
| GET | `/api/v1/doctors` | |
| GET | `/api/v1/doctors/{id}` | |
| POST | `/api/v1/doctors` | 201 |
| GET | `/api/v1/patients` | |
| POST | `/api/v1/patients` | 201 |
| GET | `/api/v1/appointments` | |
| GET | `/api/v1/appointments/{id}` | |
| POST | `/api/v1/appointments/book` | 201 |
| POST | `/api/v1/appointments/{id}/cancel` | |
| GET | `/api/v1/specialties` | |
| POST | `/api/v1/specialties` | 201 |
