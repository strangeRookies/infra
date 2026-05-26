# Strange Infra

Local development infrastructure for the Smart Safety Monitoring System.

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
