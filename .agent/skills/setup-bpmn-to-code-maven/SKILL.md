---
name: setup-bpmn-to-code-maven
description: "Set up the bpmn-to-code Maven plugin in an existing project. Adds the plugin configuration to pom.xml with code generation goals. Use when the user asks to 'add bpmn-to-code to my Maven project', 'set up BPMN code generation for Maven', or 'configure the Maven plugin'."
allowed-tools: Read, Write, Edit, Glob
---

# Skill: setup-bpmn-to-code-maven

Set up the bpmn-to-code Maven plugin in an existing Maven project.

## IMPORTANT

- Only modify `pom.xml` — do not create new files unless the user asks.
- Always show the user a draft of the changes before applying them.
- Do not overwrite existing plugin configurations without asking.
- If no `pom.xml` is found, abort and tell the user this skill requires an existing Maven project.

## Instructions

### Step 1 – Detect project structure

1. Use Glob to find `pom.xml` in the project root.
2. Read the `pom.xml` to understand existing plugins, dependencies, and build configuration.
3. Check whether a `<build><plugins>` section already exists.
4. Use Glob to find existing BPMN files (`**/*.bpmn`).
5. Detect the output language by checking for `src/main/kotlin` vs `src/main/java` directories.
6. Look at existing package structures to suggest a package path.

### Step 2 – Gather configuration

Ask the user for the following parameters (skip any already provided in `$ARGUMENTS`). Suggest defaults based on Step 1 detection:

- **Process engine**: `CAMUNDA_7`, `ZEEBE`, or `OPERATON`
- **Output language**: `KOTLIN` or `JAVA` (default: based on detected source directories)
- **Package path**: Java/Kotlin package for generated code (suggest based on existing packages)
- **File pattern**: Glob pattern for BPMN files (suggest based on found `.bpmn` files, default: `src/main/resources/**/*.bpmn`)
- **Versioning**: Whether to enable API versioning (default: `false`)
- **Included elements** (optional): Filter which API objects to generate. Available values: `ELEMENTS`, `SERVICE_TASKS`, `MESSAGES`, `TIMERS`, `ERRORS`, `SIGNALS`, `VARIABLES`. Default: all elements.

### Step 3 – Look up latest version

Check [Maven Central](https://central.sonatype.com/artifact/io.github.emaarco/bpmn-to-code-maven) or the project README for the latest plugin version.

### Step 4 – Draft changes

Prepare the following changes to `pom.xml`. Use the example at `examples/maven-example/pom.xml` as the canonical reference.

Add the plugin block inside `<build><plugins>`:

```xml
<plugin>
    <groupId>io.github.emaarco</groupId>
    <artifactId>bpmn-to-code-maven</artifactId>
    <version>VERSION</version>
    <executions>
        <execution>
            <goals>
                <goal>generate-bpmn-api</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <baseDir>${project.basedir}</baseDir>
        <filePattern>USER_FILE_PATTERN</filePattern>
        <outputFolderPath>${project.basedir}/src/main/LANGUAGE</outputFolderPath>
        <packagePath>USER_PACKAGE</packagePath>
        <outputLanguage>LANGUAGE</outputLanguage>
        <processEngine>ENGINE</processEngine>
        <useVersioning>BOOLEAN</useVersioning>
    </configuration>
</plugin>
```

If the user chose specific `includedElements`, add:

```xml
<includedElements>
    <includedElement>ELEMENT_TYPE</includedElement>
    <!-- ... -->
</includedElements>
```

inside the `<configuration>` block.

If the `<build><plugins>` section does not exist yet, create the full structure.

### Step 5 – Show draft and confirm

Present the complete set of changes to the user and ask:
*"Here are the changes I'll make to your pom.xml. Proceed? (yes / edit / cancel)"*

Apply edits and show again if the user requests changes.

### Step 6 – Apply changes

Write the changes to `pom.xml` using the Edit tool.

### Step 7 – Verify

Suggest the user run:
```bash
mvn io.github.emaarco:bpmn-to-code-maven:generate-bpmn-api
```
to verify the setup works correctly. If the user has BPMN files in place, this should generate the process API code.
