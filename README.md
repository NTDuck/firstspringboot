# firstspringboot

## How to run

```bash
$ docker compose up -d
$ mvn spring-boot:run
```
# 1. Get an access token from Keycloak

# 2. Call the API with that token
curl \
-H "Authorization: Bearer $TOKEN" \
http://localhost:8080/api/v1/tasks

# 3. Inspect the authenticated identity
curl \
-H "Authorization: Bearer $TOKEN" \
http://localhost:8080/me