# AGENTS.md

## Project overview

Multi-module Maven project (Spring Boot 4.0.6, Java 25, Spring Cloud 2025.1.1).
Event-sourcing demo: `user-service` (JPA + MySQL) publishes CRUD events to Kafka;
`event-service` (Cassandra) consumes and stores them. Both use Spring Cloud Stream.
`end-to-end-test` module runs Testcontainers-based integration tests that spin up
all services as Docker containers.

## Modules and entrypoints

| Module | Port | Entrypoint | DB |
|---|---|---|---|
| `user-service` | 9080 | `UserServiceApplication` | MySQL |
| `event-service` | 9081 | `EventServiceApplication` | Cassandra |
| `end-to-end-test` | — | Test-only module, no main class | — |

Avro classes for `event-service` are generated from
`event-service/src/main/resources/avro/userevent-message.avsc` into
`event-service/src/main/java`.

## Key commands

```bash
# Build/test a single module
./mvnw clean test --projects user-service
./mvnw clean test --projects event-service

# End-to-end tests (requires Docker images built first)
./mvnw clean test --projects end-to-end-test -DargLine="-Dspring.profiles.active=test"
./mvnw clean test --projects end-to-end-test -DargLine="-Dspring.profiles.active=test,avro"

# Run a service locally with Maven
./mvnw clean spring-boot:run --projects user-service
./mvnw clean spring-boot:run --projects user-service -Dspring-boot.run.profiles=avro
./mvnw clean spring-boot:run --projects event-service

# Code formatting (Spotless / google-java-format)
./mvnw spotless:check
./mvnw spotless:apply
# Formatting is enforced during `verify` phase

# Avro code generation (re-generates Java classes from .avsc)
./mvnw compile --projects event-service

# Build Docker images (uses spring-boot:build-image, skips tests)
./build-docker-images.sh
```

## Infrastructure

Start via `docker compose up -d`. Containers: MySQL 9.6.0, Cassandra 5.0.8,
ZooKeeper, Kafka 7.9.5, Schema Registry, Zipkin, plus Kafka UI tools.

## Profiles

- `default` — JSON serialization to Kafka
- `avro` — Avro serialization (user-service only uses it; event-service auto-detects)
- `test` — disables Zipkin tracing, enables `ddl-auto: create-drop` (user-service)

E2E tests use `-Dspring.profiles.active=test` (JSON) or `test,avro` (Avro).

The binding destination is `com.ivanfranchin.userservice.user` (Kafka topic).
user-service output binding: `users-out-0`. event-service input binding: `users-in-0`
with consumer group `eventServiceGroup`.

## Testing quirks

- user-service unit tests use `MySQLTestcontainers` (MySQL container with
  `@ServiceConnection`) and `spring-cloud-stream-test-binder` (no real Kafka).
- event-service unit tests use `CassandraTestcontainers` (Cassandra container with
  `@ServiceConnection`) and `spring-cloud-stream-test-binder`.
- E2E tests start **real Docker containers** for all services (MySQL, Cassandra,
  Kafka, Schema Registry, user-service, event-service) via `AbstractTestcontainers`.
  They connect through Docker network `SHARED`. user-service and event-service Docker
  images must exist locally (build with `./build-docker-images.sh`).
- E2E startup timeout is 2 minutes (`STARTUP_TIMEOUT`). Awaitility polls every 1s
  with a 10s at-most for async event assertions.

## Docker scripts

| Script | Purpose |
|---|---|
| `build-docker-images.sh` | Builds `ivanfranchin/user-service:1.0.0` and `ivanfranchin/event-service:1.0.0` |
| `start-apps.sh` | Runs both Docker containers on `docker compose` network (arg: `avro` for Avro profile) |
| `stop-apps.sh` | Stops both containers |
| `remove-docker-images.sh` | Removes both Docker images |

## Conventions

- Spotless (google-java-format) enforced at `verify` phase. Run `spotless:apply` to auto-fix.
- Lombok used in both services (annotation processors configured per-module).
- EditorConfig: 2-space YAML/Properties, 4-space XML, LF line endings.
- Swagger UI at `/swagger-ui.html` (SpringDoc OpenAPI 3.0.3).
- Kafka internal port for Docker-to-Docker communication is 9092; host mapping is 29092.
