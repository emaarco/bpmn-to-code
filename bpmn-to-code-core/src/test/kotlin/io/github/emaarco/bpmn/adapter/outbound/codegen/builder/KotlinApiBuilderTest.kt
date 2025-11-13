package io.github.emaarco.bpmn.adapter.outbound.codegen.builder

import io.github.emaarco.bpmn.domain.shared.VariableDefinition
import io.github.emaarco.bpmn.domain.testBpmnModelApi
import io.github.emaarco.bpmn.domain.testNewsletterBpmnModel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class KotlinApiBuilderTest {

    private val underTest = KotlinApiBuilder()

    @Test
    fun `buildApiFile generates correct API file content`(@TempDir tempDir: Path) {

        // given: a BPMN model and a model API
        val modelApi = testBpmnModelApi(
            apiVersion = 1,
            outputFolder = tempDir.toFile(),
            packagePath = "de.emaarco.example",
            model = testNewsletterBpmnModel(
                variables = listOf(
                    VariableDefinition("subscriptionId"),
                    VariableDefinition("testVariable")
                )
            ),
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
