# Gradle Build Configuration Templates

Use this file when `buildTool=GRADLE`. Read the relevant section for the chosen engine and
approach, substitute `{{placeholders}}`, and apply to `build.gradle.kts`.

After applying changes, also patch the `plugins {}` block:
- Set `kotlin("jvm")` and `kotlin("plugin.spring")` to `{{kotlinVersion}}` (from `resources/compat.md`)
- Set the Java toolchain `languageVersion` to `{{javaVersion}}` (detected from host)

---

## 1. Imports (prepend after any existing imports at top of file)

```kotlin
import io.github.emaarco.bpmn.adapter.GenerateBpmnModelsTask
import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
```

---

## 2. Plugin (add inside `plugins {}` block)

Look up the latest version at https://plugins.gradle.org/plugin/io.github.emaarco.bpmn-to-code-gradle

```kotlin
id("io.github.emaarco.bpmn-to-code-gradle") version "{{bpmnToCodeVersion}}"
```

---

## 3. Dependencies (add inside `dependencies {}` block)

### CAMUNDA_7 — process-engine-api (default)

Min Kotlin version: **2.2.0** (from BOM `process-engine-adapter-camunda-platform-c7-bom:2026.02.1`)

```kotlin
// process-engine-api: engine-neutral abstraction layer
implementation(platform("dev.bpm-crafters.process-engine-adapters:process-engine-adapter-camunda-platform-c7-bom:2026.02.1"))
implementation("dev.bpm-crafters.process-engine-adapters:process-engine-adapter-camunda-platform-c7-embedded-spring-boot-starter")
implementation("dev.bpm-crafters.process-engine-api:process-engine-api:1.5")
implementation("dev.bpm-crafters.process-engine-worker:process-engine-worker-spring-boot-starter:0.8.0")
// Camunda 7 engine
implementation("org.camunda.bpm.springboot:camunda-bpm-spring-boot-starter-webapp:7.24.0")
// Database drivers
runtimeOnly("com.h2database:h2")
runtimeOnly("org.postgresql:postgresql")
```

### CAMUNDA_7 — plain JavaDelegate (user opted out of process-engine-api)

Min Kotlin version: **1.9.25** (Initializr default is fine)

```kotlin
implementation("org.camunda.bpm.springboot:camunda-bpm-spring-boot-starter-webapp:7.24.0")
runtimeOnly("com.h2database:h2")
runtimeOnly("org.postgresql:postgresql")
```

### ZEEBE — process-engine-api (default)

Min Kotlin version: **2.2.0** (from BOM `process-engine-adapter-camunda-platform-c8-bom:2026.02.1`)

```kotlin
// process-engine-api: engine-neutral abstraction layer
implementation(platform("dev.bpm-crafters.process-engine-adapters:process-engine-adapter-camunda-platform-c8-bom:2026.02.1"))
implementation("dev.bpm-crafters.process-engine-adapters:process-engine-adapter-camunda-platform-c8-spring-boot-starter")
implementation("dev.bpm-crafters.process-engine-api:process-engine-api:1.5")
implementation("dev.bpm-crafters.process-engine-worker:process-engine-worker-spring-boot-starter:0.8.0")
```

### OPERATON — plain JavaDelegate (no process-engine-api adapter available)

Min Kotlin version: **1.9.25** (Initializr default is fine)

```kotlin
implementation("org.operaton.bpm.springboot:operaton-bpm-spring-boot-starter-webapp:2.0.0")
runtimeOnly("com.h2database:h2")
runtimeOnly("org.postgresql:postgresql")
```

---

## 4. Code generation task (add after `dependencies {}` block)

The plugin registers `generateBpmnModelApi` automatically on apply — use `tasks.named` to
configure it, NOT `tasks.register` (which would throw "task already exists").

Replace `{{engine}}` with `CAMUNDA_7`, `ZEEBE`, or `OPERATON`.
Replace `{{packageName}}` with the chosen base package.
For Java projects change `kotlin` to `java` in `outputFolderPath` and `OutputLanguage.KOTLIN` to `OutputLanguage.JAVA`.

```kotlin
tasks.named<GenerateBpmnModelsTask>("generateBpmnModelApi") {
    baseDir = projectDir.toString()
    filePattern = "src/main/resources/bpmn/**/*.bpmn"
    outputFolderPath = "$projectDir/src/main/kotlin"
    packagePath = "{{packageName}}.api"
    outputLanguage = OutputLanguage.KOTLIN
    processEngine = ProcessEngine.{{engine}}
    useVersioning = false
}

tasks.named("classes") {
    dependsOn("generateBpmnModelApi")
}
```

---

## 5. Codegen command

```bash
./gradlew generateBpmnModelApi
```

## 6. Build command

```bash
# CAMUNDA_7 / OPERATON — full build including tests
./gradlew build

# ZEEBE — skip tests (contextLoads() requires a running Zeebe broker)
./gradlew build -x test
```

## 7. Run commands

```bash
# Local profile (H2, no Docker — embedded engines only)
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun

# Full stack (PostgreSQL via Docker)
./gradlew bootRun
```
