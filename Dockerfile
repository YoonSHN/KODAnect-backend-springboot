# 개발 스테이지
FROM maven:3.9.6-eclipse-temurin-17 AS dev

WORKDIR /app

COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
COPY config ./config

RUN mvn dependency:go-offline -B

COPY src ./src

CMD ["mvn", "spring-boot:run"]


# 빌드 스테이지
FROM maven:3.9.6-eclipse-temurin-17 AS builder

ARG RUN_MODE=prod
ENV RUN_MODE=${RUN_MODE}

WORKDIR /app

COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
COPY config ./config

RUN mvn dependency:go-offline -B

COPY src ./src

RUN echo "Build mode: $RUN_MODE" && \
    if [ "$RUN_MODE" = "prod" ]; then \
      mvn clean package -DskipTests; \
    else \
      echo "Development mode - skipping build"; \
    fi


# 운영 스테이지
FROM eclipse-temurin:17-jre

ARG RUN_MODE=prod
ENV RUN_MODE=${RUN_MODE}
ENV SPRING_PROFILES_ACTIVE=${RUN_MODE}

WORKDIR /app

COPY --from=builder /app/target/KODAnect-backend-springboot-1.0.0.jar ./app.jar
COPY application.properties ./
COPY application-${RUN_MODE}.properties ./

ENTRYPOINT ["/bin/sh", "-c", "\
  echo \"Runtime mode: $RUN_MODE\" && \
  if [ \"$RUN_MODE\" = \"dev\" ]; then \
    echo 'Dev Mode - container is idle, use dev stage to run spring-boot:run'; \
    tail -f /dev/null; \
  else \
    echo 'Prod Mode - running JAR'; \
    java -Dspring.profiles.active=$RUN_MODE -jar app.jar; \
  fi"]
