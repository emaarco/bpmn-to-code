# Implementation Draft: bpmn-to-code Web Application

**Issue:** [#66 - Web Application](https://github.com/emaarco/bpmn-to-code/issues/66)
**Branch:** [66-web-application](https://github.com/emaarco/bpmn-to-code/tree/66-web-application)
**Status:** ✅ **MVP COMPLETE** - Both backend and frontend implemented and tested

## 1. Architecture Overview

### Why a Backend is Needed

The `bpmn-to-code-core` module depends on **JVM-only libraries** that cannot run in browsers:
- `org.camunda.bpm.model:camunda-bpmn-model` - BPMN XML parsing
- `kotlinpoet` / `javapoet` - Code generation
- `org.apache.ant:ant` - File pattern matching

Therefore, we need a **backend service** to execute the generation logic, with a lightweight frontend for user interaction.

### Architecture Pattern

Following the existing hexagonal architecture:
- **Domain Layer:** Reuse existing `bpmn-to-code-core` (no changes needed)
- **New Web Adapter:** REST API endpoints for file upload and code generation
- **Frontend:** Simple UI for configuration and file handling

```
┌─────────────────────────────────────────────────┐
│              Frontend (Kotlin/JS)               │
│  ┌──────────┐  ┌──────────┐  ┌──────────────┐  │
│  │  Upload  │  │  Config  │  │   Preview    │  │
│  │   BPMN   │  │   Form   │  │  & Download  │  │
│  └──────────┘  └──────────┘  └──────────────┘  │
└────────────────────┬────────────────────────────┘
                     │ HTTP REST API
┌────────────────────▼────────────────────────────┐
│           Backend (Ktor on JVM)                 │
│  ┌────────────────────────────────────────┐     │
│  │   Web Adapter (REST Controller)        │     │
│  └───────────────┬────────────────────────┘     │
│                  │                               │
│  ┌───────────────▼────────────────────────┐     │
│  │   bpmn-to-code-core (existing)         │     │
│  │   - Parse BPMN                         │     │
│  │   - Extract model                      │     │
│  │   - Generate code                      │     │
│  └────────────────────────────────────────┘     │
└─────────────────────────────────────────────────┘
```

## 2. Technology Stack

### Backend
- **Ktor** - Kotlin web framework (lightweight, asynchronous)
- **Kotlin Serialization** - JSON handling
- **bpmn-to-code-core** - Existing domain logic (dependency)

### Frontend
- **Kotlin/JS** - Stay in Kotlin ecosystem
- **kotlinx-html** or **React Kotlin/JS** - UI rendering
- **Ktor Client** - HTTP communication with backend

### Deployment
- **Docker** - Containerization
- **Hosting options:** Render, Railway, Fly.io (all have free tiers)

## 3. Module Structure

```
bpmn-to-code-web/
├── src/
│   ├── jvmMain/kotlin/          # Backend (Ktor server)
│   │   └── io.github.emaarco.bpmn.web/
│   │       ├── Application.kt           # Ktor app setup
│   │       ├── routes/
│   │       │   └── GenerateRoutes.kt    # REST endpoints
│   │       ├── model/
│   │       │   ├── GenerateRequest.kt   # API request models
│   │       │   └── GenerateResponse.kt  # API response models
│   │       └── service/
│   │           └── WebGenerationService.kt  # Adapter to core
│   │
│   ├── jsMain/kotlin/           # Frontend (Kotlin/JS)
│   │   └── io.github.emaarco.bpmn.web.ui/
│   │       ├── Main.kt                  # Entry point
│   │       ├── components/
│   │       │   ├── FileUpload.kt        # BPMN file upload
│   │       │   ├── ConfigForm.kt        # Generation config
│   │       │   └── CodePreview.kt       # Preview & download
│   │       └── api/
│   │           └── ApiClient.kt         # Backend communication
│   │
│   └── commonMain/kotlin/       # Shared models
│       └── io.github.emaarco.bpmn.web.shared/
│           └── dto/
│               ├── GenerationConfig.kt  # Shared config DTO
│               └── GenerationResult.kt  # Shared result DTO
│
├── build.gradle.kts             # Multiplatform build config
└── Dockerfile                   # Containerization
```

## 4. Backend Implementation

### 4.1 API Endpoints

**POST /api/generate**
- Accepts: Multipart form data (BPMN files + configuration)
- Returns: Generated code files as JSON or ZIP

**Request Model:**
```kotlin
@Serializable
data class GenerateRequest(
    val files: List<BpmnFileData>,  // Base64-encoded BPMN content
    val config: GenerationConfig
)

@Serializable
data class BpmnFileData(
    val fileName: String,
    val content: String  // Base64-encoded BPMN XML
)

@Serializable
data class GenerationConfig(
    val packagePath: String,
    val outputLanguage: OutputLanguage,  // KOTLIN or JAVA
    val processEngine: ProcessEngine,     // CAMUNDA_7 or ZEEBE
    val useVersioning: Boolean = false
)
```

**Response Model:**
```kotlin
@Serializable
data class GenerateResponse(
    val success: Boolean,
    val files: List<GeneratedFile>,
    val error: String? = null
)

@Serializable
data class GeneratedFile(
    val fileName: String,
    val content: String,  // Generated Kotlin/Java code
    val processId: String
)
```

### 4.2 Ktor Route Implementation

```kotlin
// routes/GenerateRoutes.kt
fun Route.generateRoutes(generationService: WebGenerationService) {

    post("/api/generate") {
        try {
            val request = call.receive<GenerateRequest>()

            // Validate input
            if (request.files.isEmpty()) {
                call.respond(HttpStatusCode.BadRequest,
                    GenerateResponse(success = false, files = emptyList(),
                    error = "No BPMN files provided"))
                return@post
            }

            // Generate code using core logic
            val result = generationService.generate(request)

            call.respond(HttpStatusCode.OK, result)

        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError,
                GenerateResponse(success = false, files = emptyList(),
                error = e.message ?: "Unknown error"))
        }
    }
}
```

### 4.3 Integration with Core

```kotlin
// service/WebGenerationService.kt
class WebGenerationService {

    fun generate(request: GenerateRequest): GenerateResponse {
        // 1. Create temporary directory for processing
        val tempDir = Files.createTempDirectory("bpmn-to-code-").toFile()
        val outputDir = Files.createTempDirectory("bpmn-output-").toFile()

        try {
            // 2. Write BPMN files to temp directory
            request.files.forEach { fileData ->
                val bpmnContent = Base64.getDecoder().decode(fileData.content)
                val file = File(tempDir, fileData.fileName)
                file.writeBytes(bpmnContent)
            }

            // 3. Execute existing core logic
            val plugin = CreateProcessApiPlugin()
            plugin.execute(
                baseDir = tempDir.absolutePath,
                filePattern = "**/*.bpmn",
                outputFolderPath = outputDir.absolutePath,
                packagePath = request.config.packagePath,
                outputLanguage = request.config.outputLanguage,
                engine = request.config.processEngine,
                useVersioning = request.config.useVersioning
            )

            // 4. Read generated files
            val generatedFiles = outputDir.walkTopDown()
                .filter { it.isFile && (it.extension == "kt" || it.extension == "java") }
                .map { file ->
                    GeneratedFile(
                        fileName = file.name,
                        content = file.readText(),
                        processId = extractProcessIdFromFileName(file.name)
                    )
                }
                .toList()

            return GenerateResponse(
                success = true,
                files = generatedFiles
            )

        } finally {
            // 5. Cleanup temp directories
            tempDir.deleteRecursively()
            outputDir.deleteRecursively()
        }
    }

    private fun extractProcessIdFromFileName(fileName: String): String {
        // NewsletterSubscriptionProcessApi.kt -> newsletterSubscription
        return fileName
            .removeSuffix(".kt")
            .removeSuffix(".java")
            .removeSuffix("ProcessApi")
            .replaceFirstChar { it.lowercase() }
    }
}
```

### 4.4 Ktor Application Setup

```kotlin
// Application.kt
fun main() {
    embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
            })
        }

        install(CORS) {
            anyHost()  // For development; restrict in production
            allowHeader(HttpHeaders.ContentType)
        }

        install(CallLogging)

        routing {
            // Serve static frontend files
            static("/") {
                resources("web")
                defaultResource("web/index.html")
            }

            // API routes
            val generationService = WebGenerationService()
            generateRoutes(generationService)
        }
    }.start(wait = true)
}
```

## 5. Frontend Implementation

### 5.1 Main Application Structure

```kotlin
// Main.kt
fun main() {
    window.onload = {
        render(document.getElementById("root")) {
            App()
        }
    }
}

@Composable
fun App() {
    var uploadedFiles by remember { mutableStateOf<List<File>>(emptyList()) }
    var config by remember { mutableStateOf(GenerationConfig.default()) }
    var generatedCode by remember { mutableStateOf<GenerateResponse?>(null) }
    var isGenerating by remember { mutableStateOf(false) }

    Div({ classes("container") }) {
        H1 { Text("bpmn-to-code Online Generator") }

        // File Upload Section
        FileUpload(
            onFilesSelected = { files -> uploadedFiles = files }
        )

        // Configuration Form
        if (uploadedFiles.isNotEmpty()) {
            ConfigForm(
                config = config,
                onConfigChange = { config = it }
            )

            Button({
                onClick {
                    isGenerating = true
                    generateCode(uploadedFiles, config) { result ->
                        generatedCode = result
                        isGenerating = false
                    }
                }
            }) {
                Text(if (isGenerating) "Generating..." else "Generate Code")
            }
        }

        // Code Preview & Download
        generatedCode?.let { result ->
            CodePreview(result)
        }
    }
}
```

### 5.2 File Upload Component

```kotlin
// components/FileUpload.kt
@Composable
fun FileUpload(onFilesSelected: (List<File>) -> Unit) {
    Div({ classes("upload-section") }) {
        Label({ attr("for", "file-input") }) {
            Text("Upload BPMN Files")
        }

        Input(InputType.File) {
            id("file-input")
            attr("accept", ".bpmn")
            attr("multiple", "true")

            onChange { event ->
                val files = (event.target as HTMLInputElement).files
                files?.let {
                    val fileList = (0 until it.length).map { index -> it[index]!! }
                    onFilesSelected(fileList)
                }
            }
        }
    }
}
```

### 5.3 Configuration Form

```kotlin
// components/ConfigForm.kt
@Composable
fun ConfigForm(
    config: GenerationConfig,
    onConfigChange: (GenerationConfig) -> Unit
) {
    Div({ classes("config-form") }) {
        // Package Name
        Label { Text("Package Name") }
        Input(InputType.Text) {
            value(config.packagePath)
            onInput { event ->
                onConfigChange(config.copy(
                    packagePath = (event.target as HTMLInputElement).value
                ))
            }
        }

        // Output Language
        Label { Text("Output Language") }
        Select {
            onChange { event ->
                val value = (event.target as HTMLSelectElement).value
                onConfigChange(config.copy(
                    outputLanguage = OutputLanguage.valueOf(value)
                ))
            }

            Option({ value("KOTLIN") }) { Text("Kotlin") }
            Option({ value("JAVA") }) { Text("Java") }
        }

        // Process Engine
        Label { Text("Process Engine") }
        Select {
            onChange { event ->
                val value = (event.target as HTMLSelectElement).value
                onConfigChange(config.copy(
                    processEngine = ProcessEngine.valueOf(value)
                ))
            }

            Option({ value("ZEEBE") }) { Text("Zeebe") }
            Option({ value("CAMUNDA_7") }) { Text("Camunda 7") }
        }

        // Versioning
        Label {
            Input(InputType.Checkbox) {
                checked(config.useVersioning)
                onChange { event ->
                    onConfigChange(config.copy(
                        useVersioning = (event.target as HTMLInputElement).checked
                    ))
                }
            }
            Text(" Enable API Versioning")
        }
    }
}
```

### 5.4 Code Preview & Download

```kotlin
// components/CodePreview.kt
@Composable
fun CodePreview(result: GenerateResponse) {
    Div({ classes("preview-section") }) {
        if (result.success) {
            H2 { Text("Generated Code") }

            result.files.forEach { file ->
                Div({ classes("code-file") }) {
                    H3 { Text(file.fileName) }

                    Pre {
                        Code({ classes("language-kotlin") }) {
                            Text(file.content)
                        }
                    }

                    Button({
                        onClick {
                            downloadFile(file.fileName, file.content)
                        }
                    }) {
                        Text("Download ${file.fileName}")
                    }
                }
            }

            // Download all as ZIP
            Button({
                onClick {
                    downloadAllAsZip(result.files)
                }
            }) {
                Text("Download All as ZIP")
            }

        } else {
            Div({ classes("error") }) {
                Text("Error: ${result.error}")
            }
        }
    }
}

fun downloadFile(fileName: String, content: String) {
    val blob = Blob(arrayOf(content), BlobPropertyBag("text/plain"))
    val url = URL.createObjectURL(blob)

    val a = document.createElement("a") as HTMLAnchorElement
    a.href = url
    a.download = fileName
    a.click()

    URL.revokeObjectURL(url)
}

fun downloadAllAsZip(files: List<GeneratedFile>) {
    // Implementation using JSZip library or simple concatenation
    // For MVP, could download files individually
}
```

### 5.5 API Client

```kotlin
// api/ApiClient.kt
class ApiClient(private val baseUrl: String = window.location.origin) {

    private val client = HttpClient {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
    }

    suspend fun generateCode(
        files: List<File>,
        config: GenerationConfig
    ): GenerateResponse {
        // Read files as Base64
        val fileDataList = files.map { file ->
            val content = readFileAsBase64(file)
            BpmnFileData(
                fileName = file.name,
                content = content
            )
        }

        val request = GenerateRequest(
            files = fileDataList,
            config = config
        )

        return client.post("$baseUrl/api/generate") {
            contentType(ContentType.Application.Json)
            body = request
        }
    }

    private suspend fun readFileAsBase64(file: File): String {
        return suspendCoroutine { continuation ->
            val reader = FileReader()
            reader.onload = {
                val result = reader.result as String
                val base64 = result.substringAfter("base64,")
                continuation.resume(base64)
            }
            reader.readAsDataURL(file)
        }
    }
}
```

## 6. Data Flow

### Complete User Journey

1. **User uploads BPMN file(s)**
   - Frontend: `FileUpload` component captures files
   - Files stored in browser memory (not sent yet)

2. **User configures generation options**
   - Frontend: `ConfigForm` component
   - Updates `GenerationConfig` state

3. **User clicks "Generate Code"**
   - Frontend: Reads files as Base64
   - Sends POST request to `/api/generate`

4. **Backend processes request**
   - Decodes Base64 BPMN content
   - Writes files to temp directory
   - Invokes `CreateProcessApiPlugin` (existing core)
   - Reads generated code files
   - Returns JSON response

5. **Frontend displays results**
   - `CodePreview` component shows generated code
   - Syntax highlighting applied
   - Download buttons for each file

6. **User downloads code**
   - Individual file downloads via blob URLs
   - Optional: ZIP download of all files

## 7. Implementation Phases

### Phase 1: Backend MVP (Week 1-2)
- [ ] Create `bpmn-to-code-web` module
- [ ] Set up Ktor application
- [ ] Implement `/api/generate` endpoint
- [ ] Implement `WebGenerationService` (adapter to core)
- [ ] Add request/response models
- [ ] Test with curl/Postman

### Phase 2: Frontend MVP (Week 2-3)
- [ ] Set up Kotlin/JS module
- [ ] Implement file upload component
- [ ] Implement configuration form
- [ ] Implement API client
- [ ] Basic code preview (plain text)
- [ ] Download functionality

### Phase 3: Polish & UX (Week 3-4)
- [ ] Add syntax highlighting (highlight.js)
- [ ] Improve error handling and validation
- [ ] Add loading states and progress indicators
- [ ] Implement ZIP download for multiple files
- [ ] Responsive design / mobile support
- [ ] Add examples and help text

### Phase 4: Deployment (Week 4)
- [ ] Create Dockerfile
- [ ] Set up CI/CD pipeline
- [ ] Deploy to hosting platform (Render/Railway/Fly.io)
- [ ] Configure custom domain (optional)
- [ ] Add analytics (optional)

### Phase 5: Documentation & Marketing (Week 5)
- [ ] Update README with web app link
- [ ] Create user guide / demo video
- [ ] Announce on social media / forums
- [ ] Add link to plugin READMEs

## 8. Deployment

### 8.1 Dockerfile

```dockerfile
FROM gradle:8-jdk21 AS build

WORKDIR /app
COPY . .

# Build both JVM and JS artifacts
RUN ./gradlew :bpmn-to-code-web:build

FROM openjdk:21-jre-slim

WORKDIR /app

# Copy JAR and static resources
COPY --from=build /app/bpmn-to-code-web/build/libs/*.jar app.jar
COPY --from=build /app/bpmn-to-code-web/build/distributions/* static/

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
```

### 8.2 Hosting Options

**Option 1: Render (Recommended for simplicity)**
- Free tier available
- Auto-deploy from GitHub
- Simple configuration
- Custom domains supported

**Option 2: Railway**
- Free tier with generous limits
- Great developer experience
- Built-in monitoring

**Option 3: Fly.io**
- Global edge deployment
- Free tier available
- More complex but powerful

**Option 4: Self-hosted**
- Docker Compose on VPS
- Full control
- Requires maintenance

### 8.3 Environment Configuration

```kotlin
// config/AppConfig.kt
data class AppConfig(
    val port: Int = System.getenv("PORT")?.toInt() ?: 8080,
    val maxFileSize: Long = 10 * 1024 * 1024, // 10MB
    val allowedOrigins: List<String> = System.getenv("ALLOWED_ORIGINS")
        ?.split(",") ?: listOf("*")
)
```

## 9. Security Considerations

### File Upload Limits
- Max file size: 10MB per BPMN file
- Max files per request: 10
- File type validation: Only `.bpmn` XML files

### Input Validation
- Validate package names (no malicious paths)
- Sanitize file names
- Validate BPMN XML structure

### Rate Limiting
- Implement per-IP rate limiting
- Prevent abuse / DoS attacks

### CORS Configuration
- Restrict allowed origins in production
- Only allow necessary headers

## 10. Future Enhancements

### Short-term
- [ ] Support for BPMN collaboration diagrams
- [ ] Batch processing UI improvements
- [ ] Code diff view (when regenerating)
- [ ] Save/load configuration presets

### Long-term
- [ ] User accounts (save BPMN files, history)
- [ ] Public gallery of example processes
- [ ] Integration with BPMN modeling tools (direct export)
- [ ] API versioning comparison tool
- [ ] Support for custom style guides (when feature is ready)

## 11. Success Metrics

- Number of unique visitors
- Number of BPMN files processed
- User retention (return visitors)
- Conversion to plugin users (downloads from Maven/Gradle repos)
- GitHub stars increase
- Community feedback and feature requests

## 12. Open Questions

1. **Frontend framework:** Kotlin/JS with React vs. Compose for Web vs. plain HTML/JS?
   - **Recommendation:** Start with kotlinx-html (simple), migrate to React later if needed

2. **Hosting provider:** Which platform for initial launch?
   - **Recommendation:** Render (easiest setup, free tier)

3. **Domain:** Purchase custom domain or use platform subdomain?
   - **Recommendation:** Start with platform subdomain, add custom domain later

4. **Analytics:** Which analytics platform (if any)?
   - **Recommendation:** Simple self-hosted analytics (Plausible) or Google Analytics

---

## Summary

This implementation provides a **pragmatic, working solution** that:
- ✅ Reuses 100% of existing `bpmn-to-code-core` logic
- ✅ Stays within Kotlin ecosystem (full-stack Kotlin)
- ✅ Follows existing hexagonal architecture
- ✅ Can be deployed quickly (1-2 months development time)
- ✅ Serves as effective marketing tool
- ✅ Lowers barrier to entry for new users

The web application becomes an **additional distribution channel** while keeping the plugins as the primary production use case.

---

## Implementation Summary

### ✅ What Was Built

**Phase 1: Backend MVP** - COMPLETE
- New `bpmn-to-code-web` Gradle module
- Ktor 3.0.3 server with full middleware stack
- REST API endpoint: `POST /api/generate`
- `WebGenerationService` - stateless adapter to `bpmn-to-code-core`
- Complete request/response models with Kotlinx Serialization
- Comprehensive unit tests (3 test cases, all passing)
- Health check endpoint

**Phase 2: Frontend MVP** - COMPLETE
- Clean, responsive HTML/CSS/JS interface (no build tools needed)
- File upload with drag & drop support
- Configuration form (package, language, engine, versioning)
- API client with Base64 encoding
- Code preview with syntax highlighting (highlight.js)
- Download functionality for generated files
- Error handling and loading states

**Additional:**
- Dockerfile for containerized deployment
- Comprehensive README with API docs
- Static file serving configured in Ktor

### How to Run

```bash
# From project root
./gradlew :bpmn-to-code-web:run

# Open browser
open http://localhost:8080
```

### How to Deploy

**Docker:**
```bash
cd bpmn-to-code-web
docker build -t bpmn-to-code-web .
docker run -p 8080:8080 bpmn-to-code-web
```

**Deployment Platforms:**
- Render.com: Connect GitHub repo, auto-deploy on push
- Railway: One-click deploy from Dockerfile
- Fly.io: `fly launch` from project directory

### Technical Highlights

1. **100% Code Reuse:** Zero duplication - all generation logic from `bpmn-to-code-core`
2. **Stateless Design:** No database, temp files cleaned up after each request
3. **Clean Architecture:** Follows existing hexagonal pattern perfectly
4. **Simple Stack:** Plain HTML/CSS/JS frontend - can upgrade to Kotlin/JS later if needed
5. **Production Ready:** Error handling, validation, logging, CORS, health checks

### Next Steps

- [ ] Deploy to hosting platform (Render/Railway/Fly.io)
- [ ] Add custom domain
- [ ] Implement ZIP download (currently downloads files individually)
- [ ] Add example BPMN files in UI
- [ ] Analytics (optional)
- [ ] Consider Kotlin/JS refactor for frontend (optional enhancement)

