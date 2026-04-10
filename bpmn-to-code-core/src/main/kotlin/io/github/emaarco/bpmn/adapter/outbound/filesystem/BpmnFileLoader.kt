package io.github.emaarco.bpmn.adapter.outbound.filesystem

import io.github.emaarco.bpmn.application.port.outbound.LoadBpmnFilesPort
import io.github.emaarco.bpmn.domain.BpmnResource
import io.github.oshai.kotlinlogging.KotlinLogging
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.PathMatcher
import kotlin.io.path.absolute
import kotlin.io.path.name
import kotlin.streams.toList

class BpmnFileLoader : LoadBpmnFilesPort {

    private val logger = KotlinLogging.logger {}

    override fun loadFrom(baseDirectory: String, filePattern: String): List<BpmnResource> {
        val basePath = Path.of(baseDirectory).absolute().normalize()
        val (searchDir, pattern) = resolvePattern(basePath, filePattern)

        val matcher = createMatcher(pattern)
        val files = Files.walk(searchDir)
            .filter { Files.isRegularFile(it) }
            .filter { matcher.matches(searchDir.relativize(it)) }
            .toList()

        logger.info { "Found ${files.size} files matching pattern $pattern in directory $searchDir" }

        return files.map { file ->
            BpmnResource(
                fileName = file.name,
                content = file.toFile().inputStream(),
            )
        }
    }

    private fun createMatcher(pattern: String): PathMatcher {
        val fs = FileSystems.getDefault()
        val primary = fs.getPathMatcher("glob:$pattern")
        if (!pattern.startsWith("**/")) return primary
        val rootPattern = pattern.removePrefix("**/")
        val rootMatcher = fs.getPathMatcher("glob:$rootPattern")
        return PathMatcher { path -> primary.matches(path) || rootMatcher.matches(path) }
    }

    private fun resolvePattern(basePath: Path, pattern: String): Pair<Path, String> {

        if (!pattern.contains('/')) return basePath to pattern

        val segments = pattern.split('/')
        val wildcard = checkForWildcard(segments)

        return if (wildcard.hasNoWildcard()) {
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
        fun hasNoWildcard() = !isPresent
    }
}
