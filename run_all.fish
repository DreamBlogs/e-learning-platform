#!/usr/bin/env fish
# Start the full AI e-learning stack locally (Docker + backend + frontend).
# Usage: ./run_all.fish
# Stop:  Ctrl+C

set -g ROOT_DIR (realpath (dirname (status filename)))
set -g COMPOSE_FILE "$ROOT_DIR/infra/docker-compose.yml"
set -g BACKEND_LOG "$ROOT_DIR/backend.log"
set -g FRONTEND_LOG "$ROOT_DIR/frontend.log"
set -g PID_FILE "$ROOT_DIR/.run_all.pids"
set -g BACKEND_PID ""
set -g FRONTEND_PID ""
set -g RUN_ALL_CLEANED 0

# ---- Local defaults (override via environment) -----------------
set -q DB_URL; or set -gx DB_URL "jdbc:postgresql://localhost:5432/ai_elearning"
set -q DB_USERNAME; or set -gx DB_USERNAME "ai_learning"
set -q DB_PASSWORD; or set -gx DB_PASSWORD "ai_learning"
set -q REDIS_HOST; or set -gx REDIS_HOST "localhost"
set -q REDIS_PORT; or set -gx REDIS_PORT "6379"
set -q RABBITMQ_HOST; or set -gx RABBITMQ_HOST "localhost"
set -q RABBITMQ_PORT; or set -gx RABBITMQ_PORT "5672"
set -q RABBITMQ_USERNAME; or set -gx RABBITMQ_USERNAME "ai_learning"
set -q RABBITMQ_PASSWORD; or set -gx RABBITMQ_PASSWORD "ai_learning"
set -q MINIO_ENDPOINT; or set -gx MINIO_ENDPOINT "http://localhost:9000"
set -q MINIO_ACCESS_KEY; or set -gx MINIO_ACCESS_KEY "minioadmin"
set -q MINIO_SECRET_KEY; or set -gx MINIO_SECRET_KEY "minioadmin"
set -q MINIO_BUCKET; or set -gx MINIO_BUCKET "learning-materials"
set -q JWT_ISSUER; or set -gx JWT_ISSUER "ai-elearning"
set -q JWT_SECRET; or set -gx JWT_SECRET "change-this-development-secret-change-this"
set -q JWT_ACCESS_TOKEN_TTL_MINUTES; or set -gx JWT_ACCESS_TOKEN_TTL_MINUTES "60"
set -q CORS_ALLOWED_ORIGINS; or set -gx CORS_ALLOWED_ORIGINS "http://localhost:3000"
set -q SERVER_PORT; or set -gx SERVER_PORT "8080"

function log
    printf '\n▶ %s\n' $argv
end

function die
    printf '\n✗ %s\n' $argv >&2
    exit 1
end

function compose
    if docker compose version >/dev/null 2>&1
        docker compose -f $COMPOSE_FILE $argv
    else if command -q docker-compose
        docker-compose -f $COMPOSE_FILE $argv
    else
        die "Docker Compose not found. Install Docker with the compose plugin."
    end
end

function require_cmd
    command -q $argv[1]; or die "Missing required command: $argv[1]"
end

function wait_for_port
    set -l host $argv[1]
    set -l port $argv[2]
    set -l label $argv[3]
    set -l max_attempts (test (count $argv) -ge 4; and echo $argv[4]; or echo 60)

    for i in (seq 1 $max_attempts)
        if nc -z $host $port 2>/dev/null
            return 0
        end
        sleep 1
    end
    die "$label did not become ready on $host:$port (see logs)."
end

function kill_process_tree
    set -l pid $argv[1]
    test -n "$pid"; or return
    if kill -0 $pid 2>/dev/null
        # Stop child processes first (gradle → java, npm → node)
        pkill -TERM -P $pid 2>/dev/null
        kill -TERM $pid 2>/dev/null
    end
end

function cleanup --on-signal INT --on-signal TERM
    if test $RUN_ALL_CLEANED -eq 1
        return
    end
    set -g RUN_ALL_CLEANED 1

    log "Stopping services..."

    kill_process_tree $BACKEND_PID
    kill_process_tree $FRONTEND_PID

    rm -f $PID_FILE
    compose down >/dev/null 2>&1
    printf '\n✅ All stopped.\n'
end

# ---- Prerequisites ---------------------------------------------
require_cmd docker
require_cmd java
require_cmd gradle
require_cmd npm

docker info >/dev/null 2>&1
or die "Docker daemon is not running. Start Docker and try again."

# ---- 1. Infrastructure ---------------------------------------
log "Starting Docker services (Postgres, Redis, RabbitMQ, MinIO)..."
if docker compose version >/dev/null 2>&1
    docker compose -f $COMPOSE_FILE up -d --wait
else if command -q docker-compose
    docker-compose -f $COMPOSE_FILE up -d
    log "Waiting for PostgreSQL..."
    for i in (seq 1 60)
        docker exec ai-elearning-postgres pg_isready -U ai_learning -d ai_elearning >/dev/null 2>&1
        and break
        sleep 1
    end
    docker exec ai-elearning-postgres pg_isready -U ai_learning -d ai_elearning >/dev/null 2>&1
    or die "PostgreSQL failed to start."
else
    die "Docker Compose not found. Install Docker with the compose plugin."
end

# ---- 2. Backend ------------------------------------------------
log "Starting Spring Boot backend (log: backend.log)..."
echo -n '' >$BACKEND_LOG

cd $ROOT_DIR/backend
begin
    exec gradle bootRun >>$BACKEND_LOG 2>&1
end &
set -g BACKEND_PID $last_pid
echo $BACKEND_PID >$PID_FILE

log "Waiting for backend on port $SERVER_PORT..."
wait_for_port localhost $SERVER_PORT Backend 120

# ---- 3. Frontend -----------------------------------------------
log "Starting Next.js frontend (log: frontend.log)..."
echo -n '' >$FRONTEND_LOG

cd $ROOT_DIR/frontend
if not test -d node_modules
    npm ci
end
begin
    exec npm run dev >>$FRONTEND_LOG 2>&1
end &
set -g FRONTEND_PID $last_pid
echo $FRONTEND_PID >>$PID_FILE

log "Waiting for frontend on port 3000..."
wait_for_port localhost 3000 Frontend 90

# ---- Ready -----------------------------------------------------
printf '\n'
printf '════════════════════════════════════════════════════════\n'
printf '  Stack is running. Press Ctrl+C to stop everything.\n'
printf '════════════════════════════════════════════════════════\n'
printf '  Frontend:  http://localhost:3000\n'
printf '  Login:     http://localhost:3000/login\n'
printf '  Backend:   http://localhost:%s/api\n' $SERVER_PORT
printf '  Swagger:   http://localhost:%s/swagger-ui.html\n' $SERVER_PORT
printf '  Logs:      tail -f backend.log frontend.log\n'
printf '════════════════════════════════════════════════════════\n'
printf '\n'

# Stay alive until Ctrl+C. Do not watch PIDs: $last_pid often points at a
# short-lived wrapper, which made the script exit and run cleanup immediately.
while true
    if not kill -0 $BACKEND_PID 2>/dev/null
        printf '\n⚠ Backend stopped unexpectedly. See backend.log\n' >&2
        break
    end
    if not kill -0 $FRONTEND_PID 2>/dev/null
        printf '\n⚠ Frontend stopped unexpectedly. See frontend.log\n' >&2
        break
    end
    sleep 2
end

cleanup
