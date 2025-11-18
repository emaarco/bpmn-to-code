# Use distroless Java 21 image for smaller size and better security
FROM gcr.io/distroless/java21-debian12:nonroot

WORKDIR /app

# Copy the pre-built fat JAR from local build
COPY bpmn-to-code-web/build/libs/bpmn-to-code-web-*-fat.jar app.jar

# Expose port
EXPOSE 8080

# Set environment variables
ENV PORT=8080
ENV JAVA_TOOL_OPTIONS="-Xmx512m -Xms256m"

# CORS configuration (allow all origins by default)
# Set to comma-separated list of origins to restrict: "https://example.com,https://app.example.com"
ENV ALLOWED_CORS_ORIGINS=""

# Legal links configuration (disabled by default)
ENV LEGAL_LINKS_ENABLED=false
ENV IMPRINT_URL=""
ENV PRIVACY_URL=""

# Note: Distroless images don't support shell-based health checks
# Health checks should be configured at the orchestrator level (Kubernetes, Docker Compose, etc.)

# Run the application
# Distroless doesn't have sh, so use direct java invocation
ENTRYPOINT ["java", "-jar", "app.jar"]
