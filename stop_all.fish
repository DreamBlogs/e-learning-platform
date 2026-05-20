#!/usr/bin/env fish
# stop_all.fish – Stop all services started by run_all.fish
# Usage: ./stop_all.fish

set -g ROOT_DIR (realpath (dirname (status filename)))
set -g COMPOSE_FILE "$ROOT_DIR/infra/docker-compose.yml"
set -g PID_FILE "$ROOT_DIR/.run_all.pids"

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

function kill_process_tree
    set -l pid $argv[1]
    test -n "$pid"; or return
    if kill -0 $pid 2>/dev/null
        pkill -TERM -P $pid 2>/dev/null
        kill -TERM $pid 2>/dev/null
        printf '  Killed PID %s\n' $pid
    else
        printf '  PID %s already stopped\n' $pid
    end
end

function free_port
    set -l port $argv[1]
    if command -q fuser
        fuser -k "$port/tcp" 2>/dev/null
    end
end

# ---- Stop -------------------------------------------------------
if not test -f $PID_FILE
    log "No PID file found — stopping processes on ports 8080 and 3000..."
    free_port 8080
    free_port 3000
    compose down
    exit 0
end

log "Stopping services..."

# Read PIDs from file
set -l pids (cat $PID_FILE)

for pid in $pids
    kill_process_tree $pid
end

rm -f $PID_FILE

free_port 8080
free_port 3000

log "Stopping Docker Compose services..."
compose down

log "All stopped."
