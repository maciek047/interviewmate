# InterviewMate Backend

This repository contains the Kotlin/Spring Boot backend for InterviewMate. It exposes a REST endpoint to generate interview questions using the OpenAI API.

## API

### `POST /api/questions`

Request body:
```json
{
  "jobName": "Software Engineer",
  "jobDetails": "Detailed job description..."
}
```

Response body:
```json
{
  "questions": ["Question 1", "Question 2", ...]
}
```

Set the `OPENAI_API_KEY` environment variable before running the application.

## Running locally

```
./gradlew :app:bootRun
```

## Deploying to Heroku

A `Procfile` and `system.properties` are provided for deployment on the Heroku Java buildpack.
