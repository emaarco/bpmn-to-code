package io.github.emaarco.bpmn.adapter.outbound.filesystem

import io.github.emaarco.bpmn.domain.GeneratedJsonFile
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ProcessJsonFileSaverTest {

    private val underTest = ProcessJsonFileSaver()

    @Test
    fun `writeFiles writes multiple files to output folder`(@TempDir tempDir: File) {

        // given: multiple generated JSON files
        val firstFile = GeneratedJsonFile(fileName = "order.json", content = """{"process":"order"}""")
        val secondFile = GeneratedJsonFile(fileName = "payment.json", content = """{"process":"payment"}""")

        // when: writeFiles is called
        underTest.writeFiles(listOf(firstFile, secondFile), tempDir.absolutePath)

        // then: both files are written with correct content
        val orderFile = File(tempDir, "order.json")
        val paymentFile = File(tempDir, "payment.json")

        assertThat(orderFile.exists()).isTrue()
        assertThat(paymentFile.exists()).isTrue()
        assertThat(orderFile.readText()).isEqualTo("""{"process":"order"}""")
        assertThat(paymentFile.readText()).isEqualTo("""{"process":"payment"}""")
    }

    @Test
    fun `writeFiles creates output folder if it does not exist`(@TempDir tempDir: File) {

        // given: a non-existent output subfolder
        val newFolder = File(tempDir, "generated/json")
        val file = GeneratedJsonFile(fileName = "order.json", content = "{}")

        // when: writeFiles is called with the new folder path
        underTest.writeFiles(listOf(file), newFolder.absolutePath)

        // then: the folder is created and the file is written
        assertThat(newFolder.exists()).isTrue()
        assertThat(File(newFolder, "order.json").exists()).isTrue()
    }
}
