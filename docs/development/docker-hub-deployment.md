# Docker Hub Deployment Guide

This guide explains how to build and deploy new versions of `bpmn-to-code-web` to Docker Hub.

## Prerequisites

### 1. Docker Hub Account
- Repository: `emaarco/bpmn-to-code-web`
- Access: Maintainer or Owner permissions required

### 2. Docker Installation
Verify Docker is installed:
```bash
docker --version
```

### 3. Docker Hub Authentication
Log in to Docker Hub:
```bash
docker login
```

### 4. Repository Access
Ensure you're working in the project root:
```bash
cd /path/to/bpmn-to-code
```

## Deployment Steps

### Step 1: Update Version

Edit `bpmn-to-code-web/build.gradle.kts`:
```kotlin
version = "0.0.16"  // Increment version
```

Follow semantic versioning:
- **Major** (X.0.0): Breaking changes
- **Minor** (0.X.0): New features, backward-compatible
- **Patch** (0.0.X): Bug fixes, backward-compatible

### Step 2: Build the Docker Image

Run the Gradle Docker build task:
```bash
./gradlew :bpmn-to-code-web:dockerBuild
```

This task:
1. Builds the fat JAR: `./gradlew :bpmn-to-code-web:buildFatJar`
2. Runs `docker build` using `bpmn-to-code-web/Dockerfile`
3. Tags image as:
   - `emaarco/bpmn-to-code-web:VERSION` (e.g., `0.0.16`)
   - `emaarco/bpmn-to-code-web:latest`

**Platform Architecture:**
The build is configured to create AMD64/x86_64 images using `--platform linux/amd64`. This ensures compatibility with most cloud Kubernetes clusters (AWS, GCP, Azure, etc.), even when building locally on Apple Silicon Macs.

**Image Details:**
- Base image: `gcr.io/distroless/java21-debian12:nonroot` (Google Distroless)
- Image size: ~300MB (base: ~195MB, JAR: ~105MB)
- Security: Minimal attack surface (no shell, package managers, or unnecessary tools)
- User: Runs as non-root user by default

**Health Checks:**
The distroless image doesn't support shell-based health checks. Configure health checks at the orchestrator level:
- Kubernetes: Use `livenessProbe` and `readinessProbe` with HTTP GET to `/`
- Docker Compose: Use `test: ["CMD", "curl", "-f", "http://localhost:8080/"]` (requires curl in image)

Verify the image was created:
```bash
docker images | grep bpmn-to-code-web
```

Expected output:
```
emaarco/bpmn-to-code-web   0.0.16    abc123def456   ...
emaarco/bpmn-to-code-web   latest    abc123def456   ...
```

### Step 3: Test the Image Locally

Run the container:
```bash
./gradlew :bpmn-to-code-web:dockerRun
```

Or manually:
```bash
docker run -p 8080:8080 --rm emaarco/bpmn-to-code-web:latest
```

**Verify functionality:**
1. Open browser: `http://localhost:8080`
2. Upload a BPMN file
3. Generate code and verify output
4. Stop container: `Ctrl+C`

### Step 4: Tag Git Release

**IMPORTANT:** Always create a Git tag before pushing to Docker Hub. Release versions must be tagged.

Create Git tag for the version:
```bash
git tag -a v0.0.16 -m "Release version 0.0.16"
git push origin v0.0.16
```

Create a GitHub release:
1. Go to: https://github.com/emaarco/bpmn-to-code/releases
2. Click "Draft a new release"
3. Select tag `v0.0.16`
4. Add release notes
5. Publish release

### Step 5: Push to Docker Hub

Push the image:
```bash
./gradlew :bpmn-to-code-web:dockerPush
```

This pushes both tags:
- `emaarco/bpmn-to-code-web:VERSION`
- `emaarco/bpmn-to-code-web:latest`

**Manual push (alternative):**
```bash
docker push emaarco/bpmn-to-code-web:0.0.16
docker push emaarco/bpmn-to-code-web:latest
```

### Step 6: Verify Deployment

Check Docker Hub:
1. Visit: https://hub.docker.com/r/emaarco/bpmn-to-code-web/tags
2. Confirm new version appears in tag list
3. Verify `latest` tag updated

Pull and test from Docker Hub:
```bash
docker pull emaarco/bpmn-to-code-web:0.0.16
docker run -p 8080:8080 emaarco/bpmn-to-code-web:0.0.16
```

## Gradle Tasks Reference

### Build Tasks
```bash
# Build fat JAR only
./gradlew :bpmn-to-code-web:buildFatJar

# Build Docker image (includes buildFatJar)
./gradlew :bpmn-to-code-web:dockerBuild

# Build and push to Docker Hub
./gradlew :bpmn-to-code-web:dockerPush

# Run container locally
./gradlew :bpmn-to-code-web:dockerRun
```

### Task Configuration
Defined in `bpmn-to-code-web/build.gradle.kts`:
```kotlin
val dockerImageName = "emaarco/bpmn-to-code-web"
val dockerImageTag = project.version.toString()
```

## Docker Commands Reference

### Image Management
```bash
# List images
docker images | grep bpmn-to-code-web

# Remove local image
docker rmi emaarco/bpmn-to-code-web:VERSION

# Remove all versions
docker rmi $(docker images emaarco/bpmn-to-code-web -q)

# Inspect image
docker inspect emaarco/bpmn-to-code-web:latest
```

### Container Management
```bash
# Run container (foreground)
docker run -p 8080:8080 --rm emaarco/bpmn-to-code-web:latest

# Run container (background)
docker run -d -p 8080:8080 --name bpmn-web emaarco/bpmn-to-code-web:latest

# Stop container
docker stop bpmn-web

# View logs
docker logs bpmn-web

# Remove container
docker rm bpmn-web
```
