package io.github.emaarco.bpmn.adapter.outbound.filesystem

import io.github.emaarco.bpmn.application.port.outbound.LoadBpmnFilesPort
import io.github.emaarco.bpmn.application.port.outbound.LoggerPort
import org.apache.tools.ant.DirectoryScanner
import java.io.File
import java.nio.file.Path
import kotlin.io.path.absolute

class BpmnFileLoader(
    private val logger: LoggerPort
) : LoadBpmnFilesPort {

    /**
     * Retrieves all files from the specified base-directory
     * that match the given wildcard pattern.
     * @param baseDirectory The root directory to start the search.
     * @param filePattern The wildcard pattern to match the files.
     */
    override fun loadFrom(baseDirectory: String, filePattern: String): List<File> {
        val basePath = Path.of(baseDirectory).absolute().normalize()
        val (searchDir, pattern) = resolvePattern(basePath, filePattern)

        val scanner = DirectoryScanner()
        scanner.basedir = searchDir.toFile()
        scanner.setIncludes(arrayOf(pattern))
        scanner.scan()

        val files = scanner.includedFiles.map { File(searchDir.toFile(), it) }
        logger.info("Found ${files.size} files matching pattern $pattern in directory $searchDir")

        return files
    }

    private fun resolvePattern(basePath: Path, pattern: String): Pair<Path, String> {

        if (!pattern.contains('/')) return basePath to pattern

        val segments = pattern.split('/')
        val wildcard = checkForWildcard(segments)

        return if (wildcard.hasNone()) {
            val dirPath = segments.dropLast(1).joinToString("/")
            val fileName = segments.last()
            basePath.resolve(dirPath).normalize() to fileName
        } else if (wildcard.position == 0) {
            basePath to pattern
        } else {
            val dirPath = segments.take(wildcard.position).joinToString("/")
            val globPattern = segments.drop(wildcard.position).joinToString("/")
            basePath.resolve(dirPath).normalize() to globPattern
        }
    }

    private fun checkForWildcard(pathSegments: List<String>): WildcardCheckResult {
        val position = pathSegments.indexOfFirst { it.contains('*') }
        return if (position == -1) {
            WildcardCheckResult(position = position, isPresent = false)
        } else {
            WildcardCheckResult(position = position, isPresent = true)
        }
    }

    private data class WildcardCheckResult(
        val position: Int,
        val isPresent: Boolean
    ) {
        fun hasNone() = !isPresent
    }
}