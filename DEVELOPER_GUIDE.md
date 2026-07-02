# AI-Powered Performance Analysis Platform — Developer Guide

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Complete Technology Stack](#2-complete-technology-stack)
3. [Architecture Overview](#3-architecture-overview)
4. [Project Layout](#4-project-layout)
5. [Infrastructure Deep-Dive](#5-infrastructure-deep-dive)
   - [Docker & Docker Compose](#51-docker--docker-compose)
   - [PostgreSQL](#52-postgresql)
   - [Apache Kafka & Zookeeper](#53-apache-kafka--zookeeper)
   - [Redis](#54-redis)
   - [OpenRouter (AI Provider)](#55-openrouter-ai-provider)
6. [Module Reference](#6-module-reference)
   - [shared/common-lib](#61-sharedcommon-lib)
   - [discovery-server](#62-discovery-server)
   - [config-server](#63-config-server)
   - [api-gateway](#64-api-gateway)
   - [user-service](#65-user-service)
   - [report-service](#66-report-service)
   - [analysis-service](#67-analysis-service)
   - [notification-service](#68-notification-service)
7. [Build System (Maven)](#7-build-system-maven)
8. [Key Framework Details](#8-key-framework-details)
   - [Spring Boot 4.x Auto-configuration](#81-spring-boot-4x-auto-configuration)
   - [Spring Cloud Config](#82-spring-cloud-config)
   - [Spring Cloud Netflix Eureka](#83-spring-cloud-netflix-eureka)
   - [Spring Cloud Gateway MVC](#84-spring-cloud-gateway-mvc)
   - [Spring Data JPA + Hibernate 7](#85-spring-data-jpa--hibernate-7)
   - [Flyway Database Migrations](#86-flyway-database-migrations)
   - [Spring Kafka](#87-spring-kafka)
   - [Spring Security 7](#88-spring-security-7)
   - [JJWT 0.12.5](#89-jjwt-0125)
   - [MapStruct 1.6](#810-mapstruct-16)
   - [Lombok](#811-lombok)
   - [SpringDoc OpenAPI / Swagger UI](#812-springdoc-openapi--swagger-ui)
   - [Spring RestClient](#813-spring-restclient)
9. [End-to-End Flow](#9-end-to-end-flow)
10. [Security Architecture](#10-security-architecture)
11. [Event-Driven Flow (Kafka)](#11-event-driven-flow-kafka)
12. [Configuration Management](#12-configuration-management)
13. [Database Design](#13-database-design)
14. [Code Structure (Clean Architecture)](#14-code-structure-clean-architecture)
15. [Running Locally (IntelliJ)](#15-running-locally-intellij)
16. [Running via Docker Compose](#16-running-via-docker-compose)
17. [Port Reference](#17-port-reference)
18. [API Reference](#18-api-reference)

---

## 1. Project Overview

This platform allows users to upload performance test reports (JMeter, Gatling) and automatically receive AI-generated analysis identifying bottlenecks, error rates, and optimisation recommendations.

**Core capability:**
> Upload a load test report → AI reads it → Returns structured findings with severity, description, and actionable recommendations → User is notified

---

## 2. Complete Technology Stack

### Application

| Category | Technology | Version | Purpose |
|---|---|---|---|
| Language | Java | 21 (LTS) | All services |
| Framework | Spring Boot | 4.1.0 | Application container and auto-configuration |
| Cloud Framework | Spring Cloud | 2025.1.2 | Service discovery, config, gateway |
| Web Layer | Spring Web MVC (`spring-boot-starter-webmvc`) | 7.0.8 | REST controllers, servlet stack |
| Security | Spring Security | 7.1.0 | Authentication, authorisation, filter chain |
| JWT | JJWT (io.jsonwebtoken) | 0.12.5 | HS256 token generation and validation |
| Persistence | Spring Data JPA | 4.1.0 | Repository abstraction |
| ORM | Hibernate ORM | 7.4.1.Final | JPA implementation |
| Connection Pool | HikariCP | 7.0.2 | Database connection pooling |
| Schema Migration | Flyway | 12.4.0 | Database migrations |
| Mapping | MapStruct | 1.6.0 | Compile-time DTO and entity mappers |
| Boilerplate | Lombok | 1.18.46 | `@Data`, `@Builder`, `@Slf4j`, etc. |
| API Docs | SpringDoc OpenAPI | 2.8.8 | Swagger UI generation |
| Validation | Hibernate Validator | 9.1.0 | Bean validation (`@Valid`, `@NotNull`) |
| JSON | Jackson | 2.21.4 | Serialisation/deserialisation |
| HTTP Client | Spring RestClient | (Spring Web 7.0.8) | AI API calls to OpenRouter |

### Infrastructure

| Component | Technology | Version | Purpose |
|---|---|---|---|
| Service Registry | Netflix Eureka Server/Client | 2.0.6 | Service discovery (`lb://service-name`) |
| Config Server | Spring Cloud Config Server | 5.0.4 | Centralised configuration |
| API Gateway | Spring Cloud Gateway Server MVC | 5.0.2 | Routing, JWT validation, request enrichment |
| Load Balancer | Spring Cloud LoadBalancer | 5.0.2 | Client-side load balancing for gateway routing |
| Database | PostgreSQL | 16 | Relational data store (4 separate databases) |
| Messaging | Apache Kafka | 4.2.1 (clients) | Async event streaming between services |
| Coordination | Apache Zookeeper | bundled with Confluent 7.6.1 | Kafka broker coordination |
| Cache | Redis | 7.2 | User profile cache (Lettuce client 7.5.2) |
| Containers | Docker | Latest | Service containerisation |
| Orchestration | Docker Compose | v3.9 | Multi-container local development |

### AI / External

| Component | Technology | Purpose |
|---|---|---|
| AI Provider | OpenRouter | Proxies to Claude, GPT-4, Llama, etc. |
| API Protocol | OpenAI-compatible REST | `/v1/chat/completions` endpoint |
| Default Model | `anthropic/claude-3.5-sonnet` | Configurable via `AI_MODEL` env var |

---

## 3. Architecture Overview

```
                         ┌─────────────────────────────┐
                         │      API Gateway :8080       │
                         │  spring-cloud-gateway-server │
                         │         -webmvc              │
                         │  JWT validation / routing    │
                         │  Correlation ID injection    │
                         └──────────────┬──────────────┘
                                        │ lb://service-name (Eureka)
               ┌────────────────────────┼────────────────────────────┐
               │                        │                            │
     ┌─────────▼──────┐     ┌──────────▼──────┐      ┌─────────────▼──────┐
     │ user-service   │     │ report-service  │      │ analysis-service   │
     │     :8081      │     │     :8082       │      │      :8083         │
     │ JWT auth       │     │ File upload     │      │ OpenRouter AI call │
     │ Redis cache    │     │ Kafka producer  │      │ Kafka consumer     │
     │ BCrypt         │     │ File storage    │      │ Kafka producer     │
     └────────────────┘     └────────┬────────┘      └────────────────────┘
                                     │                        │
                              Kafka topic:             Kafka topic:
                              report-uploaded         analysis-completed
                                     │                        │
                                     └──────────┬────────────┘
                                                │
                                   ┌────────────▼──────────┐
                                   │ notification-service  │
                                   │        :8084          │
                                   │ Kafka consumer        │
                                   │ In-app feed API       │
                                   └───────────────────────┘

═══════════════════ Infrastructure Layer ═══════════════════

  ┌──────────────────┐   ┌─────────────────────┐
  │ discovery-server │   │   config-server      │
  │  Eureka :8761    │   │   Spring Config :8888│
  │  All services    │   │   Serves config YAMLs│
  │  register here   │   │   from config-repo/  │
  └──────────────────┘   └─────────────────────┘

  ┌──────────────────┐   ┌─────────────────────┐   ┌──────────────────┐
  │  PostgreSQL :5432│   │   Kafka :9092        │   │   Redis :6379    │
  │  4 databases:    │   │   (ext: 29092)       │   │   User profile   │
  │  user_db         │   │   Zookeeper :2181    │   │   cache, 30min   │
  │  report_db       │   │   Topics:            │   │   TTL            │
  │  analysis_db     │   │   - report-uploaded  │   │                  │
  │  notification_db │   │   - analysis-        │   │                  │
  └──────────────────┘   │     completed        │   └──────────────────┘
                         └─────────────────────┘

  ┌──────────────────────────────────────────────────┐
  │  OpenRouter API (external)                        │
  │  https://openrouter.ai/api/v1/chat/completions   │
  │  Routes to Claude/GPT-4/Llama models             │
  └──────────────────────────────────────────────────┘
```

---

## 4. Project Layout

```
AI-Powered Performance Analysis Platform/
│
├── shared/
│   └── common-lib/              ← Shared JAR: DTOs, exceptions, JwtUtil, filters
│       ├── pom.xml              ← groupId: com.platform, artifactId: common-lib, version: 1.0.0
│       └── src/main/java/com/platform/common/
│
├── discovery-server/            ← Netflix Eureka server
├── config-server/               ← Spring Cloud Config server
│   └── src/main/resources/
│       └── config-repo/         ← All service YAML configs live here
├── api-gateway/                 ← Spring Cloud Gateway MVC
├── user-service/                ← Auth + profiles
├── report-service/              ← File upload + metrics
├── analysis-service/            ← AI analysis
├── notification-service/        ← Event-driven notifications
│
└── docker/
    ├── docker-compose.yml       ← Full stack orchestration
    ├── .env                     ← Environment variables (passwords, API keys)
    └── init-db.sh               ← Creates 4 PostgreSQL databases on first run
```

Each service directory is an **independent Maven project** (standalone `pom.xml` with `spring-boot-starter-parent`). There is no root parent POM — each module builds and runs independently.

---

## 5. Infrastructure Deep-Dive

### 5.1 Docker & Docker Compose

**File:** `docker/docker-compose.yml`  
**Format:** Compose v3.9

All infrastructure services run as Docker containers. The Compose file defines 9 services:

| Service | Image | Host Port | Container Port | Purpose |
|---|---|---|---|---|
| `postgres` | `postgres:16-alpine` | 5432 | 5432 | Primary database |
| `zookeeper` | `confluentinc/cp-zookeeper:7.6.1` | — | 2181 | Kafka coordination |
| `kafka` | `confluentinc/cp-kafka:7.6.1` | 29092 | 9092 | Event streaming |
| `redis` | `redis:7.2-alpine` | 6379 | 6379 | Cache |
| `discovery-server` | Built from `discovery-server/Dockerfile` | 8761 | 8761 | Eureka registry |
| `config-server` | Built from `config-server/Dockerfile` | 8888 | 8888 | Config server |
| `api-gateway` | Built from `api-gateway/Dockerfile` | 8080 | 8080 | Gateway |
| `user-service` | Built from `user-service/Dockerfile` | 8081 | 8081 | Auth service |
| `report-service` | Built from `report-service/Dockerfile` | 8082 | 8082 | Upload service |
| `analysis-service` | Built from `analysis-service/Dockerfile` | 8083 | 8083 | AI service |
| `notification-service` | Built from `notification-service/Dockerfile` | 8084 | 8084 | Notification feed |

**Docker Networks:**  
All containers share a single bridge network `platform-net`. Services communicate using their container names as hostnames (e.g., `postgres`, `kafka`, `redis`).

**Docker Volumes:**

| Volume | Mounted by | Purpose |
|---|---|---|
| `postgres_data` | `postgres` container | Persists PostgreSQL data across restarts |
| `redis_data` | `redis` container | Persists Redis data (optional) |
| `report_uploads` | `report-service` (read/write), `analysis-service` (read-only) | Shared file storage for uploaded reports |

The `report_uploads` volume is a critical cross-service dependency: report-service writes uploaded files here, and analysis-service must read from the same location. Without this shared volume, the AI service would fail to access the uploaded files.

**Health Checks:**  
Infrastructure containers declare health checks so Spring services only start after dependencies are ready:

```yaml
postgres:
  healthcheck:
    test: ["CMD-SHELL", "pg_isready -U postgres"]
    interval: 10s
    timeout: 5s
    retries: 5

kafka:
  healthcheck:
    test: ["CMD-SHELL", "kafka-topics --bootstrap-server kafka:9092 --list"]
    interval: 30s
    timeout: 10s
    retries: 5
    start_period: 30s

redis:
  healthcheck:
    test: ["CMD", "redis-cli", "ping"]
    interval: 10s
    timeout: 3s
    retries: 5
```

Spring services use `depends_on: condition: service_healthy` to enforce startup order.

**Service startup dependency chain:**
```
postgres (healthy)  ──┐
kafka    (healthy)  ──┼──▶ discovery-server ──▶ config-server ──▶ all business services
redis    (healthy)  ──┘
zookeeper           ──▶ kafka
```

**Environment file:** `docker/.env`  
Contains all secrets and configuration values injected into containers:

```dotenv
# Database
POSTGRES_USER=postgres
POSTGRES_PASSWORD=admin          # Password set when container is FIRST created
DB_HOST=postgres                 # Container hostname (Docker DNS)
DB_USERNAME=postgres
DB_PASSWORD=admin                # Must match POSTGRES_PASSWORD

# JWT signing key (base64-encoded 256-bit HMAC-SHA256)
JWT_SECRET=bXlTdXBlclNlY3JldEtleUZvckpXVFNpZ25pbmdXaXRoSFMyNTZBbGdvcml0aG0xMjM0NTY3ODk=

# Config Server
CONFIG_PASSWORD=config-secret
CONFIG_SERVER_HOST=config-server

# Eureka
EUREKA_HOST=discovery-server

# Kafka (internal Docker network address)
KAFKA_BOOTSTRAP_SERVERS=kafka:9092

# Redis
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=                  # Empty = no password

# AI (you must supply your own key)
OPENROUTER_API_KEY=your-openrouter-api-key-here
AI_MODEL=anthropic/claude-3.5-sonnet

# File storage (path inside containers)
UPLOAD_DIR=/app/uploads
```

> **Important:** `POSTGRES_PASSWORD` only takes effect when the container is **first created**. If the container already exists with a different password, you must destroy the volume (`docker compose down -v`) and recreate it.

---

### 5.2 PostgreSQL

**Image:** `postgres:16-alpine`  
**Port:** 5432  
**Configuration:** Configured entirely through Docker environment variables — no custom `postgresql.conf` is used.

**Database initialisation — `docker/init-db.sh`:**  
This script is mounted into the container at `/docker-entrypoint-initdb.d/init-db.sh`. PostgreSQL's official Docker image automatically runs all `.sh` and `.sql` files in this directory on **first container initialisation**.

```bash
#!/bin/bash
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE user_db;
    CREATE DATABASE report_db;
    CREATE DATABASE analysis_db;
    CREATE DATABASE notification_db;
EOSQL
```

This creates four isolated databases — one per service. Each service connects to only its own database.

**Connection URL pattern (from config-repo YAMLs):**
```
jdbc:postgresql://${DB_HOST:localhost}:5432/user_db
```

**Connection pool (HikariCP):** Spring Boot auto-configures HikariCP as the default connection pool. Default settings (min 10, max 10 connections) are used — tunable via `spring.datasource.hikari.*`.

**Local development note:** If a local PostgreSQL installation is also running on port 5432, it intercepts connections before Docker. In this case, create the four databases in the local PostgreSQL instance and ensure its password matches `DB_PASSWORD`.

---

### 5.3 Apache Kafka & Zookeeper

**Kafka image:** `confluentinc/cp-kafka:7.6.1`  
**Zookeeper image:** `confluentinc/cp-zookeeper:7.6.1`  
**Kafka internal port:** 9092 (used by services inside Docker network)  
**Kafka external port:** 29092 (used by services running locally on host — e.g., IntelliJ)

**Listener configuration:**
```yaml
KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092,PLAINTEXT_HOST://localhost:29092
KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
```

- `PLAINTEXT://kafka:9092` — advertised to services inside Docker (use `kafka:9092`)
- `PLAINTEXT_HOST://localhost:29092` — advertised to clients on the host machine (use `localhost:29092`)

**Topic configuration:**
```yaml
KAFKA_AUTO_CREATE_TOPICS_ENABLE: "true"    # Topics created on first publish
KAFKA_NUM_PARTITIONS: 3                     # 3 partitions per topic (parallelism)
KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1  # Single broker — no replication
```

**Topics created automatically:**

| Topic | Partitions | Producer | Consumer Group | Purpose |
|---|---|---|---|---|
| `report-uploaded` | 3 | report-service | `analysis-service-group` | Triggers AI analysis |
| `analysis-completed` | 3 | analysis-service | `notification-service-group` | Triggers notifications |

**Producer serialisation (all producers):**
```yaml
spring:
  kafka:
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      properties:
        spring.json.add.type.headers: false   # Don't embed Java class name in message headers
```

`add.type.headers: false` is critical — without it, Kafka embeds the producer's fully-qualified class name. Since producer and consumer are in different services with different package names, this would break deserialization.

**Consumer deserialisation (per service):**
```yaml
# analysis-service consumer
spring:
  kafka:
    consumer:
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.value.default.type: com.platform.analysis.domain.event.ReportUploadedEvent
        spring.json.trusted.packages: com.platform.analysis.domain.event
```

Each consuming service declares its own local `event` record class that matches the JSON shape of the published event. There is no shared event schema JAR — services are decoupled by JSON contract.

---

### 5.4 Redis

**Image:** `redis:7.2-alpine`  
**Port:** 6379  
**Authentication:** No password (open access) — suitable for local development  
**Persistence:** AOF/RDB persistence is enabled by default via the `redis_data` volume  
**Client library:** Lettuce 7.5.2.RELEASE (Spring Boot's default — async, reactive-capable)

**Usage in this project:** Only `user-service` uses Redis, for caching user profiles:

```java
// Key pattern: "user:{userId}"
// TTL: 30 minutes
// Serialiser: GenericJackson2JsonRedisSerializer (stores JSON with @class type hint)
redisTemplate.opsForValue().set("user:" + userId, userResponse, Duration.ofMinutes(30));
```

Cache is evicted on profile update or user deactivation. On cache miss, the user is fetched from PostgreSQL and re-cached.

**Redis configuration in `user-service` (from `user-service.yml` via config-server):**
```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
```

**`RedisConfig.java` (user-service):** Defines a `RedisTemplate<String, Object>` bean with:
- Key serialiser: `StringRedisSerializer`
- Value serialiser: `GenericJackson2JsonRedisSerializer` (JSON with `@class` type embedding)

---

### 5.5 OpenRouter (AI Provider)

**Base URL:** `https://openrouter.ai/api/v1`  
**Protocol:** OpenAI-compatible REST API  
**Authentication:** Bearer token (`Authorization: Bearer <OPENROUTER_API_KEY>`)  
**Endpoint used:** `POST /chat/completions`

**How it works:** OpenRouter is a gateway that proxies requests to multiple AI model providers (Anthropic Claude, OpenAI GPT-4, Meta Llama, Mistral, etc.). By sending requests in OpenAI's format, you can switch between models by changing only the `model` field.

**Request format:**
```json
{
  "model": "anthropic/claude-3.5-sonnet",
  "messages": [{ "role": "user", "content": "<prompt>" }],
  "temperature": 0.3,
  "max_tokens": 4096
}
```

**Response format (standard OpenAI structure):**
```json
{
  "choices": [{
    "message": {
      "role": "assistant",
      "content": "[{\"severity\":\"HIGH\",\"category\":\"Response Time\",...}]"
    }
  }]
}
```

**Configuration (from `analysis-service.yml`):**
```yaml
spring:
  ai:
    openai:
      api-key: ${OPENROUTER_API_KEY}
      base-url: https://openrouter.ai/api/v1
      chat:
        options:
          model: ${AI_MODEL:anthropic/claude-3.5-sonnet}
          temperature: 0.3
          max-tokens: 4096
```

The `spring.ai.*` properties are used only to store config values — Spring AI library itself is NOT used (it's incompatible with Spring Boot 4.x). The `RestClient` reads these properties via `@Value`.

**`AiConfig.java` creates the HTTP client:**
```java
@Bean
public RestClient openRouterRestClient(
        @Value("${spring.ai.openai.base-url}") String baseUrl,
        @Value("${spring.ai.openai.api-key}") String apiKey) {
    return RestClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader("Authorization", "Bearer " + apiKey)
            .defaultHeader("Content-Type", "application/json")
            .build();
}
```

**Report content handling:** Files are truncated to 15,000 characters before being sent to the AI to stay within model context limits and manage API costs.

---

## 6. Module Reference

### 6.1 `shared/common-lib`

**Purpose:** Shared library JAR. Every service depends on it. Provides cross-cutting concerns consistently across all services.

**Maven coordinates:** `com.platform:common-lib:1.0.0`  
**Parent:** `spring-boot-starter-parent:4.1.0`

**What it contains:**

| Package | Class | Purpose |
|---|---|---|
| `dto` | `ApiResponse<T>` | Standard JSON wrapper: `{success, message, data, timestamp}` |
| `dto` | `PageResponse<T>` | Pagination: `{content, page, size, totalElements, totalPages}` |
| `dto` | `ErrorResponse` | Error body with field-level validation errors |
| `exception` | `BaseException` | Abstract root exception (HTTP status + error code) |
| `exception` | `BusinessException` | 400-range domain rule violation |
| `exception` | `ResourceNotFoundException` | 404 entity not found |
| `exception` | `UnauthorizedException` | 401 access denied |
| `audit` | `AuditableEntity` | JPA `@MappedSuperclass`: `createdAt`, `updatedAt`, `createdBy`, `updatedBy`, `@Version` |
| `security` | `JwtUtil` | JWT generate / validate / extract (JJWT 0.12.5, HS256) |
| `security` | `JwtProperties` | `@ConfigurationProperties(prefix="jwt")` |
| `util` | `CorrelationIdUtil` | ThreadLocal `X-Correlation-ID` holder |
| `web` | `CorrelationIdFilter` | Servlet filter — reads or generates correlation ID |
| `web` | `GlobalExceptionHandlerBase` | Abstract `@RestControllerAdvice` |
| `config` | `PlatformCommonAutoConfiguration` | `@AutoConfiguration` — registers `CorrelationIdFilter` |

**Auto-configuration:** `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` lists `PlatformCommonAutoConfiguration`. Spring Boot's auto-configuration mechanism scans this file at startup and registers the correlation filter in every service automatically — zero manual wiring.

**Building:** Must be installed to local Maven repository before any service can build:
```powershell
cd shared/common-lib
mvn install -DskipTests
```
This installs `common-lib-1.0.0.jar` to `~/.m2/repository/com/platform/common-lib/1.0.0/`.

---

### 6.2 `discovery-server`

**Port:** 8761  
**Spring Boot parent:** 4.1.0  
**Spring Cloud:** 2025.1.2  
**Key dependency:** `spring-cloud-starter-netflix-eureka-server`

**Purpose:** Netflix Eureka service registry. All services register themselves here on startup. The API gateway uses Eureka to resolve `lb://user-service` to an actual IP:port at request time.

**Key configuration:**
```yaml
eureka:
  instance:
    hostname: ${EUREKA_HOSTNAME:localhost}
  client:
    register-with-eureka: false   # The server itself doesn't register
    fetch-registry: false
  server:
    wait-time-in-ms-when-sync-empty: 0
    enable-self-preservation: false    # Don't hold stale instances
    renewal-percent-threshold: 0.49
```

**Eureka dashboard:** `http://localhost:8761` — shows all registered services, their IP:port, and health status.

**Must start FIRST** before any other service.

---

### 6.3 `config-server`

**Port:** 8888  
**Spring Boot parent:** 4.1.0  
**Spring Cloud:** 2025.1.2  
**Key dependencies:** `spring-cloud-config-server`, `spring-cloud-starter-netflix-eureka-client`, `spring-boot-starter-security`

**Purpose:** Centralised configuration server. Serves YAML files from its `config-repo/` directory over HTTP. All services fetch their configuration from here on startup. This avoids distributing secrets (DB passwords, JWT keys) across every service.

**Configuration storage mode:** `native` (filesystem) — no Git required:
```yaml
spring:
  profiles:
    active: native
  cloud:
    config:
      server:
        native:
          search-locations: classpath:/config-repo
```

**Security:** HTTP Basic Auth protects all config endpoints:
```yaml
spring:
  security:
    user:
      name: config
      password: ${CONFIG_PASSWORD:config-secret}
```

**Config files served (location: `config-server/src/main/resources/config-repo/`):**

| File | Consumed by | Key properties served |
|---|---|---|
| `application.yml` | All services (shared defaults) | Common logging, actuator settings |
| `api-gateway.yml` | api-gateway | Route definitions, JWT secret, port 8080 |
| `user-service.yml` | user-service | DB URL, Redis, JWT config, port 8081 |
| `report-service.yml` | report-service | DB URL, Kafka, upload dir, port 8082 |
| `analysis-service.yml` | analysis-service | DB URL, Kafka, OpenRouter, port 8083 |
| `notification-service.yml` | notification-service | DB URL, Kafka, port 8084 |

**How services fetch config:** Every service's `application.yml` contains:
```yaml
spring:
  config:
    import: "optional:configserver:http://config:${CONFIG_PASSWORD:config-secret}@${CONFIG_SERVER_HOST:localhost}:8888"
```

The `optional:` prefix means if config server is unreachable, the service continues with its local defaults. Config server properties override local defaults (higher precedence).

**Must start SECOND** (after discovery-server, before business services).

---

### 6.4 `api-gateway`

**Port:** 8080  
**Spring Boot parent:** 4.1.0  
**Spring Cloud:** 2025.1.2  
**Key dependencies:** `spring-cloud-starter-gateway-server-webmvc`, `spring-boot-starter-security`, `spring-cloud-starter-config`, `spring-cloud-starter-netflix-eureka-client`, JJWT, common-lib

**Purpose:** Single entry point for all external traffic. Validates JWTs, enriches requests with user identity, routes to downstream services.

**Key filter classes:**

| Class | Type | Order | Purpose |
|---|---|---|---|
| `CorrelationIdGatewayFilter` | `OncePerRequestFilter` | `HIGHEST_PRECEDENCE` | Reads/generates `X-Correlation-ID`, injects into request and response |
| `JwtAuthenticationFilter` | `OncePerRequestFilter` | Before `UsernamePasswordAuthenticationFilter` | Validates JWT, injects `X-User-ID` + `X-User-Role` via `MutableHttpServletRequest` |

**`MutableHttpServletRequest`:** A `HttpServletRequestWrapper` subclass that allows adding new headers. Spring Cloud Gateway MVC reads headers from this wrapper when proxying, so downstream services receive the injected identity headers.

**`GatewayConfig.java`:** Creates `JwtUtil` bean using `JwtProperties` (bound from `jwt.*` config):
```java
@Bean
public JwtUtil jwtUtil(JwtProperties jwtProperties) {
    return new JwtUtil(jwtProperties.getSecret(), ...);
}
```

**Route configuration (in `api-gateway.yml`, served by config-server):**
```yaml
spring:
  cloud:
    gateway:
      mvc:
        routes:
          - id: user-service-auth
            uri: lb://user-service
            predicates:
              - Path=/api/auth/**
```

**Important:** Uses `spring.cloud.gateway.mvc.routes` (MVC gateway namespace), NOT `spring.cloud.gateway.routes` (reactive gateway namespace). Using the wrong key causes routes to be silently ignored.

**Load balancing:** The `lb://` URI prefix triggers Spring Cloud LoadBalancer, which resolves the service name against Eureka to get an actual `host:port`. This means the gateway does NOT need hardcoded service URLs — services are found dynamically.

---

### 6.5 `user-service`

**Port:** 8081  
**Database:** `user_db` (PostgreSQL)  
**Cache:** Redis (user profile, 30-min TTL)  
**Key dependencies:** JPA, Security, Redis, JJWT, MapStruct, Flyway, SpringDoc, common-lib

**Tables:** `users`, `refresh_tokens`

**Key classes:**

| Layer | Class | Responsibility |
|---|---|---|
| Controller | `AuthController` | `/api/auth/register`, `/login`, `/refresh`, `/logout` |
| Controller | `UserController` | `/api/users/me` (GET/PUT), admin endpoints |
| Service | `AuthServiceImpl` | Register (BCrypt hash + token pair), login (BCrypt verify), token refresh (rotation), logout |
| Service | `UserServiceImpl` | Profile read (Redis cache-first), update, deactivate |
| Security | `JwtTokenProvider` | Wraps `JwtUtil` — access token (15 min), refresh token (7 days) |
| Security | `JwtAuthenticationFilter` | Validates `Authorization: Bearer` header, sets `SecurityContextHolder` |
| Security | `CustomUserDetailsService` | `UserDetailsService` — loads user by email for Spring Security |
| Config | `RedisConfig` | `RedisTemplate<String,Object>` with JSON serialiser |
| Config | `JpaConfig` | `@EnableJpaAuditing` + `AuditorAware` bean (reads `X-User-ID` header) |

**Note:** The user-service has its OWN `JwtAuthenticationFilter` (different from the gateway's). This filter reads the `Authorization` header directly — it's used when calling the user-service directly (bypassing gateway) or for the auth endpoints themselves. For normal proxied requests via gateway, only the gateway's JWT filter runs.

---

### 6.6 `report-service`

**Port:** 8082  
**Database:** `report_db` (PostgreSQL)  
**Messaging:** Kafka producer (topic: `report-uploaded`)  
**Storage:** Local filesystem (`UPLOAD_DIR=/app/uploads`)  
**Key dependencies:** JPA, Kafka, Security, MapStruct, Flyway, SpringDoc, common-lib

**Tables:** `reports`, `report_metrics`

**Key classes:**

| Layer | Class | Responsibility |
|---|---|---|
| Controller | `ReportController` | Multipart upload, list, get, delete, re-analyse |
| Service | `ReportServiceImpl` | Orchestrates upload → store → publish flow |
| Infrastructure | `FileStorageService` | `Files.copy()` to `{UPLOAD_DIR}/{userId}/{uuid}{ext}` |
| Infrastructure | `ReportEventPublisher` | `KafkaTemplate.send()` to `report-uploaded` |
| Security | `GatewaySecurityFilter` | `OncePerRequestFilter` — reads `X-User-ID`, `X-User-Role`, sets security context |

**File upload config:**
```yaml
spring:
  servlet:
    multipart:
      max-file-size: 50MB
      max-request-size: 55MB
```

**Supported tools:** `JMETER`, `GATLING` (validated by `PerformanceTool` enum)

---

### 6.7 `analysis-service`

**Port:** 8083  
**Database:** `analysis_db` (PostgreSQL)  
**Messaging:** Kafka consumer (topic: `report-uploaded`) + Kafka producer (topic: `analysis-completed`)  
**AI:** OpenRouter via Spring `RestClient`  
**Key dependencies:** JPA, Kafka, Security, MapStruct, Flyway, SpringDoc, common-lib

**Tables:** `analyses`, `analysis_findings`

**Key classes:**

| Layer | Class | Responsibility |
|---|---|---|
| Controller | `AnalysisController` | GET by ID, by reportId, list |
| Service | `AnalysisServiceImpl` | Kafka event handling + read queries |
| Infrastructure | `ReportUploadedConsumer` | `@KafkaListener("report-uploaded")` |
| Infrastructure | `PerformanceAnalysisAiService` | File read → prompt build → RestClient call → JSON parse |
| Infrastructure | `AnalysisEventPublisher` | `KafkaTemplate.send("analysis-completed", ...)` |
| Config | `AiConfig` | `RestClient` bean: baseUrl=OpenRouter, Authorization=Bearer |

**Transaction design:** `processReportUploadedEvent` is NOT `@Transactional` at the service level. Each `repository.save()` uses its own short-lived transaction (via `@Transactional` on `AnalysisRepositoryAdapter.save()`). This prevents holding a DB connection during the potentially 10–30 second AI API call.

**Finding severity levels:** `CRITICAL`, `HIGH`, `MEDIUM`, `LOW`, `INFO`

---

### 6.8 `notification-service`

**Port:** 8084  
**Database:** `notification_db` (PostgreSQL)  
**Messaging:** Kafka consumer (topic: `analysis-completed`)  
**Key dependencies:** JPA, Kafka, Security, MapStruct, Flyway, SpringDoc, common-lib

**Tables:** `notifications`

**Key classes:**

| Layer | Class | Responsibility |
|---|---|---|
| Controller | `NotificationController` | GET all, GET unread, GET count, PATCH read, PATCH read-all |
| Service | `NotificationServiceImpl` | Create notification from event + read/mark operations |
| Infrastructure | `AnalysisCompletedConsumer` | `@KafkaListener("analysis-completed")` |

**Notification types:** `ANALYSIS_COMPLETED` | `ANALYSIS_FAILED`

---

## 7. Build System (Maven)

Each service is a **standalone Maven project** — there is no root parent POM. This means each service can be built and run independently without the others being present.

**Parent declaration in every service POM:**
```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.1.0</version>
    <relativePath/>
</parent>
```

**Shared library dependency** (in all business services + gateway):
```xml
<dependency>
    <groupId>com.platform</groupId>
    <artifactId>common-lib</artifactId>
    <version>1.0.0</version>
</dependency>
```

**MapStruct annotation processor** (in services that use MapStruct):
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
            </path>
            <path>
                <groupId>org.mapstruct</groupId>
                <artifactId>mapstruct-processor</artifactId>
                <version>1.6.0</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

Both Lombok AND MapStruct must be in `annotationProcessorPaths` together, in that order. MapStruct needs Lombok to process first so it can see the generated getter/setter/builder methods.

**Build order for local development:**
```powershell
# 1. Install shared library first
cd shared/common-lib && mvn install -DskipTests

# 2. Build any service (example)
cd user-service && mvn package -DskipTests
```

---

## 8. Key Framework Details

### 8.1 Spring Boot 4.x Auto-configuration

Spring Boot 4.x restructured the monolithic `spring-boot-autoconfigure` JAR into many smaller focused JARs (e.g., `spring-boot-webmvc`, `spring-boot-hibernate`, `spring-boot-kafka`). This means:

- Auto-configuration class names and packages changed from Spring Boot 3.x
- Third-party libraries built for Spring Boot 3.x may reference moved classes and fail at startup
- **Example:** Spring AI 1.0.0 references `org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration` which was moved/removed in Spring Boot 4.x — making Spring AI 1.0.0 incompatible

**`@ConditionalOn*` annotations** work as before — Spring Boot only activates auto-configuration when the right classes/properties are on the classpath.

---

### 8.2 Spring Cloud Config

**Client-side import** (`application.yml` of each service):
```yaml
spring:
  config:
    import: "optional:configserver:http://config:${CONFIG_PASSWORD}@${CONFIG_SERVER_HOST}:8888"
```

- `optional:` — if config server is down, service starts with defaults (useful for local dev without config server)
- Config server properties **override** local `application.yml` properties (Config Server has higher precedence)
- Properties loaded at bootstrap time, before the Spring application context is fully created

---

### 8.3 Spring Cloud Netflix Eureka

**Client config** (in each service):
```yaml
eureka:
  client:
    service-url:
      defaultZone: http://${EUREKA_HOST:localhost}:8761/eureka/
  instance:
    prefer-ip-address: true    # Register with IP, not hostname
```

- `prefer-ip-address: true` is important in Docker where container hostnames may not resolve correctly
- Services deregister automatically on graceful shutdown
- The gateway uses `lb://service-name` which Spring Cloud LoadBalancer resolves via Eureka

---

### 8.4 Spring Cloud Gateway MVC

The **MVC** (non-reactive) variant of Spring Cloud Gateway, introduced in Spring Cloud 2023.0.x. Unlike the original reactive gateway (WebFlux-based), this runs on the standard servlet stack:

| Feature | Reactive Gateway | MVC Gateway (this project) |
|---|---|---|
| Artifact | `spring-cloud-starter-gateway` | `spring-cloud-starter-gateway-server-webmvc` |
| Stack | WebFlux / Netty | Spring MVC / Tomcat |
| Filter base class | `GlobalFilter` (reactive) | `OncePerRequestFilter` (servlet) |
| Security | `@EnableWebFluxSecurity` | `@EnableWebSecurity` |
| Route config key | `spring.cloud.gateway.routes` | `spring.cloud.gateway.mvc.routes` |

**Important:** Using `spring.cloud.gateway.routes` with the MVC gateway silently ignores all routes — the correct key is `spring.cloud.gateway.mvc.routes`.

---

### 8.5 Spring Data JPA + Hibernate 7

**JPA provider:** Hibernate 7.4.1.Final  
**Jakarta Persistence API:** 3.2.0 (Jakarta EE 11)

**`AuditableEntity`** (from common-lib) uses Spring Data JPA auditing:
```java
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity {
    @CreatedDate  private OffsetDateTime createdAt;
    @LastModifiedDate private OffsetDateTime updatedAt;
    @CreatedBy   private String createdBy;     // set by AuditorAware
    @LastModifiedBy  private String updatedBy; // set by AuditorAware
    @Version     private Long version;         // optimistic locking
}
```

**`JpaConfig.java`** (each service):
```java
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaConfig {
    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> {
            try {
                // For HTTP requests: read X-User-ID from request
                var attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
                return Optional.ofNullable(attrs.getRequest().getHeader("X-User-ID"));
            } catch (Exception e) {
                return Optional.of("system");  // For Kafka consumers (no HTTP context)
            }
        };
    }
}
```

**`ddl-auto: none`:** All services have Hibernate set to `none` — schema creation is handled by Flyway, not Hibernate. In production you would use `validate` after schema is stable.

**Lazy loading:** `@OneToMany` collections (e.g., `Report.metrics`, `Analysis.findings`) are `FetchType.LAZY`. Repository adapter `save()` methods are `@Transactional` to keep the persistence context open through the mapper call that accesses lazy collections.

---

### 8.6 Flyway Database Migrations

**Version:** 12.4.0 (includes `flyway-database-postgresql`)  
**Location:** `src/main/resources/db/migration/`  
**Convention:** `V{version}__{description}.sql`

**Migrations per service:**

| Service | Migrations |
|---|---|
| user-service | `V1__create_users_table.sql`, `V2__create_refresh_tokens_table.sql` |
| report-service | `V1__create_reports_table.sql`, `V2__create_metrics_table.sql` |
| analysis-service | `V1__create_analyses_table.sql`, `V2__create_findings_table.sql` |
| notification-service | `V1__create_notifications_table.sql` |

**Current setup:** Flyway is **disabled** (`spring.flyway.enabled: false`) in this project's local setup because the databases were created manually via psql. To re-enable, set `enabled: true` and `baseline-on-migrate: true` in the config-server YAMLs.

---

### 8.7 Spring Kafka

**Version:** 4.1.0 (spring-kafka), Kafka clients 4.2.1  
**Auto-configuration:** `spring-boot-starter-kafka` (Spring Boot 4.x style)

**Producer pattern (fire-and-forget with logging):**
```java
kafkaTemplate.send(topic, key, event)
    .whenComplete((result, ex) -> {
        if (ex != null) log.error("Failed to publish", ex);
        else log.info("Published successfully");
    });
```

**Consumer pattern:**
```java
@KafkaListener(
    topics = "${app.kafka.topic.report-uploaded:report-uploaded}",
    groupId = "${app.kafka.consumer.group-id:analysis-service-group}"
)
public void consume(ReportUploadedEvent event) {
    CorrelationIdUtil.set(event.correlationId());
    try {
        analysisService.processReportUploadedEvent(event);
    } finally {
        CorrelationIdUtil.clear();
    }
}
```

The `CorrelationIdUtil.set()` / `clear()` pattern propagates the request trace ID from the original HTTP call through the Kafka message into the async consumer's log context.

---

### 8.8 Spring Security 7

**Version:** 7.1.0  
**Mode:** Stateless (`SessionCreationPolicy.STATELESS`) across all services

**Configuration pattern (each business service):**
```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(s -> s.sessionCreationPolicy(STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**", "/actuator/**", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
            .anyRequest().authenticated()
        )
        .addFilterBefore(gatewaySecurityFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
}
```

**`@EnableMethodSecurity`** is on user-service's config — enables `@PreAuthorize("hasRole('ADMIN')")` on controller methods.

---

### 8.9 JJWT 0.12.5

Three JAR artifacts required:
```xml
<dependency>io.jsonwebtoken:jjwt-api:0.12.5</dependency>          <!-- API -->
<dependency>io.jsonwebtoken:jjwt-impl:0.12.5 (runtime)</dependency> <!-- Implementation -->
<dependency>io.jsonwebtoken:jjwt-jackson:0.12.5 (runtime)</dependency> <!-- JSON via Jackson -->
```

**Token structure:** Standard JWT with HS256 signing:
- **Header:** `{"alg":"HS256","typ":"JWT"}`
- **Payload:** `{"sub":"<userId>","role":"USER","email":"...","iat":...,"exp":...}`
- **Signature:** HMAC-SHA256 of header + payload using the shared secret

**Secret:** A base64-encoded 256-bit key shared between `api-gateway` and `user-service` via the `JWT_SECRET` environment variable. In this project, a development key is pre-configured — replace it in production.

---

### 8.10 MapStruct 1.6

MapStruct generates type-safe mapper implementations at **compile time** (no reflection at runtime). Two levels of mapping in each service:

1. **Entity ↔ Domain** (`infrastructure/persistence/mapper/`): JPA entity ↔ plain domain model
2. **Domain ↔ DTO** (`application/mapper/`): domain model ↔ request/response records

**Example mapper:**
```java
@Mapper(componentModel = "spring")  // Generates Spring @Component
public interface ReportMapper {
    @Mapping(target = "tool", expression = "java(report.getTool().name())")
    ReportResponse toResponse(Report report);  // enum → String conversion
}
```

**`componentModel = "spring"`** makes the generated implementation a Spring bean, injectable via `@RequiredArgsConstructor`.

**Lombok + MapStruct ordering:** Both must be in `annotationProcessorPaths`, Lombok first. MapStruct reads the getters/setters/builders that Lombok generates.

---

### 8.11 Lombok

Used throughout for boilerplate reduction:

| Annotation | Effect |
|---|---|
| `@Getter` / `@Setter` | Generate getters/setters |
| `@Builder` | Generate builder pattern |
| `@NoArgsConstructor` / `@AllArgsConstructor` | Generate constructors |
| `@RequiredArgsConstructor` | Constructor for `final` fields (used for constructor injection) |
| `@Slf4j` | Generate `private static final Logger log = LoggerFactory.getLogger(...)` |
| `@Data` | `@Getter` + `@Setter` + `@ToString` + `@EqualsAndHashCode` + `@RequiredArgsConstructor` |

**Constructor injection:** `@RequiredArgsConstructor` + `final` fields satisfies Spring's constructor injection requirement without needing `@Autowired` on fields.

---

### 8.12 SpringDoc OpenAPI / Swagger UI

**Version:** 2.8.8  
**Artifact:** `springdoc-openapi-starter-webmvc-ui`  
**Swagger UI path:** `/swagger-ui.html`  
**OpenAPI JSON path:** `/v3/api-docs`

**`OpenApiConfig.java`** (each service) defines metadata:
```java
@Bean
public OpenAPI openAPI() {
    return new OpenAPI()
        .info(new Info().title("User Service API").version("1.0.0"))
        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
        .components(new Components().addSecuritySchemes("bearerAuth",
            new SecurityScheme().type(HTTP).scheme("bearer").bearerFormat("JWT")));
}
```

Swagger UI is accessible **directly on each service port** (bypassing the gateway) — useful for testing individual services without authentication headers.

---

### 8.13 Spring RestClient

**Version:** Part of Spring Web 7.0.8 (included via `spring-boot-starter-webmvc`)  
**Purpose:** Synchronous HTTP client replacing `RestTemplate`. Used in analysis-service to call OpenRouter.

`RestClient` is the successor to `RestTemplate` in Spring Framework 6.1+ / Spring Boot 3.2+. It provides a fluent builder API with full type safety via `ParameterizedTypeReference`:

```java
Map<String, Object> response = restClient.post()
    .uri("/chat/completions")
    .body(requestBody)
    .retrieve()
    .body(new ParameterizedTypeReference<>() {});
```

It is synchronous (blocking) — suitable here since the analysis is already running in a background Kafka consumer thread and doesn't need to be non-blocking.

---

## 9. End-to-End Flow

```
Step 1: Register
  Client ──POST /api/auth/register──▶ Gateway :8080
                                         │ CorrelationIdGatewayFilter: assign X-Correlation-ID
                                         │ JwtAuthenticationFilter: shouldNotFilter() = true (public path)
                                         │ Spring Security: permitAll() for /api/auth/**
                                         │ Spring Cloud Gateway MVC: route to lb://user-service
                                         ▼ (Eureka resolves lb://user-service → localhost:8081)
                                    user-service :8081
                                         │ JwtAuthenticationFilter: no token → pass through
                                         │ SecurityConfig: permitAll() for /api/auth/**
                                         │ AuthController.register()
                                         │   → BCrypt.encode(password)
                                         │   → save User to user_db.users
                                         │   → JwtTokenProvider.generateAccessToken() [15 min]
                                         │   → JwtTokenProvider.generateRefreshToken() [7 days]
                                         │   → save RefreshToken to user_db.refresh_tokens
                                         └──▶ { accessToken, refreshToken, tokenType, expiresIn, user }

Step 2: Upload Report
  Client ──POST /api/reports (multipart)──▶ Gateway :8080
    Authorization: Bearer <accessToken>       │ JwtAuthenticationFilter:
                                              │   extractToken() → JWT
                                              │   jwtUtil.isTokenValid(token) → true
                                              │   extractSubject() → userId
                                              │   getClaims().get("role") → "USER"
                                              │   MutableHttpServletRequest.putHeader("X-User-ID", userId)
                                              │   MutableHttpServletRequest.putHeader("X-User-Role", "USER")
                                              │   SecurityContextHolder.set(auth)
                                              ▼
                                         report-service :8082
                                              │ GatewaySecurityFilter:
                                              │   request.getHeader("X-User-ID") → userId
                                              │   SecurityContextHolder.set(auth with ROLE_USER)
                                              │ ReportController.uploadReport()
                                              │   → FileStorageService.store(file, userId)
                                              │      → Files.copy() → /app/uploads/{userId}/{uuid}.csv
                                              │   → save Report(status=UPLOADED) to report_db.reports
                                              │   → ReportEventPublisher.publishReportUploaded()
                                              │      → KafkaTemplate.send("report-uploaded", event)
                                              └──▶ { id, title, status: "UPLOADED", ... }

Step 3: AI Analysis (async — Kafka consumer)
  Kafka broker [topic: report-uploaded, partition: hash(reportId) % 3]
    │
    ▼ (analysis-service-group consumer)
  analysis-service :8083 — ReportUploadedConsumer.consume()
    │ CorrelationIdUtil.set(event.correlationId())   ← trace propagation
    │ AnalysisServiceImpl.processReportUploadedEvent()
    │   │ AnalysisRepositoryAdapter.save(IN_PROGRESS) ← own @Transactional
    │   │ PerformanceAnalysisAiService.analyseReport(filePath, tool)
    │   │   │ Files.readString(filePath)              ← shared volume
    │   │   │ content.substring(0, 15000) if > 15KB  ← token limit protection
    │   │   │ buildPrompt(content, tool)
    │   │   │ RestClient.post("/chat/completions")
    │   │   │   body: { model, messages, temperature, max_tokens }
    │   │   │   → POST https://openrouter.ai/api/v1/chat/completions
    │   │   │   ← { choices[0].message.content: "[{...findings...}]" }
    │   │   └─ parseFindings(aiResponse) → List<AnalysisFinding>
    │   │ AnalysisRepositoryAdapter.save(COMPLETED + findings) ← own @Transactional
    │   │ AnalysisEventPublisher.publishAnalysisCompleted()
    │   │   → KafkaTemplate.send("analysis-completed", event)
    │   └─ (returns)
    └─ CorrelationIdUtil.clear()

Step 4: Notification (async — Kafka consumer)
  Kafka broker [topic: analysis-completed]
    │
    ▼ (notification-service-group consumer)
  notification-service :8084 — AnalysisCompletedConsumer.consume()
    │ NotificationServiceImpl.handleAnalysisCompleted()
    │   → save Notification(
    │       type=ANALYSIS_COMPLETED,
    │       title="Analysis Complete — 3 finding(s)",
    │       message="Your performance report has been analysed..."
    │     ) to notification_db.notifications
    └─ (done)

Step 5: Client polls for results
  GET /api/analyses/report/{reportId}  ──▶ Gateway ──▶ analysis-service
                                                          └──▶ { status, findings, aiModel }

  GET /api/notifications               ──▶ Gateway ──▶ notification-service
                                                          └──▶ { content: [{ title, read, ... }] }
```

---

## 10. Security Architecture

### JWT token lifecycle

```
Register/Login
  └─▶ user-service issues:
        accessToken  (JWT, 15 min, signed HS256)
        refreshToken (opaque UUID, 7 days, stored in DB)

Each API request
  └─▶ Client sends: Authorization: Bearer <accessToken>
  └─▶ Gateway validates: signature + expiry
  └─▶ Gateway injects: X-User-ID + X-User-Role into forwarded request
  └─▶ Downstream service trusts these headers (no re-validation)

Token refresh (when accessToken expires)
  └─▶ POST /api/auth/refresh { refreshToken }
  └─▶ user-service: verify refreshToken in DB, not revoked, not expired
  └─▶ Old refreshToken revoked
  └─▶ New accessToken + refreshToken issued

Logout
  └─▶ POST /api/auth/logout { refreshToken }
  └─▶ refreshToken.revoked = true in DB
  └─▶ (accessToken remains valid until natural expiry — stateless)
```

### Gateway security filter chain

```
Incoming request
       │
       ▼
CorrelationIdGatewayFilter (HIGHEST_PRECEDENCE)
  → Set X-Correlation-ID on request and response
       │
       ▼
JwtAuthenticationFilter
  ├── Path starts with /api/auth/ or /actuator/ ?
  │     └── YES → shouldNotFilter() = true → skip this filter entirely
  └── NO → protected path
         ├── No Authorization header or doesn't start with "Bearer "?
         │     └── response.setStatus(401) → return
         ├── jwtUtil.isTokenValid(token) = false?
         │     └── response.setStatus(401) → return
         └── Valid token
               → MutableHttpServletRequest.putHeader("X-User-ID", userId)
               → MutableHttpServletRequest.putHeader("X-User-Role", role)
               → SecurityContextHolder.setAuthentication(auth)
               → chain.doFilter(mutableRequest, response)
       │
       ▼
Spring Security AuthorizationFilter
  → requestMatchers("/api/auth/**").permitAll()  → always pass
  → anyRequest().authenticated()                 → need SecurityContext set (done above)
       │
       ▼
Spring Cloud Gateway MVC Router
  → Match spring.cloud.gateway.mvc.routes predicates
  → Resolve lb://service-name via Eureka
  → HTTP proxy to downstream service (headers including X-User-ID forwarded)
```

### Downstream service security

```
Request arrives at downstream service (e.g., report-service)
  │
  ▼ (already passed through gateway authentication)
GatewaySecurityFilter (OncePerRequestFilter)
  → userId = request.getHeader("X-User-ID")
  → role   = request.getHeader("X-User-Role")
  → SecurityContextHolder.setAuthentication(
        new UsernamePasswordAuthenticationToken(userId, null, [ROLE_USER])
    )
  │
  ▼
Spring Security AuthorizationFilter
  → /api/reports/** requires authenticated → SecurityContext set → PASS
  → @PreAuthorize("hasRole('ADMIN')") → checked against SecurityContext authorities
  │
  ▼
@RestController method executes
  → (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal() = userId
```

---

## 11. Event-Driven Flow (Kafka)

### Topics

| Topic | Partitions | Producer | Consumer Group | Payload |
|---|---|---|---|---|
| `report-uploaded` | 3 | report-service | `analysis-service-group` | `ReportUploadedEvent` |
| `analysis-completed` | 3 | analysis-service | `notification-service-group` | `AnalysisCompletedEvent` |

### Event records (Java records — JSON-serialised)

```java
// report-service/domain/event/ReportUploadedEvent.java
record ReportUploadedEvent(
    UUID reportId,
    UUID userId,
    String tool,           // "JMETER" or "GATLING"
    String filePath,       // absolute path on shared volume
    String correlationId,  // for distributed tracing
    OffsetDateTime timestamp
) {}

// analysis-service/domain/event/AnalysisCompletedEvent.java
record AnalysisCompletedEvent(
    UUID analysisId,
    UUID reportId,
    UUID userId,
    int findingsCount,
    String status,         // "COMPLETED" or "FAILED"
    String correlationId,
    OffsetDateTime timestamp
) {}
```

### Serialisation design

```
Producer (report-service)                Consumer (analysis-service)
---------------------------------        ---------------------------------
JsonSerializer                           JsonDeserializer
add.type.headers: false                  value.default.type: 
                                           com.platform.analysis.domain
                                           .event.ReportUploadedEvent
                                         trusted.packages:
                                           com.platform.analysis.domain.event
```

`add.type.headers: false` means the Kafka message contains only JSON, no `__TypeId__` header. Each consumer declares its own local class that matches the JSON shape. This allows producer and consumer to evolve their packages independently — they are coupled only by JSON field names.

---

## 12. Configuration Management

### Property resolution order (highest to lowest priority)

1. Environment variables (set in IntelliJ run config / Docker `.env`)
2. Config Server YAML (`config-repo/*.yml`) — fetched at startup
3. Service local `application.yml`

### Property injection flow

```
Environment: DB_PASSWORD=admin
     │
     ▼ injected via ${DB_PASSWORD} placeholder
config-repo/user-service.yml:
  spring.datasource.password: ${DB_PASSWORD:postgres}
     │
     ▼ served by config-server over HTTP (Basic Auth)
user-service fetches at startup:
  spring.config.import: configserver:http://config:config-secret@localhost:8888
     │
     ▼ Spring binds to DataSourceProperties
HikariCP uses password for connection
```

### Environment variables reference (all services)

| Variable | Default | Used by | Purpose |
|---|---|---|---|
| `DB_HOST` | `localhost` | user, report, analysis, notification | PostgreSQL hostname |
| `DB_USERNAME` | `postgres` | user, report, analysis, notification | DB user |
| `DB_PASSWORD` | `postgres` | user, report, analysis, notification | DB password |
| `JWT_SECRET` | (dev key) | gateway, user-service | HS256 signing key (base64) |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | report, analysis, notification | Kafka address |
| `OPENROUTER_API_KEY` | (none — required) | analysis-service | AI API authentication |
| `AI_MODEL` | `anthropic/claude-3.5-sonnet` | analysis-service | AI model name |
| `REDIS_HOST` | `localhost` | user-service, gateway | Redis hostname |
| `REDIS_PORT` | `6379` | user-service | Redis port |
| `REDIS_PASSWORD` | (empty) | user-service | Redis password |
| `UPLOAD_DIR` | `/app/uploads` | report-service, analysis-service | File storage path |
| `CONFIG_SERVER_HOST` | `localhost` | all services | Config server hostname |
| `CONFIG_PASSWORD` | `config-secret` | all services + config-server | Config server Basic Auth password |
| `EUREKA_HOST` | `localhost` | all services | Eureka hostname |

---

## 13. Database Design

### Schema overview

```
┌─────────────────────────────────────────────────────────────────────────┐
│  user_db (PostgreSQL)                                                    │
│                                                                          │
│  users                              refresh_tokens                       │
│  ─────────────────────              ─────────────────────────────        │
│  id UUID PK                         id UUID PK                           │
│  email VARCHAR(255) UNIQUE          user_id UUID → users(id) CASCADE     │
│  password_hash VARCHAR(255)         token VARCHAR(512) UNIQUE            │
│  first_name VARCHAR(100)            expires_at TIMESTAMPTZ               │
│  last_name VARCHAR(100)             revoked BOOLEAN DEFAULT FALSE         │
│  role VARCHAR(20) DEFAULT 'USER'    created_at TIMESTAMPTZ               │
│  active BOOLEAN DEFAULT TRUE                                             │
│  email_verified BOOLEAN             Indexes:                             │
│  created_at / updated_at            idx_refresh_tokens_user_id           │
│  created_by / updated_by            idx_refresh_tokens_token             │
│  version BIGINT (optimistic lock)                                        │
│                                     Indexes:                             │
│                                     idx_users_email                      │
│                                     idx_users_role                       │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│  report_db (PostgreSQL)                                                  │
│                                                                          │
│  reports                            report_metrics                       │
│  ─────────────────────              ─────────────────────────────        │
│  id UUID PK                         id UUID PK                           │
│  user_id UUID (no FK constraint)    report_id UUID → reports(id) CASCADE │
│  title VARCHAR(255)                 endpoint VARCHAR(500)                │
│  description TEXT                   http_method VARCHAR(10)              │
│  tool VARCHAR(50)                   samples BIGINT                       │
│  status VARCHAR(50)                 average_response_time_ms DECIMAL     │
│  file_name VARCHAR(500)             min/max/p50/p90/p95/p99 DECIMAL      │
│  file_path VARCHAR(1000)            throughput DECIMAL(12,4)             │
│  file_size BIGINT                   error_rate DECIMAL(6,4)              │
│  created_at / updated_at / version  error_count BIGINT                   │
│                                     created_at / updated_at / version    │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│  analysis_db (PostgreSQL)                                                │
│                                                                          │
│  analyses                           analysis_findings                    │
│  ─────────────────────              ─────────────────────────────        │
│  id UUID PK                         id UUID PK                           │
│  report_id UUID (no FK)             analysis_id UUID → analyses(id) CASCADE│
│  user_id UUID (no FK)               severity VARCHAR(20) — enum          │
│  status VARCHAR(50)                 category VARCHAR(100)                │
│  ai_model VARCHAR(100)              title VARCHAR(500)                   │
│  prompt_tokens INTEGER              description TEXT                     │
│  completion_tokens INTEGER          recommendation TEXT                  │
│  created_at / updated_at / version  affected_endpoint VARCHAR(500)       │
│                                     metric_value VARCHAR(200)            │
│                                     threshold_value VARCHAR(200)         │
│                                     created_at TIMESTAMPTZ               │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│  notification_db (PostgreSQL)                                            │
│                                                                          │
│  notifications                                                           │
│  ─────────────────────────────────────────────────────────────          │
│  id UUID PK                                                              │
│  user_id UUID (no FK — cross-service reference)                          │
│  type VARCHAR(100) — ANALYSIS_COMPLETED / ANALYSIS_FAILED               │
│  title VARCHAR(500)                                                      │
│  message TEXT                                                            │
│  reference_id UUID (analysisId)                                          │
│  reference_type VARCHAR(100) = "ANALYSIS"                                │
│  read_at TIMESTAMPTZ (null = unread)                                     │
│  created_at / updated_at / version                                       │
└─────────────────────────────────────────────────────────────────────────┘
```

**Cross-service `user_id`:** Present in `reports`, `analyses`, and `notifications` tables, but there are no foreign key constraints pointing to `user_db.users`. Referential integrity is enforced at the application layer — if a user is deactivated, their data remains accessible to admins. This is the standard microservices pattern for bounded contexts.

**Optimistic locking:** Every table has a `version BIGINT` column (via `AuditableEntity`). Hibernate uses `@Version` to detect concurrent modifications — if two requests try to update the same row, one gets a `StaleObjectStateException`.

**UUID primary keys:** All tables use `UUID` as PK with `DEFAULT gen_random_uuid()` (requires `pgcrypto` extension, enabled via `CREATE EXTENSION IF NOT EXISTS "pgcrypto"`).

---

## 14. Code Structure (Clean Architecture)

Every business service follows the same four-layer package structure:

```
com.platform.{service}/
│
├── domain/                          ← Inner core — zero framework dependencies
│   ├── model/                       │  Pure Java classes (NOT JPA entities)
│   │   ├── Report.java              │  @Builder @Getter @Setter @NoArgsConstructor @AllArgsConstructor
│   │   └── ReportMetric.java        │
│   ├── repository/                  │  Interfaces only — no implementation
│   │   ├── ReportRepository.java    │  defines: save(), findById(), findByUserId(), etc.
│   │   └── ReportMetricRepository.java
│   ├── event/                       │  Java records for Kafka messages
│   │   └── ReportUploadedEvent.java
│   ├── exception/                   │  Domain-specific exceptions
│   │   └── ReportNotFoundException.java  extends ResourceNotFoundException
│   └── model/enum/
│       ├── PerformanceTool.java     │  JMETER, GATLING
│       └── ReportStatus.java        │  UPLOADED, PROCESSED, ANALYSIS_REQUESTED, FAILED
│
├── application/                     ← Use cases and orchestration
│   ├── service/
│   │   └── ReportService.java       │  Interface declaring use cases
│   ├── service/impl/
│   │   └── ReportServiceImpl.java   │  @Service @RequiredArgsConstructor @Transactional
│   │                                │  Injects: ReportRepository, ReportMapper, FileStorageService, ...
│   ├── dto/
│   │   ├── request/
│   │   │   └── (no request DTOs for reports — multipart params used)
│   │   └── response/
│   │       ├── ReportResponse.java      ← Java record (immutable DTO)
│   │       ├── ReportSummaryResponse.java
│   │       └── ReportMetricResponse.java
│   └── mapper/
│       └── ReportMapper.java        │  @Mapper(componentModel="spring")
│                                    │  Maps Report domain ↔ ReportResponse DTO
│
├── infrastructure/                  ← Framework implementations
│   ├── persistence/
│   │   ├── entity/
│   │   │   ├── ReportEntity.java    │  @Entity @Table("reports") extends AuditableEntity
│   │   │   └── ReportMetricEntity.java
│   │   ├── repository/
│   │   │   ├── ReportJpaRepository.java       │  extends JpaRepository<ReportEntity, UUID>
│   │   │   └── ReportMetricJpaRepository.java
│   │   ├── adapter/
│   │   │   ├── ReportRepositoryAdapter.java    │  @Component — implements ReportRepository
│   │   │   └── ReportMetricRepositoryAdapter.java
│   │   └── mapper/
│   │       ├── ReportEntityMapper.java         │  @Mapper — ReportEntity ↔ Report domain
│   │       └── ReportMetricEntityMapper.java
│   ├── messaging/
│   │   └── ReportEventPublisher.java  │  @Component — KafkaTemplate wrapper
│   ├── security/
│   │   └── GatewaySecurityFilter.java │  OncePerRequestFilter — reads X-User-ID
│   └── storage/
│       └── FileStorageService.java    │  @Service — Files.copy() to UPLOAD_DIR
│
├── presentation/
│   ├── controller/
│   │   └── ReportController.java    │  @RestController @RequestMapping("/api/reports")
│   └── advice/
│       └── GlobalExceptionHandler.java  │  @RestControllerAdvice extends GlobalExceptionHandlerBase
│
└── config/
    ├── SecurityConfig.java          │  @EnableWebSecurity @EnableMethodSecurity
    ├── JpaConfig.java               │  @EnableJpaAuditing + AuditorAware @Bean
    └── OpenApiConfig.java           │  OpenAPI @Bean with JWT bearer scheme
```

**Dependency rule (enforced by package structure, not build tools):**
- `domain` → nothing (no imports from any other layer)
- `application` → `domain` only
- `infrastructure` → `domain` + `application` (implements their interfaces/uses their DTOs)
- `presentation` → `application` only (calls service interfaces, returns DTOs)
- `config` → any layer (wires it all together)

---

## 15. Running Locally (IntelliJ)

### Prerequisites

- Java 21+ JDK (configured as Project SDK in IntelliJ)
- Docker Desktop (running)
- IntelliJ IDEA (Ultimate or Community)
- Maven (bundled with IntelliJ or standalone)

### One-time setup

**1. Install common-lib to local Maven repository:**
```powershell
cd "d:\...\shared\common-lib"
mvn install -DskipTests
```

**2. Link common-lib as a Maven module in IntelliJ:**  
Maven tool window (right panel) → `+` button → navigate to `shared/common-lib/pom.xml` → OK.  
This allows IntelliJ to resolve `com.platform:common-lib:1.0.0` directly from source instead of the JAR.

**3. Start infrastructure containers:**
```powershell
cd docker
docker compose up postgres zookeeper kafka redis -d
```

**4. Create databases (first time only, if not using Docker init script):**
```powershell
$env:PGPASSWORD = "admin"
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -h localhost `
  -c "CREATE DATABASE user_db;" `
  -c "CREATE DATABASE report_db;" `
  -c "CREATE DATABASE analysis_db;" `
  -c "CREATE DATABASE notification_db;"
```

**5. Create upload directory:**
```powershell
mkdir C:\tmp\uploads
```

### Service startup order (IntelliJ Run Configurations)

| Order | Main Class | Env vars (additional) | Port | Wait for |
|---|---|---|---|---|
| 1 | `com.platform.discovery.DiscoveryServerApplication` | — | 8761 | `Started Eureka Server` in log |
| 2 | `com.platform.config.ConfigServerApplication` | `CONFIG_PASSWORD=config-secret` | 8888 | `http://localhost:8888/actuator/health` = UP |
| 3 | `com.platform.gateway.ApiGatewayApplication` | JWT_SECRET, REDIS_HOST, CONFIG vars | 8080 | Port 8080 responds |
| 4 | `com.platform.user.UserServiceApplication` | All + REDIS_HOST | 8081 | `Started UserServiceApplication` |
| 5 | `com.platform.report.ReportServiceApplication` | All + UPLOAD_DIR, KAFKA | 8082 | `Started ReportServiceApplication` |
| 6 | `com.platform.analysis.AnalysisServiceApplication` | All + UPLOAD_DIR, KAFKA, OPENROUTER | 8083 | `Started AnalysisServiceApplication` |
| 7 | `com.platform.notification.NotificationServiceApplication` | All + KAFKA | 8084 | `Started NotificationServiceApplication` |

### Full environment variable sets (paste into IntelliJ run config → Environment Variables)

**discovery-server:** (no additional env vars needed — all defaults work)

**config-server:**
```
CONFIG_PASSWORD=config-secret;EUREKA_HOST=localhost
```

**api-gateway:**
```
JWT_SECRET=bXlTdXBlclNlY3JldEtleUZvckpXVFNpZ25pbmdXaXRoSFMyNTZBbGdvcml0aG0xMjM0NTY3ODk=;REDIS_HOST=localhost;REDIS_PASSWORD=;CONFIG_SERVER_HOST=localhost;CONFIG_PASSWORD=config-secret;EUREKA_HOST=localhost
```

**user-service:**
```
DB_HOST=localhost;DB_USERNAME=postgres;DB_PASSWORD=admin;JWT_SECRET=bXlTdXBlclNlY3JldEtleUZvckpXVFNpZ25pbmdXaXRoSFMyNTZBbGdvcml0aG0xMjM0NTY3ODk=;REDIS_HOST=localhost;REDIS_PASSWORD=;CONFIG_SERVER_HOST=localhost;CONFIG_PASSWORD=config-secret;EUREKA_HOST=localhost
```

**report-service:**
```
DB_HOST=localhost;DB_USERNAME=postgres;DB_PASSWORD=admin;JWT_SECRET=bXlTdXBlclNlY3JldEtleUZvckpXVFNpZ25pbmdXaXRoSFMyNTZBbGdvcml0aG0xMjM0NTY3ODk=;KAFKA_BOOTSTRAP_SERVERS=localhost:29092;CONFIG_SERVER_HOST=localhost;CONFIG_PASSWORD=config-secret;EUREKA_HOST=localhost;UPLOAD_DIR=C:/tmp/uploads
```

**analysis-service:**
```
DB_HOST=localhost;DB_USERNAME=postgres;DB_PASSWORD=admin;JWT_SECRET=bXlTdXBlclNlY3JldEtleUZvckpXVFNpZ25pbmdXaXRoSFMyNTZBbGdvcml0aG0xMjM0NTY3ODk=;KAFKA_BOOTSTRAP_SERVERS=localhost:29092;OPENROUTER_API_KEY=<your-key>;AI_MODEL=anthropic/claude-3.5-sonnet;CONFIG_SERVER_HOST=localhost;CONFIG_PASSWORD=config-secret;EUREKA_HOST=localhost;UPLOAD_DIR=C:/tmp/uploads
```

**notification-service:**
```
DB_HOST=localhost;DB_USERNAME=postgres;DB_PASSWORD=admin;JWT_SECRET=bXlTdXBlclNlY3JldEtleUZvckpXVFNpZ25pbmdXaXRoSFMyNTZBbGdvcml0aG0xMjM0NTY3ODk=;KAFKA_BOOTSTRAP_SERVERS=localhost:29092;CONFIG_SERVER_HOST=localhost;CONFIG_PASSWORD=config-secret;EUREKA_HOST=localhost
```

> **Kafka port:** Use `localhost:29092` (external port) when running services locally. Use `kafka:9092` only inside Docker containers.

---

## 16. Running via Docker Compose

**Prerequisites:** Docker Desktop running, OpenRouter API key set in `docker/.env`

**Edit `docker/.env`:**
```dotenv
OPENROUTER_API_KEY=sk-or-v1-xxxxxxxx   # Replace with your real key
DB_PASSWORD=admin                         # Must match POSTGRES_PASSWORD
POSTGRES_PASSWORD=admin
```

**Start everything:**
```powershell
cd docker
docker compose up --build
```

This builds all 7 service images and starts all 11 containers. First build takes ~5–10 minutes to download base images and compile Java code.

**Start only infrastructure (to run services in IntelliJ):**
```powershell
cd docker
docker compose up postgres zookeeper kafka redis -d
```

**Stop all containers:**
```powershell
docker compose down
```

**Stop and delete all data (full reset):**
```powershell
docker compose down -v   # -v removes named volumes (postgres_data, redis_data, report_uploads)
```

**View logs for a specific service:**
```powershell
docker compose logs -f analysis-service
```

---

## 17. Port Reference

| Service | Host Port | Container Port | Protocol | Notes |
|---|---|---|---|---|
| API Gateway | 8080 | 8080 | HTTP | Main entry point for all clients |
| User Service | 8081 | 8081 | HTTP | Direct access for dev/Swagger |
| Report Service | 8082 | 8082 | HTTP | Direct access for dev/Swagger |
| Analysis Service | 8083 | 8083 | HTTP | Direct access for dev/Swagger |
| Notification Service | 8084 | 8084 | HTTP | Direct access for dev/Swagger |
| Discovery Server | 8761 | 8761 | HTTP | Eureka dashboard |
| Config Server | 8888 | 8888 | HTTP | Protected by Basic Auth |
| PostgreSQL | 5432 | 5432 | TCP | `admin` password |
| Redis | 6379 | 6379 | TCP | No password |
| Kafka (external) | 29092 | — | TCP | For localhost connections |
| Kafka (internal) | — | 9092 | TCP | Docker network only |
| Zookeeper | — | 2181 | TCP | Internal only |

---

## 18. API Reference

All production traffic goes through `http://localhost:8080` (API Gateway). Protected endpoints require `Authorization: Bearer <accessToken>`.

### Authentication — no token required

| Method | Path | Request Body | Response |
|---|---|---|---|
| POST | `/api/auth/register` | `{email, password, firstName, lastName}` | `{accessToken, refreshToken, user}` |
| POST | `/api/auth/login` | `{email, password}` | `{accessToken, refreshToken, user}` |
| POST | `/api/auth/refresh` | `{refreshToken}` | `{accessToken, refreshToken, user}` |
| POST | `/api/auth/logout` | `{refreshToken}` | `{message: "Logout successful"}` |

### Users — token required

| Method | Path | Description |
|---|---|---|
| GET | `/api/users/me` | Own profile |
| PUT | `/api/users/me` | Update `firstName`, `lastName` |
| GET | `/api/users` | All users — ADMIN only |
| GET | `/api/users/{id}` | Any user by ID — ADMIN only |
| DELETE | `/api/users/{id}` | Deactivate user — ADMIN only |

### Reports — token required

| Method | Path | Description |
|---|---|---|
| POST | `/api/reports` | Upload report (`multipart/form-data`: `title`, `tool`, `file`) |
| GET | `/api/reports` | Own reports (paginated: `?page=0&size=20`) |
| GET | `/api/reports/{id}` | Report with metrics |
| DELETE | `/api/reports/{id}` | Delete report + file |
| POST | `/api/reports/{id}/analyse` | Re-trigger AI analysis |
| GET | `/api/reports/admin/all` | All reports — ADMIN only |

### Analyses — token required

| Method | Path | Description |
|---|---|---|
| GET | `/api/analyses` | Own analyses (paginated) |
| GET | `/api/analyses/{id}` | Analysis with all findings |
| GET | `/api/analyses/report/{reportId}` | Analysis for a specific report |
| GET | `/api/analyses/admin/all` | All analyses — ADMIN only |

### Notifications — token required

| Method | Path | Description |
|---|---|---|
| GET | `/api/notifications` | All notifications (paginated) |
| GET | `/api/notifications/unread` | Unread only (paginated) |
| GET | `/api/notifications/unread/count` | `{count: 5}` |
| PATCH | `/api/notifications/{id}/read` | Mark one as read |
| PATCH | `/api/notifications/read-all` | Mark all as read |

### Standard response shapes

```json
// Success
{
  "success": true,
  "message": "Report uploaded",
  "data": { ... },
  "timestamp": "2026-06-20T12:00:00Z"
}

// Error
{
  "success": false,
  "message": "Validation failed",
  "errorCode": "VALIDATION_ERROR",
  "errors": [{ "field": "email", "message": "must not be blank" }],
  "timestamp": "2026-06-20T12:00:00Z"
}

// Paginated list
{
  "success": true,
  "data": {
    "content": [...],
    "page": 0,
    "size": 20,
    "totalElements": 42,
    "totalPages": 3,
    "last": false
  }
}
```

### Swagger UI — per service (bypass gateway)

| Service | URL |
|---|---|
| User | `http://localhost:8081/swagger-ui.html` |
| Report | `http://localhost:8082/swagger-ui.html` |
| Analysis | `http://localhost:8083/swagger-ui.html` |
| Notification | `http://localhost:8084/swagger-ui.html` |
| Eureka Dashboard | `http://localhost:8761` |
| Config Server Health | `http://localhost:8888/actuator/health` |
