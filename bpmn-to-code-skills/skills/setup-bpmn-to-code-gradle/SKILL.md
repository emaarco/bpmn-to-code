---
name: setup-bpmn-to-code-gradle
description: "Set up the bpmn-to-code Gradle plugin in an existing project. Adds the plugin dependency and configures the code generation task. Use when the user asks to 'add bpmn-to-code to my Gradle project', 'set up BPMN code generation', or 'configure the Gradle plugin'."
allowed-tools: Read, Write, Edit, Glob, Bash(./gradlew *)
---

# Skill: setup-bpmn-to-code-gradle

Set up the bpmn-to-code Gradle plugin in an existing Gradle project.

## IMPORTANT

- Only modify `build.gradle.kts` (Kotlin DSL) or `build.gradle` (Groovy DSL) — never both.
- Always show the user a draft of the changes before applying them.
- Do not overwrite existing plugin or task configurations without asking.
- If no Gradle build file is found, abort and tell the user this skill requires an existing Gradle project.

## Instructions

### Step 1 – Detect project structure

1. Use Glob to find `build.gradle.kts` or `build.gradle` in the project root. Prefer Kotlin DSL (`build.gradle.kts`) if both exist.
2. Read the build file to understand existing plugins, repositories, and dependencies.
3. Use Glob to find existing BPMN files (`**/*.bpmn`).
4. Detect the output language by checking for `src/main/kotlin` vs `src/main/java` directories.
5. Look at existing package structures to suggest a package path.

### Step 2 – Gather configuration

Ask the user for the following parameters (skip any already provided in `$ARGUMENTS`). Suggest defaults based on Step 1 detection:

- **Process engine**: `CAMUNDA_7`, `ZEEBE`, or `OPERATON`
- **Output language**: `KOTLIN` or `JAVA` (default: based on detected source directories)
- **Package path**: Java/Kotlin package for generated code (suggest based on existing packages)
- **File pattern**: Glob pattern for BPMN files (suggest based on found `.bpmn` files, default: `src/main/resources/**/*.bpmn`)

### Step 3 – Look up latest version

Check the [Gradle Plugin Portal](https://plugins.gradle.org/plugin/io.github.emaarco.bpmn-to-code-gradle) or the project README for the latest plugin version.

### Step 4 – Draft changes

Prepare the following changes to the build file. Use the example at `examples/gradle-example/build.gradle.kts` as the canonical reference.

**For Kotlin DSL (`build.gradle.kts`):**

1. Add imports at the top of the file:
   ```kotlin
   import io.github.emaarco.bpmn.adapter.GenerateBpmnModelsTask
   import io.github.emaarco.bpmn.domain.shared.OutputLanguage
   import io.github.emaarco.bpmn.domain.shared.ProcessEngine
   ```

2. Add the plugin to the `plugins {}` block:
   ```kotlin
   id("io.github.emaarco.bpmn-to-code-gradle") version "<version>"
   ```

3. Register the code generation task:
   ```kotlin
   tasks.register<GenerateBpmnModelsTask>("generateBpmnModelApi") {
       baseDir = projectDir.toString()
       filePattern = "<user-chosen-pattern>"
       outputFolderPath = "$projectDir/src/main/<kotlin|java>"
       packagePath = "<user-chosen-package>"
       outputLanguage = OutputLanguage.<KOTLIN|JAVA>
       processEngine = ProcessEngine.<engine>
   }
   ```

**For Groovy DSL (`build.gradle`):** adapt the syntax accordingly (no type parameters, use `=` assignments, string-based enum references).

### Step 5 – Show draft and confirm

Present the complete set of changes to the user and ask:
*"Here are the changes I'll make to your build file. Proceed? (yes / edit / cancel)"*

Apply edits and show again if the user requests changes.

### Step 6 – Apply changes

Write the changes to the build file using the Edit tool.

### Step 7 – Verify

Suggest the user run:
```bash
./gradlew generateBpmnModelApi
```
to verify the setup works correctly. If the user has BPMN files in place, this should generate the process API code.
