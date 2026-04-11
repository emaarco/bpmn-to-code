package io.github.emaarco.bpmn.adapter.inbound

import io.github.emaarco.bpmn.adapter.outbound.engine.ExtractBpmnAdapter
import io.github.emaarco.bpmn.adapter.outbound.filesystem.BpmnFileLoader
import io.github.emaarco.bpmn.adapter.outbound.json.BpmnJsonGenerator
import io.github.emaarco.bpmn.application.port.outbound.ExtractBpmnPort
import io.github.emaarco.bpmn.application.port.outbound.LoadBpmnFilesPort
import io.github.emaarco.bpmn.domain.GeneratedJsonFile
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.validation.ValidationConfig
import io.github.emaarco.bpmn.domain.service.BpmnValidationService
import io.github.emaarco.bpmn.domain.validation.ValidationPhase
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File

class CreateProcessJsonFilesystemPlugin(
    private val bpmnFileLoader: LoadBpmnFilesPort = BpmnFileLoader(),
    private val bpmnExtractor: ExtractBpmnPort = ExtractBpmnAdapter(),
    private val jsonGenerator: BpmnJsonGenerator = BpmnJsonGenerator(),
) {

    private val logger = KotlinLogging.logger {}

    fun execute(
        baseDir: String,
        filePattern: String,
        outputFolderPath: String,
        engine: ProcessEngine,
        validationConfig: ValidationConfig = ValidationConfig(),
    ) {
        val validationService = BpmnValidationService(validationConfig)
        val inputFiles = bpmnFileLoader.loadFrom(baseDir, filePattern)
        val models = inputFiles.map { bpmnExtractor.extract(it, engine) }
        validationService.validate(models, engine, ValidationPhase.PRE_MERGE)

        val outputFolder = File(outputFolderPath)
        if (!outputFolder.exists()) outputFolder.mkdirs()

        models.forEach { model ->
            val json = jsonGenerator.generate(model)
            val fileName = "${model.processId}.json"
            val file = File(outputFolder, fileName)
            file.writeText(json)
            logger.info { "Generated $fileName in file-system" }
        }
    }
}
