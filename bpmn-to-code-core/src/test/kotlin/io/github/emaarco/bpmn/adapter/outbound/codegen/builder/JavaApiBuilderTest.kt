package io.github.emaarco.bpmn.adapter.outbound.codegen.builder

import io.github.emaarco.bpmn.domain.testBpmnModelApi
import io.github.emaarco.bpmn.domain.testNewsletterBpmnModel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class JavaApiBuilderTest {

    private val underTest = JavaApiBuilder()

    @Test
    fun `buildApiFile generates correct API file content`(@TempDir tempDir: Path) {

        val modelApi = testBpmnModelApi(
            model = testNewsletterBpmnModel(
                variables = listOf(
                    io.github.emaarco.bpmn.domain.shared.VariableDefinition("subscriptionId"),
                    io.github.emaarco.bpmn.domain.shared.VariableDefinition("testVariable")
                )
            ),
            apiVersion = 1,
            outputFolder = tempDir.toFile(),
            packagePath = "de.emaarco.example"
        )

        // when: we build the API file
        underTest.buildApiFile(modelApi)

        // then: expect the generated file to contain the expected content
        val generatedFile = File(tempDir.toFile(), "de/emaarco/example/${modelApi.fileName()}.java")
        val expectedFile = File(javaClass.getResource("/api/NewsletterSubscriptionProcessApiJava.txt").toURI())
        val generatedContent = generatedFile.readText()
        val expectedContent = expectedFile.readText()
        assertThat(generatedContent).isEqualToIgnoringWhitespace(expectedContent)
    }

    @Test
    fun `maps content of id to valid variable name format`(@TempDir tempDir: Path) {

        // given: a model with flow nodes that have slashes in their names
        val defaultModel = testNewsletterBpmnModel()
        val modifiedNodes = defaultModel.flowNodes.map { it.copy(id = it.getName().replace("_", "-")) }
        val modelApi = testBpmnModelApi(
            model = testNewsletterBpmnModel(flowNodes = modifiedNodes),
            apiVersion = 1,
            outputFolder = tempDir.toFile(),
            packagePath = "de.emaarco.example"
        )

        // when: we build the API file
        underTest.buildApiFile(modelApi)

        // then: expect the generated file to contain the expected content
        val generatedFile = File(tempDir.toFile(), "de/emaarco/example/${modelApi.fileName()}.java")
        assertThat(generatedFile.exists()).isTrue()

    }
}
