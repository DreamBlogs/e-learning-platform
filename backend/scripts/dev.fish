#!/usr/bin/env fish

set script_dir (dirname (status --current-filename))
set backend_dir (realpath "$script_dir/..")
set repo_dir (realpath "$backend_dir/..")

set -gx POSTGRES_PORT 5433
set -gx DB_URL jdbc:postgresql://localhost:5433/ai_elearning
set -gx DB_USERNAME ai_learning
set -gx DB_PASSWORD ai_learning

set -gx REDIS_HOST localhost
set -gx REDIS_PORT 6379

set -gx RABBITMQ_HOST localhost
set -gx RABBITMQ_PORT 5672
set -gx RABBITMQ_USERNAME ai_learning
set -gx RABBITMQ_PASSWORD ai_learning

set -gx MINIO_ENDPOINT http://localhost:9000
set -gx MINIO_ACCESS_KEY minioadmin
set -gx MINIO_SECRET_KEY minioadmin
set -gx MINIO_BUCKET learning-materials

set -gx JWT_ISSUER ai-elearning
set -gx JWT_SECRET change-this-development-secret-change-this
set -gx JWT_ACCESS_TOKEN_TTL_MINUTES 60

set -gx CORS_ALLOWED_ORIGINS http://localhost:3000

echo "Starting local infrastructure..."
docker compose -f "$repo_dir/infra/docker-compose.yml" up -d

if test $status -ne 0
    echo "Docker Compose failed. Check Docker daemon and port conflicts."
    exit 1
end

echo "Waiting for PostgreSQL..."
for i in (seq 1 30)
    docker exec ai-elearning-postgres pg_isready -U ai_learning -d ai_elearning >/dev/null 2>&1
    if test $status -eq 0
        break
    end
    sleep 1
end

echo "Starting Spring Boot on http://localhost:8080 ..."
cd "$backend_dir"
gradle bootRun
