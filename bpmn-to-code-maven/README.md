# 🚀 bpmn-to-code Maven Plugin

bpmn-to-code is a plugin designed to simplify process automation.
Its vision is to foster clean & robust solutions for BPMN-based process automation.
Therefore, it aims to provide a range of features —
such as generating API definition files from BPMN process models —
to reduce manual effort, simplify testing,
promote the creation of clean process models,
and ensure consistency between your BPMN model and your code.

## ✨ How to Use

Add the following plugin configuration to your project's pom.xml within the <build> section.
This configuration instructs the plugin on where to locate your BPMN files,
where to output the generated API files, and how to format the output
(e.g., language, package, and process engine).

```xml

<build>
    <plugins>
        <plugin>
            <groupId>io.github.emaarco</groupId>
            <artifactId>bpmn-to-code-maven</artifactId>
            <version>0.0.3-alpha</version>
            <executions>
                <execution>
                    <goals>
                        <goal>generate</goal>
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