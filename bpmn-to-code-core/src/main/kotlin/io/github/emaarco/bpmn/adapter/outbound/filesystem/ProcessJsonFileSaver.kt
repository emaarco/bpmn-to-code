package io.github.emaarco.bpmn.adapter.outbound.filesystem

import io.github.emaarco.bpmn.application.port.outbound.SaveProcessJsonPort
import io.github.emaarco.bpmn.domain.GeneratedJsonFile
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File

class ProcessJsonFileSaver : SaveProcessJsonPort {

    private val logger = KotlinLogging.logger {}

    override fun writeFiles(
        generatedFiles: List<GeneratedJsonFile>,
        outputFolderPath: String,
    ) {
        val outputFolder = File(outputFolderPath)
        if (!outputFolder.exists()) {
            logger.debug { "Creating output folder: $outputFolderPath" }
            outputFolder.mkdirs()
        }

        generatedFiles.forEach { generatedFile ->
            val file = File(outputFolder, generatedFile.fileName)
            file.writeText(generatedFile.content)
            logger.info { "Generated ${generatedFile.fileName} in file-system" }
        }
    }
}
