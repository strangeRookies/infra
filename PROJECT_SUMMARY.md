# Strange Back

Spring Boot backend for the Smart Safety Monitoring System MVP.

## Realtime Pipeline

```text
Python Edge AI Mock Publisher
-> Redis Pub/Sub safety-events
-> Spring Boot Backend
-> WebSocket STOMP /topic/alerts
-> React Frontend
```

## Redis To WebSocket Alerts

The backend subscribes to the Redis Pub/Sub channel `safety-events`, maps each JSON payload to `SafetyEventDto`, and broadcasts it to the STOMP topic `/topic/alerts`.

### Event Shape

```json
{
  "type": "fall_detected",
  "camera_id": "cam_01",
  "timestamp": "2026-05-26T10:00:00Z",
  "severity": "HIGH",
  "message": "Fall detected from mock edge AI"
}
```

### Environment Variables

```text
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_CHANNEL=safety-events
SERVER_PORT=8080
WEBSOCKET_ALLOWED_ORIGIN_PATTERNS=http://localhost:3000,http://localhost:5173
```

No Redis password is configured for local development.

### Run

Start Redis first:

```bash
docker compose up -d redis
```

Run the backend:

```bash
./gradlew bootRun
```

On Windows without the Gradle wrapper:

```bash
gradle bootRun
```

### WebSocket Contract

```text
STOMP endpoint: /ws
Subscribe topic: /topic/alerts
Redis channel: safety-events
```

### Manual Verification

1. Start Redis.
2. Start this Spring Boot backend.
3. Run `strange_ai/mock_edge_ai.py`.
4. Check backend logs for Redis event receive logs.
5. Connect a STOMP client or the React frontend to `/ws`.
6. Subscribe to `/topic/alerts`.
7. Confirm safety event JSON messages arrive in real time.

### Scope

- This stage does not implement React UI changes.
- This stage does not implement DB persistence, JWT, RBAC, FCM, or external notifications.
- Secrets, passwords, API keys, personal videos, and private test media are not stored in this repository.
