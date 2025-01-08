package io.github.emaarco.bpmn.adapter

import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("unused")
class BpmnModelGeneratorPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register("generateBpmnModelApi", GenerateBpmnModelsTask::class.java) {
            group = "BPMN"
            description = "Generates API-files from BPMN files to interact with a process-engine‚"
        }
    }
}

