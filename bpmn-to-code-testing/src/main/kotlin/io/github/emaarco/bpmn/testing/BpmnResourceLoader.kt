package io.github.emaarco.bpmn.testing

import io.github.emaarco.bpmn.domain.BpmnResource
import java.net.URL
import java.nio.file.FileSystems
import java.nio.file.FileSystemNotFoundException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.name
import kotlin.io.path.readBytes

/**
 * Loads `.bpmn` files from either the classpath or a filesystem directory.
 *
 * Supports both directories (walks recursively for `.bpmn` files) and
 * individual `.bpmn` file paths. Classpath loading uses
 * [Thread.currentThread().contextClassLoader] to resolve the given path,
 * and handles both expanded filesystem resources and JAR-packaged resources.
 */
internal object BpmnResourceLoader {

    fun fromClasspath(classpathPath: String): List<BpmnResource> {
        val classLoader = Thread.currentThread().contextClassLoader
        val normalizedPath = classpathPath.trimEnd('/')
        val resources = classLoader.getResources(normalizedPath).toList()
        require(resources.isNotEmpty()) {
            "No classpath resources found at '$normalizedPath'"
        }
        return resources.flatMap { url -> loadFromUrl(url) }
    }

    fun fromDirectory(directory: Path): List<BpmnResource> {
        require(Files.isDirectory(directory)) {
            "Path is not a directory: $directory"
        }
        return walkForBpmnFiles(directory)
    }

    private fun loadFromUrl(url: URL): List<BpmnResource> {
        return when (url.protocol) {
            "file" -> loadFromPath(Path.of(url.toURI()))
            "jar"  -> loadFromJar(url)
            else   -> error("Unsupported classpath protocol: ${url.protocol}")
        }
    }

    private fun loadFromPath(path: Path): List<BpmnResource> {
        if (Files.isRegularFile(path) && path.extension == "bpmn") {
            return listOf(BpmnResource(fileName = path.name, content = path.readBytes()))
        }
        require(Files.isDirectory(path)) {
            "Classpath resource is not a directory or .bpmn file: $path"
        }
        return walkForBpmnFiles(path)
    }

    /**
     * Loads BPMN files from inside a jar archive.
     *
     * Jar loading is required when the classpath resource is packaged inside a jar rather than
     * expanded onto the filesystem — for example, when `bpmn-to-code-testing` is used as a
     * library dependency and the user's BPMN test files are bundled in their own test jar.
     */
    private fun loadFromJar(url: URL): List<BpmnResource> {
        val jarUri = url.toURI()
        val subPath = url.path.substringAfter("!")
        return try {
            val existing = FileSystems.getFileSystem(jarUri)
            loadFromPath(existing.getPath(subPath))
        } catch (_: FileSystemNotFoundException) {
            FileSystems.newFileSystem(jarUri, emptyMap<String, Any>()).use { fs ->
                loadFromPath(fs.getPath(subPath))
            }
        }
    }

    private fun walkForBpmnFiles(directory: Path): List<BpmnResource> {
        return Files.walk(directory)
            .filter { it.extension == "bpmn" }
            .map { BpmnResource(fileName = it.name, content = it.readBytes()) }
            .toList()
    }
}
