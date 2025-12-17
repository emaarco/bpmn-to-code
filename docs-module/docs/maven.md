# Maven Plugin

Generate Process APIs from BPMN files in your Maven builds.

## Installation

Add the plugin to your `pom.xml` within the `<build>` section:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>io.github.emaarco</groupId>
            <artifactId>bpmn-to-code-maven</artifactId>
            <version>0.0.17</version>
            <executions>
                <execution>
                    <goals>
                        <goal>generate-bpmn-api</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <baseDir>${project.basedir}</baseDir>
                <filePattern>src/main/resources/**/*.bpmn</filePattern>
                <outputFolderPath>${project.basedir}/src/main/kotlin</outputFolderPath>
                <packagePath>com.example.processes</packagePath>
                <outputLanguage>KOTLIN</outputLanguage>
                <processEngine>ZEEBE</processEngine>
                <useVersioning>false</useVersioning>
            </configuration>
        </plugin>
    </plugins>
</build>
```

## Configuration Parameters

| Parameter | Type | Description | Required | Default |
|-----------|------|-------------|----------|---------|
| `baseDir` | String | Base directory for file pattern resolution | Yes | - |
| `filePattern` | String | Ant-style pattern to match BPMN files | Yes | - |
| `outputFolderPath` | String | Directory where generated code will be written | Yes | - |
| `packagePath` | String | Package name for generated classes | Yes | - |
| `outputLanguage` | Enum | `KOTLIN` or `JAVA` | Yes | - |
| `processEngine` | Enum | `CAMUNDA_7`, `ZEEBE`, or `OPERATON` | Yes | - |
| `useVersioning` | Boolean | Enable file-based API versioning | No | `false` |

### File Pattern Examples

```xml
<!-- All BPMN files in resources -->
<filePattern>src/main/resources/**/*.bpmn</filePattern>

<!-- Specific directory -->
<filePattern>src/main/resources/processes/*.bpmn</filePattern>

<!-- Exclude directories (dev, test) -->
<filePattern>src/main/resources/processes/!(dev|test)/**/*.bpmn</filePattern>

<!-- Only files ending with -process.bpmn -->
<filePattern>src/main/resources/**/*-process.bpmn</filePattern>
```

## Usage

**Generate API:**

```bash
mvn io.github.emaarco:bpmn-to-code-maven:generate-bpmn-api
```

**Integrate with build:**

The plugin automatically executes during the build lifecycle if configured with `<executions>`.

## Examples

### Basic Zeebe + Kotlin

```xml
<plugin>
    <groupId>io.github.emaarco</groupId>
    <artifactId>bpmn-to-code-maven</artifactId>
    <version>0.0.17</version>
    <executions>
        <execution>
            <goals>
                <goal>generate-bpmn-api</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <baseDir>${project.basedir}</baseDir>
        <filePattern>src/main/resources/**/*.bpmn</filePattern>
        <outputFolderPath>${project.basedir}/src/main/kotlin</outputFolderPath>
        <packagePath>com.example.processes</packagePath>
        <outputLanguage>KOTLIN</outputLanguage>
        <processEngine>ZEEBE</processEngine>
    </configuration>
</plugin>
```

### Camunda 7 + Java

```xml
<plugin>
    <groupId>io.github.emaarco</groupId>
    <artifactId>bpmn-to-code-maven</artifactId>
    <version>0.0.17</version>
    <executions>
        <execution>
            <goals>
                <goal>generate-bpmn-api</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <baseDir>${project.basedir}</baseDir>
        <filePattern>src/main/resources/**/*.bpmn</filePattern>
        <outputFolderPath>${project.basedir}/src/main/java</outputFolderPath>
        <packagePath>com.example.processes</packagePath>
        <outputLanguage>JAVA</outputLanguage>
        <processEngine>CAMUNDA_7</processEngine>
    </configuration>
</plugin>
```

### Multi-Environment with Profiles

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
                        <!-- Exclude dev and test folders -->
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
</profiles>
```

## Troubleshooting

### No BPMN files found

Check:

- `baseDir` is correct (usually `${project.basedir}`)
- `filePattern` matches your file locations
- BPMN files have `.bpmn` extension

### Generated code has errors

Verify:

- `packagePath` doesn't conflict with existing packages
- BPMN element IDs are valid identifiers (alphanumeric + underscore)
- `outputLanguage` matches your project (Kotlin vs Java)

### Task types not generated

Task types are only extracted for **Zeebe** and **Operaton** engines. Camunda 7 processes won't have a `TaskTypes` object.

### Plugin version not found

Check:

- Maven Central is accessible
- Version `0.0.17` exists (or use latest from [Maven Central](https://central.sonatype.com/artifact/io.github.emaarco/bpmn-to-code-maven))

## Related

- [Gradle Plugin](gradle.md) - Gradle alternative
- [Web Application](web.md) - No build tool required
- [Architecture](architecture.md) - How it works
- [GitHub](https://github.com/emaarco/bpmn-to-code) - Source code and issues
