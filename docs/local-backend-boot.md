# Local Backend Boot Verification

This checklist verifies that the backend foundation can boot locally before adding more product features.

## 1. Prerequisites

Required local tools:

```bash
docker --version
docker compose version
java -version
gradle -v
```

Expected:

- Docker daemon is running.
- Java 21 or newer is available.
- Gradle is installed, or a Gradle wrapper is added later.

This repository currently has Gradle project files, but no generated Gradle wrapper.

## 2. Environment Variables

Copy the backend env template:

```bash
cp backend/.env.example backend/.env
```

For local development, defaults are:

```bash
SERVER_PORT=8080
DB_URL=jdbc:postgresql://localhost:5432/ai_elearning
DB_USERNAME=ai_learning
DB_PASSWORD=ai_learning
POSTGRES_PORT=5432
REDIS_HOST=localhost
REDIS_PORT=6379
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=ai_learning
RABBITMQ_PASSWORD=ai_learning
MINIO_ENDPOINT=http://localhost:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin
MINIO_BUCKET=learning-materials
JWT_ISSUER=ai-elearning
JWT_SECRET=change-this-development-secret-change-this
JWT_ACCESS_TOKEN_TTL_MINUTES=60
CORS_ALLOWED_ORIGINS=http://localhost:3000
```

Before real deployment, replace `JWT_SECRET`, database password, RabbitMQ password, and MinIO credentials.

## 3. Verify Gradle Configuration

From the backend directory:

```bash
cd backend
gradle tasks
gradle clean test
```

Expected:

- Gradle resolves Spring Boot `3.3.5`.
- Java toolchain targets Java 21.
- Tests task runs, even if there are no tests yet.

If `gradle` is not installed, install Gradle or add a Gradle wrapper before continuing:

```bash
gradle wrapper
./gradlew clean test
```

## 4. Verify Docker Compose Services

Validate compose syntax:

```bash
docker compose -f infra/docker-compose.yml config
```

Start infrastructure:

```bash
docker compose -f infra/docker-compose.yml up -d
```

Check service health:

```bash
docker compose -f infra/docker-compose.yml ps
```

Expected services:

- `ai-elearning-postgres`
- `ai-elearning-redis`
- `ai-elearning-rabbitmq`
- `ai-elearning-minio`
- `ai-elearning-minio-init`

Useful URLs:

- RabbitMQ management: `http://localhost:15672`
- MinIO console: `http://localhost:9001`
- MinIO API: `http://localhost:9000`

Default local credentials:

- RabbitMQ: `ai_learning / ai_learning`
- MinIO: `minioadmin / minioadmin`

## 5. Verify PostgreSQL And pgvector

Check PostgreSQL:

```bash
docker exec -it ai-elearning-postgres psql -U ai_learning -d ai_elearning -c "SELECT version();"
```

Check pgvector extension:

```bash
docker exec -it ai-elearning-postgres psql -U ai_learning -d ai_elearning -c "SELECT extname FROM pg_extension WHERE extname = 'vector';"
```

Expected:

```text
vector
```

## 6. Verify Flyway Migrations

After the backend boots, verify tables:

```bash
docker exec -it ai-elearning-postgres psql -U ai_learning -d ai_elearning -c "\dt"
```

Expected tables:

- `flyway_schema_history`
- `users`
- `subjects`
- `materials`

Check Flyway history:

```bash
docker exec -it ai-elearning-postgres psql -U ai_learning -d ai_elearning -c "SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank;"
```

Expected:

- version `1`
- description `initial schema`
- success `true`

### Troubleshooting: PostgreSQL Password Authentication Failed

If Spring Boot fails with:

```text
FATAL: password authentication failed for user "ai_learning"
```

then the application reached PostgreSQL, but the password in the running database does not match `DB_PASSWORD`.

Common causes:

- An old Docker volume was initialized with a different password.
- A local PostgreSQL server is already using port `5432`.
- The app is loading different environment variables than expected.

Check what is listening on port `5432`:

```bash
docker ps --format "table {{.Names}}\t{{.Image}}\t{{.Ports}}"
```

Check the current env values:

```bash
echo "$DB_URL"
echo "$DB_USERNAME"
echo "$DB_PASSWORD"
```

For fish:

```fish
echo $DB_URL
echo $DB_USERNAME
echo $DB_PASSWORD
```

For a fresh local dev database, reset the Docker volume:

```bash
docker compose -f infra/docker-compose.yml down -v
docker compose -f infra/docker-compose.yml up -d
```

If you are inside `backend/`, use:

```bash
docker compose -f ../infra/docker-compose.yml down -v
docker compose -f ../infra/docker-compose.yml up -d
```

This deletes the local PostgreSQL, Redis, RabbitMQ, and MinIO volumes. Do not use it if the local data matters.

### Troubleshooting: Port 5432 Already Allocated

If Docker Compose fails with:

```text
Bind for 0.0.0.0:5432 failed: port is already allocated
```

then another PostgreSQL instance or container is already using host port `5432`.

Option A: find and stop the process/container using `5432`:

```bash
docker ps --format "table {{.Names}}\t{{.Image}}\t{{.Ports}}"
sudo ss -ltnp 'sport = :5432'
```

Option B: run this project's PostgreSQL on host port `5433`.

For bash/zsh:

```bash
export POSTGRES_PORT=5433
export DB_URL=jdbc:postgresql://localhost:5433/ai_elearning
docker compose -f infra/docker-compose.yml up -d
```

For fish, from `backend/`:

```fish
set -gx POSTGRES_PORT 5433
set -gx DB_URL jdbc:postgresql://localhost:5433/ai_elearning
docker compose -f ../infra/docker-compose.yml up -d
gradle bootRun
```

## 7. Verify Redis

```bash
docker exec -it ai-elearning-redis redis-cli ping
```

Expected:

```text
PONG
```

## 8. Verify RabbitMQ

```bash
docker exec -it ai-elearning-rabbitmq rabbitmq-diagnostics -q ping
```

Expected:

```text
Ping succeeded
```

After backend boot, verify queues:

```bash
docker exec -it ai-elearning-rabbitmq rabbitmqctl list_queues name durable
```

Expected queues:

- `material.processing`
- `embedding.generation`
- `content.analysis`

## 9. Verify MinIO

Check MinIO live endpoint:

```bash
curl -i http://localhost:9000/minio/health/live
```

Expected HTTP status:

```text
200 OK
```

Check the bucket from the `mc` container image:

```bash
docker run --rm --network infra_default minio/mc:RELEASE.2024-10-08T09-37-26Z \
  sh -c "mc alias set local http://minio:9000 minioadmin minioadmin && mc ls local"
```

Expected bucket:

```text
learning-materials
```

## 10. Start Spring Boot Backend

Recommended one-command local start:

For fish:

```fish
fish backend/scripts/dev.fish
```

For bash/zsh:

```bash
bash backend/scripts/dev.sh
```

These scripts:

- use PostgreSQL host port `5433` to avoid conflicts with other local PostgreSQL containers
- start Docker Compose infrastructure
- export Spring Boot environment variables
- run `gradle bootRun`

From the backend directory:

For bash/zsh:

```bash
cd backend
set -a
. ./.env
set +a
gradle bootRun
```

For fish:

```fish
cd backend
for line in (grep -v '^\s*#' .env | grep -v '^\s*$')
    set -gx (string split -m 1 '=' $line)
end
gradle bootRun
```

The project also has default local values in `application.yml`, so for the default Docker Compose setup you can usually run only:

```bash
cd backend
gradle bootRun
```

Alternative after adding Gradle wrapper:

```bash
./gradlew bootRun
```

Expected startup signs:

- Flyway applies `V1__initial_schema.sql`.
- Hibernate validates schema successfully.
- Tomcat starts on port `8080`.
- RabbitMQ queues are declared.
- No database, Redis, RabbitMQ, or MinIO health errors.

`gradle bootRun` is a long-running server process. When you see:

```text
> :bootRun
```

or:

```text
80% EXECUTING
```

that is expected. It means the backend is running. Keep that terminal open and run health checks from a second terminal. Stop the server with `Ctrl+C`.

## 11. Healthcheck Endpoints

Application health:

```bash
curl http://localhost:8080/actuator/health
```

Liveness:

```bash
curl http://localhost:8080/actuator/health/liveness
```

Readiness:

```bash
curl http://localhost:8080/actuator/health/readiness
```

Metrics:

```bash
curl http://localhost:8080/actuator/metrics
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON:

```bash
curl http://localhost:8080/v3/api-docs
```

Expected health status:

```json
{"status":"UP"}
```

If health is `DOWN`, check details in logs first. The current security config exposes health without authentication but hides detailed health unless authorized.

## 12. Smoke-Test Checklist

Run this before implementing more features:

- Docker daemon is running.
- `docker compose -f infra/docker-compose.yml config` passes.
- `docker compose -f infra/docker-compose.yml up -d` starts all infrastructure services.
- PostgreSQL accepts connections.
- `pgvector` extension exists.
- Redis returns `PONG`.
- RabbitMQ management UI opens.
- MinIO console opens.
- `learning-materials` bucket exists.
- Backend starts with `gradle bootRun`.
- Flyway creates `users`, `subjects`, and `materials`.
- `GET /actuator/health` returns `UP`.
- Swagger UI opens.
- Register API returns JWT.
- Login API returns JWT.
- Authenticated subject create/list/get/update/delete works.
- Authenticated material create/list/get works.
- Authenticated PDF material upload works.
- Uploaded PDF moves from `UPLOADED` to `PROCESSING` to `PROCESSED`, or `FAILED` with an error message.
- Extracted PDF text is available through `/api/materials/{materialId}/extracted-text`.
- Unauthenticated `/api/subjects` request returns `401`.

## 13. Curl Examples

Set base URL:

```bash
BASE_URL=http://localhost:8080
```

Register:

```bash
curl -s -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "student@example.com",
    "password": "password123",
    "displayName": "Student"
  }'
```

Login and capture token:

```bash
TOKEN=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "student@example.com",
    "password": "password123"
  }' | jq -r '.data.accessToken')
```

Create subject:

```bash
SUBJECT_ID=$(curl -s -X POST "$BASE_URL/api/subjects" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "Java",
    "description": "Java backend learning materials",
    "color": "#2563eb"
  }' | jq -r '.data.id')
```

List subjects:

```bash
curl -s "$BASE_URL/api/subjects" \
  -H "Authorization: Bearer $TOKEN" | jq
```

Get subject:

```bash
curl -s "$BASE_URL/api/subjects/$SUBJECT_ID" \
  -H "Authorization: Bearer $TOKEN" | jq
```

Update subject:

```bash
curl -s -X PATCH "$BASE_URL/api/subjects/$SUBJECT_ID" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "Advanced Java",
    "description": "Updated Java learning scope",
    "color": "#16a34a"
  }' | jq
```

Create material metadata:

```bash
MATERIAL_ID=$(curl -s -X POST "$BASE_URL/api/subjects/$SUBJECT_ID/materials" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "fileName": "oop-notes.pdf",
    "fileType": "application/pdf",
    "storageKey": "users/student/java/oop-notes.pdf",
    "sizeBytes": 1024
  }' | jq -r '.data.id')
```

List subject materials:

```bash
curl -s "$BASE_URL/api/subjects/$SUBJECT_ID/materials" \
  -H "Authorization: Bearer $TOKEN" | jq
```

Get material:

```bash
curl -s "$BASE_URL/api/materials/$MATERIAL_ID" \
  -H "Authorization: Bearer $TOKEN" | jq
```

Upload PDF material:

```bash
UPLOAD_MATERIAL_ID=$(curl -s -X POST "$BASE_URL/api/subjects/$SUBJECT_ID/materials/upload" \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@/absolute/path/to/sample.pdf;type=application/pdf" | jq -r '.data.id')
```

Poll material status:

```bash
curl -s "$BASE_URL/api/materials/$UPLOAD_MATERIAL_ID" \
  -H "Authorization: Bearer $TOKEN" | jq '.data.status, .data.errorMessage'
```

Get extracted PDF text:

```bash
curl -s "$BASE_URL/api/materials/$UPLOAD_MATERIAL_ID/extracted-text" \
  -H "Authorization: Bearer $TOKEN" | jq
```

Verify protected endpoint rejects anonymous calls:

```bash
curl -i "$BASE_URL/api/subjects"
```

Expected:

```text
HTTP/1.1 401
```

Delete subject:

```bash
curl -i -X DELETE "$BASE_URL/api/subjects/$SUBJECT_ID" \
  -H "Authorization: Bearer $TOKEN"
```

Expected:

```text
HTTP/1.1 204
```
