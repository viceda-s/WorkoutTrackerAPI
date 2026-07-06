<div align="center">

# 🏋️ WorkoutTrackerAPI

**A secure, RESTful backend for planning workouts, browsing exercises, and tracking training progress.**

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.16-6DB33F?logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)
![Status](https://img.shields.io/badge/status-in%20development-yellow)
![Version](https://img.shields.io/badge/version-0.0.1--SNAPSHOT-blue)
![License](https://img.shields.io/badge/license-unlicensed-lightgrey)

</div>

---

## About

**WorkoutTrackerAPI** is a backend service for a fitness tracking application, built to satisfy the [roadmap.sh Fitness Workout Tracker](https://roadmap.sh/projects/fitness-workout-tracker) project spec. Users register and authenticate, browse a library of exercises filterable by type and muscle group, and — as the remaining milestones land — will be able to build workout plans, log completed sessions, and pull progress reports over time.

The project is deliberately built with **professional-grade habits** rather than tutorial shortcuts: schema changes are version-controlled through Flyway migrations (no `hibernate.ddl-auto=update`), authentication is stateless JWT rather than server-side sessions, passwords are never stored or logged in plaintext, and the codebase is organized by feature (`auth`, `user`, `exercise`, `config`) so each vertical slice — entity → repository → service → controller — can be reasoned about independently.

## Key Features

**Implemented**
- ✅ **User registration** with BCrypt password hashing and duplicate-email rejection
- ✅ **JWT-based authentication** — stateless login issuing a signed bearer token
- ✅ **Protected routes** via a custom Spring Security filter chain (`JwtAuthFilter`)
- ✅ **Exercise library** — browse all exercises, or filter by `exerciseType` (`STRENGTH`, `CARDIO`, `FLEXIBILITY`) or `muscleGroup` (`CHEST`, `BACK`, `LEGS`, `SHOULDERS`, `ARMS`, `CORE`)
- ✅ **Versioned schema migrations** with Flyway, seeded with a starter set of real exercises
- ✅ **Unit test coverage** on the service layer (JUnit 5 + Mockito) for exercises and auth

**Planned** — see [Roadmap](#roadmap)
- 🚧 Workout plan CRUD (create/list/update/delete, owned per-user)
- 🚧 Workout status tracking (`PLANNED` / `COMPLETED` / `CANCELED`)
- 🚧 Progress reports (completed volume per exercise over a date range)
- 🚧 OpenAPI/Swagger documentation

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

### Running tests

```bash
./mvnw test
```

## Roadmap

- [ ] `POST /api/workouts` — create a workout plan (exercises + sets/reps/weight), scoped to the authenticated user
- [ ] `GET /api/workouts` / `GET /api/workouts/{id}` — list/retrieve own workout plans, with ownership enforcement
- [ ] `PUT /api/workouts/{id}` / `DELETE /api/workouts/{id}` — update/delete own plans
- [ ] `PATCH /api/workouts/{id}/status` — transition a plan between `PLANNED`, `COMPLETED`, `CANCELED`
- [ ] `GET /api/workouts/reports` — progress reports (total completed workouts and volume per exercise over a date range)
- [ ] OpenAPI/Swagger UI via `springdoc-openapi`
- [ ] CI pipeline for automated build/test on push

## Contributing

This started as a personal learning project, but suggestions, bug reports, and pull requests are welcome.

1. Fork the repository and create a feature branch off `main` (e.g. `feature/workout-crud`)
2. Make your changes, following the existing feature-based package structure (`auth`, `user`, `exercise`, `config`)
3. Add or update unit tests for any service-layer logic you touch
4. Verify everything passes before opening a PR:

   ```bash
   ./mvnw test
   ```

5. Open a pull request with a clear description of the change and why it's needed

## License

This project does not yet have a license file, so all rights are reserved by default. If you plan to reuse or fork this code, please open an issue to ask about terms. A permissive license such as [MIT](https://choosealicense.com/licenses/mit/) is a common choice for portfolio/learning projects like this one — once selected, it will be added here and as a `LICENSE` file at the repository root.
