package io.github.emaarco.bpmn.web.service

import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.web.model.GenerateRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*

class WebGenerationServiceTest {

    private val service = WebGenerationService()

    private fun loadBpmnBase64(resourcePath: String): String {
        val bytes = javaClass.classLoader.getResourceAsStream(resourcePath)!!.readBytes()
        return Base64.getEncoder().encodeToString(bytes)
    }

    @Test
    fun `should generate Kotlin API from BPMN file`() {
        val base64Content = loadBpmnBase64("bpmn/c8-newsletter.bpmn")

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
        assertThat(response.success).describedAs("Generation should succeed").isTrue()
        assertThat(response.files).describedAs("Should generate at least one file").isNotEmpty()
        assertThat(response.error).describedAs("Should not have errors").isNull()

        val generatedFile = response.files.first()
        assertThat(generatedFile.fileName).describedAs("Should generate Kotlin file").endsWith(".kt")
        assertThat(generatedFile.content).describedAs("Should contain Kotlin object declaration").contains("object")
        assertThat(generatedFile.content).describedAs("Should contain process ID").contains("newsletterSubscription")
    }

    @Test
    fun `should generate Java API from BPMN file`() {
        val base64Content = loadBpmnBase64("bpmn/c8-newsletter.bpmn")

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

        assertThat(response.success).isTrue()
        assertThat(response.files).isNotEmpty()

        val generatedFile = response.files.first()
        assertThat(generatedFile.fileName).describedAs("Should generate Java file").endsWith(".java")
        assertThat(generatedFile.content).describedAs("Should contain Java class declaration").contains("class")
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

        assertThat(response.success).isFalse()
        assertThat(response.error).isNotNull()
        assertThat(response.files).isEmpty()
    }

    @Test
    fun `should process up to 3 BPMN files successfully`() {
        // Note: The 3-file limit is enforced at the route layer, not service layer
        // This test verifies the service can handle 3 files without errors
        val c8Base64 = loadBpmnBase64("bpmn/c8-newsletter.bpmn")

        val request = GenerateRequest(
            files = listOf(
                GenerateRequest.BpmnFileData("c8-newsletter.bpmn", c8Base64),
                GenerateRequest.BpmnFileData("c8-newsletter-copy1.bpmn", c8Base64),
                GenerateRequest.BpmnFileData("c8-newsletter-copy2.bpmn", c8Base64)
            ),
            config = GenerateRequest.GenerationConfig(
                outputLanguage = OutputLanguage.KOTLIN,
                processEngine = ProcessEngine.ZEEBE
            )
        )

        val response = service.generate(request)

        assertThat(response.error).isNull()
        assertThat(response.success).describedAs("Should successfully process 3 files").isTrue()
        assertThat(response.files).describedAs("Should generate at least one API file").isNotEmpty()
    }
}
