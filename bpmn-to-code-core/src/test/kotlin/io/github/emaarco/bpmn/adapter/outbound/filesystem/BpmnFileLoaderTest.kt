package io.github.emaarco.bpmn.adapter.outbound.filesystem

import io.github.emaarco.bpmn.adapter.logger.NoOpLoggerAdapter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class BpmnFileLoaderTest {

    private val logger = NoOpLoggerAdapter()
    private val underTest = BpmnFileLoader(logger)

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
    fun `loadFrom returns matching files from outside current root using relative paths`(@TempDir tempDir: Path) {

        // given: a directory structure with files outside the base directory
        val baseDir = Files.createDirectory(tempDir.resolve("project"))
        val externalDir = Files.createDirectory(tempDir.resolve("external"))
        val externalFile = Files.createFile(externalDir.resolve("external-process.bpmn")).toFile()
        Files.createFile(externalDir.resolve("other.txt"))

        // when: we call loadFrom with a relative path pattern going outside the root
        val result = underTest.loadFrom(baseDir.toString(), "../external/*.bpmn")

        // then: expect the list to contain the external BPMN file
        assertThat(result).containsExactly(externalFile)
    }

    @Test
    fun `loadFrom returns matching files from external subdirectories using recursive wildcard`(@TempDir tempDir: Path) {

        // given: a directory structure with nested external files
        val baseDir = Files.createDirectory(tempDir.resolve("project"))
        val externalDir = Files.createDirectory(tempDir.resolve("external"))
        val subDir1 = Files.createDirectory(externalDir.resolve("subdir1"))
        val subDir2 = Files.createDirectory(externalDir.resolve("subdir2"))
        val deepDir = Files.createDirectory(subDir1.resolve("deep"))

        val externalFile1 = Files.createFile(externalDir.resolve("root-process.bpmn")).toFile()
        val externalFile2 = Files.createFile(subDir1.resolve("sub1-process.bpmn")).toFile()
        val externalFile3 = Files.createFile(subDir2.resolve("sub2-process.bpmn")).toFile()
        val externalFile4 = Files.createFile(deepDir.resolve("deep-process.bpmn")).toFile()
        Files.createFile(externalDir.resolve("other.txt"))
        Files.createFile(subDir1.resolve("readme.md"))

        // when: we call loadFrom with a recursive wildcard pattern
        val result = underTest.loadFrom(baseDir.toString(), "../external/**/*.bpmn")

        // then: expect the list to contain all BPMN files from external directory tree
        assertThat(result).containsExactlyInAnyOrder(externalFile1, externalFile2, externalFile3, externalFile4)
    }

    @Test
    fun `loadFrom returns matching files from deeply nested external paths`(@TempDir tempDir: Path) {

        // given: a directory structure with deeply nested external files
        val projectDir = Files.createDirectory(tempDir.resolve("workspace"))
        val baseDir = Files.createDirectory(projectDir.resolve("current"))
        val deepDir = Files.createDirectory(tempDir.resolve("shared"))
        val nestedDir = Files.createDirectory(deepDir.resolve("resources"))
        val bpmnDir = Files.createDirectory(nestedDir.resolve("bpmn"))

        val deepFile1 = Files.createFile(bpmnDir.resolve("shared-process.bpmn")).toFile()
        val deepFile2 = Files.createFile(nestedDir.resolve("resource-process.bpmn")).toFile()
        Files.createFile(bpmnDir.resolve("config.xml"))

        // when: we call loadFrom with a deep relative path pattern
        val result = underTest.loadFrom(baseDir.toString(), "../../shared/**/*.bpmn")

        // then: expect the list to contain all BPMN files from the deep external path
        assertThat(result).containsExactlyInAnyOrder(deepFile1, deepFile2)
    }

    @Test
    fun `loadFrom returns specific external file when exact path is provided`(@TempDir tempDir: Path) {

        // given: a directory structure with specific external files
        val baseDir = Files.createDirectory(tempDir.resolve("project"))
        val externalDir = Files.createDirectory(tempDir.resolve("external"))
        val specificDir = Files.createDirectory(externalDir.resolve("specific"))

        val targetFile = Files.createFile(specificDir.resolve("target.bpmn")).toFile()
        Files.createFile(specificDir.resolve("other.bpmn"))
        Files.createFile(externalDir.resolve("different.bpmn"))

        // when: we call loadFrom with a specific file pattern
        val result = underTest.loadFrom(baseDir.toString(), "../external/specific/target.bpmn")

        // then: expect the list to contain only the specific target file
        assertThat(result).containsExactly(targetFile)
    }
}
