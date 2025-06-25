# InterviewMate Backend

This repository contains a minimal Kotlin Spring Boot backend for InterviewMate. It exposes endpoints to generate interview questions and CVs using the OpenAI API.

## Requirements
- Java 17+
- Gradle
- OpenAI API key set in the `OPENAI_API_KEY` environment variable

## Running locally
```bash
./gradlew bootRun
```

Endpoints:
- `POST /generate-cv` – body `{ "jobName": "<role>" }`
- `POST /generate-questions` – body `{ "jobName": "<role>", "jobDetails": "<description>" }`

The server binds to port defined by `$PORT` (default `8080`). Heroku will automatically provide this variable.

## Deploying to Heroku
Build the project and push the jar with a `Procfile`.
```
./gradlew clean build
```

Heroku will run the jar using the command in `Procfile`.
