# Single stage - use pre-built JAR (run `mvn package` before docker build)
# This avoids SSL issues in corporate networks with proxy/firewall interception
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Create non-root user
RUN groupadd -g 1001 myrc && \
    useradd -m -u 1001 -g myrc myrc && \
    chown -R myrc:myrc /app

# Copy pre-built JAR (build with: cd backend && mvn clean package -DskipTests)
COPY --chown=myrc:myrc backend/target/*.jar app.jar

# Switch to non-root user
USER myrc

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -sf http://localhost:8080/api/health || exit 1

# Expose API port
EXPOSE 8080

# JVM optimizations for containers
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
