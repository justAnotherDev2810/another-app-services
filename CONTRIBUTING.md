
<!--
  Title: justanotherapp — Contributing Guide
  Version: 0.0.1-SNAPSHOT
  Last Updated: 2026-07-03
  Maintainer: justAnotherDev2810
-->

# Contributing to justanotherapp

> Developer onboarding guide, architecture deep-dive, and codebase navigation.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Development Setup](#development-setup)
- [Architecture Deep-Dive](#architecture-deep-dive)
- [Key Code Areas](#key-code-areas)
- [Coding Conventions](#coding-conventions)
- [Maven Module Dependency Graph](#maven-module-dependency-graph)
- [Adding a New Service](#adding-a-new-service)
- [Adding a New Entity / Endpoint](#adding-a-new-entity--endpoint)
- [Adding a New Event Type](#adding-a-new-event-type)
- [Testing Strategy](#testing-strategy)
- [Pull Request Process](#pull-request-process)
- [Things to Check Out](#things-to-check-out)

---

## Code of Conduct

This project adheres to the [Contributor Covenant Code of Conduct](https://www.contributor-covenant.org/version/2/1/code_of_conduct/). Be respectful, constructive, and inclusive. Report unacceptable behavior to the maintainers.

## Development Setup

### IDE Configuration (IntelliJ IDEA)

1. **Open the project:** `File > Open >` select the root `pom.xml` — IntelliJ auto-imports modules.
2. **Lombok annotation processing:**
   - `Settings > Build, Execution, Deployment > Compiler > Annotation Processors`
   - Check `Enable annotation processing`
3. **Run configurations:** Create two Spring Boot run configs:
   - `another-service`: Main class `com.microservice.justanotherapp.JustanotherappApplication`, active profile (none)
   - `ingestor-service`: Main class `com.microservice.ingestor.IngestorServiceApplication`, active profile (none)
4. **Checkstyle / formatting:** The project does not yet enforce a formatter. Align with the existing style (4-space indents, no tabs).

### Git Workflow

```bash
# Branch naming convention
git checkout -b feature/your-feature-name
git checkout -b fix/your-bugfix-name
git checkout -b chore/your-chore-name

# Keep your branch up to date
git fetch origin nested-microservice-structure
git rebase origin/nested-microservice-structure
```

> 💡 **Tip:** Use rebase over merge to keep a linear history. Never rebase shared branches.

### First-Time Build

```bash
./mvnw clean install -DskipTests
```

This compiles the dependency order correctly. If you need to skip tests during active development, use `-DskipTests`. Use `-Dmaven.test.failure.ignore=true` to keep building when tests fail.

---

## Architecture Deep-Dive

### Module Dependency Graph

```
justanotherapp (root POM)
  └── services (parent POM)
        ├── job-api                 ← Shared DTOs (plain jar, no deps)
        │     └── depends on: Lombok, Jackson
        ├── job-client              ← Feign interfaces (plain jar)
        │     └── depends on: job-api, OpenFeign
        ├── another-service         ← Runnable Spring Boot app
        │     └── depends on: job-api (*), Spring Web, JPA, Kafka, PostgreSQL
        └── ingestor-service        ← Runnable Spring Boot app
              └── depends on: job-api, job-client, Spring Web, WebFlux, Kafka, OpenFeign
```

(*) `another-service` imports `job-api` DTOs `<!-- @check: verify another-service actually declares job-api dependency in its pom.xml -->`

### `another-service` — REST CRUD Layer

```
Controller (@RestController)
    │  Maps HTTP ↔ DTO, delegates to Service interface
    ▼
Service Interface         ← Contract (e.g., UserService)
    │
    ▼
ServiceImpl (@Service, @Transactional)
    │  Business logic: validation, mapping, exception handling
    ▼
Repository (Spring Data JPA)
    │  ORM persistence, custom queries
    ▼
Entity (@Entity, @Table)  ← JPA entity mapped to DB table
```

**Key pattern:** DTOs are never passed to repositories. Services convert DTO → Entity, then Entity → DTO via `toEntity()` / `fromEntity()` static methods.

### `ingestor-service` — Event Pipeline

```
HTTP POST /api/events
    │  CloudEvent JSON payload
    ▼
@RestController (?)
    │  <!-- @check: the controller for /api/events is not present in the current source -->
    ▼
kafkaProducer.publish()
    │  Extracts "type" → resolves Kafka topic (e.g., "com.microservice.user.create" → "user.create")
    │  Extracts "data" → serializes to JSON string
    ▼
Kafka topic "user.create"
    │
    ▼
kafkaConsumer.onUserCreate()
    │  Deserializes JSON → UserDto
    ▼
AnotherServiceClient.createUser()   ← WebClient HTTP POST to another-service
    ▼
another-service POST /api/user
```

### Job Service — Shared Libraries

`job-service` is a POM aggregator packaging **two plain jars** (no Spring Boot plugin, not executable):

- **`job-api`:** Shared DTOs (`UserDto`) and entity types used across service boundaries. No Spring dependencies — just Lombok + Jackson.
- **`job-client`:** Feign client interfaces for calling downstream services. Depends on `job-api` for DTO types.

**Why a separate module?** Avoids circular dependencies between `another-service` and `ingestor-service`. Both can depend on `job-api` without coupling to each other.

### Database Schema Strategy

- **Per-service schemas:** Each microservice gets its own PostgreSQL schema (e.g., `app_schema_users_service`)
- **Migrations via Flyway:** All DDL changes go through versioned SQL files
- **JPA `ddl-auto: validate`:** Hibernate never creates or alters tables — it only validates entities match the existing schema
- **No cross-schema queries:** Services never query another service's tables. All cross-service communication is via HTTP/Kafka

---

## Key Code Areas

These are the most important files to study first:

### `another-service`

| File | Why It Matters |
|---|---|
| `controller/UserController.java` (`services/another-service/src/.../controller/UserController.java`) | REST layer — see how HTTP concerns are handled |
| `service/impl/UserServiceImpl.java` (`services/another-service/src/.../service/impl/UserServiceImpl.java`) | Business logic with `@Transactional`, exception handling, DTO↔Entity mapping |
| `entity/User.java` (`services/another-service/src/.../entity/User.java`) | JPA entity with `@PrePersist` lifecycle callback |
| `exception/GlobalExceptionHandler.java` (`services/another-service/src/.../exception/GlobalExceptionHandler.java`) | Centralized error handling — `@RestControllerAdvice` |
| `application.yml` (`services/another-service/src/main/resources/application.yml`) | All external configuration in one place |

### `ingestor-service`

| File | Why It Matters |
|---|---|
| `kafka/kafkaProducer.java` (`services/ingestor-service/src/.../kafka/kafkaProducer.java`) | CloudEvent → Kafka topic routing logic |
| `kafka/kafkaConsumer.java` (`services/ingestor-service/src/.../kafka/kafkaConsumer.java`) | Kafka listener → downstream HTTP call |
| `client/AnotherServiceClient.java` (`services/ingestor-service/src/.../client/AnotherServiceClient.java`) | WebClient-based downstream HTTP client |
| `config/kafkaConfig.java` (`services/ingestor-service/src/.../config/kafkaConfig.java`) | Kafka topic bean definitions |
| `dto/CloudEventDto.java` (`services/ingestor-service/src/.../dto/CloudEventDto.java`) | CloudEvents 1.0 spec DTO with raw `JsonNode` data |

### `job-service`

| File | Why It Matters |
|---|---|
| `job-api/.../dto/UserDto.java` (`services/job-service/job-api/.../dto/UserDto.java`) | Shared DTO — contracts across service boundaries |
| `job-client/.../client/AnotherServiceClient.java` (`services/job-service/job-client/.../client/AnotherServiceClient.java`) | Feign client interface <!-- @check: this file name conflicts with ingestor's AnotherServiceClient — verify which is actually used --> |

### Infrastructure

| File | Why It Matters |
|---|---|
| `Dockerfile` | Multi-stage build: base-builder → tester → builder → runtime |
| `docker-compose.yml` | Full dev environment topology |
| `database-migrations/scripts/migrate.sh` | Per-schema Flyway migration orchestrator |
| `database-migrations/config/flyway-base.conf` | Flyway config with placeholder-based schema switching |

---

## Coding Conventions

### Java

- Use **Lombok**: `@Data` for DTOs, `@Getter`/`@Setter` for entities, `@RequiredArgsConstructor` for DI, `@Builder` for constructors, `@Slf4j` for logging
- Use **constructor injection** with `final` fields (no `@Autowired` on fields)
- Use **`@Transactional`** at the service layer, never at the controller
- DTOs have `toEntity()` and static `fromEntity()` methods — never expose entities to controllers
- Use **`ResponseStatusException`** for expected HTTP errors, `GenericException` for business rule violations
- Use **WebClient** for HTTP calls (non-blocking, Spring Boot 3.x standard)
- Use **switch expressions** (Java 17+) for matching patterns

### Naming

| Element | Convention | Example |
|---|---|---|
| Classes | PascalCase | `UserServiceImpl` |
| Methods | camelCase | `findAll()` |
| Constants | UPPER_SNAKE_CASE | `USER_CREATE_TOPIC` |
| Packages | lowercase, dotted | `com.microservice.ingestor.kafka` |
| Tables | snake_case, plural | `users`, `admins` |
| Columns | snake_case | `user_name`, `created_at` |
| Topics | lowercase, dotted | `user.create` |
| CloudEvent types | reverse-DNS | `com.microservice.user.create` |

### Testing

- Test classes end with `Test` suffix (e.g., `UserServiceTest`)
- Use `@SpringBootTest` for integration tests, plain JUnit 5 for unit tests
- Use `@MockBean` for mocking dependencies in integration tests
- Aim for: unit test service logic, integration test the full Spring context

---

## Adding a New Service

1. **Create the module directory:**
   ```bash
   mkdir -p services/new-service/src/main/java/com/microservice/newservice
   mkdir -p services/new-service/src/main/resources
   mkdir -p services/new-service/src/test/java/com/microservice/newservice
   ```

2. **Add `pom.xml`:**
   - Parent: `com.microservice:service-parent:0.0.1-SNAPSHOT`
   - Dependency on `job-api` if consuming shared DTOs
   - Dependency on `job-client` if calling another service via Feign

3. **Register in parent POM:**
   ```xml
   <!-- services/pom.xml -->
   <module>new-service</module>
   ```

4. **Add to Docker Compose:**
   - Service definition with build args (`SERVICE_PATH`, `JAR_NAME`)
   - Depends on: `postgres` (if using DB), `kafka` (if using Kafka)
   - Environment variables for config

5. **Add migration directory:**
   ```bash
   mkdir -p database-migrations/migrations/new-service
   ```

6. **Add to `migrate.sh`:** Uncomment or add the Flywall execution block for the new schema.

## Adding a New Entity / Endpoint

1. **Write the Flyway migration:** `V3__create_teams_table.sql`
2. **Create the JPA entity:** `entity/Team.java` (with `@Entity`, `@Table`, `@PrePersist`)
3. **Create the repository:** `repository/TeamRepository.java` (extend `JpaRepository`)
4. **Create the DTO:** `dto/TeamDto.java` (with `toEntity()` / `fromEntity()`)
5. **Create the service interface + impl:** `service/TeamService.java` + `service/impl/TeamServiceImpl.java`
6. **Create the controller:** `controller/TeamController.java` (with `@RequestMapping("/api/teams")`)
7. **If the DTO is shared:** Move it to `job-api`, update both `pom.xml` files

> ⚠️ **Warning:** JPA `ddl-auto` is `validate`. After running the migration, ensure your entity matches the table exactly, or the app won't start.

## Adding a New Event Type

1. **Define the topic:** Add a constant in `ingestor-service/.../config/kafkaConfig.java`:
   ```java
   public static final String TEAM_CREATE_TOPIC = "team.create";
   ```

2. **Create the topic bean:**
   ```java
   @Bean
   public NewTopic teamCreateTopic() {
       return TopicBuilder.name(TEAM_CREATE_TOPIC).partitions(1).replicas(1).build();
   }
   ```

3. **Map the CloudEvent type:** Add a case in `kafkaProducer.resolveTopic()`:
   ```java
   case "com.microservice.team.create" -> kafkaConfig.TEAM_CREATE_TOPIC;
   ```

4. **Add a listener:** Create a new method in `kafkaConsumer` (or a new consumer class) with `@KafkaListener(topics = kafkaConfig.TEAM_CREATE_TOPIC)`

5. **Create the downstream client** if calling another service:
   - Add a new WebClient caller (or reuse `AnotherServiceClient`)

## Testing Strategy

### Current Coverage

- `another-service` has a single `@SpringBootTest` that verifies the context loads (`JustanotherappApplicationTests.java`)
- No dedicated unit tests exist for service, controller, or repository layers

### Where Tests Should Be Added

| Layer | Test Type | What to Cover |
|---|---|---|
| Service | Unit test with mocked repository | Business logic, DTO↔Entity mapping, exception paths |
| Controller | `@WebMvcTest` | HTTP mapping, request/response serialization, validation |
| Repository | `@DataJpaTest` | Custom queries, entity mapping |
| Kafka | `@EmbeddedKafkaTest` | Producer sends correct topic, consumer deserializes correctly |
| Integration | `@SpringBootTest` + Testcontainers | Full pipeline: CloudEvent → Kafka → another-service |

### Running Tests

```bash
# All tests
./mvnw clean test

# Module-specific
./mvnw test -pl services/another-service
```

> 💡 **Tip:** Kafka tests require a running Kafka broker or `@EmbeddedKafka`. The `spring-kafka-test` dependency is already declared in both runnable service POMs.

## Pull Request Process

1. **Create a feature branch** from `nested-microservice-structure`
2. **Make your changes** following the conventions above
3. **Run all tests:** `./mvnw clean verify`
4. **Build Docker images:** `docker compose build`
5. **Commit with a descriptive message:**
   ```
   feat(another-service): add teams CRUD endpoints
   
   - Adds Team entity, repository, service, and controller
   - Flyway migration V3 for teams table
   - OpenAPI docs available at /swagger-ui.html
   
   Closes #123
   ```
6. **Push and open a PR** against `nested-microservice-structure`
7. **Ensure CI passes** (once configured)
8. **Request review** from at least one maintainer

### PR Checklist

- [ ] Tests pass locally
- [ ] New migrations are backward-compatible (no DROP or destructive ALTER)
- [ ] New endpoints are documented in Swagger/OpenAPI
- [ ] Configuration is externalized (no hardcoded values)
- [ ] Logging is added for key operations (create, update, delete)
- [ ] Error handling covers expected failure modes
- [ ] Docker Compose updated if new service / dependency added

---

## Things to Check Out

As you explore the codebase, pay special attention to these areas:

### 1. Feign vs WebClient — Two Client Implementations

There are **two** classes named `AnotherServiceClient`:

- `ingestor-service/.../client/AnotherServiceClient.java` — uses Spring WebFlux `WebClient` (blocking call)
- `job-service/job-client/.../client/AnotherServiceClient.java` — uses OpenFeign `@FeignClient` (declarative)

This is a transitional state. The ingestor was written first with WebClient; the `job-client` module was added later for Feign-based consumption. **Decide on one approach and consolidate.** <!-- @decision: pick Feign or WebClient as the standard -->

### 2. `AdminRepository` — Partial JPA Override

`AdminRepository.java` in `another-service` explicitly declares `findAll()`, `findById()`, `save()`, `deleteById()`, and `existsById()` methods that are already provided by `JpaRepository`. These are redundant and should be removed. <!-- @cleanup: drop explicit method declarations from AdminRepository -->

### 3. Missing Event Controller

The `ingestor-service` `application.yaml` implies there should be a `POST /api/events` endpoint, but no `@RestController` class is present in the source tree. The `kafkaProducer` is wired to receive `CloudEventDto` objects but nothing calls it from the HTTP layer. <!-- @gap: implement the event ingestion REST controller -->

### 4. Schema Consistency

The `user_name` column in `User.java` entity field uses `getUserName()` / `setUserName()`, while the DTO field is `username`. The mapping `user.getUserName() ↔ dto.username` works but is inconsistent. <!-- @cleanup: align entity field name with DTO field name -->

### 5. `@EnableFeignClients` Scanning

`IngestorServiceApplication` enables Feign clients with `basePackages = "com.microservice.job.client"`. The `job-client` module's `AnotherServiceClient` is a Feign interface. However, the `ingestor-service` currently uses a `WebClient`-based `AnotherServiceClient` instead of the Feign one. <!-- @decision: switch to the Feign client or remove the dependency on job-client -->

### 6. Docker Compose Host Port Conflicts

PostgreSQL external port is `5433`, not the default `5432`. This avoids conflicts with local Postgres but means any tool connecting from the host must use port `5433`. If you change this, update all references.

### 7. Flyway Migration for Admins

`V2__create_admins_table.sql` creates the `admins` table with a `username` column (lowercase, no underscore). The `Admin` JPA entity maps to field `username`. However, the `User` entity uses `user_name` (with underscore). Be deliberate about naming conventions for new entities.

<!--
  Key decisions for CONTRIBUTING structure:
  1. Architecture deep-dive with module dependency graph — essential for understanding the "why" behind the module split
  2. "Key Code Areas" table with file paths and reasons — fastest path to productivity for new devs
  3. "Things to Check Out" section exposes known tech debt and inconsistencies — honest documentation builds trust
  4. Procedural guides (adding service, entity, event type) — answers "how do I extend this?" without asking
  5. PR checklist — operationalizes quality standards
  6. Coding conventions table — quick reference, not prose
-->

