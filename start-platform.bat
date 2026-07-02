@echo off
setlocal enabledelayedexpansion

:: ============================================================
::  AI-Powered Performance Analysis Platform  --  Startup Script
::  Starts Docker Desktop (if needed) then spins up all services
::  in correct dependency order via docker compose health checks.
:: ============================================================

title AI Performance Platform - Startup

echo.
echo  ================================================
echo   AI-Powered Performance Analysis Platform
echo   Starting all services...
echo  ================================================
echo.

:: ── Step 1: Ensure Docker Desktop is running ──────────────
echo [1/4] Checking Docker Desktop...

docker info >nul 2>&1
if %errorlevel% == 0 (
    echo        Docker is already running.
    goto :docker_ready
)

echo        Docker is not running. Starting Docker Desktop...
start "" "C:\Program Files\Docker\Docker\Docker Desktop.exe"

echo        Waiting for Docker daemon to become ready (up to 60s)...
set /a attempts=0
:wait_docker
set /a attempts+=1
if !attempts! gtr 30 (
    echo.
    echo  [ERROR] Docker did not start within 60 seconds.
    echo          Please start Docker Desktop manually and re-run this script.
    echo.
    pause
    exit /b 1
)
timeout /t 2 /nobreak >nul
docker info >nul 2>&1
if %errorlevel% neq 0 (
    set /p "=." <nul
    goto :wait_docker
)
echo.
echo        Docker is ready.

:docker_ready

:: ── Step 2: Navigate to docker compose directory ──────────
echo.
echo [2/4] Navigating to docker compose directory...
cd /d "%~dp0docker"
if %errorlevel% neq 0 (
    echo  [ERROR] Could not find the docker\ directory.
    pause
    exit /b 1
)

:: ── Step 3: Pull / build images ───────────────────────────
echo.
echo [3/4] Building service images (this may take several minutes on first run)...
echo.
docker compose build --parallel
if %errorlevel% neq 0 (
    echo.
    echo  [ERROR] Image build failed. Check the output above for details.
    pause
    exit /b 1
)

:: ── Step 4: Start all services in dependency order ────────
::
::  Docker Compose honours `depends_on + condition: service_healthy`
::  so the containers start automatically in the correct sequence:
::
::    Tier 1 (parallel, no deps)  : postgres | zookeeper | redis
::    Tier 2 (after zookeeper)    : kafka
::    Tier 3 (first platform svc) : discovery-server (Eureka)
::    Tier 4 (after discovery)    : config-server
::    Tier 5 (after config+infra) : api-gateway
::                                  user-service
::                                  report-service
::                                  analysis-service
::                                  notification-service
::
echo.
echo [4/4] Starting all services in dependency order...
echo.
docker compose up -d
if %errorlevel% neq 0 (
    echo.
    echo  [ERROR] docker compose up failed. Check the output above for details.
    pause
    exit /b 1
)

:: ── Done ──────────────────────────────────────────────────
echo.
echo  ================================================
echo   All services started successfully!
echo  ================================================
echo.
echo   Service endpoints:
echo     API Gateway      :  http://localhost:8080
echo     User Service     :  http://localhost:8081/swagger-ui.html
echo     Report Service   :  http://localhost:8082/swagger-ui.html
echo     Analysis Service :  http://localhost:8083/swagger-ui.html
echo     Notification Svc :  http://localhost:8084/swagger-ui.html
echo     Eureka Dashboard :  http://localhost:8761
echo     Config Server    :  http://localhost:8888/actuator/health
echo     PostgreSQL       :  localhost:5432  (user: postgres / admin)
echo     Redis            :  localhost:6379
echo     Kafka            :  localhost:29092
echo.
echo   Useful commands (run from the docker\ folder):
echo     docker compose logs -f                  -- tail all logs
echo     docker compose logs -f analysis-service -- tail one service
echo     docker compose down                     -- stop all services
echo     docker compose down -v                  -- stop + wipe all data
echo.
pause
