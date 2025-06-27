# 🌐 KODA 공식 홈페이지 리뉴얼 프로젝트

<br>

<div align="center">
  <img src="https://github.com/user-attachments/assets/512b48f0-3820-4b82-a83f-899318114941" />
</div>

<br>

```
기증으로 이어지는 소중한 생명, 그리고 그 가치를 믿고 이어주는 웹의 신뢰를 함께 만듭니다.
```

<br>

KODA(한국장기조직기증원) 공식 홈페이지는 현재 PC 웹과 모바일 웹이 분리되어 운영되고 있으며, 웹 접근성 기준을 충족하지 못해 사용자 경험 개선이 요구되고 있습니다. 
이번 프로젝트는 현대적이고 반응형 구조를 갖춘 통합 웹사이트로 개편하여, **웹 접근성 강화**, **사용자 편의성 향상**, 그리고 **대국민 서비스의 연속성 확보**를 목표로 합니다.

<br>

**백엔드 스택**
<div align="center">
  <img src="https://github.com/user-attachments/assets/de41c475-f0f8-4931-83bd-4e028a2fa370" style="width:100%;" />
</div>

<br>

**인프라 스택**
<div align="center">
  <img src="https://github.com/user-attachments/assets/59835619-e181-4c27-b56d-f232358af6bf" style="width:100%;" />
</div>

<br>

**협업 도구 스택**
<div align="center">
  <img src="https://github.com/user-attachments/assets/3b0c8fac-f416-4deb-b801-6be31b398120" style="width:100%;" />
</div>

<br>

**배포 아키텍처**
<div align="center">
  <img src="https://github.com/user-attachments/assets/d9567878-78ad-4aa5-8fd2-4dba3a1b336b" style="width:100%;" />
</div>

KODAnect-Backend-SpringBoot

📦 Java 17 · Spring Boot 2.7.18 · MySQL 8 · Docker · Log4j2 · Sentry

목차

프로젝트 소개

아키텍처

빠른 시작

필수 요구사항

환경 변수

개발 환경 실행

프로덕션 빌드 & 배포

주요 기능

테스트 & 품질

모니터링 & 로깅

CI/CD 파이프라인

기여 가이드

로드맵

라이선스

프로젝트 소개

KODAnect는 기부/추모 플랫폼의 백엔드 서비스로,Spring Boot 위에 eGovFrame AOP 예외 처리, 구조화 로깅, Sentry 에러 트래킹 등을 갖춥니다.

아키텍처

┌─────────┐      ┌─────────┐      ┌─────────┐
│  GitHub │─push▶│ Jenkins │─▶ SonarQube  
└─────────┘      └─────────┘      └─────────┘
     │                             │
     ▼                             ▼
┌──────────┐       Docker image      ┌───────────┐
│  Docker  │────────────────────────▶│  Nginx    │
│ Compose  │                          │  Spring   │
└──────────┘                          │  FastAPI  │
                                      │  MySQL    │
                                      └───────────┘
                                             │
                                             ▼
                                          Sentry ▶ Slack

빌드 & 정적분석: Jenkins + Checkstyle, SpotBugs, OWASP-DC

컨테이너화: Dockerfile 멀티스테이지 + docker-compose(dev/prod)

예외 처리: eGovFrame AOP → EgovExcepHndlr → SLF4J/SecureLogger

MDC-기반 로깅: LoggingAspect, ActionLogMdcAspect

에러 트래킹: Sentry 연동 → Slack 알림

빠른 시작

필수 요구사항

JDK 17

Maven 3.6+

Docker & Docker Compose 3.8+

MySQL 8.x

(Mac/Linux) sshpass

환경 변수

키

설명

DB_HOST

MySQL 호스트 (예: 127.0.0.1)

DB_PORT

MySQL 포트 (예: 3306)

DB_NAME

데이터베이스 이름

DB_USERNAME

DB 사용자명

DB_PASSWORD

DB 비밀번호

SENTRY_DSN

Sentry DSN URL

SENTRY_AUTH_TOKEN

Sentry API 토큰

SPRING_PROFILES_ACTIVE

dev 또는 prod

DOCKER_USER

도커 허브 사용자명

IMAGE_TAG

이미지 태그 (배포 시 자동 생성됨)

개발 환경 실행

git clone https://github.com/FC-DEV3-Final-Project/KODAnect-backend-springboot.git
cd KODAnect-backend-springboot

# .env.dev 파일 작성(DB 연결 정보 등)
cp .env.example .env.dev
# 필요한 값을 채워넣으세요.

docker-compose -f docker-compose.dev.yml up --build

서비스는 http://localhost:8080 에서 실행됩니다.

로그 파일 및 업로드 디렉터리(./files, ./uploads)가 컨테이너와 바인딩됩니다.

프로덕션 빌드 & 배포

# 1) Jenkins 또는 로컬에서 도커 이미지 빌드 & 푸시
docker build --target prod -t ${DOCKER_USER}/kodanect:${IMAGE_TAG} .
docker push ${DOCKER_USER}/kodanect:${IMAGE_TAG}

# 2) 서버에서 docker-compose.prod.yml 이용해 배포
ssh user@server \
  'cd /path/to/repo && \
   export IMAGE_TAG=${IMAGE_TAG} && \
   docker-compose -f docker-compose.prod.yml pull && \
   docker-compose -f docker-compose.prod.yml up -d'

주요 기능

REST API: 도메인별 CRUD (Article, Donation, Heaven, Memorial, Recipient…)

Cursor-Pagination: 효율적 커서 기반 페이지네이션

파일 업로드/다운로드: S3 대신 로컬 스토리지(컨테이너 바인딩)

예외 핸들링: 도메인별 *ExceptionHandler, 글로벌 예외 관리

테스트 & 품질

단위/통합 테스트: JUnit 4, Spring Test

코드 스타일: Checkstyle (config/checkstyle/*.xml)

정적 분석: SpotBugs (FindSecBugs), OWASP Dependency-Check

커버리지 리포트: JaCoCo → SonarCloud 연동

모니터링 & 로깅

Log4j2: log4j2-dev.xml / log4j2-prod.xml

MDC

LoggingAspect → 서비스 계층 클래스/메서드 정보

ActionLogMdcAspect → 프론트/백엔드 액션 로그 컨텍스트

Sentry

Spring Boot Starter 연동

Release & 환경 태깅 → Slack 알림

CI/CD 파이프라인

Jenkinsfile:

Checkout → Checkstyle → Build → Test → Coverage

Docker Build & Push → GitHub Deployment → Server Deploy → Health-check

SonarCloud, Slack 알림, Sentry Release 생성

기여 가이드

Fork & Clone

브랜치 생성: feature/your-feature-name

코드 작성 → mvn clean verify 통과

Pull Request

제목: [FEATURE|FIX] 간단한 설명

설명: 변경사항 요약 + 동작 스크린샷(필요 시)

로드맵

🔜 JWT 기반 인증/인가

🔜 Redis 캐시 레이어

🔜 Grafana + Prometheus 모니터링

🔜 AWS S3 파일스토리지 전환

🔜 Kubernetes 배포

라이선스

Apache License 2.0 © FC-DEV3-Final-Project
