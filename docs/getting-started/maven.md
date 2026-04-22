# 🚀 Maven Setup

The bpmn-to-code Maven plugin generates type-safe Process API files from your BPMN models during `mvn compile`. It's published on [Maven Central](https://central.sonatype.com/artifact/io.github.emaarco/bpmn-to-code-maven) and integrates into your existing build lifecycle with minimal configuration.

## 1. Add the plugin

Add the following to the `<build>` section of your `pom.xml`:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>io.github.emaarco</groupId>
            <artifactId>bpmn-to-code-maven</artifactId>
            <version>1.0.0</version>
            <executions>
                <execution>
                    <goals>
                        <goal>generate-bpmn-api</goal>
                    </goals>
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
    </plugins>
</build>
```

See [Configuration](/guide/configuration) for all available parameters.

## 2. Generate the API

```bash
mvn io.github.emaarco:bpmn-to-code-maven:generate-bpmn-api
```

Or, since the goal is bound to the `generate-sources` phase by default:

```bash
mvn compile
```

The generated Process API file(s) will appear in your configured output folder.

## 3. Automated setup with AI Skills

Using [Claude Code](https://docs.anthropic.com/en/docs/claude-code)? The `setup-bpmn-to-code-maven` skill can configure the plugin for you automatically — it detects your project structure, finds your BPMN files, and adds the right `pom.xml` configuration.

After setup, use the `migrate-to-bpmn-to-code-apis` skill to replace hardcoded BPMN strings across your codebase with references to the generated Process API.

```bash
npx skills add https://github.com/emaarco/bpmn-to-code/tree/main/.claude/skills/setup-bpmn-to-code-maven
npx skills add https://github.com/emaarco/bpmn-to-code/tree/main/.claude/skills/migrate-to-bpmn-to-code-apis
```

See [AI Skills](/skills/) for all available skills.

## 4. Advanced configuration

Need multiple engines, separate packages per domain, or file filtering? See [Maven Advanced Configuration](/getting-started/maven-advanced).
