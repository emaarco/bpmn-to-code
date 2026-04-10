package io.github.emaarco.bpmn.testing

import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class BpmnResourceLoaderTest {

    @Test
    fun `fromClasspath loads bpmn files from classpath directory`() {
        val resources = BpmnResourceLoader.fromClasspath("bpmn", ProcessEngine.CAMUNDA_7)
        assertThat(resources).isNotEmpty
        assertThat(resources).allMatch { it.fileName.endsWith(".bpmn") }
    }

    @Test
    fun `fromClasspath sets the correct engine on loaded resources`() {
        val resources = BpmnResourceLoader.fromClasspath("bpmn", ProcessEngine.ZEEBE)
        assertThat(resources).allMatch { it.engine == ProcessEngine.ZEEBE }
    }

    @Test
    fun `fromClasspath throws on missing classpath path`() {
        assertThatThrownBy {
            BpmnResourceLoader.fromClasspath("nonexistent/path", ProcessEngine.CAMUNDA_7)
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("No classpath resources found")
    }

    @Test
    fun `fromClasspath handles trailing slash in path`() {
        val resources = BpmnResourceLoader.fromClasspath("bpmn/", ProcessEngine.CAMUNDA_7)
        assertThat(resources).isNotEmpty
    }

    @Test
    fun `fromDirectory loads all bpmn files recursively`(@TempDir tempDir: Path) {
        val subDir = Files.createDirectory(tempDir.resolve("sub"))
        Files.createFile(tempDir.resolve("root.bpmn"))
        Files.createFile(subDir.resolve("nested.bpmn"))
        Files.createFile(tempDir.resolve("other.xml"))

        val resources = BpmnResourceLoader.fromDirectory(tempDir, ProcessEngine.ZEEBE)

        assertThat(resources.map { it.fileName }).containsExactlyInAnyOrder("root.bpmn", "nested.bpmn")
    }

    @Test
    fun `fromDirectory sets the correct engine on loaded resources`(@TempDir tempDir: Path) {
        Files.createFile(tempDir.resolve("process.bpmn"))

        val resources = BpmnResourceLoader.fromDirectory(tempDir, ProcessEngine.CAMUNDA_7)

        assertThat(resources).allMatch { it.engine == ProcessEngine.CAMUNDA_7 }
    }

    @Test
    fun `fromDirectory throws when path is not a directory`(@TempDir tempDir: Path) {
        val file = Files.createFile(tempDir.resolve("process.bpmn"))

        assertThatThrownBy {
            BpmnResourceLoader.fromDirectory(file, ProcessEngine.ZEEBE)
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Path is not a directory")
    }
}
