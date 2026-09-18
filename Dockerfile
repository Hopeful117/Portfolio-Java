FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=5s --start-period=30s --retries=6 \
  CMD curl --fail --silent http://127.0.0.1:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
