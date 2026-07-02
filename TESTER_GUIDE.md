# AI-Powered Performance Analysis Platform — Tester Guide

## Table of Contents

1. [Prerequisites & Setup](#1-prerequisites--setup)
2. [Main Workflow (Golden Path)](#2-main-workflow-golden-path)
   - [Step 1 — Register](#step-1--register)
   - [Step 2 — Login](#step-2--login)
   - [Step 3 — Upload a Report](#step-3--upload-a-report)
   - [Step 4 — Wait for AI Analysis](#step-4--wait-for-ai-analysis)
   - [Step 5 — View Analysis Results](#step-5--view-analysis-results)
   - [Step 6 — Check Notifications](#step-6--check-notifications)
3. [Postman Setup](#3-postman-setup)
4. [Swagger UI Setup](#4-swagger-ui-setup)
5. [Sample Test Data Files](#5-sample-test-data-files)
6. [Authentication APIs](#6-authentication-apis)
7. [User APIs](#7-user-apis)
8. [Report APIs](#8-report-apis)
9. [Analysis APIs](#9-analysis-apis)
10. [Notification APIs](#10-notification-apis)
11. [Admin APIs](#11-admin-apis)
12. [Error Reference](#12-error-reference)
13. [Testing Checklist](#13-testing-checklist)

---

## 1. Prerequisites & Setup

### Services that must be running

Before testing, verify all services are up by opening:

| URL | Expected result |
|---|---|
| `http://localhost:8761` | Eureka dashboard — all services listed |
| `http://localhost:8888/actuator/health` | `{"status":"UP"}` |
| `http://localhost:8080/actuator/health` | `{"status":"UP"}` |

### Base URL

All API calls go through the **API Gateway**:
```
http://localhost:8080
```

Do NOT call individual services directly (8081, 8082, etc.) for end-to-end testing — use the gateway so authentication is enforced.

### Authentication header format

Every protected endpoint requires:
```
Authorization: Bearer <accessToken>
```

You receive the `accessToken` from register or login. It expires in **15 minutes** — use the refresh endpoint to get a new one.

---

## 2. Main Workflow (Golden Path)

This is the complete end-to-end test of the platform's core feature. Run these steps in order.

---

### Step 1 — Register

**POST** `http://localhost:8080/api/auth/register`

**Headers:**
```
Content-Type: application/json
```

**Request body:**
```json
{
  "email": "tester@example.com",
  "password": "Test@1234",
  "firstName": "Test",
  "lastName": "User"
}
```

**Expected response — 201 Created:**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 900,
    "user": {
      "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "email": "tester@example.com",
      "firstName": "Test",
      "lastName": "User",
      "role": "USER",
      "active": true,
      "createdAt": "2026-06-20T10:00:00Z"
    }
  },
  "timestamp": "2026-06-20T10:00:00Z"
}
```

**Save the `accessToken` value** — you need it for all subsequent requests.

---

### Step 2 — Login

> Skip this step if you just registered — you already have a token.

**POST** `http://localhost:8080/api/auth/login`

**Headers:**
```
Content-Type: application/json
```

**Request body:**
```json
{
  "email": "tester@example.com",
  "password": "Test@1234"
}
```

**Expected response — 200 OK:** (same shape as register)

---

### Step 3 — Upload a Report

**POST** `http://localhost:8080/api/reports`

**Headers:**
```
Authorization: Bearer <accessToken>
Content-Type: multipart/form-data
```

**Form fields:**

| Field | Type | Value |
|---|---|---|
| `title` | text | `My JMeter Load Test` |
| `description` | text | `Homepage stress test — 50 users` |
| `tool` | text | `JMETER` |
| `file` | file | Upload the CSV file from [Section 5](#5-sample-test-data-files) |

**Expected response — 201 Created:**
```json
{
  "success": true,
  "message": "Report uploaded",
  "data": {
    "id": "a1b2c3d4-0000-0000-0000-000000000001",
    "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "title": "My JMeter Load Test",
    "description": "Homepage stress test — 50 users",
    "tool": "JMETER",
    "status": "UPLOADED",
    "fileName": "jmeter-results.csv",
    "fileSize": 1024,
    "metrics": [],
    "createdAt": "2026-06-20T10:01:00Z",
    "updatedAt": "2026-06-20T10:01:00Z"
  },
  "timestamp": "2026-06-20T10:01:00Z"
}
```

**Save the `data.id` value** — this is your `reportId`, needed in Steps 4–5.

---

### Step 4 — Wait for AI Analysis

The analysis happens **automatically in the background** via Kafka. There is nothing to call here — the analysis-service picked up the Kafka event the moment you uploaded the report.

- **Fast models (Claude Haiku, GPT-3.5):** ~5–10 seconds
- **Slower models (Claude Sonnet, GPT-4):** ~15–30 seconds

You can track the status by calling the analysis endpoint in the next step.

---

### Step 5 — View Analysis Results

**GET** `http://localhost:8080/api/analyses/report/{reportId}`

Replace `{reportId}` with the ID from Step 3.

**Headers:**
```
Authorization: Bearer <accessToken>
```

**Expected response — 200 OK (when COMPLETED):**
```json
{
  "success": true,
  "message": "Analysis retrieved",
  "data": {
    "id": "b2c3d4e5-0000-0000-0000-000000000002",
    "reportId": "a1b2c3d4-0000-0000-0000-000000000001",
    "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "status": "COMPLETED",
    "aiModel": "anthropic/claude-3.5-sonnet",
    "findings": [
      {
        "id": "c3d4e5f6-0000-0000-0000-000000000003",
        "severity": "HIGH",
        "category": "Response Time",
        "title": "POST /api/orders exceeds acceptable response time threshold",
        "description": "The endpoint is returning responses in 3200ms on average, which is 6x the recommended 500ms threshold under 50 concurrent users.",
        "recommendation": "Add database indexes on order_date and user_id columns. Consider caching the product catalogue to reduce DB lookups.",
        "affectedEndpoint": "POST /api/orders",
        "metricValue": "3200ms",
        "thresholdValue": "500ms"
      },
      {
        "id": "d4e5f6a7-0000-0000-0000-000000000004",
        "severity": "CRITICAL",
        "category": "Error Rate",
        "title": "503 errors detected — service under load",
        "description": "8.5% of requests are returning 503 Service Unavailable, indicating the server is dropping connections under stress.",
        "recommendation": "Increase connection pool size. Add horizontal scaling or a load balancer. Set max-connections in Tomcat config.",
        "affectedEndpoint": "GET /api/products",
        "metricValue": "8.5%",
        "thresholdValue": "< 1%"
      }
    ],
    "createdAt": "2026-06-20T10:01:05Z",
    "updatedAt": "2026-06-20T10:01:30Z"
  }
}
```

> **Status = `IN_PROGRESS`?** Wait a few more seconds and call again.  
> **Status = `FAILED`?** The AI call failed — check that `OPENROUTER_API_KEY` is valid.

---

### Step 6 — Check Notifications

**GET** `http://localhost:8080/api/notifications`

**Headers:**
```
Authorization: Bearer <accessToken>
```

**Expected response — 200 OK:**
```json
{
  "success": true,
  "message": "Notifications retrieved",
  "data": {
    "content": [
      {
        "id": "e5f6a7b8-0000-0000-0000-000000000005",
        "type": "ANALYSIS_COMPLETED",
        "title": "Analysis Complete — 2 finding(s)",
        "message": "Your performance report has been analysed. 2 issue(s) were identified. View your results to see recommendations.",
        "referenceId": "b2c3d4e5-0000-0000-0000-000000000002",
        "referenceType": "ANALYSIS",
        "read": false,
        "readAt": null,
        "createdAt": "2026-06-20T10:01:31Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "last": true
  }
}
```

**Mark the notification as read:**

**PATCH** `http://localhost:8080/api/notifications/{notificationId}/read`

```
Authorization: Bearer <accessToken>
```

---

## 3. Postman Setup

### Collection structure (recommended)

Create a Postman collection named `AI Performance Platform` with these folders:

```
📁 AI Performance Platform
  📁 Auth
    POST Register
    POST Login
    POST Refresh Token
    POST Logout
  📁 Users
    GET My Profile
    PUT Update Profile
    GET All Users (Admin)
  📁 Reports
    POST Upload Report
    GET My Reports
    GET Report by ID
    DELETE Report
    POST Re-trigger Analysis
  📁 Analyses
    GET Analysis by Report ID
    GET My Analyses
    GET Analysis by ID
  📁 Notifications
    GET All Notifications
    GET Unread Notifications
    GET Unread Count
    PATCH Mark as Read
    PATCH Mark All as Read
```

### Collection variables

In your collection → **Variables** tab, set:

| Variable | Initial Value | Description |
|---|---|---|
| `baseUrl` | `http://localhost:8080` | Gateway URL |
| `accessToken` | *(empty)* | Auto-filled by login test script |
| `refreshToken` | *(empty)* | Auto-filled by login test script |
| `reportId` | *(empty)* | Copy from upload response |
| `analysisId` | *(empty)* | Copy from analysis response |
| `notificationId` | *(empty)* | Copy from notification response |

Use `{{baseUrl}}` and `{{accessToken}}` in every request.

### Auto-capture token after login/register

In your **Login** request → **Tests** tab, paste:

```javascript
const response = pm.response.json();
if (response.success && response.data) {
    pm.collectionVariables.set("accessToken", response.data.accessToken);
    pm.collectionVariables.set("refreshToken", response.data.refreshToken);
    console.log("Tokens saved. Access token expires in " + response.data.expiresIn + "s");
}
```

Do the same for the **Register** request.

### Auto-capture reportId after upload

In your **Upload Report** request → **Tests** tab:

```javascript
const response = pm.response.json();
if (response.success && response.data) {
    pm.collectionVariables.set("reportId", response.data.id);
    console.log("Report ID saved: " + response.data.id);
}
```

### Collection-level Authorization

In the collection → **Authorization** tab:
- Type: `Bearer Token`
- Token: `{{accessToken}}`

All requests inside the collection inherit this automatically. Auth endpoints override it with `No Auth`.

### Pre-request script for token refresh (optional)

In collection-level **Pre-request Script**, add automatic token refresh:

```javascript
// Auto-refresh if token is about to expire
const expiresAt = pm.collectionVariables.get("tokenExpiresAt");
const now = Date.now();

if (expiresAt && now > (parseInt(expiresAt) - 60000)) {
    const refreshToken = pm.collectionVariables.get("refreshToken");
    
    pm.sendRequest({
        url: pm.collectionVariables.get("baseUrl") + "/api/auth/refresh",
        method: "POST",
        header: { "Content-Type": "application/json" },
        body: {
            mode: "raw",
            raw: JSON.stringify({ refreshToken: refreshToken })
        }
    }, (err, res) => {
        if (!err && res.json().success) {
            pm.collectionVariables.set("accessToken", res.json().data.accessToken);
            pm.collectionVariables.set("tokenExpiresAt", now + 900000); // 15 min
        }
    });
}
```

Set `tokenExpiresAt` in the login Tests script too:
```javascript
pm.collectionVariables.set("tokenExpiresAt", Date.now() + 900000);
```

---

## 4. Swagger UI Setup

Each service exposes its own Swagger UI. Use these for exploring individual service APIs without going through the gateway:

| Service | Swagger URL | Note |
|---|---|---|
| User Service | `http://localhost:8081/swagger-ui.html` | Auth endpoints — no token needed |
| Report Service | `http://localhost:8082/swagger-ui.html` | Must set token manually |
| Analysis Service | `http://localhost:8083/swagger-ui.html` | Must set token manually |
| Notification Service | `http://localhost:8084/swagger-ui.html` | Must set token manually |

### How to authenticate in Swagger UI

1. Open any Swagger URL
2. Click the **Authorize** button (lock icon, top right)
3. In the `bearerAuth` section, enter your `accessToken` (without the word "Bearer")
4. Click **Authorize** → **Close**
5. All endpoints now include the `Authorization: Bearer` header automatically

> **Note:** When testing via Swagger UI, requests go directly to that service (bypassing the gateway). The `X-User-ID` and `X-User-Role` headers are NOT automatically injected by the gateway. For services that require these headers (report, analysis, notification), pass them manually:
> - `X-User-ID: <your-user-id>`
> - `X-User-Role: USER`

For full end-to-end testing (including gateway JWT validation), use **Postman** and call `http://localhost:8080`.

---

## 5. Sample Test Data Files

### JMeter CSV format (`jmeter-results.csv`)

Create this file and save it as `jmeter-results.csv`:

```csv
timeStamp,elapsed,label,responseCode,responseMessage,threadName,success,bytes,grpThreads,allThreads,Latency
1700000000000,245,GET /api/products,200,OK,Thread Group 1-1,true,15234,50,50,12
1700000000500,312,GET /api/products,200,OK,Thread Group 1-2,true,15234,50,50,15
1700000001000,3200,POST /api/orders,200,OK,Thread Group 1-3,true,567,50,50,145
1700000001500,2950,POST /api/orders,200,OK,Thread Group 1-4,true,567,50,50,132
1700000002000,4100,POST /api/orders,503,Service Unavailable,Thread Group 1-5,false,89,50,50,10
1700000002500,4800,POST /api/orders,503,Service Unavailable,Thread Group 1-6,false,89,50,50,10
1700000003000,180,GET /api/products,200,OK,Thread Group 1-7,true,15234,50,50,9
1700000003500,190,GET /api/products,200,OK,Thread Group 1-8,true,15234,50,50,8
1700000004000,5200,POST /api/checkout,504,Gateway Timeout,Thread Group 1-9,false,0,50,50,10
1700000004500,4900,POST /api/checkout,504,Gateway Timeout,Thread Group 1-10,false,0,50,50,10
1700000005000,220,GET /api/users/profile,200,OK,Thread Group 1-11,true,2048,50,50,11
1700000005500,198,GET /api/users/profile,200,OK,Thread Group 1-12,true,2048,50,50,10
```

This file simulates:
- Fast product listing (200–300ms) ✓
- Slow order creation (3–5s) — HIGH severity finding expected
- 503 errors on orders (~30% error rate) — CRITICAL severity expected
- 504 timeouts on checkout — CRITICAL severity expected

### Gatling simulation log (`gatling-results.log`)

Create this file and save it as `gatling-simulation.log` (use `tool=GATLING`):

```
RUN	GatewaySimulation	gatewaysimulation	1700000000000	description	3.9.5
REQUEST	GET /api/homepage		1700000000100	1700000000350	OK
REQUEST	POST /api/auth/login		1700000000400	1700000001200	OK
REQUEST	GET /api/dashboard		1700000001300	1700000004300	OK
REQUEST	GET /api/dashboard		1700000004400	1700000007800	OK
REQUEST	GET /api/dashboard		1700000007900	1700000012000	KO	status.find.is(200), but actually found 503
REQUEST	POST /api/reports		1700000012100	1700000018500	OK
REQUEST	POST /api/reports		1700000018600	1700000025000	KO	status.find.is(201), but actually found 500
REQUEST	GET /api/notifications		1700000025100	1700000025400	OK
REQUEST	DELETE /api/reports/old		1700000025500	1700000055500	KO	Request timeout to server
RUN END 1700000060000
```

---

## 6. Authentication APIs

Base: `http://localhost:8080/api/auth` — **No Authorization header required**

---

### POST /api/auth/register

Register a new user.

**Request:**
```json
{
  "email": "user@example.com",
  "password": "Password@123",
  "firstName": "Jane",
  "lastName": "Doe"
}
```

**Validation rules:**
- `email` — must be valid email format, unique
- `password` — minimum 8 characters (enforced by `@NotBlank` + `@Size`)
- `firstName`, `lastName` — required, not blank

**Success — 201:**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "accessToken": "eyJ...",
    "refreshToken": "eyJ...",
    "tokenType": "Bearer",
    "expiresIn": 900,
    "user": { "id": "...", "email": "user@example.com", "role": "USER", ... }
  }
}
```

**Failure — 409 (email already exists):**
```json
{
  "success": false,
  "message": "Email already registered: user@example.com",
  "errorCode": "EMAIL_ALREADY_EXISTS"
}
```

**Failure — 400 (validation):**
```json
{
  "success": false,
  "message": "Validation failed",
  "errorCode": "VALIDATION_ERROR",
  "errors": [
    { "field": "email", "message": "must be a well-formed email address" },
    { "field": "password", "message": "must not be blank" }
  ]
}
```

---

### POST /api/auth/login

**Request:**
```json
{
  "email": "user@example.com",
  "password": "Password@123"
}
```

**Success — 200:** (same structure as register)

**Failure — 401 (wrong password or email not found):**
```json
{
  "success": false,
  "message": "Invalid email or password",
  "errorCode": "INVALID_CREDENTIALS"
}
```

**Failure — 401 (deactivated account):**
```json
{
  "success": false,
  "message": "Account is deactivated",
  "errorCode": "UNAUTHORIZED"
}
```

---

### POST /api/auth/refresh

Exchange a refresh token for a new access token. The old refresh token is revoked.

**Request:**
```json
{
  "refreshToken": "eyJ..."
}
```

**Success — 200:**
```json
{
  "success": true,
  "message": "Token refreshed",
  "data": {
    "accessToken": "eyJ...(new token)...",
    "refreshToken": "eyJ...(new refresh token)...",
    "tokenType": "Bearer",
    "expiresIn": 900
  }
}
```

**Failure — 401 (expired or revoked):**
```json
{
  "success": false,
  "message": "Refresh token has expired or been revoked",
  "errorCode": "UNAUTHORIZED"
}
```

---

### POST /api/auth/logout

Revokes the refresh token. The access token remains valid until it naturally expires (15 min).

**Request:**
```json
{
  "refreshToken": "eyJ..."
}
```

**Success — 200:**
```json
{
  "success": true,
  "message": "Logout successful",
  "data": null
}
```

---

## 7. User APIs

Base: `http://localhost:8080/api/users`  
**All require:** `Authorization: Bearer <accessToken>`

---

### GET /api/users/me

Get your own profile.

**No request body.**

**Success — 200:**
```json
{
  "success": true,
  "message": "Profile retrieved",
  "data": {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "email": "user@example.com",
    "firstName": "Jane",
    "lastName": "Doe",
    "role": "USER",
    "active": true,
    "createdAt": "2026-06-20T10:00:00Z"
  }
}
```

---

### PUT /api/users/me

Update your first and last name.

**Request:**
```json
{
  "firstName": "Janet",
  "lastName": "Doe-Smith"
}
```

**Success — 200:** Returns updated `UserResponse` (same shape as GET /me).

**Note:** Email and role cannot be changed via this endpoint. Password change is not implemented in this version.

---

## 8. Report APIs

Base: `http://localhost:8080/api/reports`  
**All require:** `Authorization: Bearer <accessToken>`

---

### POST /api/reports — Upload a report

**Content-Type:** `multipart/form-data`

**Form fields:**

| Field | Required | Type | Notes |
|---|---|---|---|
| `title` | Yes | text | Display name for the report |
| `description` | No | text | Optional description |
| `tool` | Yes | text | Must be exactly `JMETER` or `GATLING` (uppercase) |
| `file` | Yes | file | The report file (max 50MB) |

**Postman — how to set up multipart:**
1. Set method to `POST`, URL to `{{baseUrl}}/api/reports`
2. Go to **Body** tab → select **form-data**
3. Add key `title` (Text) = `My Load Test`
4. Add key `description` (Text) = `Optional description`
5. Add key `tool` (Text) = `JMETER`
6. Add key `file` (File) → click **Select Files** → choose your CSV

**Success — 201:**
```json
{
  "success": true,
  "message": "Report uploaded",
  "data": {
    "id": "a1b2c3d4-...",
    "title": "My Load Test",
    "tool": "JMETER",
    "status": "UPLOADED",
    "fileName": "jmeter-results.csv",
    "fileSize": 856,
    "metrics": [],
    "createdAt": "2026-06-20T10:05:00Z"
  }
}
```

**Failure — 400 (unsupported tool):**
```json
{
  "success": false,
  "message": "Unsupported tool: LOCUST. Must be JMETER or GATLING",
  "errorCode": "UNSUPPORTED_TOOL"
}
```

**Failure — 400 (empty file):**
```json
{
  "success": false,
  "message": "File must not be empty",
  "errorCode": "EMPTY_FILE"
}
```

---

### GET /api/reports

List your uploaded reports (paginated, newest first).

**Query params:**

| Param | Default | Description |
|---|---|---|
| `page` | `0` | Page number (0-based) |
| `size` | `20` | Items per page |

**Example:** `GET /api/reports?page=0&size=5`

**Success — 200:**
```json
{
  "success": true,
  "message": "Reports retrieved",
  "data": {
    "content": [
      {
        "id": "a1b2c3d4-...",
        "title": "My Load Test",
        "tool": "JMETER",
        "status": "UPLOADED",
        "fileName": "jmeter-results.csv",
        "fileSize": 856,
        "createdAt": "2026-06-20T10:05:00Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "last": true
  }
}
```

**Note:** The list returns `ReportSummaryResponse` — no `metrics` field. Use `GET /reports/{id}` for full details.

---

### GET /api/reports/{id}

Get a single report with all parsed metrics.

**Example:** `GET /api/reports/a1b2c3d4-...`

**Success — 200:**
```json
{
  "success": true,
  "message": "Report retrieved",
  "data": {
    "id": "a1b2c3d4-...",
    "title": "My Load Test",
    "tool": "JMETER",
    "status": "UPLOADED",
    "fileName": "jmeter-results.csv",
    "fileSize": 856,
    "metrics": [
      {
        "endpoint": "POST /api/orders",
        "httpMethod": "POST",
        "samples": 6,
        "averageResponseTimeMs": 3500.00,
        "minResponseTimeMs": 2950.00,
        "maxResponseTimeMs": 5200.00,
        "p50ResponseTimeMs": 3200.00,
        "p90ResponseTimeMs": 4900.00,
        "p95ResponseTimeMs": 5000.00,
        "p99ResponseTimeMs": 5180.00,
        "throughput": 1.2,
        "errorRate": 0.3333,
        "errorCount": 2
      }
    ],
    "createdAt": "2026-06-20T10:05:00Z",
    "updatedAt": "2026-06-20T10:05:00Z"
  }
}
```

**Failure — 404 (not found or not yours):**
```json
{
  "success": false,
  "message": "Report not found with id: a1b2c3d4-...",
  "errorCode": "RESOURCE_NOT_FOUND"
}
```

---

### DELETE /api/reports/{id}

Delete a report and its uploaded file from storage.

**No request body.**

**Success — 204 No Content** (empty body)

**Failure — 404:** Report not found or belongs to another user.

---

### POST /api/reports/{id}/analyse

Manually re-trigger AI analysis on a report that has already been processed. Useful if the first analysis failed or you want a fresh run.

**No request body.**

**Success — 200:**
```json
{
  "success": true,
  "message": "Analysis requested",
  "data": null
}
```

**Failure — 400 (report not in PROCESSED state):**
```json
{
  "success": false,
  "message": "Report must be in PROCESSED state to request analysis",
  "errorCode": "INVALID_STATE"
}
```

---

## 9. Analysis APIs

Base: `http://localhost:8080/api/analyses`  
**All require:** `Authorization: Bearer <accessToken>`

---

### GET /api/analyses/report/{reportId}

Get the AI analysis for a specific report. This is the primary way to check analysis results.

**Example:** `GET /api/analyses/report/a1b2c3d4-...`

**Possible status values:**

| Status | Meaning |
|---|---|
| `IN_PROGRESS` | AI is currently analysing the file — wait and try again |
| `COMPLETED` | Analysis done — findings available |
| `FAILED` | AI call failed (check API key, retry) |

**Success — 200 (COMPLETED):**
```json
{
  "success": true,
  "message": "Analysis retrieved",
  "data": {
    "id": "b2c3d4e5-...",
    "reportId": "a1b2c3d4-...",
    "status": "COMPLETED",
    "aiModel": "anthropic/claude-3.5-sonnet",
    "findings": [
      {
        "id": "c3d4e5f6-...",
        "severity": "CRITICAL",
        "category": "Error Rate",
        "title": "High error rate on POST /api/orders",
        "description": "33% of order creation requests are failing with 503 errors under 50 concurrent users.",
        "recommendation": "Increase server capacity or add circuit breaker. Review connection pool settings.",
        "affectedEndpoint": "POST /api/orders",
        "metricValue": "33.3%",
        "thresholdValue": "< 1%"
      },
      {
        "id": "d4e5f6a7-...",
        "severity": "HIGH",
        "category": "Response Time",
        "title": "Order creation P99 exceeds 5 seconds",
        "description": "99th percentile response time for POST /api/orders is 5180ms, well above the 500ms target.",
        "recommendation": "Profile the order creation endpoint. Check for N+1 database queries and missing indexes.",
        "affectedEndpoint": "POST /api/orders",
        "metricValue": "5180ms",
        "thresholdValue": "500ms"
      }
    ],
    "createdAt": "2026-06-20T10:01:05Z",
    "updatedAt": "2026-06-20T10:01:30Z"
  }
}
```

**Finding severity levels (from most to least critical):**

| Severity | Colour | Meaning |
|---|---|---|
| `CRITICAL` | 🔴 Red | Immediate action required — production impact |
| `HIGH` | 🟠 Orange | Significant degradation — fix before release |
| `MEDIUM` | 🟡 Yellow | Moderate impact — fix in next sprint |
| `LOW` | 🔵 Blue | Minor concern — address when possible |
| `INFO` | ⚪ Grey | Informational observation — no action needed |

**Failure — 404 (analysis not found — may not have started yet):**
```json
{
  "success": false,
  "message": "Analysis not found with reportId: a1b2c3d4-...",
  "errorCode": "RESOURCE_NOT_FOUND"
}
```

> If you get 404 immediately after upload, wait 2–3 seconds and try again. The Kafka event may not have been consumed yet.

---

### GET /api/analyses

List all your analyses (paginated, newest first).

**Query params:** `page` (default 0), `size` (default 20)

**Success — 200:**
```json
{
  "success": true,
  "message": "Analyses retrieved",
  "data": {
    "content": [
      {
        "id": "b2c3d4e5-...",
        "reportId": "a1b2c3d4-...",
        "status": "COMPLETED",
        "aiModel": "anthropic/claude-3.5-sonnet",
        "findings": [...],
        "createdAt": "2026-06-20T10:01:05Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "last": true
  }
}
```

---

### GET /api/analyses/{id}

Get a specific analysis by its analysis ID (not report ID).

**Example:** `GET /api/analyses/b2c3d4e5-...`

**Success — 200:** Same as `GET /api/analyses/report/{reportId}`

---

## 10. Notification APIs

Base: `http://localhost:8080/api/notifications`  
**All require:** `Authorization: Bearer <accessToken>`

---

### GET /api/notifications

Get all your notifications (paginated, newest first).

**Query params:** `page` (default 0), `size` (default 20)

**Success — 200:**
```json
{
  "success": true,
  "message": "Notifications retrieved",
  "data": {
    "content": [
      {
        "id": "e5f6a7b8-...",
        "type": "ANALYSIS_COMPLETED",
        "title": "Analysis Complete — 2 finding(s)",
        "message": "Your performance report has been analysed. 2 issue(s) were identified. View your results to see recommendations.",
        "referenceId": "b2c3d4e5-...",
        "referenceType": "ANALYSIS",
        "read": false,
        "readAt": null,
        "createdAt": "2026-06-20T10:01:31Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "last": true
  }
}
```

**Notification types:**

| Type | When created | Message |
|---|---|---|
| `ANALYSIS_COMPLETED` | AI analysis succeeded | "N finding(s) identified" |
| `ANALYSIS_FAILED` | AI call failed | "The AI analysis encountered an error. Please try again." |

---

### GET /api/notifications/unread

Returns only unread notifications.

**Query params:** `page`, `size`

**Success — 200:** Same shape as GET /notifications but only `read: false` items.

---

### GET /api/notifications/unread/count

Returns the number of unread notifications (useful for badge counts in UI).

**Success — 200:**
```json
{
  "success": true,
  "message": "Unread count retrieved",
  "data": {
    "count": 3
  }
}
```

---

### PATCH /api/notifications/{id}/read

Mark a single notification as read.

**No request body.**

**Success — 200:**
```json
{
  "success": true,
  "message": "Notification marked as read",
  "data": {
    "id": "e5f6a7b8-...",
    "type": "ANALYSIS_COMPLETED",
    "title": "Analysis Complete — 2 finding(s)",
    "read": true,
    "readAt": "2026-06-20T10:10:00Z",
    ...
  }
}
```

**Calling this on an already-read notification:** Returns 200 with the same notification — it is idempotent (safe to call multiple times).

---

### PATCH /api/notifications/read-all

Mark ALL your notifications as read in one call.

**No request body.**

**Success — 200:**
```json
{
  "success": true,
  "message": "All notifications marked as read",
  "data": null
}
```

---

## 11. Admin APIs

These endpoints require `role: ADMIN`. Regular `USER` accounts get **403 Forbidden**.

**How to create an admin account:** Connect directly to PostgreSQL and run:
```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'your-email@example.com';
```
Then log in again (the new access token will contain `role: ADMIN`).

---

### GET /api/users (Admin)

List all registered users (paginated).

**Query params:** `page`, `size`

**Headers:** `Authorization: Bearer <adminAccessToken>`

**Success — 200:**
```json
{
  "success": true,
  "message": "Users retrieved",
  "data": {
    "content": [
      { "id": "...", "email": "user@example.com", "role": "USER", "active": true, ... },
      { "id": "...", "email": "admin@example.com", "role": "ADMIN", "active": true, ... }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 2,
    "totalPages": 1,
    "last": true
  }
}
```

---

### GET /api/users/{id} (Admin)

Get any user's profile by their UUID.

---

### DELETE /api/users/{id} (Admin)

Deactivate a user account (sets `active = false`). The user can no longer log in.

**Success — 204 No Content**

---

### GET /api/reports/admin/all (Admin)

List all reports from all users.

**Query params:** `page`, `size`

---

### GET /api/analyses/admin/all (Admin)

List all analyses from all users.

**Query params:** `page`, `size`

---

## 12. Error Reference

### HTTP status codes

| Status | Meaning | Common causes |
|---|---|---|
| 200 | OK | Request succeeded |
| 201 | Created | Resource created (register, upload) |
| 204 | No Content | Delete succeeded |
| 400 | Bad Request | Validation failed, unsupported tool, empty file, invalid state |
| 401 | Unauthorized | Missing token, invalid token, expired token, wrong credentials |
| 403 | Forbidden | Valid token but insufficient role (e.g., USER trying admin endpoints) |
| 404 | Not Found | Resource doesn't exist or belongs to another user |
| 409 | Conflict | Email already registered |
| 500 | Internal Server Error | Unexpected server-side error |

### Standard error response shape

```json
{
  "success": false,
  "message": "Human-readable error description",
  "errorCode": "MACHINE_READABLE_CODE",
  "errors": [
    { "field": "email", "message": "Validation message" }
  ],
  "timestamp": "2026-06-20T10:00:00Z"
}
```

`errors` array only appears for validation failures (400).

### Common error codes

| Error Code | HTTP | Cause |
|---|---|---|
| `VALIDATION_ERROR` | 400 | Request body failed `@Valid` checks |
| `UNSUPPORTED_TOOL` | 400 | `tool` field is not `JMETER` or `GATLING` |
| `EMPTY_FILE` | 400 | Uploaded file has no content |
| `INVALID_STATE` | 400 | Re-analyse called on non-PROCESSED report |
| `INVALID_CREDENTIALS` | 401 | Wrong email or password |
| `UNAUTHORIZED` | 401 | Token invalid, expired, revoked, or missing |
| `EMAIL_ALREADY_EXISTS` | 409 | Email already registered |
| `RESOURCE_NOT_FOUND` | 404 | Entity not found or not accessible |

---

## 13. Testing Checklist

Use this checklist to confirm all functionality works end-to-end.

### Authentication

- [ ] Register with valid data → receives accessToken and refreshToken
- [ ] Register with duplicate email → 409 error
- [ ] Register with missing fields → 400 validation error
- [ ] Login with correct credentials → receives new tokens
- [ ] Login with wrong password → 401 error
- [ ] Refresh token → new accessToken issued, old refreshToken revoked
- [ ] Use revoked refreshToken to refresh → 401 error
- [ ] Logout → 200 success
- [ ] Access protected endpoint with no token → 401
- [ ] Access protected endpoint with expired token → 401

### User Profile

- [ ] GET /api/users/me → returns own profile
- [ ] PUT /api/users/me with valid data → profile updated
- [ ] GET /api/users (as USER) → 403 Forbidden
- [ ] GET /api/users (as ADMIN) → list of all users

### Reports

- [ ] Upload report with valid CSV + tool=JMETER → 201, report saved
- [ ] Upload report with tool=GATLING → 201, report saved
- [ ] Upload with unknown tool (e.g., LOCUST) → 400 error
- [ ] Upload without file → 400 error
- [ ] GET /api/reports → list appears with uploaded report
- [ ] GET /api/reports/{id} → full report with metrics
- [ ] GET /api/reports/{othersId} → 404 (can't see other users' reports)
- [ ] DELETE /api/reports/{id} → 204
- [ ] GET /api/reports/{deletedId} → 404

### Analysis (requires Kafka and OpenRouter)

- [ ] Upload a report → analysis-service automatically processes it
- [ ] GET /api/analyses/report/{reportId} → status IN_PROGRESS, then COMPLETED
- [ ] Completed analysis has findings array with severity/category/title/description/recommendation
- [ ] GET /api/analyses → list includes the analysis
- [ ] GET /api/analyses/{id} → same as by reportId
- [ ] POST /api/reports/{id}/analyse (on PROCESSED report) → 200

### Notifications

- [ ] After analysis completes → notification created automatically
- [ ] GET /api/notifications → notification appears with type ANALYSIS_COMPLETED
- [ ] GET /api/notifications/unread/count → count = 1
- [ ] PATCH /api/notifications/{id}/read → notification marked read
- [ ] GET /api/notifications/unread/count → count = 0
- [ ] Upload 3 reports → GET /api/notifications/unread/count → count = 3
- [ ] PATCH /api/notifications/read-all → all read
- [ ] GET /api/notifications/unread → empty list

### Token expiry scenario

- [ ] Wait 15 minutes after login
- [ ] Call any protected endpoint → 401 (token expired)
- [ ] POST /api/auth/refresh → new accessToken
- [ ] Use new accessToken → request succeeds

### Pagination

- [ ] Upload 5+ reports
- [ ] GET /api/reports?size=2 → only 2 results, `totalElements` shows correct total
- [ ] GET /api/reports?page=1&size=2 → next 2 results
- [ ] GET /api/reports?page=999 → empty content list (not 404)
