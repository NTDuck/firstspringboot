# firstspringboot

## How to run

```bash
$ docker compose up -d
$ mvn spring-boot:run
```

## API Endpoints

### 1. Get All Tasks
Retrieve all tasks.

```bash
curl -X GET http://localhost:8080/api/v1/tasks
```

### 2. Get Task by ID
Retrieve details for a specific task by ID.

```bash
curl -X GET http://localhost:8080/api/v1/tasks/1
```

### 3. Create Task
Create a new task.

```bash
curl -X POST http://localhost:8080/api/v1/tasks \
  -H "Content-Type: application/json" \
  -d '{"description": "New Task"}'
```

### 4. Update Task
Update an existing task's description by ID.

```bash
curl -X PUT http://localhost:8080/api/v1/tasks/1 \
  -H "Content-Type: text/plain" \
  -d "Updated Task Description"
```

### 5. Delete Task
Delete a task by ID.

```bash
curl -X DELETE http://localhost:8080/api/v1/tasks/14
```
