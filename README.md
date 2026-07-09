<div align="center">

# 🏋️ WorkoutTrackerAPI

**A secure, RESTful backend for planning workouts, browsing exercises, and tracking training progress.**

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.16-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![Status](https://img.shields.io/badge/status-in%20development-yellow?style=for-the-badge)
![Version](https://img.shields.io/badge/version-0.0.1--SNAPSHOT-blue?style=for-the-badge)
![License](https://img.shields.io/badge/license-MIT-green?style=for-the-badge)
[![CI](https://img.shields.io/github/actions/workflow/status/viceda-s/WorkoutTrackerAPI/ci.yml?branch=main&style=for-the-badge&label=CI)](https://github.com/viceda-s/WorkoutTrackerAPI/actions/workflows/ci.yml)

</div>

---

## About

**WorkoutTrackerAPI** is a backend service for a fitness tracking application, built to satisfy the [roadmap.sh Fitness Workout Tracker](https://roadmap.sh/projects/fitness-workout-tracker) project spec. Users register and authenticate, browse a library of exercises filterable by type and muscle group, build and manage workout plans from those exercises with sets/reps/weight, and pull progress reports summarizing completed training volume over a date range. The full API surface is documented and explorable through a live Swagger UI.

The project is deliberately built with **professional-grade habits** rather than tutorial shortcuts: schema changes are version-controlled through Flyway migrations (no `hibernate.ddl-auto=update`), authentication is stateless JWT rather than server-side sessions, passwords are never stored or logged in plaintext, workout data is always scoped to its owner, and the codebase is organized by feature (`auth`, `user`, `exercise`, `workout`, `config`) so each vertical slice — entity → repository → service → controller — can be reasoned about independently.

## Key Features

**Implemented**
- ✅ **User registration** with BCrypt password hashing and duplicate-email rejection
- ✅ **JWT-based authentication** — stateless login issuing a signed bearer token, with a configurable expiry (`jwt.expiration-ms`)
- ✅ **Protected routes** via a custom Spring Security filter chain (`JwtAuthFilter`)
- ✅ **Exercise library** — browse all exercises, or filter by `exerciseType` (`STRENGTH`, `CARDIO`, `FLEXIBILITY`) or `muscleGroup` (`CHEST`, `BACK`, `LEGS`, `SHOULDERS`, `ARMS`, `CORE`)
- ✅ **Workout plan creation & listing** — authenticated users can build a plan from exercises with sets/reps/weight, and list their own plans (optionally filtered by `status`), always scoped to the requesting user
- ✅ **Full workout plan lifecycle** — retrieve, update (replacing its exercises, not appending to them), delete, and transition status (`PLANNED` → `COMPLETED` / `CANCELED`); any plan you don't own is rejected with `404`, whether it doesn't exist or simply isn't yours
- ✅ **Progress reports** — total completed workouts and total training volume (sets × reps × weight) per exercise over a date range, scoped to the caller's own completed plans
- ✅ **Versioned schema migrations** with Flyway, seeded with a starter set of real exercises
- ✅ **Unit test coverage** on the service layer (JUnit 5 + Mockito) for exercises, auth, and workouts
- ✅ **Interactive API documentation** — every endpoint annotated via springdoc-openapi with realistic request/response examples, browsable through Swagger UI
- ✅ **Continuous Integration** — GitHub Actions runs the full test suite, including the Spring context and a real Postgres service container, on every push and pull request to `main`
- ✅ **Rate Limiting** — automated protection utilizing Bucket4j and Caffeine caching, with distinct limits for unauthenticated traffic (10 requests/minute by IP) and authenticated users (100 requests/minute by user identity)
- ✅ **Consistent error responses** — a global exception handler (`GlobalExceptionHandler`) and rate limit filters return uniform RFC 7807 `ProblemDetail` JSON responses for every error, preventing stack trace leaks or Spring's default error page
- ✅ **Production-Ready & Secure** — strict rate limits on public/authenticated traffic (immune to X-Forwarded-For spoofing when deployed behind a trusted proxy configured via Tomcat's `RemoteIpValve`), safe Actuator health probes, disabled Open-In-View (OSIV) for predictable database queries, and zero PII leakage in logs

## Prerequisites

Make sure you have the following installed before setting up the project:

- **Java 21 (JDK)**
- **Docker** and **Docker Compose** (for running PostgreSQL)
- **Git**

> Maven itself isn't required — the project includes the Maven Wrapper (`./mvnw`), which downloads the correct Maven version automatically.

## Installation

1. **Clone the repository**

   ```bash
   git clone https://github.com/viceda-s/WorkoutTrackerAPI.git
   cd WorkoutTrackerAPI
   ```

2. **Configure environment variables**

   Copy the example env file and fill in your own values:

   ```bash
   cp .env.example .env
   ```

   ```dotenv
   DB_NAME=workout_tracker
   DB_USER=changeme
   DB_PASSWORD=changeme
   DB_PORT=5432
   JWT_SECRET=changeme
   ```

   `JWT_SECRET` should be a long, random string — it's used to sign and verify authentication tokens.

3. **Start PostgreSQL**

   ```bash
   docker compose up -d
   ```

4. **Run the application**

   ```bash
   ./mvnw spring-boot:run
   ```

   Flyway applies all pending migrations automatically on startup — no manual migration step needed. The API is now available at `http://localhost:8080`.

5. **Explore the API**

   Every endpoint is documented with realistic examples via Swagger UI:

   ```
   http://localhost:8080/swagger-ui.html
   ```

   The raw OpenAPI spec is available at `http://localhost:8080/v3/api-docs`.

## Usage

All examples below use `curl`. Replace `<token>` with the JWT returned from login.

### Register a new user

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"strongpassword","name":"Jane Doe"}'
```

```json
{ "id": 1, "email": "user@example.com", "name": "Jane Doe" }
```

### Log in

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"strongpassword"}'
```

```json
{ "token": "eyJhbGciOiJIUzI1NiJ9..." }
```

### Call a protected endpoint

```bash
curl http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer <token>"
```

### Browse exercises (public, no auth required)

```bash
curl http://localhost:8080/api/exercises
curl "http://localhost:8080/api/exercises?exerciseType=CARDIO"
curl "http://localhost:8080/api/exercises?muscleGroup=LEGS"
curl http://localhost:8080/api/exercises/1
```

> Note: `exerciseType` and `muscleGroup` are currently applied independently, not combined — if both are supplied, `exerciseType` takes precedence.

### Create a workout plan (authenticated)

```bash
curl -X POST http://localhost:8080/api/workouts \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
        "name": "Push Day",
        "scheduledAt": "2026-07-10T09:00:00Z",
        "exercises": [
          { "exerciseId": 1, "sets": 4, "reps": 8, "weight": 60.0 },
          { "exerciseId": 9, "sets": 3, "reps": 12, "weight": 15.0 }
        ]
      }'
```

```json
{
  "id": 1,
  "name": "Push Day",
  "scheduledAt": "2026-07-10T09:00:00Z",
  "status": "PLANNED",
  "exercises": [
    { "exerciseId": 1, "exerciseName": "Bench Press", "sets": 4, "reps": 8, "weight": 60.0, "orderIndex": 0 },
    { "exerciseId": 9, "exerciseName": "Lateral Raise", "sets": 3, "reps": 12, "weight": 15.0, "orderIndex": 1 }
  ]
}
```

A plan always starts in `PLANNED` status. `exerciseId` must reference an existing exercise, or the request is rejected.

### List your workout plans (authenticated)

```bash
curl http://localhost:8080/api/workouts \
  -H "Authorization: Bearer <token>"

curl "http://localhost:8080/api/workouts?status=PLANNED" \
  -H "Authorization: Bearer <token>"
```

Results are sorted by `scheduledAt` ascending and always scoped to the authenticated caller — you'll never see another user's plans.

### Get a single workout plan (authenticated)

```bash
curl http://localhost:8080/api/workouts/1 \
  -H "Authorization: Bearer <token>"
```

Returns the plan if it belongs to you. If it doesn't exist, or belongs to someone else, the response is `404` either way — the API never reveals that a workout id exists for another user.

### Update a workout plan (authenticated)

```bash
curl -X PUT http://localhost:8080/api/workouts/1 \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
        "name": "Push Day (Heavy)",
        "scheduledAt": "2026-07-10T09:00:00Z",
        "exercises": [
          { "exerciseId": 1, "sets": 5, "reps": 5, "weight": 80.0 }
        ]
      }'
```

The `exercises` list fully **replaces** the plan's existing exercises rather than appending to them.

### Delete a workout plan (authenticated)

```bash
curl -X DELETE http://localhost:8080/api/workouts/1 \
  -H "Authorization: Bearer <token>"
```

Returns `204 No Content` on success.

### Update a workout's status (authenticated)

```bash
curl -X PATCH http://localhost:8080/api/workouts/1/status \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"status":"COMPLETED"}'
```

Valid values are `PLANNED`, `COMPLETED`, and `CANCELED`.

### Get a progress report (authenticated)

```bash
curl "http://localhost:8080/api/workouts/reports?from=2026-07-01T00:00:00Z&to=2026-07-31T23:59:59Z" \
  -H "Authorization: Bearer <token>"
```

```json
{
  "totalCompletedWorkouts": 4,
  "exerciseVolumes": [
    { "exerciseName": "Bench Press", "totalVolume": 1600.0 },
    { "exerciseName": "Squat", "totalVolume": 2400.0 }
  ]
}
```

`from` and `to` are required ISO-8601 timestamps. Only `COMPLETED` plans scheduled within that range count — `PLANNED` and `CANCELED` plans are excluded, and volume is summed per exercise as `sets × reps × weight`.

### Error responses

Every error — whether a business rule rejection (`404`, `409`, `401`, `429`, ...) or a request validation failure — comes back as a consistent RFC 7807 `ProblemDetail` JSON response rather than a raw stack trace or Spring's default error page.

A business-rule error (e.g. requesting a workout that isn't yours):

```json
{
  "type": "about:blank",
  "title": "Not Found",
  "status": 404,
  "detail": "Workout not found",
  "instance": "/api/workouts/1"
}
```

A rate limit error (`429 Too Many Requests`):

```json
{
  "type": "about:blank",
  "title": "Too Many Requests",
  "status": 429,
  "detail": "Too many requests",
  "instance": "/api/workouts"
}
```

A validation failure (e.g. registering with a blank name) additionally includes a `properties` breakdown for field errors:

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Validation failed",
  "instance": "/api/auth/register",
  "fieldErrors": {
    "name": "must not be blank"
  }
}
```

### Running tests

```bash
./mvnw test
```

Tests also run automatically via GitHub Actions on every push and pull request to `main` — see the CI badge at the top of this README, or the [workflow file](.github/workflows/ci.yml) for details.

## Contributing

This started as a personal learning project, but suggestions, bug reports, and pull requests are welcome.

1. Fork the repository and create a feature branch off `main` (e.g. `feature/workout-crud`)
2. Make your changes, following the existing feature-based package structure (`auth`, `user`, `exercise`, `workout`, `config`)
3. Add or update unit tests for any service-layer logic you touch
4. Verify everything passes before opening a PR:

   ```bash
   ./mvnw test
   ```

5. Open a pull request with a clear description of the change and why it's needed

## License

This project is licensed under the [MIT License](LICENSE) — see the `LICENSE` file at the repository root for the full text.
