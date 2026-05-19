#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
REPO_DIR="$(cd "$BACKEND_DIR/.." && pwd)"

export POSTGRES_PORT=5433
export DB_URL=jdbc:postgresql://localhost:5433/ai_elearning
export DB_USERNAME=ai_learning
export DB_PASSWORD=ai_learning

export REDIS_HOST=localhost
export REDIS_PORT=6379

export RABBITMQ_HOST=localhost
export RABBITMQ_PORT=5672
export RABBITMQ_USERNAME=ai_learning
export RABBITMQ_PASSWORD=ai_learning

export MINIO_ENDPOINT=http://localhost:9000
export MINIO_ACCESS_KEY=minioadmin
export MINIO_SECRET_KEY=minioadmin
export MINIO_BUCKET=learning-materials

export JWT_ISSUER=ai-elearning
export JWT_SECRET=change-this-development-secret-change-this
export JWT_ACCESS_TOKEN_TTL_MINUTES=60

export CORS_ALLOWED_ORIGINS=http://localhost:3000

echo "Starting local infrastructure..."
docker compose -f "$REPO_DIR/infra/docker-compose.yml" up -d

echo "Waiting for PostgreSQL..."
for _ in {1..30}; do
  if docker exec ai-elearning-postgres pg_isready -U ai_learning -d ai_elearning >/dev/null 2>&1; then
    break
  fi
  sleep 1
done

echo "Starting Spring Boot on http://localhost:8080 ..."
cd "$BACKEND_DIR"
gradle bootRun
