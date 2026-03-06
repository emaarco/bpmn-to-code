---
title: BPMN File Filtering
description: How to filter which BPMN files are processed using Ant-style glob patterns in the filePattern parameter.
---

The `filePattern` parameter uses **Ant-style glob patterns** to control which BPMN files are picked up for code generation. No plugin code changes are needed — patterns alone handle all filtering scenarios.

## Pattern Syntax

| Wildcard | Meaning | Example |
|----------|---------|---------|
| `*` | Any characters within a single directory segment | `*.bpmn` matches `order.bpmn` but not `sub/order.bpmn` |
| `**` | Any number of directory segments (recursive) | `**/*.bpmn` matches files at any depth |
| `?` | Any single character | `order?.bpmn` matches `order1.bpmn` |
| `!(...)` | Negation — excludes matching segments | `!(dev\|test)` excludes dirs named `dev` or `test` |

## Common Patterns

### Match all BPMN files recursively

```
src/main/resources/**/*.bpmn
```

### Match files in a specific directory only (no subdirectories)

```
src/main/resources/processes/*.bpmn
```

### Match files with a specific suffix

```
src/main/resources/**/*-process.bpmn
```

### Exclude specific directories

```
src/main/resources/processes/!(dev|test)/**/*.bpmn
```

This includes everything under `processes/` except `dev/` and `test/`.

### Include only a specific subdirectory

```
src/main/resources/processes/prod/**/*.bpmn
```

## Gradle Examples

### Single task — all environments

```kotlin
tasks.named("generateBpmnModelApi", GenerateBpmnModelsTask::class) {
    baseDir = projectDir.toString()
    filePattern = "src/main/resources/**/*.bpmn"
    outputFolderPath = "$projectDir/src/main/kotlin"
    packagePath = "com.example.processes"
    outputLanguage = OutputLanguage.KOTLIN
    processEngine = ProcessEngine.ZEEBE
}
```

### Separate tasks per environment

Register multiple tasks with different patterns to generate separate API packages:

```kotlin
// Production only — excludes dev and test folders
tasks.register("generateProdApi", GenerateBpmnModelsTask::class) {
    baseDir = projectDir.toString()
    filePattern = "src/main/resources/processes/!(dev|test)/**/*.bpmn"
    outputFolderPath = "$projectDir/src/main/kotlin"
    packagePath = "com.example.processes.prod"
    outputLanguage = OutputLanguage.KOTLIN
    processEngine = ProcessEngine.ZEEBE
}

// Development — all files
tasks.register("generateDevApi", GenerateBpmnModelsTask::class) {
    baseDir = projectDir.toString()
    filePattern = "src/main/resources/processes/**/*.bpmn"
    outputFolderPath = "$projectDir/src/main/kotlin"
    packagePath = "com.example.processes.dev"
    outputLanguage = OutputLanguage.KOTLIN
    processEngine = ProcessEngine.ZEEBE
}
```

### Separate tasks per engine

```kotlin
tasks.register("generateZeebeApi", GenerateBpmnModelsTask::class) {
    baseDir = projectDir.toString()
    filePattern = "src/main/resources/zeebe/**/*.bpmn"
    outputFolderPath = "$projectDir/src/main/kotlin"
    packagePath = "com.example.processes.zeebe"
    outputLanguage = OutputLanguage.KOTLIN
    processEngine = ProcessEngine.ZEEBE
}

tasks.register("generateCamundaApi", GenerateBpmnModelsTask::class) {
    baseDir = projectDir.toString()
    filePattern = "src/main/resources/camunda/**/*.bpmn"
    outputFolderPath = "$projectDir/src/main/kotlin"
    packagePath = "com.example.processes.camunda"
    outputLanguage = OutputLanguage.KOTLIN
    processEngine = ProcessEngine.CAMUNDA_7
}
```

## Maven Examples

### Single execution — all environments

```xml
<configuration>
    <filePattern>src/main/resources/**/*.bpmn</filePattern>
    <outputFolderPath>${project.basedir}/src/main/kotlin</outputFolderPath>
    <packagePath>com.example.processes</packagePath>
    <outputLanguage>KOTLIN</outputLanguage>
    <processEngine>ZEEBE</processEngine>
</configuration>
```

### Multiple executions per engine or environment

```xml
<executions>
    <execution>
        <id>generate-zeebe-api</id>
        <goals>
            <goal>generate-bpmn-api</goal>
        </goals>
        <configuration>
            <filePattern>src/main/resources/zeebe/**/*.bpmn</filePattern>
            <outputFolderPath>${project.basedir}/src/main/kotlin</outputFolderPath>
            <packagePath>com.example.processes.zeebe</packagePath>
            <outputLanguage>KOTLIN</outputLanguage>
            <processEngine>ZEEBE</processEngine>
        </configuration>
    </execution>
    <execution>
        <id>generate-camunda-api</id>
        <goals>
            <goal>generate-bpmn-api</goal>
        </goals>
        <configuration>
            <filePattern>src/main/resources/camunda/**/*.bpmn</filePattern>
            <outputFolderPath>${project.basedir}/src/main/java</outputFolderPath>
            <packagePath>com.example.processes.camunda</packagePath>
            <outputLanguage>JAVA</outputLanguage>
            <processEngine>CAMUNDA_7</processEngine>
        </configuration>
    </execution>
</executions>
```

### Maven profiles — override pattern per environment

```xml
<profiles>
    <profile>
        <id>prod</id>
        <build>
            <plugins>
                <plugin>
                    <groupId>io.github.emaarco</groupId>
                    <artifactId>bpmn-to-code-maven</artifactId>
                    <version>0.0.17</version>
                    <configuration>
                        <filePattern>src/main/resources/processes/!(dev|test)/**/*.bpmn</filePattern>
                        <outputFolderPath>${project.basedir}/src/main/kotlin</outputFolderPath>
                        <packagePath>com.example.processes</packagePath>
                        <outputLanguage>KOTLIN</outputLanguage>
                        <processEngine>ZEEBE</processEngine>
                    </configuration>
                </plugin>
            </plugins>
        </build>
    </profile>
    <profile>
        <id>dev</id>
        <build>
            <plugins>
                <plugin>
                    <groupId>io.github.emaarco</groupId>
                    <artifactId>bpmn-to-code-maven</artifactId>
                    <version>0.0.17</version>
                    <configuration>
                        <filePattern>src/main/resources/processes/**/*.bpmn</filePattern>
                        <outputFolderPath>${project.basedir}/src/main/kotlin</outputFolderPath>
                        <packagePath>com.example.processes</packagePath>
                        <outputLanguage>KOTLIN</outputLanguage>
                        <processEngine>ZEEBE</processEngine>
                    </configuration>
                </plugin>
            </plugins>
        </build>
    </profile>
</profiles>
```

Activate with `mvn package -Pprod` or `mvn package -Pdev`.

## Directory-Based Filtering

Organizing BPMN files by environment or domain makes filtering intuitive:

```
src/main/resources/
└── processes/
    ├── prod/
    │   ├── order-process.bpmn
    │   └── payment-process.bpmn
    ├── dev/
    │   └── order-process.bpmn     ← dev variant with test timers
    └── shared/
        └── notification-process.bpmn
```

Then filter by directory:

```kotlin
// Production + shared
filePattern = "src/main/resources/processes/!(dev)/**/*.bpmn"

// Dev only
filePattern = "src/main/resources/processes/dev/**/*.bpmn"

// Shared only
filePattern = "src/main/resources/processes/shared/**/*.bpmn"
```

## Troubleshooting

**No files matched:** Verify `baseDir` is correct (usually `projectDir.toString()` for Gradle, `${project.basedir}` for Maven) and that `filePattern` is relative to it.

**Negation not working:** Ensure the pattern is `!(a|b)` not `!a` — multiple exclusions require the `|` separator inside the parentheses.

**Files from wrong directories included:** Use a more specific prefix before the `**` glob, e.g. `src/main/resources/processes/**/*.bpmn` instead of just `**/*.bpmn`.
