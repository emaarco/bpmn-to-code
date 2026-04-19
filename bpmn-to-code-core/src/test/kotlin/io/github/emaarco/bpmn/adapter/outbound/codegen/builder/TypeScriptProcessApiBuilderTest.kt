package io.github.emaarco.bpmn.adapter.outbound.codegen.builder

import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import io.github.emaarco.bpmn.domain.shared.VariableDefinition
import io.github.emaarco.bpmn.domain.testBpmnModelApi
import io.github.emaarco.bpmn.domain.testSubscribeNewsletterBpmnModel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class TypeScriptProcessApiBuilderTest {

    private val underTest = TypeScriptProcessApiBuilder()

    @Test
    fun `buildApiFile generates correct process API file`() {

        // given: a BPMN model with custom service task implementations
        val modelApi = testBpmnModelApi(
            packagePath = "de.emaarco.example",
            language = OutputLanguage.TYPESCRIPT,
            model = testSubscribeNewsletterBpmnModel(
                flowNodes = buildSubscribeNewsletterFlowNodes(
                    confirmationMailImpl = "#{newsletterSendConfirmationMail}",
                    welcomeMailImpl = "\${newsletterSendWelcomeMail}",
                    registrationCompletedImpl = "newsletter.registrationCompleted",
                    extraVariables = listOf(VariableDefinition("testVariable")),
                ),
            )
        )

        // when: we build the process API file
        val result = underTest.buildApiFile(modelApi)

        // then: a single TypeScript file is returned at the root package
        assertThat(result.fileName).isEqualTo("${modelApi.fileName()}.ts")
        assertThat(result.packagePath).isEqualTo("de.emaarco.example")

        val expectedFile = File(requireNotNull(javaClass.getResource("/api/NewsletterSubscriptionProcessApiTypeScript.txt")).toURI())
        assertThat(result.content).isEqualToIgnoringWhitespace(expectedFile.readText())
    }
}
