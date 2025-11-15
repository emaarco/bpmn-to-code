package io.github.emaarco.bpmn.web.service

import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.web.model.BpmnFileData
import io.github.emaarco.bpmn.web.model.GenerateRequest
import io.github.emaarco.bpmn.web.model.GenerationConfig
import org.junit.jupiter.api.Test
import java.io.File
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WebGenerationServiceTest {

    private val service = WebGenerationService()

    @Test
    fun `should generate Kotlin API from BPMN file`() {
        // Get sample BPMN from test resources
        val bpmnFile = File("../bpmn-to-code-core/src/test/resources/bpmn/c8-newsletter.bpmn")
        assertTrue(bpmnFile.exists(), "Sample BPMN file should exist")

        val bpmnContent = bpmnFile.readBytes()
        val base64Content = Base64.getEncoder().encodeToString(bpmnContent)

        val request = GenerateRequest(
            files = listOf(
                BpmnFileData(
                    fileName = "c8-newsletter.bpmn",
                    content = base64Content
                )
            ),
            config = GenerationConfig(
                outputLanguage = OutputLanguage.KOTLIN,
                processEngine = ProcessEngine.ZEEBE
            )
        )

        // Execute
        val response = service.generate(request)

        // Verify
        assertTrue(response.success, "Generation should succeed")
        assertTrue(response.files.isNotEmpty(), "Should generate at least one file")
        assertTrue(response.error == null, "Should not have errors")

        val generatedFile = response.files.first()
        assertTrue(generatedFile.fileName.endsWith(".kt"), "Should generate Kotlin file")
        assertTrue(generatedFile.content.contains("object"), "Should contain Kotlin object declaration")
        assertTrue(generatedFile.content.contains("newsletterSubscription"), "Should contain process ID")

        println("Generated file: ${generatedFile.fileName}")
        println("Content preview: ${generatedFile.content.take(200)}...")
    }

    @Test
    fun `should generate Java API from BPMN file`() {
        val bpmnFile = File("../bpmn-to-code-core/src/test/resources/bpmn/c8-newsletter.bpmn")
        val bpmnContent = bpmnFile.readBytes()
        val base64Content = Base64.getEncoder().encodeToString(bpmnContent)

        val request = GenerateRequest(
            files = listOf(
                BpmnFileData(
                    fileName = "c8-newsletter.bpmn",
                    content = base64Content
                )
            ),
            config = GenerationConfig(
                outputLanguage = OutputLanguage.JAVA,
                processEngine = ProcessEngine.ZEEBE
            )
        )

        val response = service.generate(request)

        assertTrue(response.success)
        assertTrue(response.files.isNotEmpty())

        val generatedFile = response.files.first()
        assertTrue(generatedFile.fileName.endsWith(".java"), "Should generate Java file")
        assertTrue(generatedFile.content.contains("class"), "Should contain Java class declaration")
    }

    @Test
    fun `should handle invalid Base64 content gracefully`() {
        val request = GenerateRequest(
            files = listOf(
                BpmnFileData(
                    fileName = "invalid.bpmn",
                    content = "not-valid-base64!!!"
                )
            ),
            config = GenerationConfig(
                outputLanguage = OutputLanguage.KOTLIN,
                processEngine = ProcessEngine.ZEEBE
            )
        )

        val response = service.generate(request)

        assertEquals(false, response.success)
        assertTrue(response.error != null)
        assertTrue(response.files.isEmpty())
    }
}
