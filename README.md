# mysky-backend

[![test](https://github.com/NikiMaisu/mysky-backend/actions/workflows/test.yml/badge.svg)](https://github.com/NikiMaisu/mysky-backend/actions/workflows/test.yml)
[![codecov](https://codecov.io/gh/NikiMaisu/mysky-backend/branch/main/graph/badge.svg)](https://codecov.io/gh/NikiMaisu/mysky-backend)

Spring Boot backend for mysky, an internal scheduling and order-tracking system built for a stretch ceiling installation business. It replaces a spreadsheet-based workflow with a proper API for quoting jobs, scheduling crews, and tracking work through to completion.

## Features

- **JWT authentication** with admin/worker roles; workers can log in with either email or phone number
- **Order management**: clients, multiple material/area line items per order, lighting fixtures, add-ons, optional granite perimeter pricing, manual or auto-generated order numbers
- **Live cost & time calculation** from line items, with an optional custom-price override for negotiated quotes
- **Scheduling**: configurable company-wide work days/hours with per-team overrides, automatic finish-time calculation that rolls over to the next working day, optional manual finish-time override
- **Calendar endpoints** for day/week/month order ranges plus per-day crew availability
- **Role-scoped visibility**: workers only see orders and schedules for their own team, admins see everything
- **Search & filtering** on orders (client name, phone, address, order number, date range, team, status)
- **CSV / XLSX export**
- **Teams & workers management**, with Flyway-versioned schema migrations
- Deployed on Fly.io with scheduled off-platform Postgres backups

## Stack

- Java 21, Spring Boot 3.5
- Spring Web, Security, Data JPA, Validation, Actuator
- PostgreSQL + Flyway migrations
- JJWT for JWT auth
- Testcontainers for integration tests

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

Integration tests use Testcontainers to spin up Postgres in Docker, so Docker must be running.
