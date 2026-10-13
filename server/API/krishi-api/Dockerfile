# ==========================================
# Stage 1: Build with Maven & Java 21
# ==========================================
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

# Copy Maven wrapper and POM files first for caching
COPY mvnw pom.xml ./
COPY .mvn .mvn

# Ensure execution permissions and fix potential Windows CRLF line endings
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

# Download project dependencies (cached layer)
RUN ./mvnw dependency:go-offline -B || true

# Copy source code and build production jar (skipping tests for container image packaging)
COPY src src
RUN ./mvnw clean package -DskipTests -B

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
