package io.github.emaarco.bpmn.adapter

import org.gradle.testkit.runner.GradleRunner

fun GradleRunner.withJacocoAgent(): GradleRunner {
    val agentArg = System.getProperty("jacocoAgentArg") ?: return this
    return withJvmArguments(agentArg)
}
