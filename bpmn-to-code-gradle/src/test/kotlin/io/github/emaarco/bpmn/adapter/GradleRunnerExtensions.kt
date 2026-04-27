package io.github.emaarco.bpmn.adapter

import org.gradle.testkit.runner.GradleRunner

fun GradleRunner.withJacocoAgent(): GradleRunner {
    val agentArg = System.getProperty("jacocoAgentArg") ?: return this
    return withEnvironment(mapOf("GRADLE_OPTS" to agentArg))
}
