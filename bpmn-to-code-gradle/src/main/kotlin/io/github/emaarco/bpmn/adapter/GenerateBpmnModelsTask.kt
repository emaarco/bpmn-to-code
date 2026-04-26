package io.github.emaarco.bpmn.adapter

import io.github.emaarco.bpmn.adapter.inbound.CreateProcessApiFilesystemPlugin
import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(
    because = "Task produces output based on files that can change at any time without the plugin knowing about it"
)
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
        validate()
        val service = CreateProcessApiFilesystemPlugin()
        service.execute(
            baseDir = baseDir,
            filePattern = filePattern,
            outputFolderPath = outputFolderPath,
            packagePath = packagePath,
            outputLanguage = outputLanguage,
            engine = processEngine,
        )
        logger.lifecycle("BPMN models generated successfully")
    }

    private fun validate() {
        check(this::baseDir.isInitialized) { "baseDir must be configured in bpmnToCode { ... }" }
        check(this::filePattern.isInitialized) { "filePattern must be configured in bpmnToCode { ... }" }
        check(this::outputFolderPath.isInitialized) { "outputFolderPath must be configured in bpmnToCode { ... }" }
        check(this::packagePath.isInitialized) { "packagePath must be configured in bpmnToCode { ... }" }
        check(this::outputLanguage.isInitialized) { "outputLanguage must be configured in bpmnToCode { ... }" }
        check(this::processEngine.isInitialized) { "processEngine must be configured in bpmnToCode { ... }" }
    }
}
