package io.github.emaarco.bpmn.adapter.outbound.filesystem

import io.github.emaarco.bpmn.application.port.outbound.SaveProcessApiPort
import io.github.emaarco.bpmn.domain.GeneratedApiFile

/**
 * Node-fs implementation of [SaveProcessApiPort]. Mirrors the JVM [ProcessApiFileSaver]:
 * the package path becomes a nested directory under the output folder.
 */
class ProcessApiFileSaver : SaveProcessApiPort {

  private val fs = nodeFs()
  private val path = nodePath()

  override fun writeFiles(
    generatedFiles: List<GeneratedApiFile>,
    outputFolderPath: String,
  ) {
    mkdirsRecursive(fs, outputFolderPath)
    generatedFiles.forEach { generatedFile ->
      val packageSegments = generatedFile.packagePath.split('.').toTypedArray()
      val packageDir = path.join(outputFolderPath, *packageSegments)
      mkdirsRecursive(fs, packageDir)
      val filePath = path.join(packageDir, generatedFile.fileName)
      fs.writeFileSync(filePath, generatedFile.content)
    }
  }
}
