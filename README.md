# baseball

Spring Boot와 React를 활용한 야구 기록 및 게임 웹 어플리케이션

## 프로젝트 개요

이 프로젝트는 Spring Boot 기반의 백엔드와 React 기반의 프론트엔드로 구성된 야구 기록 및 게임 웹 어플리케이션입니다. 사용자들은 실시간 경기 기록을 확인하고, 선수 및 팀 통계를 조회하며, 기록 분석 대시보드를 활용하고, 성적 기반 모의 야구 게임을 즐길 수 있습니다.

## 프로젝트 구조

프로젝트는 모듈화된 구조를 가지며, 각 주요 컴포넌트는 다음과 같습니다.

```
baseball/
├── .git/                 # Git 버전 관리 관련 파일
├── .idea/                # IntelliJ IDEA 설정 파일
├── baseball_db.mv.db     # H2 데이터베이스 파일 (개발용)
├── baseball_db.trace.db  # H2 데이터베이스 트레이스 파일 (개발용)
├── docker-compose.yml    # Docker Compose 설정 파일 (다중 컨테이너 오케스트레이션)
├── Dockerfile            # 메인 백엔드 애플리케이션 Docker 이미지 빌드 파일
├── package-lock.json     # 프론트엔드 Node.js 의존성 잠금 파일
├── pom.xml               # 메인 백엔드 (Maven) 프로젝트 설정 파일
├── README.md             # 프로젝트 설명 파일
├── SecurityConfig.java   # Spring Security 설정 파일
├── backend/              # (대안) 스프링 부트 백엔드 (Gradle) 프로젝트
│   ├── .gitattributes
│   ├── .gitignore
│   └── build.gradle      # Gradle 빌드 설정 파일
├── db/                   # 데이터베이스 관련 스크립트
│   └── Baseball_Ranking.sql # 야구 랭킹 데이터베이스 스크립트
├── frontend/             # 리액트 프론트엔드 프로젝트
│   ├── .gitignore
│   ├── Dockerfile        # 프론트엔드 애플리케이션 Docker 이미지 빌드 파일
│   ├── index.html
│   ├── nginx.conf        # Nginx 설정 파일 (프론트엔드 서빙)
│   ├── package-lock.json
│   ├── package.json
│   ├── README.md
│   ├── build/            # 프론트엔드 빌드 결과물
│   ├── node_modules/     # 프론트엔드 Node.js 의존성
│   ├── public/           # 정적 자원
│   └── src/              # 프론트엔드 소스 코드
│       ├── api/            # API 호출 함수
│       ├── components/     # UI 컴포넌트
│       ├── context/        # React Context API
│       ├── data/           # 정적 데이터
│       ├── hooks/          # 커스텀 훅
│       ├── pages/          # 라우팅 페이지
│       ├── store/          # 상태 관리 (Redux, Zustand 등)
│       ├── styles/         # 스타일 파일
│       └── utils/          # 유틸리티 함수
├── kubernetes/           # Kubernetes 배포 설정 파일
│   ├── backend-deployment.yaml
│   ├── backend-service.yaml
│   ├── configmap.yaml
│   ├── frontend-deployment.yaml
│   ├── frontend-service.yaml
│   ├── ingress.yaml
│   ├── namespace.yaml
│   └── secret.yaml
└── src/                  # 메인 백엔드 (Maven) 소스 코드 (표준 레이아웃)
    ├── main/
    │   ├── java/com/baseball/game/ # 백엔드 Java 소스 코드
    │   │   ├── config/         # Security, CORS 등 설정 클래스
    │   │   ├── constant/       # 상수 정의
    │   │   ├── controller/     # API 엔드포인트 정의 (MVC Controller)
    │   │   ├── dto/            # 데이터 전송 객체 (DTO)
    │   │   ├── exception/      # 커스텀 예외 처리
    │   │   ├── mapper/         # MyBatis 매퍼 인터페이스
    │   │   ├── ranking/        # 랭킹 관련 모듈
    │   │   ├── repository/     # 데이터베이스 접근 (JPA Repository 또는 MyBatis)
    │   │   ├── service/        # 비즈니스 로직 (MVC Service)
    │   │   └── util/           # 유틸리티 클래스
    │   └── resources/          # 리소스 파일
    │       ├── application.properties # Spring Boot 설정 파일
    │       ├── application.yml # Spring Boot 설정 파일
    │       ├── data.sql        # 초기 데이터 스크립트
    │       ├── log4j.xml       # 로깅 설정
    │       ├── log4jdbc.log4j2.properties # SQL 로깅 설정
    │       ├── schema.sql      # 데이터베이스 스키마 스크립트
    │       └── com/baseball/mapper/ # MyBatis XML 매퍼 파일
    └── test/                 # 백엔드 테스트 코드
        ├── java/com/baseball/game/
        └── resources/
```

## 아키텍처 및 기술 스택

### 백엔드 (Spring Boot)

*   **기술 스택**: Java 11+, Spring Boot 2.7.x, Gradle 7.5+, MyBatis, Spring Security
*   **MVC 패턴**: 백엔드는 Model-View-Controller (MVC) 디자인 패턴을 따릅니다.
    *   **Controller**: `src/main/java/com/baseball/game/controller` 패키지에 위치하며, 클라이언트의 요청을 받아 적절한 Service 계층으로 전달하고 응답을 반환합니다. RESTful API 형태로 구현됩니다.
    *   **Service**: `src/main/java/com/baseball/game/service` 패키지에 위치하며, 비즈니스 로직을 처리합니다. Controller와 Repository 사이의 중개자 역할을 합니다.
    *   **Repository (Mapper)**: `src/main/java/com/baseball/game/repository` (또는 `mapper` 인터페이스) 및 `src/main/resources/com/baseball/mapper` (MyBatis XML)에 위치하며, 데이터베이스와의 상호작용을 담당합니다.
*   **데이터베이스**: H2 (개발용), MySQL (운영용)
*   **보안**: Spring Security를 활용하여 인증 및 권한 부여를 관리합니다.

### 프론트엔드 (React)

*   **기술 스택**: Node.js 16+, React 18+, npm 8+, Axios
*   **구조**: `frontend/src` 디렉토리 내에서 컴포넌트 기반의 모듈화된 구조를 가집니다. `pages`는 라우팅되는 페이지 컴포넌트를, `components`는 재사용 가능한 UI 요소를 포함합니다. `api` 디렉토리에는 백엔드 API 호출을 위한 함수들이 정의되어 있습니다.

## Docker를 이용한 배포

프로젝트는 Docker를 사용하여 컨테이너화되어 있으며, `docker-compose`를 통해 쉽게 빌드하고 실행할 수 있습니다.

*   **`Dockerfile`**:
    *   `Dockerfile`: 백엔드 Spring Boot 애플리케이션을 위한 Docker 이미지 빌드 지침을 포함합니다.
    *   `frontend/Dockerfile`: 프론트엔드 React 애플리케이션을 위한 Docker 이미지 빌드 지침을 포함합니다. (Nginx를 통해 서빙)
*   **`docker-compose.yml`**: 백엔드, 프론트엔드, 데이터베이스 등 여러 서비스들을 하나의 명령으로 빌드, 실행, 관리할 수 있도록 정의합니다. 개발 환경 설정 및 로컬 테스트에 유용합니다.

## Kubernetes 배포

`kubernetes/` 디렉토리에는 애플리케이션을 Kubernetes 클러스터에 배포하기 위한 YAML 설정 파일들이 포함되어 있습니다. 각 서비스(백엔드, 프론트엔드)의 Deployment, Service, Ingress, ConfigMap, Secret 등을 정의합니다.

## 시작하기

### 1. Docker Compose를 이용한 실행 (권장)

프로젝트 루트 디렉토리에서 다음 명령어를 실행하여 백엔드, 프론트엔드, 데이터베이스를 한 번에 실행할 수 있습니다.

```bash
docker-compose up --build
```

애플리케이션이 실행되면:
*   백엔드: `http://localhost:8080`
*   프론트엔드: `http://localhost:3000` (또는 `http://localhost` - Nginx 설정에 따라 다름)

### 2. 개별 서비스 실행 (개발 환경)

#### 백엔드 실행 (메인 Maven 프로젝트)

프로젝트 루트 디렉토리에서 다음 명령어를 실행합니다.

```bash
# 의존성 설치 및 빌드
mvn clean install

# 애플리케이션 실행
mvn spring-boot:run
```

백엔드는 기본적으로 `http://localhost:8080`에서 실행됩니다.

#### 백엔드 실행 (대안: Gradle 프로젝트 in `backend/`)

`backend/` 디렉토리에 있는 Gradle 기반 백엔드 프로젝트를 실행하려면 다음 명령어를 사용합니다.

```bash
# 백엔드 디렉토리로 이동
cd backend

# 의존성 설치 및 빌드
./gradlew build

# 애플리케이션 실행
./gradlew bootRun
```

백엔드는 기본적으로 `http://localhost:8080`에서 실행됩니다.

#### 프론트엔드 실행

```bash
# 프론트엔드 디렉토리로 이동
cd frontend

# 의존성 설치
npm install

# 개발 서버 실행
npm start
```
프론트엔드는 기본적으로 `http://localhost:3000`에서 실행됩니다.

## API 문서

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- API 문서: `http://localhost:8080/v3/api-docs`

## 주요 기능

- 실시간 경기 기록
- 선수 및 팀 통계 조회
- 기록 분석 대시보드
- 성적 기반 모의 야구 게임

## 개발 가이드

### 백엔드

1.  `src/main/resources/application.yml`에서 데이터베이스 및 기타 설정을 확인하세요.
2.  엔티티 클래스는 `entity` 패키지에 작성합니다.
3.  API 엔드포인트는 `controller` 패키지에 REST 컨트롤러로 구현합니다.
4.  비즈니스 로직은 `service` 패키지에 구현합니다.

### 프론트엔드

1.  API 호출은 `frontend/src/api` 디렉토리의 모듈을 사용하세요.
2.  재사용 가능한 컴포넌트는 `frontend/src/components` 디렉토리에 작성하세요.
3.  페이지 컴포넌트는 `frontend/src/pages` 디렉토리에 작성하세요.