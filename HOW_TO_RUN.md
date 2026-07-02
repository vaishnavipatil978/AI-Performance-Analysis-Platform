# How to Run — AI-Powered Performance Analysis Platform

There are two modes: **full Docker** (everything containerised) or **hybrid** (infra in Docker, services in IntelliJ).

---

## Option A — Single-Click Docker (Recommended)

Double-click `start-platform.bat` at the project root.

It will:
1. Start Docker Desktop if it is not running (waits up to 60s)
2. Build all 7 service images
3. Start all 11 containers in the correct order (health-checked)

**Startup order:**
```
postgres + zookeeper + redis  (parallel, no deps)
    → kafka               (after zookeeper)
        → discovery-server    (Eureka registry)
            → config-server
                → api-gateway
                  user-service
                  report-service
                  analysis-service
                  notification-service  (all parallel after config)
```

First run takes **5–10 minutes** to download base images and compile Java.

---

## Option B — Manual Docker

```powershell
cd docker
docker compose up --build        # build + start, logs in foreground
docker compose up --build -d     # same but detached (background)
```

---

## Option C — IntelliJ + Docker Infra Only

Start only the infrastructure containers:
```powershell
cd docker
docker compose up postgres zookeeper kafka redis -d
```

Then in IntelliJ start services **in this order**, waiting for each to log `Started`:

| # | Main Class                                          | Port |
|---|-----------------------------------------------------|------|
| 1 | `com.platform.discovery.DiscoveryServerApplication` | 8761 |
| 2 | `com.platform.config.ConfigServerApplication`       | 8888 |
| 3 | `com.platform.gateway.ApiGatewayApplication`        | 8080 |
| 4 | `com.platform.user.UserServiceApplication`          | 8081 |
| 5 | `com.platform.report.ReportServiceApplication`      | 8082 |
| 6 | `com.platform.analysis.AnalysisServiceApplication`  | 8083 |
| 7 | `com.platform.notification.NotificationServiceApplication` | 8084 |

Use `KAFKA_BOOTSTRAP_SERVERS=localhost:29092` (external port) in all IntelliJ run configs.

### Environment variables for each IntelliJ run config

**discovery-server** — no extra vars needed

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

---

## Prerequisites (one-time setup)

1. **Docker Desktop** installed and running
2. **`OPENROUTER_API_KEY`** set in `docker/.env` — AI analysis will not work without it
3. **`common-lib` installed to local Maven repo** (IntelliJ mode only):
   ```powershell
   cd shared/common-lib
   mvn install -DskipTests
   ```
4. **PostgreSQL databases created** (first time, IntelliJ mode only — Docker handles this automatically):
   ```powershell
   $env:PGPASSWORD = "admin"
   & "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -h localhost `
     -c "CREATE DATABASE user_db;" `
     -c "CREATE DATABASE report_db;" `
     -c "CREATE DATABASE analysis_db;" `
     -c "CREATE DATABASE notification_db;"
   ```
5. **Upload directory** (IntelliJ mode only):
   ```powershell
   mkdir C:\tmp\uploads
   ```

---

## Verify Everything is Healthy

| URL | Expected |
|-----|----------|
| `http://localhost:8761` | Eureka dashboard — all 5 services registered |
| `http://localhost:8888/actuator/health` | `{"status":"UP"}` |
| `http://localhost:8080/actuator/health` | Gateway health |
| `http://localhost:8081/swagger-ui.html` | User Service Swagger UI |
| `http://localhost:8082/swagger-ui.html` | Report Service Swagger UI |
| `http://localhost:8083/swagger-ui.html` | Analysis Service Swagger UI |
| `http://localhost:8084/swagger-ui.html` | Notification Service Swagger UI |

---

## Useful Docker Commands

```powershell
# Run from the docker\ folder

docker compose logs -f                    # tail all service logs
docker compose logs -f analysis-service   # tail one service
docker compose ps                         # check container status
docker compose down                       # stop all services (keeps data)
docker compose down -v                    # stop + wipe all volumes (full reset)
```

---

## Service Port Reference

| Service              | Port  | Notes                          |
|----------------------|-------|--------------------------------|
| API Gateway          | 8080  | Main entry point for all APIs  |
| User Service         | 8081  | Direct access / Swagger        |
| Report Service       | 8082  | Direct access / Swagger        |
| Analysis Service     | 8083  | Direct access / Swagger        |
| Notification Service | 8084  | Direct access / Swagger        |
| Discovery Server     | 8761  | Eureka dashboard               |
| Config Server        | 8888  | Basic Auth protected           |
| PostgreSQL           | 5432  | user: postgres / pass: admin   |
| Redis                | 6379  | No password                    |
| Kafka (external)     | 29092 | For localhost connections       |
