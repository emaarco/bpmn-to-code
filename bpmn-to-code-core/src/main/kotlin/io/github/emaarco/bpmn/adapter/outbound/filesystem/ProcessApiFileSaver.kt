package io.github.emaarco.bpmn.adapter.outbound.filesystem

import io.github.emaarco.bpmn.application.port.outbound.SaveProcessApiPort
import io.github.emaarco.bpmn.domain.GeneratedApiFile
import java.io.File

class ProcessApiFileSaver : SaveProcessApiPort {

    override fun writeFiles(
        generatedFiles: List<GeneratedApiFile>,
        outputFolderPath: String
    ) {
        val outputFolder = File(outputFolderPath)
        if (!outputFolder.exists()) {
            println("Creating output folder: $outputFolderPath")
            outputFolder.mkdirs()
        }

        generatedFiles.forEach { generatedFile ->
            val packageDir = File(outputFolder, generatedFile.packagePath.replace('.', File.separatorChar))
            if (!packageDir.exists()) {
                println("Creating package folder: ${packageDir.absolutePath}")
                packageDir.mkdirs()
            }

            val file = File(packageDir, generatedFile.fileName)
            file.writeText(generatedFile.content)

            println("Generated ${generatedFile.fileName} in file-system")
        }
    }
}
