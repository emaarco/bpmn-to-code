package io.github.emaarco.bpmn.adapter

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.util.Properties

@Suppress("unused")
class BpmnModelGeneratorPlugin : Plugin<Project> {

    companion object {
        private const val RUNTIME_GROUP = "io.github.emaarco"
        private const val RUNTIME_ARTIFACT = "bpmn-to-code-runtime"
        private const val VERSION_RESOURCE = "bpmn-to-code-plugin.properties"
    }

    override fun apply(project: Project) {
        project.tasks.register("generateBpmnModelApi", GenerateBpmnModelsTask::class.java) {
            it.group = "BPMN"
            it.description = "Generates API-files from BPMN files to interact with a process-engine."
        }
        project.tasks.register("generateBpmnModelJson", GenerateBpmnJsonTask::class.java) {
            it.group = "BPMN"
            it.description = "Generates JSON representations of BPMN process models for AI and human consumption."
        }
        project.tasks.register("validateBpmnModels", ValidateBpmnModelsTask::class.java) {
            it.group = "BPMN"
            it.description = "[Experimental] Validates BPMN models against built-in rules without generating code."
        }
        addRuntimeDependency(project)
    }

    private fun addRuntimeDependency(project: Project) {
        val version = loadPluginVersion()
        val coordinates = "$RUNTIME_GROUP:$RUNTIME_ARTIFACT:$version"
        project.plugins.withId("java") {
            project.dependencies.add("implementation", coordinates)
        }
    }

    private fun loadPluginVersion(): String {
        val stream = javaClass.classLoader.getResourceAsStream(VERSION_RESOURCE)
            ?: throw GradleException(
                "[bpmn-to-code] Could not locate '$VERSION_RESOURCE' on the plugin classpath. " +
                    "This is a bug in the plugin distribution — please report it."
            )
        val properties = stream.use {
            Properties().apply { load(it) }
        }
        return properties.getProperty("version")?.takeIf { it.isNotBlank() }
            ?: throw GradleException(
                "[bpmn-to-code] '$VERSION_RESOURCE' is missing a non-blank 'version' entry. " +
                    "This is a bug in the plugin distribution — please report it."
            )
    }
}
