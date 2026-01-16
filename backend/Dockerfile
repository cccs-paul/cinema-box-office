# Multi-stage build for Box Office Backend API
# Stage 1: Build stage
FROM eclipse-temurin:25-jdk-noble AS builder

WORKDIR /build

# Copy Maven files
COPY backend/pom.xml .
COPY pom.xml ../pom.xml

# Download dependencies (cache layer)
RUN mvn dependency:resolve -DskipTests

# Copy source code
COPY backend/src ./src

# Build application
RUN mvn clean package -DskipTests

# Stage 2: Runtime stage
FROM eclipse-temurin:25-jre-noble

WORKDIR /app

# Create non-root user
RUN useradd -m -u 1000 boxoffice && \
    chown -R boxoffice:boxoffice /app

# Copy JAR from builder stage
COPY --from=builder --chown=boxoffice:boxoffice /build/target/*.jar app.jar

# Switch to non-root user
USER boxoffice

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD java -jar app.jar --check-health || exit 1

# Expose API port
EXPOSE 8080

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
