package io.github.emaarco.bpmn.adapter

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test

class BpmnModelGeneratorPluginTest {

    @Test
    fun `plugin registers all three BPMN tasks`() {
        val project = ProjectBuilder.builder().build()

        project.pluginManager.apply(BpmnModelGeneratorPlugin::class.java)

        assertThat(project.tasks.findByName("generateBpmnModelApi")).isNotNull()
        assertThat(project.tasks.findByName("generateBpmnModelJson")).isNotNull()
        assertThat(project.tasks.findByName("validateBpmnModels")).isNotNull()
    }

    @Test
    fun `plugin adds runtime dependency when java plugin is present`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("java")

        project.pluginManager.apply(BpmnModelGeneratorPlugin::class.java)

        val implementation = project.configurations.getByName("implementation")
        val deps = implementation.dependencies.map { "${it.group}:${it.name}" }
        assertThat(deps).contains("io.github.emaarco:bpmn-to-code-runtime")
    }
}
