# InterviewMate Repository Guide

This project hosts a Kotlin/Spring Boot backend. All sources live under `src/main/kotlin` with package `com.interviewmate`.

## Build & Test
- Use the Gradle wrapper (`./gradlew`).
- Build the project: `./gradlew build`.
- Run tests (once added): `./gradlew test`.
- The Boot jar is created as `build/libs/interviewmate.jar` via `./gradlew bootJar`.

## Style
- Kotlin files use 4‑space indentation and end with a newline.
- Keep comments for non‑obvious code and add `TODO` markers for future work.

## Modules
- Currently single module `:interviewmate`.
- Future agents may add entities, controllers and services under `src/main/kotlin`.

## Authentication
- Users register via `POST /api/auth/register` with an email and password. The password is hashed using BCrypt and stored in the `users` table.
- Login is handled at `POST /api/auth/login`. When credentials are valid a JWT is returned in the response body.
- Clients must send this token in the `Authorization` header (`Bearer <token>`) for all protected endpoints.
- Public endpoints: registration, login, `POST /api/questions` and the health check (`GET /actuator/health`). All other API URLs require a valid JWT.
- Tokens encode the user id and subscription status and expire after the configured `jwt.expiration-ms`.
