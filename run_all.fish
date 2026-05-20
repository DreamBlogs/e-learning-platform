#!/usr/bin/env fish
# run_all.fish – Start all project services with one command
# Usage: ./run_all.fish

set -g ROOT_DIR (realpath (dirname (status filename)))
set -g COMPOSE_FILE "$ROOT_DIR/infra/docker-compose.yml"
set -g PID_FILE "$ROOT_DIR/.run_all.pids"
set -g BACKEND_DIR "$ROOT_DIR/backend"
set -g FRONTEND_DIR "$ROOT_DIR/frontend"

function log
    printf '▶ %s\n' $argv
end

function die
    printf '✗ %s\n' $argv >&2
    exit 1
end

function compose
    if docker compose version >/dev/null 2>&1
        docker compose -f $COMPOSE_FILE $argv
    else if command -q docker-compose
        docker-compose -f $COMPOSE_FILE $argv
    else
        die "Docker Compose not found."
    end
end

function wait_for_postgres
    log "Waiting for PostgreSQL..."
    for i in (seq 1 30)
        docker exec ai-elearning-postgres pg_isready -U ai_learning -d ai_elearning >/dev/null 2>&1
        if test $status -eq 0
            log "PostgreSQL is ready."
            return 0
        end
        sleep 1
    end
    die "PostgreSQL did not become ready in 30 seconds."
end

function wait_for_port
    set -l port $argv[1]
    set -l service $argv[2]
    log "Waiting for $service on port $port..."
    for i in (seq 1 30)
        if command -q lsof
            if lsof -i :$port >/dev/null 2>&1
                log "$service is ready on port $port."
                return 0
            end
        else
            if curl -s http://localhost:$port >/dev/null 2>&1
                log "$service is ready on port $port."
                return 0
            end
        end
        sleep 1
    end
    log "Warning: $service may not be ready on port $port."
end

# ---- Cleanup on exit --------------------------------------------
function cleanup --on-signal INT --on-signal TERM
    log "Received interrupt signal, stopping all services..."
    if test -f $PID_FILE
        set -l pids (cat $PID_FILE)
        for pid in $pids
            if kill -0 $pid 2>/dev/null
                pkill -TERM -P $pid 2>/dev/null
                kill -TERM $pid 2>/dev/null
            end
        end
        rm -f $PID_FILE
    end
    compose down >/dev/null 2>&1
    log "All services stopped."
    exit 0
end

# ---- Start ------------------------------------------------------
log "Starting all services..."

# 1. Start infrastructure
log "Starting Docker Compose infrastructure..."
compose up -d

if test $status -ne 0
    die "Docker Compose failed. Check Docker daemon and port conflicts."
end

# 2. Wait for PostgreSQL
wait_for_postgres

# 3. Load backend environment variables
log "Loading backend environment..."
if test -f "$BACKEND_DIR/.env"
    for line in (grep -v '^\s*#' "$BACKEND_DIR/.env" | grep -v '^\s*$')
        set -l parts (string split -m 1 '=' $line)
        if test (count $parts) -eq 2
            set -gx $parts[1] $parts[2]
        end
    end
else
    die "Backend .env file not found at $BACKEND_DIR/.env"
end

# 4. Start backend
log "Starting Spring Boot backend on http://localhost:8080 ..."
cd "$BACKEND_DIR"
gradle bootRun &
set -l backend_pid $last_pid
echo $backend_pid > $PID_FILE

wait_for_port 8080 "Backend"

# 5. Start frontend (if package.json exists)
if test -f "$FRONTEND_DIR/package.json"
    log "Starting Next.js frontend on http://localhost:3000 ..."
    cd "$FRONTEND_DIR"
    npm run dev &
    set -l frontend_pid $last_pid
    echo $frontend_pid >> $PID_FILE

    wait_for_port 3000 "Frontend"
else
    log "Frontend package.json not found — skipping frontend startup."
end

log "All services started."
log "Backend:  http://localhost:8080"
log "Swagger:  http://localhost:8080/swagger-ui.html"
log "Health:   http://localhost:8080/actuator/health"
if test -f "$FRONTEND_DIR/package.json"
    log "Frontend: http://localhost:3000"
end
log "RabbitMQ: http://localhost:15672 (ai_learning / ai_learning)"
log "MinIO:    http://localhost:9001 (minioadmin / minioadmin)"
log ""
log "Stop all: ./stop_all.fish"
log "PIDs saved to: $PID_FILE"
log ""
log "Press Ctrl+C to stop all services."

# Keep script alive so Ctrl+C works
wait
