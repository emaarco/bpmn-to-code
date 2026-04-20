# README Template

Write verbatim to `README.md` in the project root. Substitute all `{{placeholder}}` markers.

- `{{artifactId}}` — Maven/Gradle artifact ID (= project folder name)
- `{{processId}}` — value of `PROCESS_ID` constant from the generated ProcessApi
- `{{engine}}` — `CAMUNDA_7`, `ZEEBE`, or `OPERATON`
- `{{approach}}` — `process-engine-api` or `plain JavaDelegate`
- `{{ProcessApiClass}}` — generated class/object name (e.g. `NewsletterSubscriptionProcessApi`)
- `{{bpmnFileName}}` — filename of the BPMN file (e.g. `newsletter-subscription.bpmn`)
- `{{packageName}}` — base package (e.g. `com.example.newsletter`)
- `{{enginePackage}}` — inbound adapter sub-package: `camunda`, `zeebe`, or `delegate`
- `{{taskTypesTable}}` — Markdown table rows, one per TaskType constant (generated at runtime)
- `{{gradleRunLocal}}` / `{{mavenRunLocal}}` — run commands for local profile
- `{{gradleRun}}` / `{{mavenRun}}` — run commands for full stack
- `{{gradleCodegen}}` / `{{mavenCodegen}}` — codegen commands

Include the plain-delegate warning block only when `approach=plain JavaDelegate`.
Include the Zeebe UI line only when `engine=ZEEBE`.
Include the quick-start block only when `engine` is `CAMUNDA_7` or `OPERATON`.

---

```markdown
# {{artifactId}}

Spring Boot service implementing the `{{processId}}` process.

## Architecture

This project follows **hexagonal architecture** (ports and adapters).

```
adapter/inbound/{{enginePackage}}/   ← workers / delegates  (engine adapter)
application/port/inbound/            ← use case interfaces
application/port/outbound/           ← process port interface  (process-engine-api only)
application/service/                 ← service stubs with business logic
adapter/outbound/                    ← process port adapter   (process-engine-api only)
domain/                              ← domain types
```

| | |
|---|---|
| **Engine** | {{engine}} |
| **Approach** | {{approach}} |
| **Generated API** | `{{ProcessApiClass}}` — produced by bpmn-to-code from `{{bpmnFileName}}` |

## Getting Started

<!-- Include this block only for CAMUNDA_7 and OPERATON -->
### Quick start — no Docker needed (H2 in-memory)

```bash
# Gradle
SPRING_PROFILES_ACTIVE=local {{gradleRunLocal}}

# Maven
{{mavenRunLocal}}
```
<!-- End embedded-only block -->

### Full stack — PostgreSQL via Docker

```bash
cd stack && docker compose up -d
# Gradle
{{gradleRun}}
# Maven
{{mavenRun}}
```

<!-- Include this line only for ZEEBE -->
Zeebe UI: http://localhost:8080  (demo / demo)
<!-- End Zeebe block -->

## Process

BPMN source: `src/main/resources/bpmn/{{bpmnFileName}}`

| API constant | Task type value |
|---|---|
{{taskTypesTable}}

## Regenerating the ProcessApi

Run after modifying `{{bpmnFileName}}`:

```bash
# Gradle
{{gradleCodegen}}

# Maven
{{mavenCodegen}}
```

<!-- Include this block only for plain JavaDelegate approach -->
## Delegate wiring

Each delegate class must be referenced in the BPMN service task via
`camunda:class` / `operaton:class` pointing to its fully-qualified class name, e.g.:

```
{{packageName}}.adapter.inbound.delegate.SendConfirmationMailDelegate
```

Update the BPMN file or process modeller accordingly after renaming or moving delegates.
<!-- End plain delegate block -->
```
