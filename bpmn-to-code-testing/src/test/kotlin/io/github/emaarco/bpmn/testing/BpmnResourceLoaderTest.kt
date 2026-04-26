package io.github.emaarco.bpmn.testing

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.FileOutputStream
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Path
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream

class BpmnResourceLoaderTest {

    @Test
    fun `fromClasspath loads bpmn files from classpath directory`() {
        val resources = BpmnResourceLoader.fromClasspath("bpmn")
        assertThat(resources).isNotEmpty
        assertThat(resources).allMatch { it.fileName.endsWith(".bpmn") }
    }

    @Test
    fun `fromClasspath throws on missing classpath path`() {
        assertThatThrownBy { BpmnResourceLoader.fromClasspath("nonexistent/path") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("No classpath resources found")
    }

    @Test
    fun `fromClasspath handles trailing slash in path`() {
        val resources = BpmnResourceLoader.fromClasspath("bpmn/")
        assertThat(resources).isNotEmpty
    }

    @Test
    fun `fromDirectory loads all bpmn files recursively`(@TempDir tempDir: Path) {

        // given: a directory with BPMN files in root and subdirectory, plus a non-BPMN file
        val subDir = Files.createDirectory(tempDir.resolve("sub"))
        Files.createFile(tempDir.resolve("root.bpmn"))
        Files.createFile(subDir.resolve("nested.bpmn"))
        Files.createFile(tempDir.resolve("other.xml"))

        // when: loading from the directory
        val resources = BpmnResourceLoader.fromDirectory(tempDir)

        // then: only the BPMN files are returned
        assertThat(resources.map { it.fileName }).containsExactlyInAnyOrder("root.bpmn", "nested.bpmn")
    }

    @Test
    fun `fromDirectory throws when path is not a directory`(@TempDir tempDir: Path) {

        // given: a regular file (not a directory)
        val file = Files.createFile(tempDir.resolve("process.bpmn"))

        // when / then: an exception is thrown
        assertThatThrownBy { BpmnResourceLoader.fromDirectory(file) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Path is not a directory")
    }

    @Test
    fun `fromClasspath loads single bpmn file by direct classpath path`() {
        val resources = BpmnResourceLoader.fromClasspath("bpmn/valid-process.bpmn")
        assertThat(resources).hasSize(1)
        assertThat(resources.first().fileName).isEqualTo("valid-process.bpmn")
    }

    @Test
    fun `fromClasspath loads bpmn files from jar archive`(@TempDir tempDir: Path) {

        // given: a JAR containing a .bpmn file
        val jarPath = tempDir.resolve("test-resources.jar")
        JarOutputStream(FileOutputStream(jarPath.toFile())).use { jar ->
            jar.putNextEntry(JarEntry("bpmn-in-jar/"))
            jar.closeEntry()
            jar.putNextEntry(JarEntry("bpmn-in-jar/process.bpmn"))
            jar.write("<definitions/>".toByteArray())
            jar.closeEntry()
        }

        // given: a class loader that sees the JAR on the classpath
        val classLoader = URLClassLoader(
            arrayOf(jarPath.toUri().toURL()),
            Thread.currentThread().contextClassLoader,
        )

        // when: loading from the JAR-backed classpath path
        val previous = Thread.currentThread().contextClassLoader
        Thread.currentThread().contextClassLoader = classLoader
        try {
            val resources = BpmnResourceLoader.fromClasspath("bpmn-in-jar")
            assertThat(resources).hasSize(1)
            assertThat(resources.first().fileName).isEqualTo("process.bpmn")
        } finally {
            Thread.currentThread().contextClassLoader = previous
        }
    }
}
