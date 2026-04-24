package io.github.emaarco.bpmn.web.service

import io.github.emaarco.bpmn.web.model.GenerateResponse
import java.util.Properties

/**
 * Loads the hand-written `bpmn-to-code-runtime` Kotlin sources that the web module bundles as resources
 * (copied in via `copyLibrarySources` during the build). The playground exposes these so users can
 * preview the shared types alongside their generated Process API and, if they opt in, include them
 * in the ZIP download for a self-contained bundle.
 */
class LibrarySourceProvider {

    private val cache: List<GenerateResponse.GeneratedFile> by lazy { loadLibraryFiles() }
    private val version: String by lazy { loadProjectVersion() }

    fun libraryFiles(): List<GenerateResponse.GeneratedFile> {
        return cache
    }

    fun runtimeDependency(): GenerateResponse.RuntimeDependency {
        val group = "io.github.emaarco"
        val artifact = "bpmn-to-code-runtime"
        val currentVersion = version
        return GenerateResponse.RuntimeDependency(
            group = group,
            artifact = artifact,
            version = currentVersion,
            gradleSnippet = "implementation(\"$group:$artifact:$currentVersion\")",
            mavenSnippet = """
                |<dependency>
                |    <groupId>$group</groupId>
                |    <artifactId>$artifact</artifactId>
                |    <version>$currentVersion</version>
                |</dependency>
            """.trimMargin(),
        )
    }

    private fun loadLibraryFiles(): List<GenerateResponse.GeneratedFile> {
        val manifest = readResource("library-sources/manifest.txt") ?: return emptyList()
        val fileNames = manifest.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.toList()
        return fileNames.mapNotNull { name ->
            val content = readResource("library-sources/$name") ?: return@mapNotNull null
            GenerateResponse.GeneratedFile(
                fileName = name,
                content = content,
                processId = "bpmn-to-code-runtime",
            )
        }
    }

    private fun loadProjectVersion(): String {
        val stream = javaClass.classLoader.getResourceAsStream("version.properties") ?: return "unknown"
        val properties = stream.use {
            Properties().apply { load(it) }
        }
        return properties.getProperty("projectVersion")?.takeIf { it.isNotBlank() } ?: "unknown"
    }

    private fun readResource(path: String): String? {
        val stream = javaClass.classLoader.getResourceAsStream(path) ?: return null
        return stream.use { it.readBytes().toString(Charsets.UTF_8) }
    }
}
