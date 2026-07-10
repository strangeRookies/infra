### 📬 3. 통신 브로커 (MQTT) 인프라 구축 가이드

```markdown
# [인프라 가이드] EMQX MQTT 브로커 구축 및 연동 보고서

본 문서는 스마트 안전 모니터링 시스템의 핵심 실시간 이벤트 데이터 버스 역할을 수행하는 **EMQX MQTT Broker**의 EC2 인프라 구축 내용과 개발 파트(백엔드/AI) 연동 사양을 정리한 가이드라인입니다.

---

## 1. 기술 선택 이유 (Architecture Selection Rationale)

### 1.1 Mosquitto 대신 EMQX를 선택한 이유
* **엔터프라이즈급 확장성 및 성능:** 고성능 Erlang VM(BEAM) 기반으로 아키텍처가 설계되어, 현장의 수많은 AI Edge 카메라와 센서가 대규모 동시 연결을 맺어도 극도로 낮은 지연 시간(Low Latency)과 높은 처리량(Throughput)을 유지합니다.
* **시각적 웹 대시보드(Web UI) 제공:** 브로커 자체 내부에 미려한 웹 관리 콘솔을 내장하고 있어, 실시간 트래픽 모니터링, 메시지 전달률, 현재 접속 중인 클라이언트 기기 상태 파악이 용이합니다.
* **쿠버네티스(EKS) 마이그레이션 용이성:** 최종 프로덕션 환경인 EKS(Kubernetes) 환경에 파드(Pod) 형태로 부드럽게 이식될 수 있도록 강력한 클러스터링 및 오퍼레이터 아키텍처를 지원합니다.

---

## 2. 인프라 사양 및 네트워크 구성 (Infrastructure Specifications)

### 2.1 인스턴스 정보
* **클라우드 환경:** AWS ap-northeast-2 (서울 리전)
* **운영체제(OS):** Ubuntu Server 24.04 LTS (장기 지원 버전으로 보안 및 안정성 확보)
* **인스턴스 유형:** `t3.micro` (프리티어 용량 및 테스트 최적화 사양)
* **IAM 인스턴스 프로파일:** `ec2-ai-role` (보안 키 없이 안전한 세션 관리 접속을 위해 `AmazonSSMManagedInstanceCore` 권한 포함)
* **호스트 공인 IP (Public IP):** **`3.34.103.24`**

### 2.2 방화벽 설정 (Security Group: `mqtt-broker-sg`)
외부 기기 및 웹 모니터링 허용을 위해 아래와 같이 인바운드 규칙(Inbound Rules)을 구성하였습니다.
* **포트 `1883` (Custom TCP / 소스 `0.0.0.0/0`):** MQTT 기본 데이터 통신 채널 (AI 기기 데이터 전송 및 백엔드 서버 구독용)
* **포트 `18083` (Custom TCP / 소스 `0.0.0.0/0`):** EMQX 관리자 웹 대시보드 접속 채널

---

## 3. 리눅스 서버 설치 및 구동 이력 (Installation History)

우분투 서버 터미널(Session Manager) 환경에서 수동 빌드 대신 공식 릴리즈 저장소를 등록하여 정석대로 패키지 설치를 완료했습니다.

```bash
# 1. 관리자 권한 승격
sudo su -

# 2. EMQX 공식 저장소 스크립트 검증 및 레포지토리 등록
curl -s [https://assets.emqx.com/scripts/install-emqx-deb.sh](https://assets.emqx.com/scripts/install-emqx-deb.sh) | sudo bash

# 3. 패키지 설치 진행
apt-get install emqx -y

# 4. 백그라운드 서비스 기동 및 상시 자동 시작(Enable) 프로세스 등록
systemctl start emqx
systemctl enable emqx

# 5. 상태 검증 및 구동 확인 완료
systemctl status emqx
# -> 결과: Active: active (running) 확인 완료

* **설치 명령어:**
    ```
    bash

    curl -s [https://assets.emqx.com/scripts/install-emqx-deb.sh](https://assets.emqx.com/scripts/install-emqx-deb.sh) | sudo bash
    sudo apt-get install emqx -y
    sudo systemctl start emqx && sudo systemctl enable emqx
    
    ```
* **AWS 보안 그룹(Inbound) 설정:**
    * `1883`: 백엔드/AI 데이터 통신 (TCP)
    * `18083`: EMQX 관리 대시보드 (HTTP)
    * `8083`: 대시보드 내 웹소켓 클라이언트 테스트용 (TCP)

---

## 연동 가이드 

모든 클라이언트는 아래 접속 정보를 기반으로 브로커에 연결합니다.

* **Broker URL:** `tcp://3.34.103.24:1883`
* **구독(Subscribe) 토픽 설계:** * 백엔드 팀은 통합 수신을 위해 **`safety/alert/#`** 채널을 구독합니다.
    * `#` (와일드카드)를 통해 하위의 모든 위험 알림(helmet, pose 등)을 일괄 수신합니다.
* **QoS (Quality of Service):** `1` (메시지 전달 보장 수준 설정)
* **Client ID 규칙:** 서버 재시작 시 충돌 방지를 위해 반드시 `랜덤 난수`를 접미사로 붙입니다. (예: `spring-backend-${random.uuid}`)

---

## 테스트 및 디버깅 가이드

### 유령 연결(Ghost Connection) 처리
* **현상:** 서버 강제 종료 시, 기존 세션이 Keepalive(60초) 동안 유지되어 대시보드에 연결이 중복 표시됨.
* **해결:** 정상적인 동작이므로 60초가 지나면 자동 해제됩니다. 클라이언트 아이디를 고유하게 설정하면 문제없습니다.

### 수동 테스트 (WebSocket Client 사용)
코드 작성 전, 직접 데이터를 발행해 볼 수 있습니다.
1. **대시보드 접속:** `http://3.34.103.24:18083` 접속
2. **도구 이동:** 좌측 하단 `Diagnose` -> `WebSocket Client` 클릭
3. **연결:** `Connect` 버튼 클릭 (8083 포트 사용)
4. **발행(Publish):** * **Topic:** `safety/alert/helmet`
    * **Payload:** `{"message": "위험 감지 테스트"}`
5. **검증:** 백엔드 서버(bootrun) 로그에 수신된 JSON 데이터 확인

---

## 4. 시스템 구조도


시스템의 전체적인 흐름은 다음과 같습니다.
1. **Publisher (AI Edge Camera):** 현장의 카메라가 `safety/alert/helmet` 등의 토픽으로 데이터를 발행(Publish)합니다.
2. **Broker (EMQX):** 브로커가 메시지를 받아 적절한 구독자에게 전달합니다.
3. **Subscriber (Backend Server):** 백엔드 서버가 `safety/alert/#`를 구독 중이므로, 모든 위험 알림 데이터를 실시간으로 수신하여 DB에 저장하거나 알림을 전송합니다.

---

## 5. 관리 및 모니터링 팁
* **연결 상태 확인:** 대시보드 `Clients` 메뉴에서 초록색 불(Connected) 여부를 상시 확인하세요.
* **트래픽 분석:** `Monitor` 메뉴를 통해 시간대별 위험 감지 이벤트 발생 건수를 시각화할 수 있습니다.
* **로그 관리:** 서버 터미널에서 `tail -f /var/log/emqx/emqx.log` 명령어를 통해 실시간 동작 로그를 직접 추적할 수 있습니다.