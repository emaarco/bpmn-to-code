package io.github.emaarco.bpmn.adapter

import org.gradle.testkit.runner.GradleRunner

fun GradleRunner.withJacocoAgent(): GradleRunner {
    val agentArg = System.getProperty("jacocoAgentArg") ?: return this
    val env = System.getenv().toMutableMap()
    val existing = env["GRADLE_OPTS"]
    env["GRADLE_OPTS"] = if (existing.isNullOrBlank()) agentArg else "$existing $agentArg"
    return withEnvironment(env)
}
