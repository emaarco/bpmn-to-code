> 🚧 **This plugin is currently in a beta-version.
> Feel free to test it and provide feedback! :)**

# 🚀 bpmn-to-code

bpmn-to-code is a Maven and Gradle plugin designed to simplify process automation.
Its vision is to foster clean & robust solutions for BPMN-based process automation.
Therefore, it aims to provide a range of features —
such as generating API definition files from BPMN process models —
to reduce manual effort, simplify testing, promote the creation of clean process models,
and ensure consistency between your BPMN model and your code.

## **🤩** What it can do for you

**Streamlined Process Automation**

Say goodbye to the tedious task of manually referencing BPMN elements. bpmn-to-code automatically extracts key
details—like element IDs, messages, and worker types—and generates a lightweight “**Process API**” that keeps your
process models and code in sync.

**Java & Kotlin Code Generation**

Whether you’re developing in **Java** or **Kotlin**, our plugin creates ready-to-use API definitions that integrate
seamlessly with your testing frameworks, messaging systems, or any other automation logic.

**Engine Agnostic & Extensible**

Currently, bpmn-to-code supports both **Camunda** 7 and **Zeebe**. Built with extensibility in mind, it is designed so
that adding support for additional process engines is straightforward—if there is enough demand.

**Styleguide Validation** (🚧)

Looking ahead, I’m planing a styleguide validation feature. Much like a linter for code, it will analyze your BPMN
models against your custom style guide - ensuring that element IDs, message names, and task types adhere to predefined
patterns and naming conventions. This will help maintain designs that are as clean and consistent as your code.

## 💡 Process-API generation in action

One of the key current features is generating a lightweight “Process API” for your BPMN models.
Let’s say you have a newsletter subscription workflow (BPMN) that looks like this:

<img src="docs/example-process.png" alt="Example Process" width="800"/>

After running bpmn-to-code, you’ll have a Kotlin (or Java) file that programmatically references your process model. For
example:

```kotlin
package com.example.process

public object NewsletterSubscriptionProcessApi {
    public val PROCESS_ID: String = "newsletterSubscription"

    public object Elements {
        public val StartEvent_RequestReceived: String = "StartEvent_RequestReceived"
        public val Activity_SendConfirmationMail: String = "Activity_SendConfirmationMail"
        public val Activity_ConfirmRegistration: String = "Activity_ConfirmRegistration"
        public val Activity_SendWelcomeMail: String = "Activity_SendWelcomeMail"
        public val EndEvent_RegistrationCompleted: String = "EndEvent_RegistrationCompleted"
    }

    public object Messages {
        public val Message_FormSubmitted: String = "Message_FormSubmitted"
        public val Message_SubscriptionConfirmed: String = "Message_SubscriptionConfirmed"
    }

    public object TaskTypes {
        public val Activity_SendConfirmationMail: String = "newsletter.sendConfirmationMail"
        public val Activity_SendWelcomeMail: String = "newsletter.sendWelcomeMail"
    }
}
```

All you need is a simple Gradle task configuration that specifies **where** your BPMN models reside,
**where** to output the generated API files, **which** language (Java or Kotlin) to generate,
and **which** process engine your models target (e.g., Camunda 7 or Zeebe):

```kotlin
tasks.named("generateBpmnModelApi", GenerateBpmnModelsTask::class) {
    baseDir = projectDir.toString()
    filePattern = "src/main/resources/**/*.bpmn"
    outputFolderPath = "$projectDir/src/main/kotlin"
    packagePath = "de.emaarco.example"
    outputLanguage = OutputLanguage.KOTLIN
    processEngine = ProcessEngine.ZEEBE
}
```

Once configured, **bpmn-to-code** automatically picks up your BPMN files and generates convenient,
type-safe references you can use throughout your application—be it for testing, messaging,
or worker definitions.

## 📦 Modules

bpmn-to-code is available for both Gradle and Maven.
For detailed installation and configuration instructions,
please refer to the respective module README's:

- [bpmn-to-code-gradle](bpmn-to-code-gradle/README.md): Gradle plugin integration for
  projects using Gradle.
- [bpmn-to-code-maven](bpmn-to-code-maven/README.md): Maven plugin integration for
  projects using Maven.
- [bpmn-to-code-core](bpmn-to-code-core): Contains the core logic for parsing BPMN files and generating the API
  representation.

## 📬 Get the Plugin

You can find the plugin on either
the [Maven Central Repository](https://central.sonatype.com/artifact/io.github.emaarco/bpmn-to-code-maven)
or the [Gradle Plugin Portal](https://plugins.gradle.org/plugin/io.github.emaarco.bpmn-to-code-gradle)

## 🤝 Contributing

Community contributions are at the heart of bpmn-to-code’s vision.
If you have ideas to improve the code generation, want to add support for a new engine,
or are keen to help shape the styleguide validator,
please join me on [GitHub](https://github.com/example/bpmn-to-code).
Submit issues, open pull requests, or simply start a discussion.
