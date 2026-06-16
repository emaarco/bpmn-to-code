package io.github.emaarco.bpmn.adapter

import io.github.emaarco.bpmn.adapter.inbound.CreateProcessJsonFilesystemPlugin
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@Deprecated(
    "bpmn-to-code is moving to the io.miragon namespace; the 'io.github.emaarco' coordinates are deprecated " +
        "and will receive no further updates. Migrate to io.miragon — https://github.com/miragon/bpmn-to-code"
)
@DisableCachingByDefault(
    because = "Task produces output based on files that can change at any time without the plugin knowing about it"
)
abstract class GenerateBpmnJsonTask : DefaultTask() {

    @Input
    lateinit var baseDir: String

    @Input
    lateinit var filePattern: String

    @Input
    lateinit var outputFolderPath: String

    @Input
    lateinit var processEngine: ProcessEngine

    @TaskAction
    fun execute() {
        validate()
        val plugin = CreateProcessJsonFilesystemPlugin()
        plugin.execute(
            baseDir = baseDir,
            filePattern = filePattern,
            outputFolderPath = outputFolderPath,
            engine = processEngine,
        )
        logger.lifecycle("BPMN JSON files generated successfully")
    }

    private fun validate() {
        check(this::baseDir.isInitialized) { "baseDir must be configured in bpmnToCode { ... }" }
        check(this::filePattern.isInitialized) { "filePattern must be configured in bpmnToCode { ... }" }
        check(this::outputFolderPath.isInitialized) { "outputFolderPath must be configured in bpmnToCode { ... }" }
        check(this::processEngine.isInitialized) { "processEngine must be configured in bpmnToCode { ... }" }
    }
}
