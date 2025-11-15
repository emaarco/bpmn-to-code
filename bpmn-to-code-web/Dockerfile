FROM gradle:8-jdk21 AS build

WORKDIR /app

# Copy gradle files
COPY settings.gradle.kts .
COPY build.gradle.kts .
COPY gradle gradle
COPY gradlew .

# Copy all modules
COPY bpmn-to-code-core bpmn-to-code-core
COPY bpmn-to-code-web bpmn-to-code-web

# Build the application
RUN ./gradlew :bpmn-to-code-web:build --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built JAR
COPY --from=build /app/bpmn-to-code-web/build/libs/*.jar app.jar

# Expose port
EXPOSE 8080

# Set environment variables
ENV PORT=8080
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Run the application
CMD java $JAVA_OPTS -jar app.jar
