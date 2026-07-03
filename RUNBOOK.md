
<!--
  Title: justanotherapp — Runbook
  Version: 0.0.1-SNAPSHOT
  Last Updated: 2026-07-03
  Maintainer: justAnotherDev2810
-->

# Runbook: justanotherapp

> Operational guide for running, monitoring, and troubleshooting the justanotherapp microservices ecosystem.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Environment Setup](#environment-setup)
- [Running Locally](#running-locally)
- [Running with Docker Compose](#running-with-docker-compose)
- [Database Migrations](#database-migrations)
- [Kafka & Event Pipeline](#kafka--event-pipeline)
- [Health Checks](#health-checks)
- [Logging & Debugging](#logging--debugging)
- [Common Tasks](#common-tasks)
- [Troubleshooting](#troubleshooting)

---

## Prerequisites

- [ ] Java 17 JDK (`java -version`)
- [ ] Maven 3.9+ (`mvn -version`) — or use `./mvnw`
- [ ] Docker Desktop 24+ (`docker --version`, `docker compose version`)
- [ ] Git (`git --version`)
- [ ] Ports available: `5432`/`5433` (PostgreSQL), `8091` (another-service), `8092` (ingestor-service), `9092`/`29092` (Kafka), `8080` (Kafka UI), `5050` (pgAdmin)

> ⚠️ **Warning:** PostgreSQL port `5433` is mapped externally in `docker-compose.yml` (internal `5432`). Adjust if you already have a local Postgres on `5432`.

## Environment Setup

### Clone & Verify

```bash
git clone git@github.com:justAnotherDev2810/another-app-services.git
cd justanotherapp
git checkout nested-microservice-structure
```

### Check Maven Build

```bash
./mvnw clean install -DskipTests
```

This compiles all modules: `job-api` → `job-client` → `another-service` → `ingestor-service`. Build artifacts land in each module's `target/` directory.

> **First build takes 2-5 minutes** due to dependency downloads. Subsequent builds use the local `.m2` cache.

## Running Locally

### Step 1: Start Infrastructure

```bash
docker compose up -d postgres kafka
```

Wait for health checks to pass:

```bash
docker compose ps
# Both postgres and kafka should show "healthy"
```

### Step 2: Run Database Migrations

```bash
docker compose up flyway-migrations
# Wait for "All migrations completed successfully"
```

> 💡 **Tip:** The migrations container exits after completion. It is not a long-running service.

### Step 3: Start another-service

```bash
./mvnw spring-boot:run -pl services/another-service
```

Expected output:

```
2026-07-03T10:00:00.123Z  INFO [another-service] Started JustanotherappApplication in 4.2 seconds
```

Verify: http://localhost:8091/actuator/health <!-- @check: verify if actuator is enabled -->

### Step 4: Start ingestor-service

In a **separate terminal**:

```bash
./mvnw spring-boot:run -pl services/ingestor-service
```

Expected output:

```
2026-07-03T10:01:00.456Z  INFO [ingestor-service] Started IngestorServiceApplication in 3.8 seconds
```

### Step 5: Smoke Test

```bash
# Create a user directly via another-service
curl -s -X POST http://localhost:8091/api/user \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Test","lastName":"User","username":"smoke1","email":"smoke@test.com","role":"USER"}' | jq .

# Verify it appears
curl -s http://localhost:8091/api/user | jq .
```

## Running with Docker Compose

Start everything (including built services):

```bash
docker compose up --build -d
```

This runs:
1. `postgres` — PostgreSQL 16
2. `pgadmin` — pgAdmin 4 (port 5050)
3. `flyway-migrations` — runs then exits
4. `kafka` — Confluent Kafka 7.6 (single-node KRaft mode)
5. `kafka-ui` — Kafka UI (port 8080)
6. `another-service` — port 8091
7. `ingestor-service` — port 8092

### Monitor

```bash
# Tail all logs
docker compose logs -f

# Tail a specific service
docker compose logs -f another-service

# Check container health
docker compose ps
```

### Rebuild a Single Service

```bash
docker compose build another-service
docker compose up -d another-service
```

### Full Teardown

```bash
docker compose down -v
```

> ⚠️ **Warning:** `-v` removes the PostgreSQL volume. All data is lost.

## Database Migrations

### How Migrations Work

1. The `flyway-migrations` container runs the `migrate.sh` script
2. It iterates over service directories under `database-migrations/migrations/`
3. Each service gets its own **schema** (e.g., `app_schema_users_service`)
4. Flyway applies `.sql` files in version order
5. After completion, the container exits with code 0

### Migration Files

| File | Schema | Table |
|---|---|---|
| `V1__create_users_table.sql` | `app_schema_users_service` | `users` |
| `V2__create_admins_table.sql` | `app_schema_users_service` | `admins` |

### Adding a New Migration

```bash
# Create the file
touch database-migrations/migrations/another-service/V3__add_user_status_column.sql

# Edit with your DDL
echo "ALTER TABLE users ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';" > database-migrations/migrations/another-service/V3__add_user_status_column.sql

# Re-run migrations
docker compose up flyway-migrations
```

> 💡 **Tip:** The `another-service` JPA config uses `ddl-auto: validate`. After applying a migration, ensure your JPA entities match the new schema, or the app will fail to start.

### Connecting Directly to PostgreSQL

```bash
# Via psql in the container
docker compose exec postgres psql -U postgres -d justanotherapp

# Via pgAdmin: http://localhost:5050
# Email: admin@justanotherapp.com
# Password: admin
# Add server: host=postgres, port=5432, user=postgres, password=2810
```

## Kafka & Event Pipeline

### Architecture

```
CloudEvent → ingestor-service (POST /api/events)
                  ↓
          Kafka topic: "user.create"
                  ↓
    ingestor-service (Kafka listener)
                  ↓
          another-service (POST /api/user)
```

### Test the Full Pipeline

```bash
# 1. Send a CloudEvent to the ingestor
curl -X POST http://localhost:8092/api/events \
  -H "Content-Type: application/json" \
  -d '{
    "specversion": "1.0",
    "type": "com.microservice.user.create",
    "source": "/runbook-test",
    "id": "runbook-'$(uuidgen)'",
    "datacontenttype": "application/json",
    "data": {
      "firstName": "Pipeline",
      "lastName": "Test",
      "username": "pipeline-test",
      "email": "pipeline@test.com",
      "role": "USER"
    }
  }'

# 2. Verify the user was created
curl -s http://localhost:8091/api/user | jq '.[] | select(.username=="pipeline-test")'
```

### Kafka UI

Browse topics, messages, and consumer groups at http://localhost:8080.

### Manual Kafka Commands

```bash
# Enter the Kafka container
docker compose exec kafka bash

# List topics
kafka-topics --bootstrap-server localhost:9092 --list

# Consume messages from the topic
kafka-console-consumer --bootstrap-server localhost:9092 \
  --topic user.create --from-beginning

# Produce a raw message
echo '{"firstName":"Raw","lastName":"Message"}' | \
  kafka-console-producer --bootstrap-server localhost:9092 \
    --topic user.create --property "parse.key=true" --property "key.separator=:"
```

## Health Checks

### Service Health Endpoints

| Service | Health Check URL | Expected Status |
|---|---|---|
| another-service | `http://localhost:8091/actuator/health` | `{"status":"UP"}` <!-- @check: add spring-boot-starter-actuator if missing --> |
| ingestor-service | `http://localhost:8092/actuator/health` | `{"status":"UP"}` <!-- @check: add spring-boot-starter-actuator if missing --> |

### Docker Health Checks

```bash
# All containers healthy?
docker compose ps --format "table {{.Name}}\t{{.Status}}"
```

### Connectivity Check

```bash
# Does ingestor reach another-service?
docker compose exec ingestor-service curl -s http://another-service:8091/api/user | head -c 100
```

## Logging & Debugging

### Service Logs

```bash
# Real-time logs
docker compose logs -f another-service

# Last 100 lines with timestamps
docker compose logs --tail=100 -t another-service
```

### Application Logs (Local Run)

Logs appear in `stdout` by default. Configure log levels in `application.yml`:

```yaml
logging:
  level:
    com.microservice: DEBUG
    org.springframework.kafka: DEBUG
    org.hibernate.SQL: DEBUG
```

### Common Log Patterns

| Log Line | Meaning | Action |
|---|---|---|
| `[CONSUMER] Received message from topic 'user.create'` | Kafka message consumed | Normal |
| `[CLIENT] Calling another-service POST /api/user` | Downstream HTTP call initiated | Normal |
| `[PRODUCER] Publishing to topic 'user.create'` | CloudEvent routed to Kafka | Normal |
| `o.h.engine.jdbc.spi.SqlExceptionHelper: ERROR: relation "users" does not exist` | Migration not run | Run `docker compose up flyway-migrations` |
| `Failed to deserialize message` | Invalid JSON payload | Check CloudEvent `data` field |
| `ConnectException: Connection refused: another-service/8091` | Downstream unavailable | Check another-service is running |

## Common Tasks

### Rebuild and Restart a Service

```bash
# Docker
docker compose build another-service
docker compose up -d another-service

# Local (with live reload via spring-boot-devtools)
./mvnw spring-boot:run -pl services/another-service
```

### Reset Everything (Clean Slate)

```bash
docker compose down -v
docker compose up -d postgres kafka
docker compose up flyway-migrations
./mvnw clean install -DskipTests
```

### Run Tests

```bash
# All modules
./mvnw clean test

# Single module
./mvnw test -pl services/another-service

# Single test class
./mvnw test -pl services/another-service -Dtest=JustanotherappApplicationTests
```

### Package Without Tests

```bash
./mvnw clean package -DskipTests
# Jars are at:
#   services/another-service/target/another-service-0.0.1-SNAPSHOT.jar
#   services/ingestor-service/target/ingestor-service-0.0.1-SNAPSHOT.jar
```

## Troubleshooting

### Problem: "Port 5432 already in use"

**Cause:** Local PostgreSQL instance running on host.

**Fix:** Either stop the local Postgres, or change the host port mapping in `docker-compose.yml`:

```yaml
ports:
  - "5433:5432"   # already mapped to 5433 externally
```

Update your local `application.yml` to use the mapped port if connecting from host.

### Problem: "Schema "app_schema_users_service" does not exist"

**Cause:** Flyway migrations haven't been run, or the migration container failed.

**Fix:**

```bash
docker compose up flyway-migrations
```

If it still fails:

```bash
docker compose logs flyway-migrations
```

### Problem: "Kafka connection refused"

**Cause:** Kafka not ready, or the advertised listener is misconfigured.

**Fix:**

```bash
# Ensure Kafka is healthy
docker compose ps kafka

# Check Kafka logs
docker compose logs kafka

# For local runs, ensure bootstrap-servers is localhost:9092
# For Docker runs, it should be kafka:29092 (internal)
```

### Problem: "No topic found for event type"

**Cause:** The CloudEvent `type` field doesn't match any mapping in `kafkaProducer.resolveTopic()`.

**Fix:** Add a new case to the switch statement in `services/ingestor-service/src/main/java/com/microservice/ingestor/kafka/kafkaProducer.java`:

```java
case "com.microservice.user.create" -> kafkaConfig.USER_CREATE_TOPIC;
```

### Problem: "Failed to call another-service" in ingestor logs

**Cause:** `another-service` is not running or unreachable from the ingestor's network.

**Fix:**

```bash
# Verify another-service is up
curl http://localhost:8091/api/user

# In Docker, verify the endpoint env var
docker compose exec ingestor-service env | grep ANOTHER_SERVICE_ENDPOINT
```

### Problem: "User already exists with username"

**Cause:** The `username` or `email` column has a unique constraint and the value already exists.

**Fix:** Use a different username/email, or delete the existing record via the DELETE endpoint.

### Problem: Maven build fails with "Non-resolvable parent POM"

**Cause:** You're building a submodule directly without the parent.

**Fix:** Always build from the root:

```bash
cd justanotherapp
./mvnw clean install -pl services/another-service -am
```

The `-am` flag (also-make) ensures parent modules are built first.

<!--
  Key decisions for RUNBOOK structure:
  1. Prerequisites checkbox list — ensures nothing is missed before starting
  2. Step-by-step local run with explicit "expected output" — confirms success at each stage
  3. Docker section separated — different workflow, different audience
  4. Common pool of knowledge: DB migrations, Kafka pipeline, health checks
  5. Log pattern table — preempts "what does this log line mean?" questions
  6. Troubleshooting section ordered by probability of occurrence
-->

