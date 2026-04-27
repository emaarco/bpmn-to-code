package io.github.emaarco.bpmn.adapter

import org.gradle.testkit.runner.GradleRunner
import java.io.File

fun GradleRunner.withJacocoAgent(projectDir: File): GradleRunner {
    val agentArg = System.getProperty("jacocoAgentArg") ?: return this
    // Write org.gradle.jvmargs into the test project's gradle.properties so the
    // Gradle daemon starts with the JaCoCo agent attached (env-based approaches
    // don't work reliably because TestKit may reuse a daemon started without the agent).
    File(projectDir, "gradle.properties").appendText("\norg.gradle.jvmargs=$agentArg\n")
    return this
}
