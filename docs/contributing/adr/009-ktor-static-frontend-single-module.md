# ADR 009: Ktor with Static Frontend (Single Module)

## Status
Accepted

## Context
After deciding to build a web module (see ADR 008), we need to choose the technical architecture. Key considerations:

1. **Web Framework**: Need a JVM-based framework to leverage existing Kotlin codebase and `bpmn-to-code-core`
2. **Frontend Approach**: Decide between static files, server-side rendering, or separate SPA framework
3. **Module Structure**: Single module vs. separate backend/frontend modules
4. **Deployment Simplicity**: Must package easily into Docker container

### Framework Options
- **Ktor**: Lightweight Kotlin framework with native coroutines and static file serving
- **Spring Boot**: Popular but heavier, more opinionated
- **Javalin**: Lightweight Java framework, less Kotlin-idiomatic

### Frontend Options
- **Static HTML/CSS/JS**: Simple, requires no build tooling
- **Kotlin/JS + React**: Type-safe but adds build complexity
- **Separate React/Vue app**: Modern but requires separate module and build pipeline

## Decision
Use **Ktor** as the web framework with **static HTML/CSS/JavaScript** served from a **single module** (`bpmn-to-code-web`).

### Architecture
```
bpmn-to-code-web/
├── src/main/kotlin/          # Backend (Ktor application)
│   ├── Application.kt         # Server setup
│   ├── routes/                # REST endpoints
│   ├── service/               # Integration with bpmn-to-code-core
│   └── model/                 # Request/response DTOs
└── src/main/resources/
    └── static/                # Frontend (HTML/CSS/JS)
        ├── index.html
        ├── css/styles.css
        └── js/app.js
```

### Technology Stack
- **Backend**: Ktor 3.x (Kotlin web framework)
- **Frontend**: Vanilla JavaScript with HTML/CSS
- **API**: RESTful JSON endpoints
- **Serialization**: kotlinx.serialization
- **Packaging**: Single executable JAR via Ktor plugin

## Rationale

### Why Ktor?
1. **Kotlin-Native**: Written in Kotlin, leverages coroutines, feels natural with existing codebase
2. **Lightweight**: Minimal dependencies, fast startup, small footprint for Docker
3. **Static File Support**: Built-in `static {}` resource serving without additional configuration
4. **Modern**: Async by default, uses Kotlin DSL for routing
5. **Ecosystem Fit**: Already using Kotlin for core logic, plugins, and build scripts

### Why Static Frontend?
1. **Simplicity**: No frontend build pipeline (no npm, webpack, babel)
2. **Single JAR**: Frontend files bundled in resources, no separate deployment
3. **Fast Development**: Edit HTML/CSS/JS and refresh browser, no compilation
4. **Docker-Friendly**: One module = one JAR = one container layer

### Why Single Module?
1. **Deployment Simplicity**: Single JAR contains both backend and frontend
2. **Docker Efficiency**: One `buildFatJar` task produces complete application
3. **Version Coherence**: Backend and frontend always versioned together
4. **Reduced Complexity**: No need for CORS, separate deployments, or version coordination

## Consequences

### Positive
- **Fast Build**: No frontend compilation, just copy static files to resources
- **Simple Docker**: Single JAR in container, one process, one port
- **Quick Setup**: `./gradlew :bpmn-to-code-web:run` starts complete application
- **Low Overhead**: Ktor is lightweight, suitable for small Docker images
- **Maintainable**: Vanilla JS is accessible to any developer, no framework lock-in

### Negative
- **Limited Interactivity**: Static JS harder to scale than React/Vue for complex UIs
- **No Type Safety in Frontend**: Unlike Kotlin/JS, vanilla JS lacks compile-time checks
- **Manual DOM Manipulation**: More verbose than declarative frameworks
- **No Hot Reload**: Frontend changes require rebuild (but restart is fast)

### Trade-offs
- **Simplicity vs. Features**: Chose simplicity since use case is straightforward (upload BPMN, download code)
- **Speed vs. Scalability**: Fast development and deployment over large-scale frontend architecture
- **Accessibility vs. Sophistication**: Vanilla JS is beginner-friendly but less powerful than frameworks

## Alternatives Considered

### Spring Boot + Thymeleaf (Rejected)
- **Pros**: Mature ecosystem, server-side rendering
- **Cons**:
  - Much heavier than Ktor (slower startup, larger JAR)
  - Thymeleaf is verbose for simple UI
  - Overcomplicated for stateless API + static files

### Ktor + Kotlin/JS React (Rejected)
- **Pros**: Full type safety across stack, shared Kotlin code
- **Cons**:
  - Requires frontend compilation step
  - Increases build complexity
  - Larger bundle size
  - Overkill for simple upload/download UI

### Separate React SPA Module (Rejected)
- **Pros**: Modern frontend architecture, rich ecosystem
- **Cons**:
  - Requires separate module and build pipeline
  - Need two Docker containers or reverse proxy
  - CORS configuration needed
  - Version coordination between backend/frontend
  - Deployment complexity (two artifacts to manage)

## Implementation Details

### Ktor Configuration
- **Plugins**: ContentNegotiation (JSON), CORS, CallLogging, StatusPages
- **Serialization**: kotlinx.serialization for request/response models
- **Static Serving**: `static("/") { resources("static") }` serves frontend from classpath

### Frontend Architecture
- **Single-page layout**: `index.html` with embedded sections
- **Fetch API**: Calls `/api/generate` with Base64-encoded BPMN
- **Syntax Highlighting**: Lightweight JS library (e.g., Prism.js)
- **File Upload**: Drag-and-drop + file picker with validation

### Packaging
- **Development**: `./gradlew :bpmn-to-code-web:run` (runs Netty embedded server)
- **Production**: `./gradlew :bpmn-to-code-web:buildFatJar` creates single executable JAR
- **Docker**: JAR copied into minimal JRE image

## Future Considerations
If the UI grows significantly complex:
- Migrate to Kotlin/JS with React for type safety
- Split into separate frontend module with its own build
- Current architecture allows gradual migration without breaking changes

## Related ADRs
- ADR 008: Web Module for Browser-Based Access—Strategic decision to build web module
