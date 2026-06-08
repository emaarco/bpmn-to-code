package io.github.emaarco.bpmn.adapter.outbound.filesystem

import io.github.emaarco.bpmn.application.port.outbound.SaveProcessJsonPort
import io.github.emaarco.bpmn.domain.GeneratedJsonFile

/** Node-fs [SaveProcessJsonPort]: all files are written flat into the output folder. */
class ProcessJsonFileSaver : SaveProcessJsonPort {

  private val fs = nodeFs()
  private val path = nodePath()

  override fun writeFiles(generatedFiles: List<GeneratedJsonFile>, outputFolderPath: String) {
    mkdirsRecursive(fs, outputFolderPath)
    generatedFiles.forEach { generatedFile ->
      val filePath = path.join(outputFolderPath, generatedFile.fileName)
      fs.writeFileSync(filePath, generatedFile.content)
    }
  }
}
