# firstspringboot

## Setup

```bash
docker-compose up -d
./mvnw spring-boot:run
```

## Authentication

Obtain JWT access token from Keycloak:

```bash
TOKEN=$(curl -s -X POST "http://localhost:8081/realms/firstspringbootrealm/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=firstspringbootauthclient" \
  -d "client_secret=e3JK0b1YHtRcLX1EV4ec0vvPCshohF1p" \
  -d "username=firstspringbootuser" \
  -d "password=firstspringbootuserpassword" | jq -r .access_token)
```

## API Routes Verification

### System & Health

```bash
# Health Status
curl -s http://localhost:8080/actuator/health

# Ping
curl -s http://localhost:8080/api/v1/ping
```

### User Management

```bash
# Get User Profile
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

### Exports

```bash
# 1. Trigger export via GET /api/v1/tasks/export or GET /api/v1/users/export -> 202 Accepted, Location: /api/v1/exports/{id}

# 2. Check Export Status (PENDING -> PROCESSING -> SUCCESS / FAILED)
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/exports/1

# 3. Retrieve Presigned Download URL (when status is SUCCESS)
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/exports/1/download
```

### Imports

The import workflow is fully asynchronous:

1. **Initiate Import**:
   Call `POST /api/v1/tasks/import` or `POST /api/v1/users/import`. This creates a new `Import` job in `PENDING` status, generates a presigned MinIO PUT upload URL, launches background asynchronous processing, and returns `202 Accepted` with `Location: /api/v1/imports/{id}`.

2. **Retrieve Presigned Upload URL & Inspect Status**:
   ```bash
   curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/imports/1
   ```
   The response contains `url`, which is the presigned PUT URL.

3. **Upload File to Storage**:
   Upload the Excel (`.xlsx`) file directly to the presigned upload URL via HTTP PUT:
   ```bash
   curl -T /path/to/data.xlsx "http://localhost:9000/firstspringboot/imports-1.xlsx?..."
   ```

4. **Automatic Asynchronous Processing**:
   The background `ImportServiceImpl.Processor` detects the completed upload in object storage, validates file integrity and business rules, upserts valid records into the local database (and Keycloak for users), and updates the status to `SUCCESS` (or `FAILED` if validation errors occur).

5. **Track Status & Completion**:
   Reload/poll the status endpoint to observe transitions (`PENDING` $\rightarrow$ `PROCESSING` $\rightarrow$ `SUCCESS` / `FAILED`):
   ```bash
   curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/imports/1
   ```

### Audit Logs

```bash
# Query Audit Logs (Supports day, serviceName, actorUserId, actorUsername, action, result params)
curl -s -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/v1/audit?serviceName=UserService"
curl -s -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/v1/audit?action=CREATE_TASK"
```
