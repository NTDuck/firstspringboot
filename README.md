# firstspringboot

A Spring Boot 2.4 service implementing task and user management, asynchronous Excel import/export via MinIO object storage, Keycloak OAuth2 Resource Server authentication and background user sync, and auditing with Spring AOP.

## Prerequisites & Setup

- **Java**: OpenJDK 11
- **Docker & Docker Compose**: For MariaDB, Keycloak, and MinIO infrastructure

### Start Infrastructure

```bash
docker-compose up -d
```

### Run Application

```bash
./mvnw spring-boot:run
```

### Run Tests

```bash
./mvnw clean test
```

## Architecture & Code Conventions

- **Granular Service Separation**: Follows single-responsibility pattern with small, cohesive helper methods and explicit domain boundaries.
- **Dependency Injection**: Constructor-based injection via Lombok `@RequiredArgsConstructor` and `private final` fields.
- **Transactional & Caching Semantics**: Declarative `@Transactional(readOnly = true)` for query paths, `@Transactional` for writes, and Spring Cache annotations (`@Cacheable`, `@CachePut`, `@CacheEvict`) on service methods.
- **Auditing**: Method-level `@Auditable` intercepted by `AuditAspect`, automatically logging actor identity, service name, action, and execution outcome to `audit_events`.
- **Global Error Handling**: Custom domain exceptions extend `BaseGloballyHandledException` and are translated to structured JSON responses via `GlobalExceptionHandler`.
- **JPA Entities & Auditing**: Entities extend `BaseEntity` / `AuditableEntity` using Lombok `@SuperBuilder` and JPA auditing listeners (`@CreatedDate`, `@LastModifiedDate`, `@CreatedBy`, `@LastModifiedBy`).

## Authentication

Obtain a JWT access token from Keycloak:

```bash
TOKEN=$(curl -s -X POST "http://localhost:8081/realms/firstspringbootrealm/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=firstspringbootauthclient" \
  -d "client_secret=e3JK0b1YHtRcLX1EV4ec0vvPCshohF1p" \
  -d "username=firstspringbootuser" \
  -d "password=firstspringbootuserpassword" | jq -r .access_token)
```

## API Reference & Verification

### System & Health

```bash
# Health Status
curl -s http://localhost:8080/actuator/health

# Ping (Returns 204 No Content)
curl -i -s http://localhost:8080/ping
```

### User Management

```bash
# Get Current User Profile
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/users/profile

# List Users
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/users

# Create User
curl -s -X POST http://localhost:8080/api/v1/users \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"keycloakId":"kc-101","name":"John Doe","email":"john@example.com","firstName":"John","lastName":"Doe"}'

# Trigger Users Export (Returns 202 Accepted with Location: /api/v1/exports/{id})
curl -i -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/users/export

# Trigger Users Import (Returns 202 Accepted with Location: /api/v1/imports/{id})
curl -i -s -X POST -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/users/import
```

### Task Management

```bash
# List Tasks
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/tasks

# Create Task
curl -s -X POST http://localhost:8080/api/v1/tasks \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"description":"Sample task description"}'

# Get Task by ID
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/tasks/1

# Update Task by ID
curl -s -X PUT http://localhost:8080/api/v1/tasks/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"description":"Updated task description"}'

# Trigger Tasks Export (Returns 202 Accepted with Location: /api/v1/exports/{id})
curl -i -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/tasks/export

# Trigger Tasks Import (Returns 202 Accepted with Location: /api/v1/imports/{id})
curl -i -s -X POST -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/tasks/import

# Delete Task by ID
curl -s -X DELETE http://localhost:8080/api/v1/tasks/1 \
  -H "Authorization: Bearer $TOKEN"

# Delete All Tasks
curl -s -X DELETE http://localhost:8080/api/v1/tasks \
  -H "Authorization: Bearer $TOKEN"
```

### Exports Workflow

1. **Trigger Export**: Call `GET /api/v1/tasks/export` or `GET /api/v1/users/export` $\rightarrow$ `202 Accepted` with `Location: /api/v1/exports/{id}`.
2. **Poll Status**:
   ```bash
   curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/exports/1
   ```
   Transitions: `PENDING` $\rightarrow$ `PROCESSING` $\rightarrow$ `SUCCESS` / `FAILED`.
3. **Retrieve Presigned Download URL**:
   ```bash
   curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/exports/1/download
   ```

### Imports Workflow

1. **Initiate Import**:
   Call `POST /api/v1/tasks/import` or `POST /api/v1/users/import`. Returns `202 Accepted` with `Location: /api/v1/imports/{id}`.
2. **Inspect Job & Presigned Upload URL**:
   ```bash
   curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/imports/1
   ```
   The `url` field contains the presigned HTTP PUT URL.
3. **Upload Spreadsheet**:
   Upload the Excel (`.xlsx`) file to the presigned URL:
   ```bash
   curl -T /path/to/data.xlsx "http://localhost:9000/firstspringboot/imports-1.xlsx?..."
   ```
4. **Asynchronous Processing**:
   `ImportServiceImpl.Processor` detects the uploaded object, validates structure and contents, upserts records to the repository, and updates status to `SUCCESS` (or `FAILED` with error diagnostics).
5. **Poll Status**:
   ```bash
   curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/imports/1
   ```

### Audit Logs

```bash
# Query Audit Logs (Filter by day, serviceName, actorUserId, actorUsername, action, result)
curl -s -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/v1/audit?serviceName=UserService"
curl -s -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/v1/audit?action=CREATE_TASK"
curl -s -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/v1/audit?result=true"
```
