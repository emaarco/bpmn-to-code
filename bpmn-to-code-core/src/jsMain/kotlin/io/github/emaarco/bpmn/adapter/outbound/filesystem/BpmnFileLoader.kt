package io.github.emaarco.bpmn.adapter.outbound.filesystem

import io.github.emaarco.bpmn.application.port.outbound.LoadBpmnFilesPort
import io.github.emaarco.bpmn.domain.BpmnResource

/**
 * Best-effort Node-fs implementation of [LoadBpmnFilesPort].
 *
 * It recursively walks [baseDirectory] and returns every regular file whose name matches the
 * suffix of [filePattern] (everything after the last `/`, with a leading `*` treated as a
 * suffix match). This is intentionally simpler than the JVM glob matcher — the JS code-gen entry
 * point typically receives BPMN content directly rather than discovering files — but it compiles,
 * reads real files via `fs`, and handles the common recursive-glob and plain `*.bpmn` cases.
 */
class BpmnFileLoader : LoadBpmnFilesPort {

  private val fs = nodeFs()
  private val path = nodePath()

  override fun loadFrom(baseDirectory: String, filePattern: String): List<BpmnResource> {
    val suffix = patternSuffix(filePattern)
    val result = mutableListOf<BpmnResource>()
    walk(baseDirectory) { name, fullPath ->
      if (matchesSuffix(name, suffix)) {
        result.add(BpmnResource(fileName = name, content = readFileBytes(fullPath)))
      }
    }
    return result
  }

  private fun walk(directory: String, onFile: (name: String, fullPath: String) -> Unit) {
    if (!fs.existsSync(directory)) return
    val entries = fs.readdirSync(directory, js("({ withFileTypes: true })"))
    entries.forEach { entry ->
      val name = entry.name as String
      val fullPath = path.join(directory, name)
      if (entry.isDirectory() as Boolean) {
        walk(fullPath, onFile)
      } else {
        onFile(name, fullPath)
      }
    }
  }

  private fun readFileBytes(fullPath: String): ByteArray {
    // Read as latin1 to preserve raw bytes 1:1, then map back to bytes.
    val content = fs.readFileSync(fullPath, "latin1")
    return ByteArray(content.length) { content[it].code.toByte() }
  }

  private fun patternSuffix(filePattern: String): String {
    val lastSegment = filePattern.substringAfterLast('/')
    return lastSegment.removePrefix("*")
  }

  private fun matchesSuffix(name: String, suffix: String): Boolean {
    return if (suffix.isEmpty()) true else name.endsWith(suffix)
  }
}
