# ============================================================================
# Dockerfile — SytTechSchedulerService (Spring Boot 4 + Java 25)
# Multi-stage: build (Maven + JDK) → runtime (JRE only).
# ============================================================================

# ── Stage 1: build ─────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Cache de dependências (camada reaproveitada enquanto o pom.xml não muda)
COPY pom.xml ./
COPY mvnw ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Cdigo-fonte + build (os testes ficam para o pipeline CI)
COPY src src
# -Dmaven.antrun.skip pula o plugin que instala git-hooks (no faz sentido no container).
RUN ./mvnw package -DskipTests -Dmaven.antrun.skip=true -B

# ── Stage 2: runtime ───────────────────────────────────────────────────────
FROM eclipse-temurin:25-jre-alpine
LABEL maintainer="SytTech <contato@syttech.com>"
LABEL description="SytTech Scheduler Service — Spring Boot 4 + Java 25"

# Usuário não-root
RUN addgroup -S appuser && adduser -S appuser -G appuser
WORKDIR /app

COPY --from=build --chown=appuser:appuser \
     /app/target/SytTechSchedulerService-0.0.1-SNAPSHOT.jar app.jar

USER appuser
EXPOSE 8082

HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD wget -qO- http://localhost:8082/actuator/health || exit 1

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-jar", \
    "app.jar"]

