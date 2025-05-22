# 개발용 스테이지
FROM maven:3.9.6-eclipse-temurin-17 AS dev

WORKDIR /app


COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

COPY config ./config


RUN mvn dependency:go-offline -B


COPY src ./src

CMD ["mvn", "spring-boot:run"]


# 운영용 빌드 스테이지
FROM maven:3.9.6-eclipse-temurin-17 AS builder

ARG RUN_MODE=prod
ENV RUN_MODE=${RUN_MODE}

WORKDIR /app

COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

RUN mvn dependency:go-offline -B

COPY src ./src
COPY config ./config

RUN echo "빌드 모드: $RUN_MODE" && \
    if [ "$RUN_MODE" = "prod" ]; then \
      mvn clean package -DskipTests; \
    else \
      echo "Development mode - build skipped"; \
    fi


# 운영용 실행 스테이지
FROM eclipse-temurin:17-jre

ARG RUN_MODE=prod
ENV RUN_MODE=${RUN_MODE}

WORKDIR /app

COPY --from=builder /app/target/KODAnect-backend-springboot-1.0.0.jar ./app.jar

ENTRYPOINT ["/bin/sh", "-c", "\
  echo \"실행 모드: $RUN_MODE\" && \
  if [ \"$RUN_MODE\" = \"dev\" ]; then \
    echo 'Dev Mode - 컨테이너는 대기 중, spring-boot:run 은 dev 스테이지에서 사용하세요'; \
    tail -f /dev/null; \
  else \
    echo 'Prod Mode - JAR 실행'; \
    java -jar app.jar; \
  fi"]