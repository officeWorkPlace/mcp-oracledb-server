# Multi-stage Docker build for production-ready Oracle MCP Server
FROM eclipse-temurin:17-jdk-alpine AS builder

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Download dependencies (for better layer caching)
RUN ./mvnw dependency:resolve dependency:resolve-sources

# Copy source code
COPY src/ src/

# Build the application
RUN ./mvnw clean package -DskipTests -Penhanced && \
    java -Djarmode=layertools -jar target/*.jar extract

# Production stage
FROM eclipse-temurin:17-jre-alpine AS production

# Install required packages
RUN apk add --no-cache \
    curl \
    ca-certificates \
    tzdata && \
    addgroup -g 1001 -S spring && \
    adduser -S spring -u 1001 -G spring

# Set timezone
ENV TZ=UTC

# Create app directory
WORKDIR /app

# Copy built application layers (for optimal caching)
COPY --from=builder --chown=spring:spring /app/dependencies/ ./
COPY --from=builder --chown=spring:spring /app/spring-boot-loader/ ./
COPY --from=builder --chown=spring:spring /app/snapshot-dependencies/ ./
COPY --from=builder --chown=spring:spring /app/application/ ./

# Switch to non-root user
USER spring

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Set JVM options for production (Java 17 + Oracle optimized)
ENV JAVA_OPTS="-XX:+UseG1GC \
               -XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+ExitOnOutOfMemoryError \
               -XX:+HeapDumpOnOutOfMemoryError \
               -XX:HeapDumpPath=/tmp/heapdump.hprof \
               -Djava.security.egd=file:/dev/./urandom \
               -Dspring.jmx.enabled=false \
               -Doracle.jdbc.fanEnabled=false \
               -Doracle.jdbc.implicitStatementCacheSize=20"

# Environment variables
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8080
ENV MCP_TOOLS_EXPOSURE=public

# Start the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]

# Metadata labels
LABEL maintainer="officeWorkPlace <office.place.work.007@gmail.com>"
LABEL version="1.0.0-PRODUCTION"
LABEL description="Production-ready Oracle MCP Server with 55+ tools (Enhanced) / 75+ tools (Enterprise)"
LABEL org.opencontainers.image.source="https://github.com/officeWorkPlace/mcp-oracledb-server"
LABEL org.opencontainers.image.title="MCP Oracle DB Server"
LABEL org.opencontainers.image.description="Production-ready Model Context Protocol server for Oracle DB with AI integration"
LABEL org.opencontainers.image.vendor="officeWorkPlace"
LABEL org.opencontainers.image.licenses="MIT"
LABEL org.opencontainers.image.created="2025-09-05T21:40:00Z"
LABEL baseline.mongodb.tools="39"
LABEL target.oracle.tools.enhanced="55"
LABEL target.oracle.tools.enterprise="75"
