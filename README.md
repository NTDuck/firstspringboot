# firstspringboot

## How to Run

```bash
docker compose up -d
./mvnw spring-boot:run
```

## How to Obtain Access Token (JWT) from Keycloak

To fetch a fresh, unexpired access token:

```bash
EXPORT_TOKEN=$(curl -s -X POST "http://localhost:8081/realms/firstspringbootrealm/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=firstspringbootauthclient" \
  -d "client_secret=e3JK0b1YHtRcLX1EV4ec0vvPCshohF1p" \
  -d "username=firstspringbootuser" \
  -d "password=firstspringbootuserpassword" | jq -r .access_token)
```

> **Note:** If an expired or invalid JWT token is passed, Spring Security returns `HTTP 401 Unauthorized` with an empty response body (`Content-Length: 0`), resulting in a 0-byte downloaded file when saving output with `curl -o`. Always ensure you use a fresh access token.

## How to Export Excel Files

### Export Tasks (`tasks-export.xlsx`)

```bash
curl -X GET "http://localhost:8080/api/v1/tasks/export" \
  -H "Authorization: Bearer $EXPORT_TOKEN" \
  -o tasks-export.xlsx
```

### Export Users (`users-export.xlsx`)

```bash
curl -X GET "http://localhost:8080/api/v1/users/export" \
  -H "Authorization: Bearer $EXPORT_TOKEN" \
  -o users-export.xlsx
```
