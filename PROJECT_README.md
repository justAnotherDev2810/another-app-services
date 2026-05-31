# justanotherapp

A Spring Boot 3.3.0 microservice providing RESTful CRUD APIs for **Users** and **Admins** management.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.3.0 |
| Build | Maven 3.9.15 (wrapper) |
| Persistence | Spring Data JPA / Hibernate |
| Database | PostgreSQL |
| Messaging | Apache Kafka |
| Boilerplate | Lombok |
| Testing | JUnit 5 + Spring Boot Test |

## Project Structure

```
justanotherapp/
├── pom.xml                          # Root aggregator POM
├── mvnw / mvnw.cmd                  # Maven wrapper
└── services/
    ├── pom.xml                      # Shared parent POM (dependency management)
    └── another-service/
        ├── pom.xml                  # Leaf microservice POM
        └── src/
            ├── main/
            │   ├── java/com/microservice/justanotherapp/
            │   │   ├── JustanotherappApplication.java
            │   │   ├── controller/   # REST endpoints
            │   │   │   ├── UserController.java
            │   │   │   └── AdminController.java
            │   │   ├── dto/          # Data Transfer Objects
            │   │   │   ├── UserDto.java
            │   │   │   └── AdminDto.java
            │   │   ├── entity/       # JPA entities
            │   │   │   ├── User.java
            │   │   │   └── Admin.java
            │   │   ├── repository/   # Spring Data JPA repositories
            │   │   │   ├── UserRepository.java
            │   │   │   └── AdminRepository.java
            │   │   └── service/      # Business logic
            │   │       ├── UserService.java
            │   │       ├── AdminService.java
            │   │       └── impl/
            │   │           ├── UserServiceImpl.java
            │   │           └── AdminServiceImpl.java
            │   └── resources/
            │       ├── application.properties
            │       └── application.yml
            └── test/
                └── .../JustanotherappApplicationTests.java
```

## API Endpoints

### Users (`/api/user`)
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/user` | List all users |
| GET | `/api/user/{id}` | Get user by ID |
| POST | `/api/user` | Create a user |
| PUT | `/api/user/{id}` | Update a user |
| DELETE | `/api/user/{id}` | Delete a user |

### Admins (`/api/admins`)
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/admins` | List all admins |
| GET | `/api/admins/{id}` | Get admin by ID |
| POST | `/api/admins` | Create an admin |
| PUT | `/api/admins/{id}` | Update an admin |
| DELETE | `/api/admins/{id}` | Delete an admin |

## Configuration

- **Server port:** `8091`
- **Database:** PostgreSQL at `localhost:5432`, database `justanotherapp`
- **JPA:** Hibernate DDL auto-update
- **Kafka:** `localhost:9092`, consumer group `justanotherapp-group`

## Build & Run

```bash
./mvnw clean install
./mvnw spring-boot:run -pl services/another-service
```

## Git

- Remote: `github.com/justAnotherDev2810/another-app-services.git`
- Active branch: `nested-microservice-structure`
