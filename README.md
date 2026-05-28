# mysky-backend

Spring Boot backend for mysky — internal order tracking for a stretch ceiling installation business.

## Stack

- Java 21, Spring Boot 3.5
- Spring Web, Security, Data JPA, Validation, Actuator
- PostgreSQL + Flyway migrations
- JJWT for JWT auth
- Testcontainers for integration tests

## Package layout

```
ge.mysky.backend
├── domain      // JPA entity classes
├── repository  // Spring Data JPA repositories
├── service     // business logic (incl. OrderCalculationService)
├── controller  // REST endpoints
├── dto         // request / response objects
└── config      // security, CORS, JWT config
```

## Running locally

1. Start Postgres:
   ```sh
   docker compose up -d postgres
   ```
2. Run the app:
   ```sh
   ./mvnw spring-boot:run
   ```
   The app listens on `http://localhost:8089`. Health check: `/actuator/health`.

## Environment variables

| Variable             | Default                                        | Notes                                     |
|----------------------|------------------------------------------------|-------------------------------------------|
| `DATABASE_URL`       | `jdbc:postgresql://localhost:5433/mysky`       | JDBC URL (host port 5433, see docker-compose) |
| `DATABASE_USER`      | `mysky`                                        |                                           |
| `DATABASE_PASSWORD`  | `mysky`                                        |                                           |
| `JWT_SECRET`         | dev placeholder                                | **Must** be overridden in production      |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:4000`                      | Comma-separated list                      |

## Tests

```sh
./mvnw test
```

Integration tests use Testcontainers to spin up Postgres in Docker — Docker must be running.
