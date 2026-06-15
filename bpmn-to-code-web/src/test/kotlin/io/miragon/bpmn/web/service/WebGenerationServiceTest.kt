package io.miragon.bpmn.web.service

import io.miragon.bpmn.domain.shared.OutputLanguage
import io.miragon.bpmn.domain.shared.ProcessEngine
import io.miragon.bpmn.web.model.GenerateRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.Base64

class WebGenerationServiceTest {

    private val underTest = WebGenerationService()

    @Test
    fun `should generate Kotlin API from BPMN file`() {

        // given: a valid Zeebe BPMN file encoded as Base64
        val request = GenerateRequest(
            files = listOf(
                GenerateRequest.BpmnFileData(
                    fileName = "c8-subscribe-newsletter.bpmn",
                    content = loadBpmnBase64("bpmn/c8-subscribe-newsletter.bpmn"),
                )
            ),
            config = GenerateRequest.GenerationConfig(
                outputLanguage = OutputLanguage.KOTLIN,
                processEngine = ProcessEngine.ZEEBE,
            )
        )

        // when: generating the API
        val response = underTest.generate(request)

        // then: a Kotlin file is generated containing the process constant
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

        // given: a valid Zeebe BPMN file with Java output language
        val request = GenerateRequest(
            files = listOf(
                GenerateRequest.BpmnFileData(
                    fileName = "c8-subscribe-newsletter.bpmn",
                    content = loadBpmnBase64("bpmn/c8-subscribe-newsletter.bpmn"),
                )
            ),
            config = GenerateRequest.GenerationConfig(
                outputLanguage = OutputLanguage.JAVA,
                processEngine = ProcessEngine.ZEEBE,
            )
        )

        // when: generating the API
        val response = underTest.generate(request)

        // then: a Java class file is generated
        assertThat(response.success).isTrue()
        assertThat(response.files).isNotEmpty()
        val generatedFile = response.files.first()
        assertThat(generatedFile.fileName).describedAs("Should generate Java file").endsWith(".java")
        assertThat(generatedFile.content).describedAs("Should contain Java class declaration").contains("class")
    }

    @Test
    fun `should reject a model whose target engine does not match the selected engine`() {

        // given: a Zeebe model but Camunda 7 selected (the demo's original failure mode)
        val request = GenerateRequest(
            files = listOf(
                GenerateRequest.BpmnFileData(
                    fileName = "c8-subscribe-newsletter.bpmn",
                    content = loadBpmnBase64("bpmn/c8-subscribe-newsletter.bpmn"),
                )
            ),
            config = GenerateRequest.GenerationConfig(
                outputLanguage = OutputLanguage.KOTLIN,
                processEngine = ProcessEngine.CAMUNDA_7,
            )
        )

        // when: generating the API
        val response = underTest.generate(request)

        // then: the engine-mismatch error is reported (collected alongside any other problems)
        assertThat(response.success).isFalse()
        assertThat(response.files).isEmpty()
        assertThat(response.error)
            .contains("engine-mismatch", "targets Zeebe (Camunda 8)", "selected engine is Camunda 7")
    }

    @Test
    fun `should reject a Camunda 7 model when Operaton is selected`() {

        // given: a Camunda 7 model but Operaton selected (the reported case)
        val request = GenerateRequest(
            files = listOf(
                GenerateRequest.BpmnFileData(
                    fileName = "c7-subscribe-newsletter.bpmn",
                    content = loadBpmnBase64("bpmn/c7-subscribe-newsletter.bpmn"),
                )
            ),
            config = GenerateRequest.GenerationConfig(
                outputLanguage = OutputLanguage.KOTLIN,
                processEngine = ProcessEngine.OPERATON,
            )
        )

        // when: generating the API
        val response = underTest.generate(request)

        // then: surfaced as an engine-mismatch error, not a swallowed warning
        assertThat(response.success).isFalse()
        assertThat(response.error).contains("targets Camunda 7", "selected engine is Operaton")
    }

    @Test
    fun `should handle invalid Base64 content gracefully`() {

        // given: a request with invalid Base64 content
        val request = GenerateRequest(
            files = listOf(
                GenerateRequest.BpmnFileData(
                    fileName = "invalid.bpmn",
                    content = "not-valid-base64!!!",
                )
            ),
            config = GenerateRequest.GenerationConfig(
                outputLanguage = OutputLanguage.KOTLIN,
                processEngine = ProcessEngine.ZEEBE,
            )
        )

        // when: generating the API
        val response = underTest.generate(request)

        // then: the response indicates failure with an error message
        assertThat(response.success).isFalse()
        assertThat(response.error).isNotNull()
        assertThat(response.files).isEmpty()
    }

    @Test
    fun `should process up to 3 BPMN files successfully`() {

        // given: a request with 3 identical BPMN files
        val c8Base64 = loadBpmnBase64("bpmn/c8-subscribe-newsletter.bpmn")
        val request = GenerateRequest(
            files = listOf(
                GenerateRequest.BpmnFileData(fileName = "c8-subscribe-newsletter.bpmn", content = c8Base64),
                GenerateRequest.BpmnFileData(fileName = "c8-newsletter-copy1.bpmn", content = c8Base64),
                GenerateRequest.BpmnFileData(fileName = "c8-newsletter-copy2.bpmn", content = c8Base64),
            ),
            config = GenerateRequest.GenerationConfig(
                outputLanguage = OutputLanguage.KOTLIN,
                processEngine = ProcessEngine.ZEEBE,
            )
        )

        // when: generating the API for all files
        val response = underTest.generate(request)

        // then: generation succeeds and produces at least one file
        assertThat(response.error).isNull()
        assertThat(response.success).describedAs("Should successfully process 3 files").isTrue()
        assertThat(response.files).describedAs("Should generate at least one API file").isNotEmpty()
    }

    private fun loadBpmnBase64(resourcePath: String): String {
        val bytes = javaClass.classLoader.getResourceAsStream(resourcePath)!!.readBytes()
        return Base64.getEncoder().encodeToString(bytes)
    }
}
