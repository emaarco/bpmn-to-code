# 🚀 bpmn-to-code Maven Plugin

bpmn-to-code-maven enables you to generate API-definition files from BPMN process models
within your Maven projects. Its purpose is to extract vital process information
(like element names, message names, and service task types)
and convert them into Java or Kotlin API representations —
streamlining your process automation and reducing manual errors.

## 🎯 Purpose & Use-Case

The Maven plugin automates the extraction of BPMN model data by:

- Extracting **element names** for testing and programmatic use.
- Exporting **message names** for easy process interaction.
- Providing details on **service task types** to simplify integration.

These features help you build robust BPMN-based process automation solutions with less manual work.

## ✨ How to Use

Add the following plugin configuration to your project's `pom.xml` within the `<build>` section:

```xml

<build>
    <plugins>
        <plugin>
            <groupId>io.github.emaarco</groupId>
            <artifactId>bpmn-to-code-maven</artifactId>
            <version>0.0.1</version>
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

This configuration directs the plugin on where to locate your BPMN files,
where to generate the API files, and how to configure the output (such as language, package, etc.).

## 🤝 Contributing

Contributions are welcome! Please refer to the root repository for contribution guidelines.

## 📜 License

This project is licensed under the MIT License. See the LICENSE file for details.