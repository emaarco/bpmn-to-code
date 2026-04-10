package io.github.emaarco.bpmn.testing

import io.github.emaarco.bpmn.domain.BpmnResource
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import java.net.URL
import java.nio.file.FileSystems
import java.nio.file.FileSystemNotFoundException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.name

/**
 * Loads `.bpmn` files from either the classpath or a filesystem directory.
 *
 * Supports both directories (walks recursively for `.bpmn` files) and
 * individual `.bpmn` file paths. Classpath loading uses
 * [Thread.currentThread().contextClassLoader] to resolve the given path,
 * and handles both expanded filesystem resources and JAR-packaged resources.
 */
internal object BpmnResourceLoader {

    fun fromClasspath(classpathPath: String, engine: ProcessEngine): List<BpmnResource> {
        val classLoader = Thread.currentThread().contextClassLoader
        val normalizedPath = classpathPath.trimEnd('/')
        val resources = classLoader.getResources(normalizedPath).toList()
        require(resources.isNotEmpty()) {
            "No classpath resources found at '$normalizedPath'"
        }
        return resources.flatMap { url -> loadFromUrl(url, engine) }
    }

    fun fromDirectory(directory: Path, engine: ProcessEngine): List<BpmnResource> {
        require(Files.isDirectory(directory)) {
            "Path is not a directory: $directory"
        }
        return walkForBpmnFiles(directory, engine)
    }

    private fun loadFromUrl(url: URL, engine: ProcessEngine): List<BpmnResource> {
        return when (url.protocol) {
            "file" -> loadFromPath(Path.of(url.toURI()), engine)
            "jar"  -> loadFromJar(url, engine)
            else   -> error("Unsupported classpath protocol: ${url.protocol}")
        }
    }

    private fun loadFromPath(path: Path, engine: ProcessEngine): List<BpmnResource> {
        if (Files.isRegularFile(path) && path.extension == "bpmn") {
            return listOf(BpmnResource(fileName = path.name, content = Files.newInputStream(path), engine = engine))
        }
        require(Files.isDirectory(path)) {
            "Classpath resource is not a directory or .bpmn file: $path"
        }
        return walkForBpmnFiles(path, engine)
    }

    /**
     * Loads BPMN files from inside a jar archive.
     *
     * Jar loading is required when the classpath resource is packaged inside a jar rather than
     * expanded onto the filesystem — for example, when `bpmn-to-code-testing` is used as a
     * library dependency and the user's BPMN test files are bundled in their own test jar.
     */
    private fun loadFromJar(url: URL, engine: ProcessEngine): List<BpmnResource> {
        val jarUri = url.toURI()
        val fs = try {
            FileSystems.getFileSystem(jarUri)
        } catch (_: FileSystemNotFoundException) {
            FileSystems.newFileSystem(jarUri, emptyMap<String, Any>())
        }
        return loadFromPath(fs.getPath(url.path.substringAfter("!")), engine)
    }

    private fun walkForBpmnFiles(directory: Path, engine: ProcessEngine): List<BpmnResource> {
        return Files.walk(directory)
            .filter { it.extension == "bpmn" }
            .map { BpmnResource(fileName = it.name, content = Files.newInputStream(it), engine = engine) }
            .toList()
    }
}
