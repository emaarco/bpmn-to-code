package io.github.emaarco.bpmn.adapter.outbound.codegen.builder

import io.github.emaarco.bpmn.domain.testBpmnModelApi
import io.github.emaarco.bpmn.domain.testNewsletterBpmnModel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class KotlinApiBuilderTest {

    private val underTest = KotlinApiBuilder()

    @Test
    fun `buildApiFile generates correct API file content`(@TempDir tempDir: java.nio.file.Path) {

        val modelApi = testBpmnModelApi(
            model = testNewsletterBpmnModel(),
            apiVersion = 1,
            outputFolder = tempDir.toFile(),
            packagePath = "de.emaarco.example"
        )

        // when: we build the API file
        underTest.buildApiFile(modelApi)

        // then: expect the generated file to contain the expected content
        val generatedFile = File(tempDir.toFile(), "de/emaarco/example/${modelApi.fileName()}.kt")
        val expectedFile = File(javaClass.getResource("/api/NewsletterSubscriptionProcessApiKotlin.txt").toURI())
        val generatedContent = generatedFile.readText()
        val expectedContent = expectedFile.readText()
        assertThat(generatedContent).isEqualToIgnoringWhitespace(expectedContent)
    }

}
