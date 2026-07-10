# [인프라 가이드] Amazon RDS for PostgreSQL 및 SSM 접근 체계 구축 보고서

본 문서는 스마트 안전 모니터링 시스템의 핵심 데이터 관리 체계를 구성하는 관계형 데이터베이스(RDB)인 **Amazon RDS for PostgreSQL**의 구축 내용과, 이에 안전하게 접근하기 위한 **AWS SSM (Session Manager) 기반 징검다리(Bastion) 아키텍처**를 정리한 엔지니어링 가이드입니다.

---

## 1. 기술 선택 이유 (Architecture Selection Rationale)

### 1.1 PostgreSQL을 선택한 이유
* **복잡한 데이터 구조 지원 최적화:** 현장 AI 카메라가 전송하는 비정형/반정형 위험 감지 데이터(JSON 메타데이터)를 다루는 데 있어, PostgreSQL의 강력한 `JSONB` 데이터 타입 지원이 매우 유리합니다.
* **데이터 무결성과 동시성:** 엄격한 데이터 무결성 검증과 향상된 동시성 제어(MVCC)를 제공하여, 다수의 현장 기기와 백엔드 서버가 동시에 알림 로그를 읽고 쓸 때 발생할 수 있는 병목을 최소화합니다.
* **확장성 (PostGIS 등):** 향후 다수의 작업 현장 위치 데이터나 공간 데이터를 분석해야 할 경우, 가장 강력한 공간 데이터베이스 확장 플러그인(PostGIS)을 손쉽게 연동할 수 있습니다.

### 1.2 기존 SSH 키 방식 대신 AWS SSM (Session Manager)을 선택한 이유
* **인바운드 포트(22번) 전면 차단:** 외부 인터넷에서 접속 가능한 SSH(22번) 포트를 아예 열어둘 필요가 없어, 무차별 대입 공격(Brute-force)이나 해킹 위험을 원천적으로 차단합니다.
* **키 페어(.pem) 관리 불필요:** 인프라 관리자나 개발자가 번거롭게 접속 키를 PC에 보관하고 공유할 필요가 없으며, 키 유출로 인한 대형 보안 사고를 방지합니다.
* **IAM 기반의 강력한 권한 통제:** 오직 AWS 콘솔에 로그인할 수 있고 권한이 부여된 사용자만이 브라우저를 통해 안전하게 서버 터미널에 접근할 수 있습니다.

---

## 2. 인프라 아키텍처 개요 (Architecture Overview)

[ AWS Cloud (ap-northeast-2) ]
  └── [ VPC Area ]
        ├── [ Public Subnet ]
        │     └── EC2 징검다리 서버 (SSM Agent 구동, Port 22 닫힘)
        │           ▲ (AWS 브라우저 콘솔을 통한 SSM 터미널 세션 연결)
        │
        └── [ Private Subnet Group ]
              └── Amazon RDS for PostgreSQL (Port: 15432)
                    ▲
                    │ (Inbound Allowed: Port 15432 from EC2 징검다리 & EKS/Backend)
                    └── [ Security Group: rds-private-sg ]

* **네트워크 격리:** RDS 인스턴스는 외부 인터넷에서 절대 직접 접근할 수 없는 **Private Subnet**에 숨겨져 있습니다.
* **안전한 접근(SSM):** DB에 직접 쿼리를 날리거나 상태를 확인해야 할 때는, IAM 권한(`AmazonSSMManagedInstanceCore`)이 부여된 EC2 징검다리 서버에 SSM으로 접속한 뒤 내부망을 통해 DB로 접근합니다.

---

## 3. 구축 절차 (Step-by-Step Setup Guide)

### Step 1: SSM 징검다리 EC2 서버 구축
1. **EC2 인스턴스 생성:** Ubuntu Server 24.04 LTS 선택 (t3.micro).
2. **키 페어:** '키 페어 없이 진행 (권장하지 않음)' 선택. (SSM을 사용하므로 키가 필요 없음)
3. **네트워크 및 방화벽:** 퍼블릭 IP 자동 할당 활성화. 보안 그룹에서 SSH(22) 규칙은 삭제하거나 내 IP만 허용.
4. **고급 세부 정보 (IAM 역할):** `AmazonSSMManagedInstanceCore` 정책이 포함된 IAM 역할(예: `ec2-ai-role`)을 인스턴스 프로파일에 연결하여 생성.

### Step 2: RDS 서브넷 그룹 및 파라미터 설정
1. **서브넷 그룹:** 가용 영역(AZ) 2개 이상의 프라이빗 서브넷을 묶어 RDS 전용 그룹 생성.
2. **파라미터 그룹:** `postgres16` (또는 해당 버전) 파라미터 그룹 생성 후 `timezone`을 `Asia/Seoul`로 변경하여 한국 시간대 맞춤.

### Step 3: PostgreSQL 데이터베이스 생성
1. **엔진 선택:** PostgreSQL (프리티어 또는 개발/테스트용 템플릿).
2. **연결 및 네트워크:** 생성한 VPC 및 서브넷 그룹 지정. **퍼블릭 액세스 절대 차단(아니요)**.
3. **보안 그룹 (`rds-private-sg`):** 인바운드 규칙에 사용자 지정 TCP **`5432`** 포트를 열고, 소스(Source)를 '징검다리 EC2의 보안 그룹 ID' 및 '백엔드 서버의 보안 그룹 ID'로 한정하여 지정.

---

## 4. 백엔드 팀 공유용 접속 및 연동 정보

백엔드 팀은 Spring Boot의 `application.yml` 또는 `.env` 환경 변수에 아래 정보를 매핑하여 연동을 진행합니다.

### 4.1 접속 메타데이터 (Connection Metadata)
* **DBMS 종류:** PostgreSQL
* **DB 엔드포인트(Host):** `[RDS_INSTANCE_NAME].[IDENTIFIER].ap-northeast-2.rds.amazonaws.com`
* **포트(Port):** `5432` (PostgreSQL 기본 포트)
* **데이터베이스 명(Database Name):** `[생성한 스키마/DB 이름]`
* **관리자 계정(Username):** `[설정한 관리자 ID]`
* **비밀번호(Password):** `[설정한 DB 비밀번호]`

### 4.2 Spring Boot 설정 예시 (`application.yml`)
```yaml
spring:
  datasource:
    driver-class-name: org.postgresql.Driver
    url: jdbc:postgresql://${RDS_ENDPOINT}:5432/${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect