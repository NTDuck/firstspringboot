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

# Export Users to MinIO (Returns Presigned URL)
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/users/export
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

# Export Tasks to MinIO (Returns Presigned URL)
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/tasks/export

# Delete Task by ID
curl -s -X DELETE http://localhost:8080/api/v1/tasks/1 \
  -H "Authorization: Bearer $TOKEN"

# Delete All Tasks
curl -s -X DELETE http://localhost:8080/api/v1/tasks \
  -H "Authorization: Bearer $TOKEN"
```

### Audit Logs

```bash
# Query Audit Logs (Supports day, serviceName, actorUserId, actorUsername, action, result params)
curl -s -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/v1/audit?serviceName=UserService"
```
