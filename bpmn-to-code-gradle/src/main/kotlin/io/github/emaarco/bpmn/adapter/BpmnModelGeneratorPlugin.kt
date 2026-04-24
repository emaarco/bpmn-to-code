package io.github.emaarco.bpmn.adapter

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.util.Properties

private const val RUNTIME_GROUP = "io.github.emaarco"
private const val RUNTIME_ARTIFACT = "bpmn-to-code-runtime"
private const val VERSION_RESOURCE = "bpmn-to-code-plugin.properties"

@Suppress("unused")
class BpmnModelGeneratorPlugin : Plugin<Project> {
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
        val version = loadPluginVersion(project) ?: return
        val coordinates = "$RUNTIME_GROUP:$RUNTIME_ARTIFACT:$version"
        project.plugins.withId("java") {
            project.dependencies.add("implementation", coordinates)
        }
    }

    private fun loadPluginVersion(project: Project): String? {
        val stream = javaClass.classLoader.getResourceAsStream(VERSION_RESOURCE)
        if (stream == null) {
            project.logger.warn(
                "[bpmn-to-code] Could not locate '$VERSION_RESOURCE' on the plugin classpath — " +
                    "skipping auto-add of '$RUNTIME_GROUP:$RUNTIME_ARTIFACT'. " +
                    "Add the dependency manually to ensure generated code compiles."
            )
            return null
        }
        val properties = stream.use {
            Properties().apply { load(it) }
        }
        val version = properties.getProperty("version")?.takeIf { it.isNotBlank() }
        if (version == null) {
            project.logger.warn(
                "[bpmn-to-code] '$VERSION_RESOURCE' is missing a non-blank 'version' entry — " +
                    "skipping auto-add of '$RUNTIME_GROUP:$RUNTIME_ARTIFACT'. " +
                    "Add the dependency manually to ensure generated code compiles."
            )
        }
        return version
    }
}
