---
title: "ADR 009: Ktor with Static Frontend (Single Module)"
description: Technology choices for the bpmn-to-code web module.
---

## Status
Accepted

## Context
After deciding to build a web module (see ADR 008), we need to choose the technical architecture. Key considerations:

1. **Web Framework**: Need a JVM-based framework to leverage existing Kotlin codebase and `bpmn-to-code-core`
2. **Frontend Approach**: Decide between static files, server-side rendering, or separate SPA framework
3. **Module Structure**: Single module vs. separate backend/frontend modules
4. **Deployment Simplicity**: Must package easily into Docker container

## Decision
Use **Ktor** as the web framework with **static HTML/CSS/JavaScript** served from a **single module** (`bpmn-to-code-web`).

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

### Why Static Frontend?
1. **Simplicity**: No frontend build pipeline (no npm, webpack, babel)
2. **Single JAR**: Frontend files bundled in resources, no separate deployment
3. **Docker-Friendly**: One module = one JAR = one container layer

### Why Single Module?
1. **Deployment Simplicity**: Single JAR contains both backend and frontend
2. **Version Coherence**: Backend and frontend always versioned together
3. **Reduced Complexity**: No need for CORS, separate deployments, or version coordination

## Consequences

### Positive
- **Fast Build**: No frontend compilation, just copy static files to resources
- **Simple Docker**: Single JAR in container, one process, one port
- **Quick Setup**: `./gradlew :bpmn-to-code-web:run` starts complete application
- **Maintainable**: Vanilla JS is accessible to any developer, no framework lock-in

### Negative
- **Limited Interactivity**: Static JS harder to scale than React/Vue for complex UIs
- **No Type Safety in Frontend**: Unlike Kotlin/JS, vanilla JS lacks compile-time checks
- **No Hot Reload**: Frontend changes require rebuild (but restart is fast)

## Alternatives Considered

### Spring Boot + Thymeleaf (Rejected)
- Much heavier than Ktor (slower startup, larger JAR)
- Overcomplicated for stateless API + static files

### Ktor + Kotlin/JS React (Rejected)
- Requires frontend compilation step
- Larger bundle size
- Overkill for simple upload/download UI

### Separate React SPA Module (Rejected)
- Requires separate module and build pipeline
- Need two Docker containers or reverse proxy
- CORS configuration needed
- Deployment complexity

## Related ADRs
- [ADR 008: Web Module for Browser-Based Access](008-web-module-for-browser-access) — Strategic decision to build web module
