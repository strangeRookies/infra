# [인프라 가이드] AWS Site-to-Site VPN 구축 및 연동 보고서

본 문서는 클라우드 인프라(AWS VPC)와 현장의 고성능 연산 자원(외부 Linux Ubuntu AI 서버)을 안전하고 프라이빗하게 연결하기 위해 구축한 **AWS Site-to-Site VPN** 인프라의 아키텍처 및 상세 내용을 정리한 보고서입니다.

---

## 1. 핵심 개념 및 기술 선택 이유 (Core Concepts & Rationale)

### 1.1 VPN(Virtual Private Network)이란 무엇인가?
* **인터넷 위의 비밀 터널:** 누구나 다니는 뚫려있는 공용 인터넷 도로망을 사용할 때, 데이터를 강력하게 암호화하여 마치 우리 팀만의 튼튼한 '전용 터널'을 뚫은 것처럼 안전하게 통신하는 기술입니다.
* 외부 공격자가 데이터를 가로채더라도 암호화되어 있어 해독이 불가능하므로, 민감한 현장 AI 영상 및 분석 데이터(보안 자산)를 안전하게 전송하는 데 필수적인 방패막 역할을 합니다.

### 1.2 VPN 구축 시 VPC(Virtual Private Cloud)가 반드시 함께 필요한 이유
* **터널의 '안전한 도착지(사설망)' 역할:** VPN이 외부에서 들어오는 '비밀 터널'이라면, VPC는 그 터널이 도착하는 AWS 내의 '우리 팀만의 독립된 사유지(가상 사설망)'입니다.
* **내부 IP 통합 통신망 구축:** 터널을 타고 들어온 외부 AI 서버가 인터넷을 거치지 않고 AWS 안에 있는 브로커(EMQX)나 DB(RDS)와 직접 통신하려면, AWS 측에도 이들을 품고 있는 거대한 내부망(VPC)이 있어야 합니다. **즉, 현장의 사설 네트워크와 AWS의 사설 네트워크(VPC)를 하나로 묶어 거대한 단일 사내망처럼 만들기 위해 VPC 연동이 필수적입니다.**

### 1.3 일반 VPN이 아닌 'Site-to-Site VPN' 방식을 선택한 이유
* **인프라 대 인프라의 상시 연결:** 개발자 개개인이 PC에서 프로그램을 켜서 접속하는 Client VPN과 달리, 현장 인프라 거점(웰랩스 AI 서버)과 AWS 클라우드 전체를 상시로 연결하는 튼튼하고 거대한 파이프라인입니다.
* **네트워크 이중화 지원:** AWS Site-to-Site VPN은 기본적으로 2개의 통신 경로(터널 1, 터널 2)를 제공하여, 하나의 회선에 장애가 발생하더라도 끊김 없이 자동으로 다른 경로로 통신(Failover)되는 고가용성을 보장합니다.

---

## 2. 인프라 아키텍처 개요 (Architecture Overview)

[ 외부 온프레미스 AI GPU 서버 ] 
       └── [ Customer Gateway (CGW) ] 
                     ▼
                     │ (IPsec VPN Tunnel 1 & 2 - 강력한 암호화 터널)
                     ▼
[ AWS Cloud (단일 VPC 대역) ]
       └── [ Virtual Private Gateway (VGW) ]
                     └── EMQX 브로커 서버 & RDS 데이터베이스

* **동작 원리:** 외부 AI 서버가 포즈 추적(Pose Tracking) 및 모니터링 분석을 마친 후 결과 JSON 데이터를 AWS 상의 EMQX 브로커로 보낼 때, 암호화된 VPN 터널을 타고 안전하게 사설망 라우팅을 거쳐 배달됩니다.

---

## 3. 구축 및 구성 요소 세부 정보 (Configuration Details)

### 3.1 AWS 인프라 설정 요소
1. **고객 게이트웨이 (Customer Gateway - CGW):** 외부 AI 서버 측 라우터의 공인 IP 주소를 AWS 측에 등록하여 연결 기점(시작점)을 정의합니다.
2. **가상 프라이빗 게이트웨이 (Virtual Private Gateway - VGW):** AWS VPC 단에 부착하는 VPN 종단점(도착점)으로, VPC 라우팅 테이블과 결합하여 외부 내부망 대역으로 향하는 패킷을 흡수합니다.
3. **VPN 연결 (VPN Connection):** CGW와 VGW를 매핑하고 이중화된 두 개의 IPsec 터널 사설 대역 및 Pre-Shared Key(공유 비밀키)를 생성합니다.

### 3.2 온프레미스 라우팅 스크립트 가이드 (strongSwan 기반)
외부 Linux Ubuntu 서버단에서 터널을 활성화하기 위해 다운로드한 AWS VPN 구성 파일(.conf)을 기반으로 다음과 같이 데몬을 구동합니다.
```bash
# strongSwan 설치 (리눅스 IPsec 소프트웨어 클라이언트)
sudo apt update && sudo apt install strongswan -y

# VPN 터널 설정 파일 반영 및 적용
# /etc/ipsec.conf 및 /etc/ipsec.secrets 파일 내 AWS 암호화 파라미터 매핑
sudo ipsec restart
sudo ipsec statusall

```
### 3. 상세 구축 절차 (Step-by-Step Setup Guide)
AWS 클라우드(VPC)와 현장 AI 서버를 연결하기 위해 인프라 관리자가 직접 수행한 구체적인 세팅 과정입니다.

### Step 1: 가상 프라이빗 게이트웨이 (VGW) 생성 및 VPC 연결
1. AWS 콘솔에서 **VPC** 서비스로 이동합니다.
2. 왼쪽 메뉴 하단의 **[가상 프라이빗 게이트웨이]**를 클릭하고 `생성` 버튼을 누릅니다.
3. 이름 태그(예: `smart-safety-vgw`)를 입력하여 생성한 뒤, 해당 VGW를 선택하고 **[VPC에 연결]**을 눌러 우리 시스템이 올라가 있는 메인 VPC에 부착(Attach)합니다.

### Step 2: 고객 게이트웨이 (CGW) 생성
1. 왼쪽 메뉴에서 **[고객 게이트웨이]**를 클릭하고 `생성`을 누릅니다.
2. **라우팅 옵션:** 정적(Static) 라우팅을 선택합니다.
3. **IP 주소:** 외부 현장 AI 서버(Ubuntu)가 사용하는 **공인 IP 주소**를 정확히 입력하고 생성합니다. (이 IP가 터널의 출발점이 됩니다.)

### Step 3: Site-to-Site VPN 연결 생성
1. 왼쪽 메뉴에서 **[Site-to-Site VPN 연결]**을 클릭하고 `VPN 연결 생성`을 누릅니다.
2. **대상 게이트웨이(VGW):** Step 1에서 만든 VGW를 선택합니다.
3. **고객 게이트웨이(CGW):** Step 2에서 만든 기존 CGW를 선택합니다.
4. **라우팅 옵션:** 정적(Static)을 선택하고, 현장 AI 서버가 속한 내부 사설망 대역(CIDR)을 입력합니다.
5. 생성이 완료되어 상태가 `Available`로 바뀌면, 상단의 **[구성 다운로드(Download Configuration)]** 버튼을 눌러 벤더사를 `strongSwan`으로 지정한 뒤 설정 파일(.txt)을 다운로드합니다.

### Step 4: VPC 라우팅 테이블 업데이트 (🌟 트래픽 방향 설정)
1. 왼쪽 메뉴에서 **[라우팅 테이블]**로 이동하여, 백엔드 서버나 MQTT 브로커가 위치한 서브넷의 라우팅 테이블을 엽니다.
2. 하단의 **[라우팅 전파(Route Propagation)]** 탭에서 VGW 라우팅 전파를 `예(Yes)`로 활성화합니다. (또는 수동 라우팅 탭에서 현장 대역 IP를 목적지로, 타겟을 VGW로 잡아줍니다.)

### Step 5: 온프레미스(현장 AI 서버) 터널링 클라이언트 활성화
AWS 쪽 터널 문이 열렸으므로, 현장 Ubuntu 서버에 접속하여 반대쪽 터널 문을 열어줍니다. 다운로드한 구성 파일을 바탕으로 IPsec 클라이언트를 세팅합니다.


# 1. strongSwan 패키지 설치 (리눅스용 IPsec 소프트웨어)
sudo apt update && sudo apt install strongswan -y

# 2. IPsec 터널 설정 (/etc/ipsec.conf)
# AWS에서 다운로드한 설정 파일의 [Tunnel 1] 정보를 바탕으로, 
# 좌측(Left=현장 Ubuntu IP)과 우측(Right=AWS 터널 외부 IP)을 매핑하여 입력합니다.

# 3. 사전 공유 키 입력 (/etc/ipsec.secrets)
# 다운로드한 텍스트 파일에 적힌 Pre-Shared Key(PSK) 난수를 입력하여 인증을 구성합니다.

# 4. VPN 서비스 재시작 및 상태 확인
sudo ipsec restart
sudo ipsec statusall
# (결과 화면에서 터널 상태가 ESTABLISHED 로 뜨면 상호 연결 성공입니다!)
