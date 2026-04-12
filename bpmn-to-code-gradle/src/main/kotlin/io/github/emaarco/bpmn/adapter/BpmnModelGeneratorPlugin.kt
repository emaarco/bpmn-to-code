package io.github.emaarco.bpmn.adapter

import org.gradle.api.Plugin
import org.gradle.api.Project

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
    }
}

