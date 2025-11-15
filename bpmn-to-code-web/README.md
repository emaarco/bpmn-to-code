# 🚀 bpmn-to-code-web

bpmn-to-code-web is a web application that provides browser-based access to bpmn-to-code's code generation capabilities.
It offers the same powerful Process API generation without requiring Gradle or Maven installation,
making it ideal for quick experiments, non-Java projects, or teams who prefer a graphical interface.

## 🌐 Why bpmn-to-code-web?

**No Installation Required**
- Access bpmn-to-code directly from your browser
- No build tool setup or project configuration needed
- Perfect for quick prototyping and exploration

**Self-Hostable**
- Deploy as a Docker container in your own infrastructure (🚧 work in progress)
- Full control over your BPMN files and generated code
- No data leaves your environment

**Same Powerful Core**
- 100% reuses the battle-tested `bpmn-to-code-core` logic
- Identical output to Gradle and Maven plugins
- Supports both Kotlin and Java generation
- Works with Camunda 7 and Zeebe engines

## ✨ Features

- Upload BPMN files via drag-and-drop or file picker
- Configure generation options (language, engine, package path)
- Generate type-safe Process API code instantly
- Preview generated code with syntax highlighting
- Download individual files or all files as ZIP
- Fully stateless in-memory processing (no filesystem persistence)
- **OpenAPI/Swagger documentation** - Interactive API documentation available at `/swagger` and `/openapi`

## 🚀 Quick Start

### Using the Hosted Version

🚧 **Coming Soon** - The hosted version will be available at [URL to be announced]

### Running Locally

**Prerequisites:**
- JDK 21+
- Gradle 8+

**Start the server:**
```bash
./gradlew :bpmn-to-code-web:run
```

**Access the web interface:**
```
http://localhost:8080
```

**Access the API documentation:**
```
http://localhost:8080/swagger    # Swagger UI
http://localhost:8080/openapi    # OpenAPI/ReDoc viewer
```

Then:
1. Upload your BPMN file(s)
2. Configure generation settings
3. Click "Generate"
4. Download your Process API files

### Self-Hosting with Docker

🚧 **Work in Progress** - Docker deployment is currently being finalized.

The application will be available as a Docker image for easy self-hosting:

```bash
# Pull the image (coming soon)
docker pull emaarco/bpmn-to-code-web:latest

# Run the container
docker run -p 8080:8080 emaarco/bpmn-to-code-web:latest
```
