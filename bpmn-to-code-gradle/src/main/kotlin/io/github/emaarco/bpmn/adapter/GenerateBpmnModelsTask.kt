package io.github.emaarco.bpmn.adapter

import io.github.emaarco.bpmn.adapter.inbound.CreateProcessApiPlugin
import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

abstract class GenerateBpmnModelsTask : DefaultTask() {

    @Input
    lateinit var baseDir: String

    @Input
    lateinit var filePattern: String

    @Input
    lateinit var outputFolderPath: String

    @Input
    lateinit var packagePath: String

    @Input
    lateinit var outputLanguage: OutputLanguage

    @Input
    lateinit var processEngine: ProcessEngine

    @TaskAction
    fun execute() {
        val service = CreateProcessApiPlugin()
        service.execute(baseDir, filePattern, outputFolderPath, packagePath, outputLanguage, processEngine)
        println("BPMN models generated successfully")
    }
}
