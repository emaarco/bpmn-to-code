---
name: scaffold-process-project
argument-hint: "<path/to/process.bpmn> [--output-dir <dir>] [--group-id <id>] [--artifact-id <id>] [--build-tool gradle|maven]"
description: "Scaffold a complete Spring Boot service project from a BPMN file with hexagonal architecture. Bootstraps via Spring Initializr, configures bpmn-to-code, and generates workers, use cases, and services wired to ProcessApi constants — no raw strings anywhere. Use when the user wants to start a new process service from a BPMN model."
allowed-tools: Read, Write, Edit, Glob, Bash(curl *), Bash(unzip *), Bash(mkdir *), Bash(cp *), Bash(java *), Bash(./gradlew *), Bash(mvn *), Bash(./mvnw *), WebFetch, AskUserQuestion
---

# Skill: scaffold-process-project

Bootstrap a complete, compilable Spring Boot service project from a `.bpmn` file following
hexagonal architecture. Every layer is generated — workers (or delegates), use case interfaces,
service stubs, and outbound ports — all referencing bpmn-to-code `ProcessApi` constants.
No raw BPMN strings anywhere in the generated code.

Templates and configuration snippets for each engine, build tool, and approach live in the
`resources/` directory alongside this skill. Read those files on demand; do not rely on memory.

Reference examples:
- **engine-safari** (`emaarco/engine-safari`): plain delegate pattern (`service/camunda-7`),
  process-engine-api worker pattern (`service/camunda-7-with-process-engine-api`)
- **easy-zeebe** (`emaarco/easy-zeebe`): full Zeebe Docker Compose
- **Miragon/cibseven-developer-training-exercises**: `application-local.yaml` H2 pattern

## IMPORTANT

- Never apply file changes without showing a diff summary first and receiving confirmation.
- The BPMN file is read-only input — never modify it.
- Never modify files outside the new project directory once created.
- If codegen fails, show the error and stop — do not scaffold from a partial ProcessApi.
- Generated files must compile immediately. Resolve all imports before declaring done.
- Use `AskUserQuestion` whenever a required value is missing or a decision needs user input.

## Engine × Approach Matrix

| Engine    | process-engine-api adapter? | Default approach   | Worker type            |
|-----------|-----------------------------|--------------------|------------------------|
| CAMUNDA_7 | ✅ c7-embedded               | process-engine-api | `@ProcessEngineWorker` |
| ZEEBE     | ✅ c8                        | process-engine-api | `@ProcessEngineWorker` |
| OPERATON  | ❌ none available            | plain JavaDelegate | `BaseDelegate`         |

For CAMUNDA_7/ZEEBE: offer process-engine-api as default, allow opt-out.
For OPERATON: always use plain — explain why in the summary.

---

## Instructions

### Step 1 – Collect inputs & detect environment

Parse `$ARGUMENTS`. For missing required values use `AskUserQuestion`.

| Parameter           | Required | Default                                                  |
|---------------------|----------|----------------------------------------------------------|
| `bpmnFile`          | yes      | —                                                        |
| `outputDir`         | no       | `./`                                                     |
| `groupId`           | yes      | —                                                        |
| `artifactId`        | yes      | —                                                        |
| `packageName`       | no       | `${groupId}.${artifactId}` (hyphens → dots)              |
| `buildTool`         | no       | `GRADLE`                                                 |
| `engine`            | no       | auto-detect (see below)                                  |
| `useProcessEngineApi` | no     | `true` for CAMUNDA_7/ZEEBE, `false` for OPERATON         |
| `outputLanguage`    | no       | `KOTLIN`                                                 |

**Engine auto-detection:** Read the BPMN file and scan the XML:

- `xmlns:zeebe` or `<zeebe:taskDefinition` → `ZEEBE`
- `camunda:class=`, `camunda:expression=`, or `camunda:type=` (no zeebe namespace) → `CAMUNDA_7`
- `xmlns:operaton` or `operaton:class=` → `OPERATON`
- None matched → ask the user

**Java version detection:**

```bash
java -version 2>&1 | head -1
```

Extract the major version (e.g. `21`). Check `resources/compat.md` for the engine's minimum
Java requirement. If no Java is found, or the detected version is below the engine minimum,
use `AskUserQuestion` to let the user confirm or specify the Java version to target.

**Spring Boot version:**

Fetch the current default from Spring Initializr:

```bash
curl -s "https://start.spring.io/actuator/info" \
  | grep -o '"default":"[^"]*"' | head -1 | grep -o '[0-9][^"]*'
```

Use the returned value as `bootVersion`. Check `resources/compat.md` for engine-specific
Spring Boot minimums and warn if the fetched version is below them.
If the fetch fails, use `AskUserQuestion` to ask the user for the Spring Boot version.

**Kotlin version (GRADLE + KOTLIN only):**

Read `resources/compat.md` for the minimum Kotlin plugin version required by the chosen
engine BOM. Record this as `kotlinVersion` — you will patch it into the build file in Step 3.
Do NOT use the Kotlin version generated by Initializr as-is; always check compatibility.

Show all detected/defaulted values and ask for confirmation before proceeding.

---

### Step 2 – Bootstrap via Spring Initializr

```bash
curl -G "https://start.spring.io/starter.zip" \
  -d type={{gradle-project-kotlin|maven-project}} \
  -d language={{kotlin|java}} \
  -d bootVersion={{bootVersion}} \
  -d groupId={{groupId}} \
  -d artifactId={{artifactId}} \
  -d name={{artifactId}} \
  -d packageName={{packageName}} \
  -d dependencies=web,actuator \
  -o /tmp/{{artifactId}}-scaffold.zip

unzip /tmp/{{artifactId}}-scaffold.zip -d "{{outputDir}}/{{artifactId}}"
```

- `type`: `gradle-project-kotlin` for GRADLE (Kotlin DSL), `maven-project` for MAVEN
- `language`: `kotlin` for KOTLIN, `java` for JAVA

All subsequent operations target `{{outputDir}}/{{artifactId}}`.

---

### Step 3 – Patch the build file

Read the relevant resource file for the chosen build tool:

- **Gradle** → `resources/buildtool/gradle.md`
- **Maven** → `resources/buildtool/maven.md`

From that file, take the sections matching the chosen engine and approach. Look up the
latest bpmn-to-code plugin/artifact version via WebFetch if needed.

**For Gradle + Kotlin:** also patch the Kotlin version in the `plugins {}` block to
`{{kotlinVersion}}` (both the `kotlin("jvm")` and `kotlin("plugin.spring")` lines).

**For Gradle:** also patch the Java toolchain `languageVersion` to the detected `{{javaVersion}}`.

Show a diff-style summary of all planned changes and ask:
*"Apply these changes to `build.gradle.kts` / `pom.xml`? (yes / edit / cancel)"*

Apply only after confirmation.

---

### Step 4 – Generate config files

**Docker Compose + .env**

Read the relevant stack resource:
- CAMUNDA_7 or OPERATON → `resources/stack/compose-embedded.md`
- ZEEBE → `resources/stack/compose-zeebe.md`

Write `stack/compose.yaml` and `stack/.env` verbatim, substituting `{{artifactId}}`.

**Spring application config**

Read the relevant config resource:
- CAMUNDA_7 → `resources/config/application-camunda7.md`
- OPERATON → `resources/config/application-operaton.md`
- ZEEBE → `resources/config/application-zeebe.md`

Write `src/main/resources/application.yml` and `src/main/resources/application-local.yml`
verbatim, substituting `{{artifactId}}`.

No confirmation prompt needed for config files — they are straightforward and reversible.

---

### Step 5 – Place BPMN file and run code generation

```bash
mkdir -p "{{outputDir}}/{{artifactId}}/src/main/resources/bpmn"
cp "{{bpmnFile}}" "{{outputDir}}/{{artifactId}}/src/main/resources/bpmn/"
```

Run codegen from inside the project directory:

- **Gradle:** `./gradlew generateBpmnModelApi`
- **Maven:** `mvn io.github.emaarco:bpmn-to-code-maven:generate-bpmn-api`

If codegen fails, show the full error and stop. Do not proceed until it succeeds.

Once complete, locate and read the generated `*ProcessApi.kt` (or `.java`) file under
`src/main/kotlin/{{packagePath}}/api/` (or `src/main/java/...`). Extract:

- **ProcessApi class/object name** (e.g. `NewsletterSubscriptionProcessApi`)
- **`PROCESS_ID`** constant value → derive `{{processId}}` and `{{ProcessName}}`
- **`PROCESS_ENGINE`** constant → confirm it matches the selected engine
- **All `TaskTypes` entries** → drive worker/delegate generation (name + value)
- **`Variables` structure** — Variables contains **only** per-element nested objects; there are
  no top-level flat constants. Each nested object is named in PascalCase from the BPMN element ID
  and holds the variables scoped to that specific flow node:

  ```kotlin
  object Variables {
      object ActivitySendWelcomeMail {
          const val SUBSCRIPTION_ID: String = "subscriptionId"
      }
      object ActivitySendConfirmationMail {
          const val SUBSCRIPTION_ID: String = "subscriptionId"
          const val TEST_VARIABLE: String = "testVariable"
      }
  }
  ```

  For each TaskType, find the matching service task element name (from `Elements`) and look up
  `Variables.{{ElementName}}` to determine which variables to inject into that worker/delegate.

---

### Step 6 – Scaffold hexagonal structure

Read `resources/templates/code.md`. Use the section matching the chosen approach:

- **Section A** — process-engine-api path (CAMUNDA_7 or ZEEBE with process-engine-api)
- **Section B** — plain delegate path (OPERATON, or CAMUNDA_7/ZEEBE with plain opt-out)

For each TaskType constant, derive names and generate the files described in the template.
Create the `domain/.gitkeep` placeholder regardless of approach.

**Variable injection (per element):** When generating each worker/delegate, look up the
per-element Variables sub-object for that service task (e.g. `Variables.ActivitySendWelcomeMail`).
Use those constants — not the top-level flat list — to generate `@Variable` parameters or
`execution.getVariable(...)` calls. If no per-element sub-object exists, leave the method
parameterless.

---

### Step 7 – Generate README

Read `resources/templates/readme.md`. Substitute all `{{placeholder}}` markers with the
actual values collected and derived during the preceding steps.

For `{{taskTypesTable}}`, generate one Markdown table row per TaskType:

```
| `{{ProcessApiClass}}.TaskTypes.{{TASK_CONST}}` | `{{taskTypeValue}}` |
```

Include or omit conditional blocks (`<!-- Include ... -->`) according to engine and approach.

Write the result to `README.md` in the project root.

---

### Step 8 – Verify compilation

Run from the project directory:

- **Gradle (CAMUNDA_7 / OPERATON):** `./gradlew build`
- **Gradle (ZEEBE):** `./gradlew build -x test`
  *(ZEEBE requires a running broker for Spring context tests — skip tests during scaffolding)*
- **Maven (CAMUNDA_7 / OPERATON):** `mvn verify`
- **Maven (ZEEBE):** `mvn verify -DskipTests`

If the build fails: show the full error, diagnose the most likely cause, attempt a fix,
and re-run. Use `AskUserQuestion` if the fix requires information you don't have.
Do not mark the task complete until the build passes.

---

### Step 9 – Summary

Report:

```
✅ Project scaffolded: {{outputDir}}/{{artifactId}}/

Build tool:   {{buildTool}}
Engine:       {{engine}}
Approach:     {{approach}}
Java:         {{javaVersion}}
Spring Boot:  {{bootVersion}}
Tasks:        {{count}} workers/delegates generated
              {{list of TASK_CONST names}}

── Quick start (embedded engines — H2, no Docker) ─────────────────────────
  [Gradle] SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
  [Maven]  mvn spring-boot:run -Dspring-boot.run.profiles=local

── Full stack (Docker) ────────────────────────────────────────────────────
  cd {{outputDir}}/{{artifactId}}/stack && docker compose up -d
  [Gradle] ./gradlew bootRun
  [Maven]  mvn spring-boot:run
```

For ZEEBE, replace the quick-start block with:
```
  ⚠️  Zeebe requires Docker — no H2 shortcut available.
  cd stack && docker compose up -d   # then bootRun / spring-boot:run
  Zeebe UI: http://localhost:8080  (demo / demo)
  ⚠️  contextLoads() test skipped (needs running Zeebe). Run ./gradlew test after docker compose up -d.
```

For OPERATON plain delegates, append:
```
  ⚠️  Update BPMN service tasks: set camunda:class / operaton:class to each
      delegate's fully-qualified class name before deploying.
```

Suggest next steps:
- Add business logic in the generated `Service` stubs.
- Use `/migrate-to-bpmn-to-code-apis` when adding more BPMN files later.
- Use `/setup-bpmn-to-code-gradle` or `/setup-bpmn-to-code-maven` to adjust codegen config.
