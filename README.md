
<!--
  Title: justanotherapp
  Version: 0.0.1-SNAPSHOT
  Last Updated: 2026-07-03
  Maintainer: justAnotherDev2810
-->

# justanotherapp

[![Java](https://img.shields.io/badge/Java-17-%23ED8B00?logo=openjdk)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.0-%236DB33F?logo=springboot)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.9.15-%23C71A36?logo=apachemaven)](https://maven.apache.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-%234169E1?logo=postgresql)](https://www.postgresql.org/)
[![Kafka](https://img.shields.io/badge/Kafka-7.6.0-%23231F20?logo=apachekafka)](https://kafka.apache.org/)
[![Flyway](https://img.shields.io/badge/Flyway-10.10.0-%23CC0200?logo=flyway)](https://flywaydb.org/)

> Event-driven microservices ecosystem for managing **Users** and **Admins** via REST APIs, with CloudEvents-based ingestion through Apache Kafka.

## Architecture Overview

```
                 ┌──────────────┐
                 │   External   │
                 │   Client     │
                 └──────┬───────┘
                        │ CloudEvent (POST /api/events)
                        ▼
            ┌───────────────────────┐
            │   ingestor-service    │  port 8092
            │  (CloudEvents → Kafka)│
            └────────┬──────────────┘
                     │ Kafka topic: "user.create"
                     ▼
    ┌───────────────────────────────────────┐
    │           Kafka Broker                │
    │  (Confluent CP 7.6, single-node)      │
    └────────┬──────────────────────────────┘
             │ consume
             ▼
    ┌───────────────────────┐
    │   ingestor-service    │  (Kafka listener)
    │  (WebClient → REST)   │
    └────────┬──────────────┘
             │ HTTP POST /api/user
             ▼
    ┌───────────────────────────────────────┐
    │         another-service              │  port 8091
    │  REST CRUD · JPA · PostgreSQL        │
    └───────────────────────────────────────┘
```

## Services

| Service | Port | Type | Description |
|---|---|---|---|
| `another-service` | `8091` | Runnable | REST CRUD API for Users & Admins. PostgreSQL persistence via JPA/Hibernate. Kafka producer & consumer. |
| `ingestor-service` | `8092` | Runnable | Accepts CloudEvents (`POST /api/events`), publishes to Kafka topics, consumes Kafka messages, calls downstream services via `WebClient`. |
| `job-service` | — | POM aggregator | Shared library parent. Not runnable. |
| `job-api` | — | Library | Shared DTOs and data contracts (`UserDto`, `User` entity). |
| `job-client` | — | Library | Feign client interfaces for downstream service calls. |

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.3.0 |
| Build | Maven 3.9.15 (wrapper) |
| Persistence | Spring Data JPA / Hibernate |
| Database | PostgreSQL 16 |
| Migrations | Flyway 10.10.0 (per-service schemas) |
| Messaging | Apache Kafka (Confluent CP 7.6) |
| HTTP Client | WebClient (Spring WebFlux, non-blocking) |
| API Clients | OpenFeign 4.1.1 |
| Documentation | SpringDoc OpenAPI 2.5.0 (Swagger UI) |
| Boilerplate | Lombok 1.18.32 |
| Testing | JUnit 5 + Spring Boot Test |

## API Endpoints

### `another-service` — Users (`/api/user`)

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/user` | List all users |
| GET | `/api/user/{id}` | Get user by ID |
| POST | `/api/user` | Create a user |
| PUT | `/api/user/{id}` | Update a user |
| DELETE | `/api/user/{id}` | Delete a user |

### `another-service` — Admins (`/api/admins`)

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/admins` | List all admins |
| GET | `/api/admins/{id}` | Get admin by ID |
| POST | `/api/admins` | Create an admin |
| PUT | `/api/admins/{id}` | Update an admin |
| DELETE | `/api/admins/{id}` | Delete an admin |

### `ingestor-service` — Events (`/api/events`)

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/events` | Ingest a CloudEvent (`com.microservice.user.create` → topic `user.create`) |

> ⚠️ **Note:** The `ingestor-service` event endpoint is defined internally but not exposed as a controller class in the current codebase. <!-- @check: verify the exact event endpoint path in the ingestor controller -->

## Project Structure

```
justanotherapp/
├── pom.xml                              # Root aggregator POM
├── mvnw / mvnw.cmd                      # Maven wrapper (3.9.15)
├── Dockerfile                           # Multi-stage build (base-builder → tester → builder → runtime)
├── docker-compose.yml                   # Full local dev environment
├── database-migrations/
│   ├── Dockerfile                       # Flyway container
│   ├── config/
│   │   ├── flyway-base.conf             # Shared Flyway configuration
│   │   ├── dev.properties               # Dev env overrides
│   │   └── prod.properties              # Prod env overrides (env vars only)
│   ├── migrations/
│   │   └── another-service/
│   │       ├── V1__create_users_table.sql
│   │       └── V2__create_admins_table.sql
│   └── scripts/
│       └── migrate.sh                   # Entrypoint — runs migrations per-service
├── services/
│   ├── pom.xml                          # Parent POM (dependency management)
│   ├── another-service/                 # Core REST API (port 8091)
│   │   ├── pom.xml
│   │   └── src/main/java/com/microservice/justanotherapp/
│   │       ├── controller/              # REST endpoints
│   │       ├── dto/                     # Data Transfer Objects
│   │       ├── entity/                  # JPA entities
│   │       ├── exception/               # Global exception handling
│   │       ├── repository/              # Spring Data JPA repositories
│   │       └── service/                 # Business logic + impl
│   ├── ingestor-service/                # Event ingestion (port 8092)
│   │   ├── pom.xml
│   │   └── src/main/java/com/microservice/ingestor/
│   │       ├── config/                  # Kafka topic configuration
│   │       ├── dto/                     # CloudEvent DTO
│   │       ├── kafka/                   # Producer + Consumer
│   │       └── client/                  # WebClient downstream callers
│   └── job-service/                     # Shared libraries (not runnable)
│       ├── pom.xml
│       ├── job-api/                     # Shared DTOs
│       └── job-client/                  # Feign client interfaces
└── pgadmin/
    └── servers.json                     # Pre-configured pgAdmin connection
```

## Configuration

### Environment Variables

| Variable | Default | Description |
|---|---|---|
| `SERVER_PORT` | `8091` | `another-service` HTTP port |
| `POSTGRES_URL` | `jdbc:postgresql://localhost:5432/justanotherapp` | PostgreSQL JDBC URL |
| `POSTGRES_USERNAME` | `postgres` | DB user |
| `POSTGRES_PASSWORD` | `2810` | DB password |
| `POSTGRES_SCHEMA` | — | Schema name (set in docker-compose to `app_schema_users_service`) |
| `SPRING_APPLICATION_NAME` | `another-service` | Spring app name |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka broker address |
| `ANOTHER_SERVICE_ENDPOINT` | `http://localhost:8091` | Base URL for `another-service` (used by ingestor) |

## Prerequisites

- [Java 17+](https://adoptium.net/)
- [Maven 3.9+](https://maven.apache.org/) (or use the bundled `mvnw`)
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) 24+
- [Git](https://git-scm.com/)

## Quick Start

```bash
# 1. Clone the repository
git clone git@github.com:justAnotherDev2810/another-app-services.git
cd justanotherapp

# 2. Start infrastructure (PostgreSQL, Kafka, Flyway migrations)
docker compose up -d postgres kafka flyway-migrations

# 3. Build all services
./mvnw clean install -DskipTests

# 4. Start another-service
./mvnw spring-boot:run -pl services/another-service

# 5. (In a separate terminal) Start ingestor-service
./mvnw spring-boot:run -pl services/ingestor-service
```

Once running:

- **Swagger UI (another-service):** http://localhost:8091/swagger-ui.html
- **Swagger UI (ingestor-service):** http://localhost:8092/swagger-ui.html
- **pgAdmin:** http://localhost:5050 (email: `admin@justanotherapp.com`, password: `admin`)
- **Kafka UI:** http://localhost:8080

## Docker (Full Stack)

```bash
# Build and start all services
docker compose up --build -d

# Check logs
docker compose logs -f another-service ingestor-service

# Stop everything
docker compose down -v
```

The multi-stage `Dockerfile` builds each service independently using build args:
- `SERVICE_PATH` — relative path under `services/` (e.g., `another-service`)
- `JAR_NAME` — the artifact name (e.g., `another-service-0.0.1-SNAPSHOT`)

## Database Migrations

Migrations are managed by [Flyway](https://flywaydb.org/) with per-service schemas:

| Schema | Service | Migrations |
|---|---|---|
| `app_schema_users_service` | `another-service` | `V1__create_users_table.sql`, `V2__create_admins_table.sql` |

Run migrations locally (requires Docker):

```bash
docker compose up flyway-migrations
```

> ⚠️ **Warning:** The `another-service` JPA config uses `ddl-auto: validate`. Schema changes must go through Flyway migrations — never let Hibernate auto-create tables in production.

## Error Handling

`another-service` uses a `@RestControllerAdvice` (`GlobalExceptionHandler`) that returns structured error responses:

```json
{
  "status": 400,
  "message": "User already exists with username: jdoe",
  "timestamp": 1689876543210
}
```

Custom exception classes:
- `GenericException` — maps to HTTP 400
- `ResponseStatusException` — maps to HTTP 404 (not found)
- Unhandled exceptions — maps to HTTP 500

## Development

### IDE Setup

1. Open the root `pom.xml` in IntelliJ IDEA as a project
2. Ensure Lombok annotation processing is enabled:
   - `Settings > Build > Compiler > Annotation Processors > Enable annotation processing`
3. Install the Lombok plugin (bundled in IntelliJ 2023+)

### Testing

```bash
# Run all tests
./mvnw test

# Run tests for a specific service
./mvnw test -pl services/another-service
```

### Kafka Locally

Test the event pipeline manually:

```bash
# Publish a CloudEvent to ingestor-service
curl -X POST http://localhost:8092/api/events \
  -H "Content-Type: application/json" \
  -d '{
    "specversion": "1.0",
    "type": "com.microservice.user.create",
    "source": "/connect-flow/user",
    "id": "evt-001",
    "datacontenttype": "application/json",
    "data": {
      "firstName": "John",
      "lastName": "Doe",
      "username": "jdoe",
      "email": "jdoe@example.com",
      "role": "USER"
    }
  }'
```

<!--
  Key decisions for README structure:
  1. Architecture diagram first — the most important thing is understanding how services connect
  2. Badges for quick tech-stack recognition at a glance
  3. Service table to disambiguate runnable vs library modules
  4. Full project tree with intent annotations
  5. Quick Start before Docker — developers want to run locally first
  6. Docker Full Stack section separate — it's a different workflow
  7. Explicit Kafka test curl to make the event pipeline tangible
  8. Error handling section preempts "how does this service fail?" questions
-->

