# Multi-stage build for optimized image size
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# Copy project files
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests -B

# Extract JAR info for runtime stage
RUN ls -la target/*.jar

# Runtime stage - use minimal JRE image
FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="PostHere Team"
LABEL description="PostHere Microservice - File Upload & Email Processing"
LABEL version="0.0.1"

# Create app directory
WORKDIR /app

# Copy JAR from builder
COPY --from=builder /build/target/gemini-hello-*.jar app.jar

# Create non-root user for security
RUN addgroup -S posthere && adduser -S posthere -G posthere
USER posthere

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD java -cp app.jar com.example.gemini.HelloGeminiApplication health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
CMD []
