package io.github.emaarco.bpmn.adapter.outbound.codegen.builder

import io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition.Companion.IMPL_VALUE_KEY
import io.github.emaarco.bpmn.domain.shared.VariableDefinition
import io.github.emaarco.bpmn.domain.testBpmnModelApi
import io.github.emaarco.bpmn.domain.testNewsletterBpmnModel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class JavaApiBuilderTest {

    private val underTest = JavaApiBuilder()

    @Test
    fun `buildApiFile generates correct API file content`() {

        // given: a BPMN model with custom service task implementations
        val modelApi = testBpmnModelApi(
            packagePath = "de.emaarco.example",
            model = testNewsletterBpmnModel(
                flowNodes = buildNewsletterFlowNodes(
                    confirmationMailImpl = "#{newsletterSendConfirmationMail}",
                    welcomeMailImpl = "\${newsletterSendWelcomeMail}",
                    registrationCompletedImpl = "newsletter.registrationCompleted",
                    extraVariables = listOf(VariableDefinition("testVariable")),
                )
            )
        )

        // when: we build the API file
        val result = underTest.buildApiFile(modelApi)

        // then: expect the generated content to match the expected content
        val expectedFile = File(javaClass.getResource("/api/NewsletterSubscriptionProcessApiJava.txt").toURI())
        val expectedContent = expectedFile.readText()
        assertThat(result.content).isEqualToIgnoringWhitespace(expectedContent)
        assertThat(result.fileName).isEqualTo("${modelApi.fileName()}.java")
        assertThat(result.packagePath).isEqualTo("de.emaarco.example")
    }

    @Test
    fun `maps content of id to valid variable name format`() {

        // given: a model with flow nodes that have slashes in their names
        val defaultModel = testNewsletterBpmnModel()
        val modifiedNodes = defaultModel.flowNodes.map { it.copy(id = it.getName().replace("_", "-")) }
        val modelApi = testBpmnModelApi(
            model = testNewsletterBpmnModel(flowNodes = modifiedNodes),
            packagePath = "de.emaarco.example"
        )

        // when: we build the API file
        val result = underTest.buildApiFile(modelApi)

        // then: expect the generated code contains valid Java
        assertThat(result.content).isNotEmpty()
        assertThat(result.fileName).isEqualTo("${modelApi.fileName()}.java")
    }

}
