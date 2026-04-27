# Maven Advanced Configuration

bpmn-to-code itself doesn't support excluding specific files or running multiple engines in a single execution. But Maven's build system covers both cases.

## Multiple executions

Add separate `<execution>` blocks with their own `<configuration>` to generate from different file sets or engines:

```xml
<plugin>
    <groupId>io.github.emaarco</groupId>
    <artifactId>bpmn-to-code-maven</artifactId>
    <version>2.0.1</version>
    <executions>
        <!-- Camunda 7 processes -->
        <execution>
            <id>generate-c7</id>
            <goals><goal>generate-bpmn-api</goal></goals>
            <configuration>
                <baseDir>${project.basedir}</baseDir>
                <filePattern>src/main/resources/c7/*.bpmn</filePattern>
                <outputFolderPath>${project.basedir}/src/main/java</outputFolderPath>
                <packagePath>com.example.c7</packagePath>
                <outputLanguage>KOTLIN</outputLanguage>
                <processEngine>CAMUNDA_7</processEngine>
            </configuration>
        </execution>
        <!-- Zeebe processes -->
        <execution>
            <id>generate-zeebe</id>
            <goals><goal>generate-bpmn-api</goal></goals>
            <configuration>
                <baseDir>${project.basedir}</baseDir>
                <filePattern>src/main/resources/c8/*.bpmn</filePattern>
                <outputFolderPath>${project.basedir}/src/main/java</outputFolderPath>
                <packagePath>com.example.c8</packagePath>
                <outputLanguage>KOTLIN</outputLanguage>
                <processEngine>ZEEBE</processEngine>
            </configuration>
        </execution>
    </executions>
</plugin>
```

Both executions run automatically during `mvn compile`.

## Pre-filtering files

bpmn-to-code processes all files matching the `filePattern` glob — it has no built-in include/exclude logic. If you need to exclude specific files, use the [maven-resources-plugin](https://maven.apache.org/plugins/maven-resources-plugin/) to copy only the files you want into a staging directory, then point bpmn-to-code at that directory:

```xml
<!-- 1. Copy only the BPMN files you want -->
<plugin>
    <artifactId>maven-resources-plugin</artifactId>
    <executions>
        <execution>
            <id>filter-bpmn</id>
            <phase>generate-sources</phase>
            <goals><goal>copy-resources</goal></goals>
            <configuration>
                <outputDirectory>${project.build.directory}/bpmn-staging</outputDirectory>
                <resources>
                    <resource>
                        <directory>src/main/resources</directory>
                        <includes>
                            <include>**/order-*.bpmn</include>
                            <include>**/payment-*.bpmn</include>
                        </includes>
                        <excludes>
                            <exclude>**/draft-*.bpmn</exclude>
                        </excludes>
                    </resource>
                </resources>
            </configuration>
        </execution>
    </executions>
</plugin>

<!-- 2. Generate from the staged files -->
<plugin>
    <groupId>io.github.emaarco</groupId>
    <artifactId>bpmn-to-code-maven</artifactId>
    <version>2.0.1</version>
    <executions>
        <execution>
            <goals><goal>generate-bpmn-api</goal></goals>
        </execution>
    </executions>
    <configuration>
        <baseDir>${project.build.directory}/bpmn-staging</baseDir>
        <filePattern>**/*.bpmn</filePattern>
        <outputFolderPath>${project.basedir}/src/main/java</outputFolderPath>
        <packagePath>com.example.process</packagePath>
        <outputLanguage>KOTLIN</outputLanguage>
        <processEngine>ZEEBE</processEngine>
    </configuration>
</plugin>
```

The [maven-resources-plugin](https://maven.apache.org/plugins/maven-resources-plugin/) runs first (during `generate-sources`), then bpmn-to-code generates from the filtered files.
