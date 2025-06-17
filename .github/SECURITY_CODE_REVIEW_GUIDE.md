# 보안 코드 리뷰 가이드

## 📌 목적

본 문서는 정적 분석 도구(SpotBugs, FindSecBugs, SonarQube 등)에서 탐지 가능한 **보안 취약점 및 코드 품질 문제**를 사전에 방지하고, **일관된 코드 리뷰 기준**을 마련하기 위해 작성되었습니다. PR 리뷰 과정과 CI 파이프라인에 적용 가능하며, 모든 개발자는 이 가이드를 준수해야 합니다.

---

## 🔐 보안 항목 상세 분석

| 항목 | 룰 ID / 키 | SonarQube 설명 | OWASP Mapping |
|------|------------|----------------|----------------|
| CRLF 로그 인젝션 | FindSecBugs: `CRLF_INJECTION_LOGS`<br>SonarJava: `java:S5131` | 사용자 입력을 로그에 직접 출력할 경우 로그 조작 또는 시스템 혼란 가능 | A09:2021 Logging & Monitoring Failures |
| Mutable 컬렉션 외부 노출 | SpotBugs: `EI_EXPOSE_REP` | 외부에서 내부 상태를 수정할 수 있어 캡슐화 원칙 위반 | OWASP Secure Coding Practices |
| NullPointerException 방지 | SonarJava: `java:S2259`, `java:S1699` | Null dereference로 인한 런타임 예외 가능성 | A05:2021 Security Misconfiguration |
| 문자열 대소문자 변환 시 Locale 미지정 | SonarJava: `java:S3355` | 국가별 문자 처리 차이로 예외 발생 위험 | 일반 보안 코딩 원칙 |
| serialVersionUID 미정의 | SonarJava: `java:S1948` | 직렬화된 객체 역직렬화 시 버전 불일치 위험 | OWASP: Deserialization of Untrusted Data |
| 사용되지 않는 코드 | PMD: `UnusedPrivateField`, `UnusedPrivateMethod`<br>SpotBugs: `URF_UNREAD_FIELD` | 유지보수성 저하 및 코드 오해 가능성 | 품질 관리 항목 (비보안) |
| 로그 MDC 기반 추적성 | Custom (정적 도구 탐지 X) | 트랜잭션·사용자 단위 로그 추적 강화 | A09:2021 Logging & Monitoring Failures |

---

## ✏️ 보안 코딩 규칙 예시

### 1. CRLF 로그 인젝션 방지

```java
// 잘못된 예시 - 사용자 입력값을 로그에 직접 출력
log.error("Login failed for user: {}", request.getParameter("username"));

// 올바른 예시 - [OWASP A09] 로그 인젝션 방지를 위한 필터링
String safeUsername = LogSanitizerUtils.sanitize(request.getParameter("username"));
log.error("Login failed for user: {}", safeUsername);
```
## 🛠 로그 및 입력 처리 시 유틸리티 사용 필수

- 모든 로그 출력(`log.info`, `log.error`, 등)에 사용자 입력값을 포함할 경우 **반드시** `LogSanitizerUtils` 사용
- DTO, Map, Object 등은 `sanitizeObject()`로 감싸 출력

---

### 2. 내부 mutable 컬렉션 외부 노출 금지

```java
// 잘못된 예시
public List<String> getItems() {
    return items;
}

// 올바른 예시 - [SpotBugs: EI_EXPOSE_REP]
public List<String> getItems() {
    return Collections.unmodifiableList(new ArrayList<>(items));
}
```

---

### 3. NullPointerException 방지

```java
// [SonarJava: S2259]
if (user != null && user.getName() != null) {
    ...
}

// Java 8+ 방식
String name = Optional.ofNullable(user)
                      .map(User::getName)
                      .orElse("anonymous");
```

---

### 4. 문자열 대소문자 처리 시 Locale 명시

```java
// 잘못된 예시
String email = input.toLowerCase();

// 올바른 예시 - [SonarJava: S3355]
String email = input.toLowerCase(Locale.ROOT);
```

---

### 5. Serializable 클래스의 serialVersionUID 정의

```java
public class UserSession implements Serializable {
    private static final long serialVersionUID = 1L;
    // ...
}
```

---

### 6. 사용되지 않는 코드 제거

```java
// 잘못된 예시 - 사용되지 않는 메서드
private void logDebugInfo() {
    System.out.println("debug...");
}

// 예외 상황: AOP, Reflection 기반 접근 시
@SuppressWarnings("unused")
private void tracedMethod() { ... }
```

---

### 7. MDC 기반 로그 추적

```java
// [OWASP A09] 사용자, 트랜잭션 단위 로그 추적
MDC.put("traceId", traceId);
MDC.put("userId", userId);
```

---

## PR 보안/품질 체크리스트

- [ ] 로그 출력 시 사용자 입력값 필터링(`LogSanitizerUtils.sanitize`) 또는 SecureLogger 적용했는가?
- [ ] 컬렉션 getter는 불변 객체 반환 또는 복사본 처리했는가?
- [ ] NullPointerException 방지를 위한 방어 코드가 적용되었는가?
- [ ] Serializable 클래스에 `serialVersionUID`를 지정했는가?
- [ ] 사용되지 않는 private 메서드, 필드 등을 제거했는가?
- [ ] 로그에 traceId, userId 등 MDC 정보 포함되었는가?

---

## 📌 참고 도구 및 리소스

### 정적 분석 도구

- **SpotBugs / FindSecBugs**: https://find-sec-bugs.github.io/
- **SonarQube Java Rules**: https://rules.sonarsource.com/java/
- **PMD Java Rules**: https://pmd.github.io/

### 보안 기준

- **OWASP Top 10 (2021)**: https://owasp.org/Top10/
- **OWASP Cheat Sheet Series**: https://cheatsheetseries.owasp.org/
- **OWASP Secure Coding Practices**: https://owasp.org/www-project-secure-coding-practices/

---

## 🧩 향후 확장할 예정입니다

| 항목 | 제안 내용 |
|------|-----------|
| SonarQube 연동 | CI 파이프라인에서 `blocker`, `critical` 룰 기준으로 PR 차단 |
| OWASP Tag 명시 | 항목별로 `// [OWASP A09]` 식 주석 표기 권장 |
| 룰 기반 리뷰 자동화 | GitHub Actions, Jenkins 등에서 FindSecBugs + SonarQube 실행 |
