# Multi-stage build for myRC Backend API
# Stage 1: Build stage
FROM ubuntu:24.04 AS builder

WORKDIR /build

# Install Maven and Java
RUN apt-get update && apt-get install -y --no-install-recommends \
    maven \
    openjdk-25-jdk-headless && \
    rm -rf /var/lib/apt/lists/*

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
FROM ubuntu:24.04

WORKDIR /app

# Install Java runtime and curl for health checks
RUN apt-get update && apt-get install -y --no-install-recommends \
    openjdk-25-jre-headless \
    curl && \
    rm -rf /var/lib/apt/lists/*

# Create non-root user (use different UID)
RUN useradd -m -u 1001 myrc && \
    chown -R myrc:myrc /app

# Copy JAR from builder stage
COPY --from=builder --chown=myrc:myrc /build/target/*.jar app.jar

# Switch to non-root user
USER myrc

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/api/health || exit 1

# Expose API port
EXPOSE 8080

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
