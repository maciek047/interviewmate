# InterviewMate

InterviewMate is a Kotlin/Spring Boot backend that generates interview questions from job
descriptions. Users can try it without an account (first three questions free) or subscribe for
full access and future features like CV generation.

## Tech Stack
- Kotlin & Spring Boot
- PostgreSQL
- OpenAI API (pluggable LLM service)
- JWT authentication

## Local Setup
1. Start the database:
   ```
   docker-compose up -d db
   ```
2. Copy `.env.example` to `.env` and set `OPENAI_API_KEY` and `JWT_SECRET`.
   Other values match the defaults in `application.yml`.
3. Run the application:
   ```
   ./gradlew bootRun
   ```
   The service listens on <http://localhost:8080>.

## Running Tests
```
./gradlew test
```
Tests use an in-memory H2 database.

## API Usage
- **Register** – `POST /api/auth/register` with `{ "email": "...", "password": "..." }`
- **Login** – `POST /api/auth/login` returns `{ token, subscriptionStatus }`
- **Generate Questions** – `POST /api/questions` with `{ "jobName": "Dev", "jobDescription": "Desc", "numQuestions": 5 }`
  - Without a token only three questions are returned.
  - Include `Authorization: Bearer <token>` to persist sessions and unlock the full set.
- **Subscribe** – `POST /api/subscription/subscribe` (requires token) to mark the user as subscribed.

## Deployment
### Deploying to Heroku
1. Create an app and database:
   ```
   heroku create <app-name>
   heroku addons:create heroku-postgresql:hobby-dev
   ```
2. Configure required secrets:
   ```
   heroku config:set OPENAI_API_KEY=<your_key>
   heroku config:set JWT_SECRET=<jwt_secret>
   ```
   The Postgres add-on sets `DATABASE_URL` automatically.
3. Deploy:
   ```
   git push heroku main
   ```
   The included `Procfile` and Gradle `stage` task build `interviewmate.jar`.
4. Verify:
   ```
   heroku logs --tail
   heroku open /actuator/health
   ```
This hobby deployment runs a single dyno and is not intended for heavy load. Ensure secrets are kept
out of version control and adjust CORS and other production settings as needed.

## Continuous Integration
Tests run with `./gradlew test`. TODO: add GitHub Actions and coverage badges.

