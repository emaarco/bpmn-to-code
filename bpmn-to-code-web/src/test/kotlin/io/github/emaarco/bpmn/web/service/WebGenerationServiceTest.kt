package io.github.emaarco.bpmn.web.service

import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.web.model.GenerateRequest
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
                GenerateRequest.BpmnFileData(
                    fileName = "c8-newsletter.bpmn",
                    content = base64Content
                )
            ),
            config = GenerateRequest.GenerationConfig(
                outputLanguage = OutputLanguage.KOTLIN,
                processEngine = ProcessEngine.ZEEBE
            )
        )

        // Execute
        val response = service.generate(request)

        // Verify
        assertTrue(response.success, "Generation should succeed")
        assertTrue(response.files.isNotEmpty(), "Should generate at least one file")
        assertEquals(response.error, null, "Should not have errors")

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
                GenerateRequest.BpmnFileData(
                    fileName = "c8-newsletter.bpmn",
                    content = base64Content
                )
            ),
            config = GenerateRequest.GenerationConfig(
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
                GenerateRequest.BpmnFileData(
                    fileName = "invalid.bpmn",
                    content = "not-valid-base64!!!"
                )
            ),
            config = GenerateRequest.GenerationConfig(
                outputLanguage = OutputLanguage.KOTLIN,
                processEngine = ProcessEngine.ZEEBE
            )
        )

        val response = service.generate(request)

        assertEquals(false, response.success)
        assertTrue(response.error != null)
        assertTrue(response.files.isEmpty())
    }

    @Test
    fun `should process up to 3 BPMN files successfully`() {
        // Note: The 3-file limit is enforced at the route layer, not service layer
        // This test verifies the service can handle 3 files without errors
        val c8File = File("../bpmn-to-code-core/src/test/resources/bpmn/c8-newsletter.bpmn")
        val c7File = File("../bpmn-to-code-core/src/test/resources/bpmn/c7-newsletter.bpmn")

        val c8Base64 = Base64.getEncoder().encodeToString(c8File.readBytes())
        val c7Base64 = Base64.getEncoder().encodeToString(c7File.readBytes())

        val request = GenerateRequest(
            files = listOf(
                GenerateRequest.BpmnFileData("c8-newsletter.bpmn", c8Base64),
                GenerateRequest.BpmnFileData("c7-newsletter.bpmn", c7Base64),
                GenerateRequest.BpmnFileData("c8-newsletter-copy.bpmn", c8Base64)
            ),
            config = GenerateRequest.GenerationConfig(
                outputLanguage = OutputLanguage.KOTLIN,
                processEngine = ProcessEngine.ZEEBE
            )
        )

        val response = service.generate(request)

        assertTrue(response.success, "Should successfully process 3 files")
        assertTrue(response.files.isNotEmpty(), "Should generate at least one API file")
        assertEquals(response.error, null)
    }
}
