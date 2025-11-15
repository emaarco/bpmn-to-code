# bpmn-to-code-web

Web application for bpmn-to-code - generate Process APIs from BPMN files through a browser interface.

## Status

✅ **MVP Complete** - Backend and frontend functional, ready for testing

## What it does

bpmn-to-code-web provides a web interface to use bpmn-to-code without installing Gradle or Maven plugins.

**Features:**
- Upload BPMN files via browser
- Configure generation options (language, engine, package, versioning)
- Generate Kotlin or Java Process APIs
- Download generated code files

## Architecture

- **Backend:** Ktor (Kotlin web framework) on JVM
- **Core Logic:** Reuses existing `bpmn-to-code-core` module (zero duplication)
- **Frontend:** Kotlin/JS (planned)
- **Deployment:** Docker container

## Running Locally

### Prerequisites
- JDK 21+
- Gradle 8+

### Start the Server

```bash
./gradlew :bpmn-to-code-web:run
```

The server starts on `http://localhost:8080`

**Then open your browser:**
```
http://localhost:8080
```

You'll see the web interface where you can:
1. Upload BPMN files
2. Configure generation options
3. Generate and download Process APIs

### Making Frontend Changes

If you modify the HTML, CSS, or JavaScript files:

1. **Rebuild the application** to copy static files to the build directory:
   ```bash
   ./gradlew :bpmn-to-code-web:build
   ```

2. **Restart the server** (stop with Ctrl+C if running, then):
   ```bash
   ./gradlew :bpmn-to-code-web:run
   ```

3. **Refresh your browser** to see the changes

**Quick tip:** Static files are located in:
- HTML: `src/main/resources/static/index.html`
- CSS: `src/main/resources/static/css/styles.css`
- JavaScript: `src/main/resources/static/js/app.js`

Changes to these files require a rebuild to be picked up by the running server.

### Test the API

**Health Check:**
```bash
curl http://localhost:8080/health
```

**Generate API:**
```bash
# Encode BPMN file to Base64
BASE64_CONTENT=$(base64 -i path/to/your-process.bpmn)

# Call the API
curl -X POST http://localhost:8080/api/generate \
  -H "Content-Type: application/json" \
  -d "{
    \"files\": [
      {
        \"fileName\": \"your-process.bpmn\",
        \"content\": \"$BASE64_CONTENT\"
      }
    ],
    \"config\": {
      \"packagePath\": \"com.example.process\",
      \"outputLanguage\": \"KOTLIN\",
      \"processEngine\": \"ZEEBE\",
      \"useVersioning\": false
    }
  }"
```

## API Documentation

### POST /api/generate

Generates Process API code from BPMN files.

**Request Body:**
```json
{
  "files": [
    {
      "fileName": "string",
      "content": "string (Base64-encoded BPMN XML)"
    }
  ],
  "config": {
    "packagePath": "string",
    "outputLanguage": "KOTLIN | JAVA",
    "processEngine": "ZEEBE | CAMUNDA_7",
    "useVersioning": false
  }
}
```

**Response:**
```json
{
  "success": true,
  "files": [
    {
      "fileName": "ProcessApi.kt",
      "content": "// Generated Kotlin code...",
      "processId": "processId"
    }
  ],
  "error": null
}
```

## Development Status

### ✅ Completed (MVP)
- [x] Backend module structure
- [x] Ktor application setup
- [x] Request/response models with serialization
- [x] WebGenerationService (adapter to core)
- [x] POST /api/generate endpoint
- [x] Error handling and validation
- [x] Unit tests for WebGenerationService
- [x] Frontend implementation (HTML/CSS/JS)
  - [x] File upload component with drag & drop
  - [x] Configuration form
  - [x] API client
  - [x] Code preview with syntax highlighting
  - [x] Download functionality

### 📋 Planned
- [ ] Docker containerization
- [ ] CI/CD pipeline
- [ ] Deployment to hosting platform
- [ ] Frontend polish (syntax highlighting, responsive design)
- [ ] ZIP download for multiple files
- [ ] Example BPMN files gallery

## Testing

Run the test suite:

```bash
./gradlew :bpmn-to-code-web:test
```

Test the WebGenerationService directly:

```bash
./gradlew :bpmn-to-code-web:test --tests WebGenerationServiceTest
```

## Project Structure

```
bpmn-to-code-web/
├── src/
│   ├── main/kotlin/io/github/emaarco/bpmn/web/
│   │   ├── Application.kt           # Ktor app setup
│   │   ├── model/
│   │   │   ├── GenerateRequest.kt   # API request models
│   │   │   └── GenerateResponse.kt  # API response models
│   │   ├── routes/
│   │   │   └── GenerateRoutes.kt    # REST endpoints
│   │   └── service/
│   │       └── WebGenerationService.kt  # Core integration
│   ├── main/resources/
│   │   └── logback.xml              # Logging config
│   └── test/kotlin/io/github/emaarco/bpmn/web/
│       └── service/
│           └── WebGenerationServiceTest.kt
└── build.gradle.kts
```

## Implementation Details

### How it Works

1. User uploads BPMN file(s) (Base64-encoded in JSON request)
2. Backend receives request at `/api/generate`
3. `WebGenerationService`:
   - Decodes Base64 BPMN content
   - Writes files to temporary directory
   - Invokes `CreateProcessApiPlugin` from `bpmn-to-code-core`
   - Reads generated Kotlin/Java files
   - Cleans up temporary directories
4. Returns generated code in JSON response
5. User downloads generated files

### Design Principles

- **Stateless:** No database, all processing in-memory with temp files
- **Reuse:** 100% reuse of existing `bpmn-to-code-core` logic
- **Clean Architecture:** Follows hexagonal architecture pattern
- **Security:** File validation, size limits, input sanitization

## Future Enhancements

- User accounts & history
- Public gallery of example processes
- Integration with BPMN modeling tools
- API versioning comparison tool
- Style guide validation (when feature available)

## Contributing

See main project [Contributing Guidelines](../README.md#contributing)

## License

Same as parent project - see [LICENSE](../LICENSE)
