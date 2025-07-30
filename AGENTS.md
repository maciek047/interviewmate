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
