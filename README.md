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
Spring Boot, JPA, eGovFrame, Flyway, MySQL, H2, Apache, Log4j2, JUnit, Mockito, JMeter, K6
<div align="center">
  <img src="https://github.com/user-attachments/assets/de41c475-f0f8-4931-83bd-4e028a2fa370" style="width:100%;" />
</div>
---
<br>

**인프라 스택**
Docker, Docker Compose, Jenkins, Spring Cloud, Nginx, Sentry
<div align="center">
  <img src="https://github.com/user-attachments/assets/59835619-e181-4c27-b56d-f232358af6bf" style="width:100%;" />
</div>

<br>

**협업 도구 스택**
GitHub, Postman, Figma, Notion, Slack
<div align="center">
  <img src="https://github.com/user-attachments/assets/3b0c8fac-f416-4deb-b801-6be31b398120" style="width:100%;" />
</div>

<br>

**배포 아키텍처**
<div align="center">
  <img src="https://github.com/user-attachments/assets/d9567878-78ad-4aa5-8fd2-4dba3a1b336b" style="width:100%;" />
</div>

1. **개발자가 GitHub에 코드를 푸시**  
2. Jenkins가 코드 분석(Checkstyle, JUnit 등) 및 Docker 이미지 빌드 수행  
3. 빌드된 이미지를 Nginx, Spring, FastAPI, MySQL 인프라로 배포  
4. 배포 후 오류는 Sentry를 통해 Slack으로 팀에 자동 알림 전송



---

## 📁 폴더 구조 & 주요 모듈

- `kodanect.domain`: 주요 도메인 CRUD, 예외 처리, 서비스 계층
- `kodanect.common`: AOP, 글로벌 예외 처리, 로깅 설정(Log4j2 + MDC)
- `docker-compose.*.yml`: 개발 및 운영 환경용 Docker 설정
- `config/checkstyle/`: 코드 스타일 정의
- `Jenkinsfile`: CI/CD 자동화 스크립트

## 🔍 주요 특징 요약

-  **로깅/모니터링**: Log4j2 + MDC + SecureLogger + Sentry 연동
-  **품질 분석**: Checkstyle, SpotBugs, OWASP Dependency-Check
-  **CI/CD**: Jenkins 기반 자동 빌드 및 Slack 알림
-  **예외 관리**: eGov AOP 기반 글로벌 예외 처리 구조
-  **파일 관리**: Docker 컨테이너 바인딩 방식으로 업로드 파일 처리

