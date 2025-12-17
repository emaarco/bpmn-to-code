# bpmn-to-code

Generate type-safe API definitions from BPMN process models. Supports Gradle, Maven, and Web interfaces for Camunda 7, Zeebe, and Operaton engines.

## Why bpmn-to-code?

**The Problem:**

When building process automation with BPMN, developers constantly reference element IDs, messages, and task types using error-prone magic strings. When BPMN models change, finding all affected code is manual and risky.

**The Solution:**

bpmn-to-code generates a lightweight "Process API" - type-safe constants for all BPMN elements. One source of truth, compile-time safety, IDE autocomplete.

## What It Generates

From this BPMN process:

![Newsletter Process](assets/example-process.png)

bpmn-to-code generates this Process API:

```kotlin
object NewsletterSubscriptionProcessApi {
    const val PROCESS_ID: String = "newsletterSubscription"

    object Elements {
        const val ACTIVITY_SEND_WELCOME_MAIL: String = "Activity_SendWelcomeMail"
        const val START_EVENT_SUBMIT_REGISTRATION_FORM: String = "StartEvent_SubmitRegistrationForm"
        // ... all elements
    }

    object Messages {
        const val MESSAGE_FORM_SUBMITTED: String = "Message_FormSubmitted"
        // ... all messages
    }

    object TaskTypes {
        const val NEWSLETTER_SEND_WELCOME_MAIL: String = "newsletter.sendWelcomeMail"
        // ... all task types (Zeebe/Operaton)
    }

    object Variables {
        const val SUBSCRIPTION_ID: String = "subscriptionId"
    }

    object Timers { ... }
    object Errors { ... }
    object Signals { ... }
}
```

Use it anywhere:

```kotlin
// Start process
processEngine.startProcessInstanceByKey(NewsletterSubscriptionProcessApi.PROCESS_ID)

// Publish message
messageClient.publish(NewsletterSubscriptionProcessApi.Messages.MESSAGE_FORM_SUBMITTED)

// Handle task
worker.handle(NewsletterSubscriptionProcessApi.TaskTypes.NEWSLETTER_SEND_WELCOME_MAIL)
```

## Key Features

- **Type Safety**: Compile-time validation, no magic strings
- **Multi-Engine**: Camunda 7, Zeebe, Operaton support
- **Multi-Language**: Kotlin and Java code generation
- **Build Integration**: Gradle and Maven plugins
- **Zero Install**: Web application for browser-based generation
- **Model Merging**: Combine BPMN variants (dev/staging/prod) into unified API

## How to Use

Choose your preferred method:

=== "Gradle"

    ```kotlin
    plugins {
        id("io.github.emaarco.bpmn-to-code-gradle") version "0.0.17"
    }

    tasks.named("generateBpmnModelApi", GenerateBpmnModelsTask::class) {
        baseDir = projectDir.toString()
        filePattern = "src/main/resources/**/*.bpmn"
        outputFolderPath = "$projectDir/src/main/kotlin"
        packagePath = "com.example"
        outputLanguage = OutputLanguage.KOTLIN
        processEngine = ProcessEngine.ZEEBE
    }
    ```

    [See Gradle documentation →](gradle.md)

=== "Maven"

    ```xml
    <plugin>
        <groupId>io.github.emaarco</groupId>
        <artifactId>bpmn-to-code-maven</artifactId>
        <version>0.0.17</version>
        <configuration>
            <filePattern>src/main/resources/**/*.bpmn</filePattern>
            <outputFolderPath>${project.basedir}/src/main/kotlin</outputFolderPath>
            <packagePath>com.example</packagePath>
            <outputLanguage>KOTLIN</outputLanguage>
            <processEngine>ZEEBE</processEngine>
        </configuration>
    </plugin>
    ```

    [See Maven documentation →](maven.md)

=== "Web"

    No installation required - use the browser-based application:

    1. Visit [bpmn-to-code.miragon.io](https://bpmn-to-code.miragon.io/static/index.html)
    2. Upload BPMN files
    3. Configure settings
    4. Download generated code

    [See Web documentation →](web.md)

## Links

- **Documentation**: [Gradle](gradle.md) | [Maven](maven.md) | [Web](web.md)
- **Architecture**: [System design and decisions](architecture.md)
- **Contributing**: [Development guide](contributing.md)
- **Source**: [GitHub Repository](https://github.com/emaarco/bpmn-to-code)
- **Issues**: [Report bugs or request features](https://github.com/emaarco/bpmn-to-code/issues)

## Resources

- [Maven Central](https://central.sonatype.com/artifact/io.github.emaarco/bpmn-to-code-maven) - Maven Plugin
- [Gradle Plugin Portal](https://plugins.gradle.org/plugin/io.github.emaarco.bpmn-to-code-gradle) - Gradle Plugin
- [Docker Hub](https://hub.docker.com/r/emaarco/bpmn-to-code-web) - Web Application Container
- [Blog Post](https://medium.com/miragon/simplifying-process-automation-with-bpmn-to-code-from-bpmn-models-to-process-apis-216adafeb0ac) - Vision and story

---

*bpmn-to-code is open source. Contributions welcome!*
