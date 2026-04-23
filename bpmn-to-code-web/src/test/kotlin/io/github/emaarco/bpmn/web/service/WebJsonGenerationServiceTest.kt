package io.github.emaarco.bpmn.web.service

import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.web.model.GenerateJsonRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*

class WebJsonGenerationServiceTest {

    private val underTest = WebJsonGenerationService()

    @Test
    fun `should generate JSON from sample BPMN file`() {

        // given: the sample BPMN served by the web app
        val request = GenerateJsonRequest(
            files = listOf(
                GenerateJsonRequest.BpmnFileData(
                    fileName = "c8-newsletter.bpmn",
                    content = loadSampleBase64("samples/c8-newsletter.bpmn"),
                )
            ),
            config = GenerateJsonRequest.JsonGenerationConfig(
                processEngine = ProcessEngine.ZEEBE,
            )
        )

        // when: generating JSON
        val response = underTest.generate(request)

        // then: generation succeeds
        assertThat(response.success).describedAs("Generation should succeed but got: ${response.error}").isTrue()
        assertThat(response.files).isNotEmpty()
        assertThat(response.error).isNull()
        val file = response.files.first()
        assertThat(file.fileName).endsWith(".json")
        assertThat(file.processId).isEqualTo("newsletterSubscription")
    }

    private fun loadSampleBase64(resourcePath: String): String {
        val bytes = requireNotNull(javaClass.classLoader.getResourceAsStream(resourcePath)) {
            "Could not find resource: $resourcePath"
        }.readBytes()
        return Base64.getEncoder().encodeToString(bytes)
    }
}
