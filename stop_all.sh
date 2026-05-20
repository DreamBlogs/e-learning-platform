#!/usr/bin/env bash
# stop_all.sh – Stop all services started by run_all.sh
# Usage: ./stop_all.sh

set -e

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_FILE="$ROOT_DIR/infra/docker-compose.yml"
PID_FILE="$ROOT_DIR/.run_all.pids"

log() { echo "▶ $*"; }
die() { echo "✗ $*" >&2; exit 1; }

compose() {
    if docker compose version >/dev/null 2>&1; then
        docker compose -f "$COMPOSE_FILE" "$@"
    elif command -v docker-compose >/dev/null 2>&1; then
        docker-compose -f "$COMPOSE_FILE" "$@"
    else
        die "Docker Compose not found."
    fi
}

kill_process_tree() {
    local pid="$1"
    [[ -n "$pid" ]] || return
    if kill -0 "$pid" 2>/dev/null; then
        pkill -TERM -P "$pid" 2>/dev/null || true
        kill -TERM "$pid" 2>/dev/null || true
        echo "  Killed PID $pid"
    else
        echo "  PID $pid already stopped"
    fi
}

# ---- Stop -------------------------------------------------------
if [[ ! -f "$PID_FILE" ]]; then
    log "No PID file found. Stopping Docker services anyway..."
    compose down
    exit 0
fi

log "Stopping services..."

while IFS= read -r pid; do
    kill_process_tree "$pid"
done < "$PID_FILE"

rm -f "$PID_FILE"

log "Stopping Docker Compose services..."
compose down

log "All stopped."
