# syntax=docker/dockerfile:1

# ── Global build args ─────────────────────────────────────────
# Must be declared first so Docker resolves them for all stages
ARG JAVA_VERSION=17
ARG SERVICE_PATH
ARG JAR_NAME


# ── Stage 1: base-builder ───────────────────────────────────────
# Builds job-service (shared libs: job-api + job-client) and installs
# them to the local .m2 cache so later stages can reuse them.
FROM eclipse-temurin:${JAVA_VERSION}-jdk AS base-builder
WORKDIR /build

# Copy root + parent poms first (better layer caching)
COPY .mvn .mvn
COPY mvnw mvnw
COPY pom.xml pom.xml
COPY services/pom.xml services/pom.xml

RUN chmod +x mvnw

# Copy ALL service poms (not source) so Maven's reactor can resolve
# the full <modules> list declared in services/pom.xml.
# Without this, Maven fails immediately because it can't find
# another-service/ingestor-service folders even though we don't build them yet.
COPY services/another-service/pom.xml services/another-service/pom.xml
COPY services/ingestor-service/pom.xml services/ingestor-service/pom.xml
COPY services/job-service services/job-service

# Build + install job-service (job-api + job-client) only
# -am also builds any required ancestor modules
RUN ./mvnw install \
    -pl services/job-service/job-api,services/job-service/job-client \
    -am -DskipTests -B


# ── Stage 2: tester ───────────────────────────────────────────────
# Inherits the base-builder image (job-service already installed in .m2)
# Copies in the requested service and runs its tests.
FROM base-builder AS tester
ARG SERVICE_PATH

COPY services/${SERVICE_PATH} services/${SERVICE_PATH}

RUN ./mvnw test \
    -pl services/${SERVICE_PATH} \
    -am -B


# ── Stage 3: builder ───────────────────────────────────────────────
# Packages the requested service jar. Skips tests (already ran in Stage 2).
FROM base-builder AS builder
ARG SERVICE_PATH

COPY services/${SERVICE_PATH} services/${SERVICE_PATH}

RUN ./mvnw package \
    -pl services/${SERVICE_PATH} \
    -am -DskipTests -B


# ── Stage 4: runtime ───────────────────────────────────────────────
# Slim runtime image — just the JRE and the built jar, no Maven/JDK.
FROM eclipse-temurin:${JAVA_VERSION}-jre AS runtime
ARG SERVICE_PATH
ARG JAR_NAME

WORKDIR /app

COPY --from=builder /build/services/${SERVICE_PATH}/target/${JAR_NAME}.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]