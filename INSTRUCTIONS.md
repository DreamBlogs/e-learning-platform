# Project Startup Instructions

## Quick Start

```bash
# 1. Ensure Docker is running
docker ps

# 2. Start the entire stack (Docker services, backend, frontend)
./run_all.sh
```

Press `Ctrl-C` to stop everything.

## Prerequisites

- Docker & Docker Compose
- Java 21+
- Gradle (or use `./gradlew` if wrapper exists)
- Node.js ≥ 18 & npm

## Manual Start

If you prefer to run components separately:

```bash
# Start infrastructure
docker compose -f infra/docker-compose.yml up -d

# Start backend
cd backend && gradle bootRun

# Start frontend (in another terminal)
cd frontend && npm run dev
```

## Logs

- Backend: `backend.log`
- Frontend: `frontend.log`
