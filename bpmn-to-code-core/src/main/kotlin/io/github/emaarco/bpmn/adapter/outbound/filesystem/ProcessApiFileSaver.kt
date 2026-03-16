package io.github.emaarco.bpmn.adapter.outbound.filesystem

import io.github.emaarco.bpmn.application.port.outbound.SaveProcessApiPort
import io.github.emaarco.bpmn.domain.GeneratedApiFile
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File

private val logger = KotlinLogging.logger {}

class ProcessApiFileSaver : SaveProcessApiPort {

    override fun writeFiles(
        generatedFiles: List<GeneratedApiFile>,
        outputFolderPath: String
    ) {
        val outputFolder = File(outputFolderPath)
        if (!outputFolder.exists()) {
            logger.debug { "Creating output folder: $outputFolderPath" }
            outputFolder.mkdirs()
        }

        generatedFiles.forEach { generatedFile ->
            val packageDir = File(outputFolder, generatedFile.packagePath.replace('.', File.separatorChar))
            if (!packageDir.exists()) {
                logger.debug { "Creating package folder: ${packageDir.absolutePath}" }
                packageDir.mkdirs()
            }

            val file = File(packageDir, generatedFile.fileName)
            file.writeText(generatedFile.content)

            logger.info { "Generated ${generatedFile.fileName} in file-system" }
        }
    }
}
