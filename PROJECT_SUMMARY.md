# Strange Infra

Smart Safety Monitoring System의 로컬 개발용 인프라 설정 저장소입니다.

## 역할

실시간 데이터 파이프라인에서 Redis Message Broker 실행 환경을 제공합니다.

```text
Python Edge AI -> Redis Message Broker -> Spring Boot Backend -> React Frontend
```

## Redis Message Broker

로컬 개발 환경에서는 별도 비밀번호 없이 Redis를 실행합니다.

### 실행

```bash
docker compose up -d redis
```

Docker Compose v2 플러그인이 없는 환경에서는 다음 명령을 사용합니다.

```bash
docker-compose up -d redis
```

### 컨테이너 상태 확인

```bash
docker ps
```

`strange-redis` 컨테이너가 실행 중인지 확인합니다.

### PING 검증

```bash
docker exec strange-redis redis-cli PING
```

정상 응답:

```text
PONG
```

### 접속 정보

```text
Host: localhost
Port: 6379
URL: redis://localhost:6379
Password: none
```

## 환경 변수

`.env.example`의 기본값은 로컬 개발용 Redis 접속 정보를 제공합니다.

```text
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_URL=redis://localhost:6379
```

## 데이터 볼륨

Redis 데이터는 `redis-data` Docker volume에 저장됩니다.

## 주의사항

- 이 구성은 로컬 개발용입니다.
- 실제 비밀번호나 민감정보를 저장하지 않습니다.
- Spring Boot, React, Python Edge AI 코드는 이 저장소에서 수정하지 않습니다.
