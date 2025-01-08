package io.github.emaarco.bpmn.adapter.outbound.filesystem

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.UncheckedIOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * TODO: Check if the wildcard pattern is working & all cases are covered
 */
class BpmnFileLoaderTest {

    private val underTest = BpmnFileLoader()

    @Test
    fun `loadFrom returns empty list when no files match`(@TempDir tempDir: Path) {
        val result = underTest.loadFrom(tempDir.toString(), "*.bpmn")
        assertThat(result).isEmpty()
    }

    @Test
    fun `loadFrom returns matching files in base directory`(@TempDir tempDir: Path) {

        // given: a directory with files where some match the pattern
        val file1 = Files.createFile(tempDir.resolve("process1.bpmn")).toFile()
        val file2 = Files.createFile(tempDir.resolve("process2.bpmn")).toFile()
        Files.createFile(tempDir.resolve("other.txt"))

        // when: we call loadFrom with pattern "*.bpmn"
        val result = underTest.loadFrom(tempDir.toString(), "*.bpmn")

        // then: expect the list to contain only BPMN files
        assertThat(result).containsExactlyInAnyOrder(file1, file2)
    }

    @Test
    fun `loadFrom returns matching files from subdirectories`(@TempDir tempDir: Path) {

        // given: a base directory with a subdirectory containing a matching file
        val subDir = Files.createDirectory(tempDir.resolve("subDir"))
        val subSubDir = Files.createDirectory(subDir.resolve("subSubDir"))
        val subFile = Files.createFile(subDir.resolve("diagram.bpmn")).toFile()
        val otherSubFile = Files.createFile(subSubDir.resolve("process.bpmn")).toFile()
        Files.createFile(tempDir.resolve("process.txt"))

        // when: we call loadFrom with pattern "**/*.bpmn"
        val result = underTest.loadFrom(tempDir.toString(), "**/*.bpmn")

        // then: expect the list to contain the matching subdirectory file
        assertThat(result).containsExactlyInAnyOrder(subFile, otherSubFile)
    }

    @Test
    fun `loadFrom throws UncheckedIOException when base directory does not exist`() {
        val nonExistentDir = Paths.get(System.getProperty("java.io.tmpdir"), "nonexistent-${System.nanoTime()}")
        assertThatThrownBy { underTest.loadFrom(nonExistentDir.toString(), "*.bpmn") }
            .isInstanceOf(UncheckedIOException::class.java)
    }
}
