package io.github.emaarco.bpmn.testing

import io.github.emaarco.bpmn.domain.BpmnResource
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.name

/**
 * Loads `.bpmn` files from a filesystem directory.
 */
internal object FilesystemBpmnLoader {

    fun load(directory: Path, engine: ProcessEngine): List<BpmnResource> {
        require(Files.isDirectory(directory)) {
            "Path is not a directory: $directory"
        }
        return Files.walk(directory)
            .filter { it.extension == "bpmn" }
            .map { path ->
                BpmnResource(
                    fileName = path.name,
                    content = Files.newInputStream(path),
                    engine = engine,
                )
            }
            .toList()
    }
}
