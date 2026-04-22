# ADR 008: Web Module for Browser-Based Access

## Status
Accepted

## Context
bpmn-to-code was initially available only as Gradle and Maven build plugins. This approach works well for teams already using these build tools in Java/Kotlin projects, but creates barriers for:

1. **Quick experimentation**: Users who want to try bpmn-to-code without setting up a build project
2. **Non-technical users**: Business analysts or process modelers who want to generate code without command-line tools
3. **Wider adoption**: Potential users who discover the project but don't want to install build tools for evaluation

Additionally, having a hosted version would significantly lower the entry barrier and increase project visibility.

## Decision
Create **bpmn-to-code-web** as a new module providing browser-based access to bpmn-to-code functionality.

The web module will:
- Provide a graphical interface for uploading BPMN files and configuring generation options
- Generate Process API code using the same `bpmn-to-code-core` logic as the plugins
- Support both hosted deployment (public website) and self-hosted deployment (Docker container)
- Operate statelessly with in-memory processing (no database required)

### Deployment Strategy
1. **Hosted Version**: Deploy to public URL (e.g., bpmn-to-code.miragon.io) for immediate access
2. **Docker Image**: Publish to Docker Hub (`emaarco/bpmn-to-code-web`) for self-hosting

## Rationale

### Lower Entry Barrier
- No build tool installation required
- Access via browser with immediate feedback
- Ideal for quick prototypes and demonstrations

### Broader Reach
- Hosted version enables immediate trial without setup
- Increases project visibility through web presence

### Enterprise Self-Hosting
- Docker deployment keeps BPMN files within customer infrastructure
- Addresses data security and compliance concerns
- Enables integration with internal tools and workflows

### Consistent Core Logic
- 100% reuse of `bpmn-to-code-core` ensures identical output across all interfaces
- Same BPMN parsing, model merging, and code generation
- Bugs fixed once benefit all modules

## Consequences

### Positive
- **Accessibility**: Users can try bpmn-to-code immediately without installation
- **Adoption**: Hosted version lowers friction for new users discovering the project
- **Flexibility**: Three deployment options (hosted, self-hosted Docker, build plugins) fit different use cases
- **Visibility**: Web presence increases project awareness and community growth
- 
### Negative
- **Maintenance overhead**: Additional module requires ongoing maintenance and bug fixes
- **Infrastructure costs**: Hosted version requires server hosting and monitoring
- **Support burden**: More user touchpoints means more support requests
- **Security considerations**: Web interface must validate inputs and prevent abuse

### Neutral
- **Scope expansion**: Project evolves from "build plugins" to "code generation tool with multiple interfaces"
- **Documentation split**: Need separate docs for web, Gradle, and Maven users

## Implementation
- Module: `bpmn-to-code-web`
- Core dependency: Reuses `bpmn-to-code-core` via `CreateProcessApiInMemoryPlugin`
- Backend: See ADR 009 for technology choice
- Deployment: Docker image built via Gradle task, pushed to Docker Hub
- Hosting: Deployed to `bpmn-to-code.miragon.io` (work in progress)

## Related ADRs
- ADR 009: Ktor with Static Frontend (Single Module) - Technical implementation decision

