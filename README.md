> **bpmn-to-code is early-stage and actively developing.**
> The four pillars — Generate, Validate, Surface, Ship — are taking shape, but expect rough edges.
> Feedback and contributions are very welcome.

[![Documentation](https://img.shields.io/badge/docs-bpmn--to--code-blue?style=flat-square)](https://emaarco.github.io/bpmn-to-code/)
[![Web App](https://img.shields.io/badge/web--app-try%20in%20browser-brightgreen?style=flat-square)](https://bpmn-to-code.miragon.io/static/index.html)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.emaarco/bpmn-to-code-maven?style=flat-square&label=maven)](https://central.sonatype.com/artifact/io.github.emaarco/bpmn-to-code-maven)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/io.github.emaarco.bpmn-to-code-gradle?style=flat-square&label=gradle)](https://plugins.gradle.org/plugin/io.github.emaarco.bpmn-to-code-gradle)

# bpmn-to-code

Type-safe constants from your BPMN model — for your compiler, your tests, and your AI agents.

**Generate · Validate · Surface · Ship** — a type-safe BPMN toolkit for JVM projects.

## What It Does

### Generate — Type-Safe APIs

bpmn-to-code reads your BPMN files and generates typed constants from them. Every element ID, message name, and service task type becomes a compiled constant. Rename a task in the modeler → compiler error. No more silent runtime failures from hardcoded strings.

```kotlin
// Before
@JobWorker(type = "newsletter.sendConfirmationMail")  // copied from modeler, no safety net
fun send() { ... }

// After — generated from the BPMN model
@JobWorker(type = NewsletterSubscriptionProcessApi.TaskTypes.NEWSLETTER_SEND_CONFIRMATION_MAIL)
fun send() { ... }
```

### Validate — Architecture Rules for BPMN _(beta)_

Like ArchUnit for Java, `bpmn-to-code-testing` lets you write architecture tests for your BPMN models. The standalone `validateBpmnModels` Gradle task and `validate-bpmn` Maven goal run the same checks in CI.

```kotlin
BpmnValidator
    .fromClasspath("bpmn/")
    .engine(ProcessEngine.ZEEBE)
    .validate()
    .assertNoViolations()
```

11 built-in rules cover missing implementations, undefined timers, empty processes, naming violations, and variable collisions. Add custom rules by implementing `BpmnValidationRule`.

### Surface — Process Structure in Code _(beta)_

Generates a structured JSON alongside the API. Your process is readable by AI agents, code reviewers, and CI — without opening Camunda Modeler.

```json
{
  "processId": "newsletterSubscription",
  "flowNodes": [
    { "id": "StartEvent_SubmitRegistrationForm", "displayName": "Submit newsletter form", "elementType": "START_EVENT" },
    { "id": "Activity_SendConfirmationMail", "displayName": "Send confirmation mail", "elementType": "SERVICE_TASK" }
  ]
}
```

BPMN files are XML — technically readable, but full of visual layout data, namespace declarations, and rendering hints that make them noisy for AI tools. The generated JSON strips all of that away:

- **Smaller** — no diagram coordinates, waypoints, or SVG-style metadata
- **Focused** — only the elements and relationships that matter for logic and implementation
- **Structured** — flow nodes, sequence flows, messages, and errors in predictable, typed fields

The result is a compact, deterministic representation that AI agents can reason about accurately — with no hallucinated element IDs, because the JSON is derived directly from the BPMN model by rule.

### Ship — Agent Skills _(beta)_

Drop-in agent skills that automate the entire setup workflow. Integrate the plugin into your project in one prompt, scaffold a complete process service from a BPMN file, and migrate hardcoded strings to the generated API — without touching any config manually.

```bash
/plugin marketplace add emaarco/bpmn-to-code
/plugin install bpmn-to-code@bpmn-to-code
```

Works with Claude Code out of the box.

## Gradle Setup

```kotlin
plugins {
    id("io.github.emaarco.bpmn-to-code-gradle") version "2.0.0"
}

tasks.named("generateBpmnModelApi", GenerateBpmnModelsTask::class) {
    baseDir = projectDir.toString()
    filePattern = "src/main/resources/**/*.bpmn"
    outputFolderPath = "$projectDir/src/main/kotlin"
    packagePath = "com.example.process"
    outputLanguage = OutputLanguage.KOTLIN
    processEngine = ProcessEngine.ZEEBE
}
```

## Maven Setup

```xml
<plugin>
    <groupId>io.github.emaarco</groupId>
    <artifactId>bpmn-to-code-maven</artifactId>
    <version>2.0.0</version>
    <executions>
        <execution>
            <goals><goal>generate-bpmn-api</goal></goals>
        </execution>
    </executions>
    <configuration>
        <baseDir>${project.basedir}</baseDir>
        <filePattern>src/main/resources/*.bpmn</filePattern>
        <outputFolderPath>${project.basedir}/src/main/java</outputFolderPath>
        <packagePath>com.example.process</packagePath>
        <outputLanguage>KOTLIN</outputLanguage>
        <processEngine>ZEEBE</processEngine>
    </configuration>
</plugin>
```

## Testing Module

```kotlin
dependencies {
    testImplementation("io.github.emaarco:bpmn-to-code-testing:2.0.0")
}
```

## Supported Engines

| Engine | Value |
|--------|-------|
| Camunda 8 / Zeebe | `ZEEBE` |
| Camunda 7 | `CAMUNDA_7` |
| Operaton | `OPERATON` |

## Get It

- 📦 [Maven Central](https://central.sonatype.com/artifact/io.github.emaarco/bpmn-to-code-maven) — Maven Plugin
- 📦 [Gradle Plugin Portal](https://plugins.gradle.org/plugin/io.github.emaarco.bpmn-to-code-gradle) — Gradle Plugin
- 🌐 [Web App](https://bpmn-to-code.miragon.io/static/index.html) — Try in browser, no installation
- 🐳 [Docker Hub](https://hub.docker.com/r/emaarco/bpmn-to-code-web) — Self-hostable container
- 🤖 [MCP Server](bpmn-to-code-mcp/README.md) — AI-assisted generation inside your editor

## Project Structure

- **bpmn-to-code-core** — core parsing and generation logic
- **bpmn-to-code-gradle** — Gradle plugin
- **bpmn-to-code-maven** — Maven plugin
- **bpmn-to-code-testing** — BPMN architecture testing library
- **bpmn-to-code-web** — browser-based web app
- **bpmn-to-code-mcp** — MCP server for AI-assisted generation

## Contributing

Community contributions are welcome. Submit issues, open pull requests, or start a discussion on [GitHub](https://github.com/emaarco/bpmn-to-code).
