# scaffold-process-project — Observed Issues & Improvement Notes

Recorded during end-to-end test run on 2026-04-19 (ZEEBE + Gradle, process-engine-api approach,
input: `shared/bpmn/c8-subscribe-newsletter.bpmn`).

---

## Issue 1 — `tasks.register` conflicts with plugin's own task registration

**Where:** `resources/buildtool/gradle.md`, Section 4 "Code generation task"

**Problem:** The template uses `tasks.register<GenerateBpmnModelsTask>("generateBpmnModelApi") { … }`,
but the `io.github.emaarco.bpmn-to-code-gradle` plugin already registers this task on apply.
Running the task results in:

```
Cannot add task 'generateBpmnModelApi' as a task with that name already exists.
```

**Fix:** Replace `tasks.register<GenerateBpmnModelsTask>(…)` with `tasks.named<GenerateBpmnModelsTask>(…)`.
This is consistent with how the smoke tests configure the task (see `GradlePluginSmokeTest.kt`).

---

## Issue 2 — Spring Boot version hardcoded to 3.4.5, which is no longer accepted by Initializr

**Where:** SKILL.md, Step 2 defaults table (`bootVersion = 3.4.5`)

**Problem:** `start.spring.io` now requires `>=3.5.0`. The curl call returns HTTP 400:

```json
{"status":400,"error":"Bad Request","message":"Invalid Spring Boot version '3.4.5',
Spring Boot compatibility range is >=3.5.0"}
```

**Fix:** Update default `bootVersion` to `3.5.0` (or query Initializr's `/actuator/info` for the current default).

---

## Issue 3 — Initializr type `gradle-project` generates Groovy DSL, not Kotlin DSL

**Where:** SKILL.md, Step 2 — `type` parameter mapping

**Problem:** The skill maps `GRADLE` build tool to `type=gradle-project`, which produces `build.gradle`
(Groovy DSL). Kotlin DSL requires `type=gradle-project-kotlin`.

**Fix:** Update the `type` mapping in Step 2:
- `GRADLE` → `gradle-project-kotlin`
- `MAVEN` → `maven-project`

---

## Issue 4 — Kotlin version mismatch between Initializr output and process-engine-api

**Where:** `resources/buildtool/gradle.md`, Section 2 (plugin block) and Step 2 of SKILL.md

**Problem:** Spring Initializr generates `kotlin("jvm") version "1.9.25"`, but the
`process-engine-adapter-camunda-platform-c8-bom:2026.02.1` and
`process-engine-worker-spring-boot-starter:0.8.0` are compiled with Kotlin 2.2.0 metadata.
Kotlin 1.9.x can only read metadata up to 2.0.0. Compilation fails with:

```
Module was compiled with an incompatible version of Kotlin.
The binary version of its metadata is 2.2.0, expected version is 1.9.0.
```

**Fix:** After bootstrapping via Initializr, the skill must patch the Kotlin version to match
what the process-engine dependencies require. For BOM `2026.02.1`, this means upgrading to
`kotlin("jvm") version "2.2.0"`. The skill should either:
- Hard-code a minimum Kotlin version alongside each BOM version in the resource file, or
- Detect the required Kotlin version from the BOM's POM metadata.

Add to `resources/buildtool/gradle.md` alongside each engine's dependency block:
```
Minimum Kotlin version required: 2.2.0
```

---

## Issue 5 — Wrong `kotlin-logging` import in code template

**Where:** `resources/templates/code.md`, Section A worker template

**Problem:** The template uses `import mu.KotlinLogging` (old `io.github.microutils:kotlin-logging-jvm`),
but the actual transitive dependency pulled in by `process-engine-worker-spring-boot-starter:0.8.0`
is `io.github.oshai:kotlin-logging-jvm:7.x`. That artifact uses the package
`io.github.oshai.kotlinlogging.KotlinLogging`, not `mu.KotlinLogging`.

Using the template as-is produces:
```
e: Unresolved reference: mu
e: Unresolved reference: KotlinLogging
```

**Fix:** Update the worker template to:
```kotlin
import io.github.oshai.kotlinlogging.KotlinLogging
```

And check whether the actual transitive dep is `io.github.oshai` or `io.github.microutils` at
skill invocation time if supporting multiple BOM versions.

---

## Issue 6 — Spring Boot `contextLoads()` test fails for ZEEBE (no running broker)

**Where:** SKILL.md, Step 8 "Verify compilation" (and generated `*ApplicationTests.kt`)

**Problem:** The Initializr generates a `@SpringBootTest contextLoads()` test that starts the full
application context. For ZEEBE, this requires a running Zeebe broker. Without Docker, the test fails
with `NoSuchBeanDefinitionException` (Zeebe client bean can't be created).

This means `./gradlew build` fails for ZEEBE even when all code compiles correctly.

**Fix (two options):**
- a) Run `./gradlew build -x test` for ZEEBE projects and document why in the summary.
- b) Patch the generated test class to add `@SpringBootTest(webEnvironment = WebEnvironment.NONE)`
  + `@MockBean` for the Zeebe client, or annotate with `@DisabledIfSystemProperty` / an
  `@ActiveProfiles("test")` profile that disables Zeebe auto-configuration.

Option (a) is simpler and should be implemented first. Option (b) gives a better developer experience.
The skill's Step 8 instruction should explicitly say:
> For ZEEBE: run `./gradlew build -x test` (full `./gradlew build` requires a running Zeebe broker).

---

## Issue 7 — Java toolchain version not adjusted for host environment

**Where:** SKILL.md, Step 2 (no mention of toolchain); `resources/buildtool/gradle.md`

**Problem:** Initializr defaults to `JavaLanguageVersion.of(17)`. If Java 17 is not installed (only
21 or 25 in this test environment), Gradle fails:

```
Cannot find a Java installation matching: {languageVersion=17, ...}
```

**Fix:** The skill should detect the installed Java version (`java -version`) and patch the
toolchain block accordingly. Or default to 21 (LTS) which is more commonly installed than 17.

---

## Summary Table

| # | Severity | Area | One-liner |
|---|---|---|---|
| 1 | 🔴 Breaking | `gradle.md` | Use `tasks.named` not `tasks.register` |
| 2 | 🔴 Breaking | SKILL.md defaults | Default `bootVersion` is rejected by Initializr |
| 3 | 🟠 High | SKILL.md Step 2 | Use `gradle-project-kotlin` type for Kotlin DSL |
| 4 | 🔴 Breaking | `gradle.md` + SKILL.md | Kotlin version must be ≥2.2.0 for current BOMs |
| 5 | 🔴 Breaking | `templates/code.md` | Wrong `kotlin-logging` import (`mu` vs `oshai`) |
| 6 | 🟠 High | SKILL.md Step 8 | ZEEBE context test needs `-x test` or mock config |
| 7 | 🟡 Medium | SKILL.md Step 2 | Java toolchain version should match host JDK |
