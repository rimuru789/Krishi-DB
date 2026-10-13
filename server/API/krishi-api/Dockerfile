# ==========================================
# Stage 1: Build with Maven & Java 21
# ==========================================
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy POM and source code directly
COPY pom.xml ./
COPY src src

# Build production jar using pre-installed Maven
RUN mvn -B clean package -DskipTests

# ==========================================
# Stage 2: Lean Production Runtime (Java 21 JRE)
# ==========================================
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Create non-root system user for security
RUN groupadd -r krishi && useradd -r -g krishi krishi

# Copy executable fat jar from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Set correct permissions
RUN chown -R krishi:krishi /app

USER krishi

# Render injects PORT dynamically; default to 8080
ENV PORT=8080
EXPOSE 8080

# Container memory optimization for Render free tier (512MB RAM)
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Dserver.port=${PORT} -jar app.jar"]
