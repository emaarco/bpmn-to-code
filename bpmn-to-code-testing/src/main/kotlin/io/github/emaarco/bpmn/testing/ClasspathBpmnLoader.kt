package io.github.emaarco.bpmn.testing

import io.github.emaarco.bpmn.domain.BpmnResource
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.name

/**
 * Loads `.bpmn` files from the classpath.
 *
 * Supports both directories (walks recursively for `.bpmn` files) and
 * individual `.bpmn` file paths. Uses [Thread.currentThread().contextClassLoader]
 * to resolve the given path.
 */
internal object ClasspathBpmnLoader {

    fun load(classpathPath: String, engine: ProcessEngine): List<BpmnResource> {
        val classLoader = Thread.currentThread().contextClassLoader
        val normalizedPath = classpathPath.trimEnd('/')
        val resources = classLoader.getResources(normalizedPath).toList()
        require(resources.isNotEmpty()) {
            "No classpath resources found at '$normalizedPath'"
        }
        return resources.flatMap { url -> loadFromUrl(url, engine) }
    }

    private fun loadFromUrl(url: java.net.URL, engine: ProcessEngine): List<BpmnResource> {
        return when (url.protocol) {
            "file" -> loadFromFileSystem(Path.of(url.toURI()), engine)
            "jar" -> loadFromJar(url, engine)
            else -> error("Unsupported classpath protocol: ${url.protocol}")
        }
    }

    private fun loadFromFileSystem(path: Path, engine: ProcessEngine): List<BpmnResource> {
        if (Files.isRegularFile(path) && path.extension == "bpmn") {
            return listOf(
                BpmnResource(
                    fileName = path.name,
                    content = Files.newInputStream(path),
                    engine = engine,
                )
            )
        }
        require(Files.isDirectory(path)) {
            "Classpath resource is not a directory or .bpmn file: $path"
        }
        return Files.walk(path)
            .filter { it.extension == "bpmn" }
            .map { file ->
                BpmnResource(
                    fileName = file.name,
                    content = Files.newInputStream(file),
                    engine = engine,
                )
            }
            .toList()
    }

    private fun loadFromJar(url: java.net.URL, engine: ProcessEngine): List<BpmnResource> {
        val jarUri = url.toURI()
        val env = emptyMap<String, Any>()
        val fs = try {
            FileSystems.getFileSystem(jarUri)
        } catch (_: java.nio.file.FileSystemNotFoundException) {
            FileSystems.newFileSystem(jarUri, env)
        }
        val jarPath = fs.getPath(url.path.substringAfter("!"))
        return loadFromFileSystem(jarPath, engine)
    }
}
