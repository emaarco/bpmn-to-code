# 🚀 bpmn-to-code Maven Plugin

bpmn-to-code is a plugin designed to simplify process automation.
Its vision is to foster clean & robust solutions for BPMN-based process automation.
Therefore, it aims to provide a range of features —
such as generating API definition files from BPMN process models —
to reduce manual effort, simplify testing,
promote the creation of clean process models,
and ensure consistency between your BPMN model and your code.

## ✨ How to Use

Add the `bpmn-to-code-runtime` dependency (it ships the shared types — `ProcessId`, `ElementId`, `MessageName`,
`SignalName`, `VariableName`, and the BPMN metadata records — that the generated code references):

```xml
<dependencies>
    <dependency>
        <groupId>io.github.emaarco</groupId>
        <artifactId>bpmn-to-code-runtime</artifactId>
        <version>2.0.1</version>
    </dependency>
</dependencies>
```

Then add the plugin configuration within the `<build>` section. It tells the plugin where to locate your
BPMN files, where to output the generated API files, and how to format the output (language, package, engine).

```xml

<build>
    <plugins>
        <plugin>
            <groupId>io.github.emaarco</groupId>
            <artifactId>bpmn-to-code-maven</artifactId>
            <version>2.0.1</version>
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
                <packagePath>org.example</packagePath>
                <outputLanguage>KOTLIN</outputLanguage>
                <processEngine>ZEEBE</processEngine>
            </configuration>
        </plugin>
    </plugins>
</build>
```

Once configured, bpmn-to-code-maven processes your BPMN files and generates type-safe API references that
you can seamlessly integrate into your application—whether for testing, messaging, or managing worker tasks.

To execute the plugin, run the following command:

```shell
mvn io.github.emaarco:bpmn-to-code-maven:generate-bpmn-api
```