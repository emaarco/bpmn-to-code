# Use distroless Java 21 image for smaller size and better security
FROM gcr.io/distroless/java21-debian12:nonroot

WORKDIR /app

# Copy the pre-built shadow JAR from local build
COPY bpmn-to-code-mcp/build/libs/bpmn-to-code-mcp-*-all.jar app.jar

# Expose port
EXPOSE 8080

# Set environment variables
ENV JAVA_TOOL_OPTIONS="-Xmx512m -Xms256m"

# Run the application in HTTP mode
ENTRYPOINT ["java", "-jar", "app.jar", "--http"]
