# Strange Infra

Smart Safety Monitoring System의 로컬 개발용 인프라 설정 저장소입니다.

## 역할

실시간 데이터 파이프라인에서 Redis 메시지 브로커와 기타 인프라 구성을 담당합니다.

```text
Python Edge AI -> Redis Message Broker -> Spring Boot Backend -> React Frontend
```

## Redis Message Broker

로컬 개발 환경에서는 별도 비밀번호 없이 Redis를 실행합니다.

### 실행

```bash
docker compose up -d redis
```

### 상태 확인

```bash
docker ps
```

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
```

## 데이터 볼륨

Redis 데이터는 `redis-data` Docker volume에 저장됩니다.

## 주의사항

- 이 구성은 로컬 개발용입니다.
- Redis 비밀번호나 민감정보를 포함하지 않습니다.
- Spring Boot, React, Python AI 코드는 이 저장소에서 수정하지 않습니다.
