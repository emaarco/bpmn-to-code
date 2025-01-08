package io.github.emaarco.bpmn.adapter.outbound.filesystem

import io.github.emaarco.bpmn.application.port.outbound.LoadBpmnFilesPort
import java.io.File
import java.io.IOException
import java.io.UncheckedIOException
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors.toList

class BpmnFileLoader : LoadBpmnFilesPort {

    /**
     * Retrieves all files from the specified base-directory
     * that match the given wildcard pattern.
     * @param baseDirectory The root directory to start the search.
     * @param filePattern The wildcard pattern to match the files.
     */
    override fun loadFrom(baseDirectory: String, filePattern: String): List<File> {
        val basePath = Paths.get(baseDirectory).toAbsolutePath().normalize()
        val pathMatcher = FileSystems.getDefault().getPathMatcher("glob:$filePattern")
        val pathFilter: (Path) -> Boolean = { pathMatcher.matches(basePath.relativize(it)) }
        return filterFilesFromBasePath(basePath, pathFilter)
    }

    private fun filterFilesFromBasePath(
        basePath: Path,
        pathMatcher: (Path) -> Boolean
    ) = try {
        val matchingPaths = Files.walk(basePath).filter(pathMatcher).collect(toList())
        matchingPaths.map(Path::toFile)
    } catch (e: IOException) {
        throw UncheckedIOException("Error searching for files in $basePath", e)
    }
}
