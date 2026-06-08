# Strange Infra

Local development infrastructure for the Smart Safety Monitoring System.

## Local Docker Scope

This repository now provides local Docker orchestration for the React frontend, Spring Boot backend, PostgreSQL, and Mosquitto MQTT broker.

The Python AI server is intentionally out of scope for this Docker optimization step because it depends on Torch, Ultralytics, OpenCV, CUDA, and GPU-specific runtime setup.

AWS deployment values must be injected through environment variables or deployment secrets. Do not hardcode RTSP credentials, MQTT passwords, AWS keys, or production database credentials in Dockerfiles, compose files, or README files.

## Current MVP Broker

The MVP uses Mosquitto MQTT Broker as the default message broker for safety events.

```text
Python Edge AI -> MQTT Broker (Mosquitto) -> Spring Boot MQTT Subscriber -> WebSocket -> React
```

The MQTT topic for safety events is:

```text
safety/events
```

Redis may still exist in this repository for legacy local tests or future debounce/cache experiments, but MQTT is the default broker for the current CCTV Edge AI MVP direction.

Redis is not started by the default compose command. Start it only when a legacy test needs it:

```bash
docker compose --profile redis up -d redis
```

If Docker Compose v2 is not available:

```bash
docker-compose --profile redis up -d redis
```

## Frontend Docker Image

The frontend image uses a multi-stage build:

- `node:22-alpine` installs dependencies from `package.json` and `package-lock.json` first for layer caching.
- `npm run build` runs the Vite build.
- `nginx:1.27-alpine` serves only the generated `dist` files.
- `nginx.conf` enables SPA fallback routing with `try_files`.

Build and run only the frontend:

```bash
docker build -t strange-frontend:local ../strange_front
docker run --rm -p 3000:80 strange-frontend:local
```

## Backend Docker Image

The backend image uses a multi-stage build:

- `gradle:8.10.2-jdk21-alpine` builds the Spring Boot jar.
- `eclipse-temurin:21-jre-alpine` runs only the jar.
- Gradle caches, source files, and build tools are not copied into the runtime image.

Build and run only the backend:

```bash
docker build -t strange-backend:local ../strange_back
docker run --rm -p 8080:8080 --env-file .env strange-backend:local
```

For standalone backend runs outside compose, set `DB_URL` and `MQTT_BROKER_URL` to reachable hosts.

## Compose Local Stack

Copy local environment defaults:

```bash
copy .env.example .env
```

On Linux/macOS:

```bash
cp .env.example .env
```

Build all default local services:

```bash
docker compose build
```

Fallback:

```bash
docker-compose build
```

Run frontend, backend, PostgreSQL, and MQTT:

```bash
docker compose up -d
```

Fallback:

```bash
docker-compose up -d
```

Check containers:

```bash
docker compose ps
```

Open:

```text
Frontend: http://localhost:3000
Backend:  http://localhost:8080
MQTT:     localhost:1883
```

## Image Inspection

Check image sizes:

```bash
docker images strange-frontend:local strange-backend:local
```

Inspect image layers:

```bash
docker history strange-frontend:local
docker history strange-backend:local
```

## Mosquitto MQTT Broker

### Run

```bash
docker compose up -d mosquitto
```

If Docker Compose v2 is not available:

```bash
docker-compose up -d mosquitto
```

### Ports

```text
MQTT TCP: 1883
MQTT WebSocket: 9001
```

### Local Test

Subscribe to safety events:

```bash
mosquitto_sub -h localhost -p 1883 -t safety/events
```

Publish a test safety event:

```bash
mosquitto_pub -h localhost -p 1883 -t safety/events -m '{"type":"fall_detected","camera_id":"cam_01","severity":"HIGH","message":"test"}'
```

If the Mosquitto client tools are not installed locally, use the broker container:

```bash
docker exec -it strange-mosquitto mosquitto_sub -h localhost -p 1883 -t safety/events
```

```bash
docker exec strange-mosquitto mosquitto_pub -h localhost -p 1883 -t safety/events -m '{"type":"fall_detected","camera_id":"cam_01","severity":"HIGH","message":"test"}'
```

### Configuration

Local Mosquitto configuration:

```text
mosquitto/config/mosquitto.conf
```

For local development, anonymous MQTT connections are allowed.

## Environment Variables

```text
MQTT_HOST=localhost
MQTT_PORT=1883
MQTT_WS_PORT=9001
MQTT_TOPIC=safety/events
```

## Security TODO

- Do not use anonymous MQTT access in production.
- Add MQTT username/password or certificate-based authentication before deployment.
- Add ACL rules so publishers and subscribers can only access the topics they need.
- Do not commit real passwords, API keys, certificates, or private broker credentials.

## Out Of Scope For This Step

- Spring Boot MQTT subscriber changes
- React dashboard changes
- Python Edge AI publisher changes
- Real CCTV video or private test media
