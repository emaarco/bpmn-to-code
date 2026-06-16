# Moved to io.miragon

::: warning bpmn-to-code has moved
bpmn-to-code now lives at **[github.com/miragon/bpmn-to-code](https://github.com/miragon/bpmn-to-code)**
under the `io.miragon` coordinates. The `io.github.emaarco` coordinates and the
`io.github.emaarco.bpmn-to-code-gradle` plugin id are **deprecated** and will not receive further updates.
:::

The project has moved to the **io.miragon** namespace, hosted by the
[Miragon organization](https://github.com/miragon). This is the final release published under
`io.github.emaarco`; its only purpose is to announce the move. No functionality changed.

## What changed

| | Old (`io.github.emaarco`) | New (`io.miragon`) |
| --- | --- | --- |
| Maven / Gradle groupId | `io.github.emaarco` | `io.miragon` |
| Gradle plugin id | `io.github.emaarco.bpmn-to-code-gradle` | `io.miragon.bpmn-to-code-gradle` |
| Maven artifacts | `io.github.emaarco:bpmn-to-code-*` | `io.miragon:bpmn-to-code-*` |
| Docker image | `emaarco/bpmn-to-code-web` | `miragon/bpmn-to-code-web` |
| Repository | `github.com/emaarco/bpmn-to-code` | `github.com/miragon/bpmn-to-code` |

## How to migrate

### Gradle

Update the plugin id in your `build.gradle.kts`:

```kotlin
plugins {
    // Before
    // id("io.github.emaarco.bpmn-to-code-gradle") version "2.4.0"

    // After
    id("io.miragon.bpmn-to-code-gradle") version "<new-version>"
}
```

Any `import io.github.emaarco.bpmn.*` references in your build configuration (for example
`GenerateBpmnModelsTask` or `OutputLanguage`) move to `io.miragon.bpmn.*`.

### Maven

Update the `groupId` in your `pom.xml`:

```xml
<plugin>
    <!-- Before: <groupId>io.github.emaarco</groupId> -->
    <groupId>io.miragon</groupId>
    <artifactId>bpmn-to-code-maven</artifactId>
    <version><!-- new version --></version>
</plugin>
```

### Docker

```bash
# Before
# docker pull emaarco/bpmn-to-code-web

# After
docker pull miragon/bpmn-to-code-web
```

### Regenerate your Process API

After switching coordinates, **regenerate your Process API** so the generated files line up with the
new release, and rebuild your project:

- Gradle: `./gradlew generateBpmnModelApi`
- Maven: `mvn io.miragon:bpmn-to-code-maven:generate-bpmn-api`

For the full picture and any further updates, follow the new repository at
**[github.com/miragon/bpmn-to-code](https://github.com/miragon/bpmn-to-code)**.
