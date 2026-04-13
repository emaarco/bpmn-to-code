package io.github.emaarco.bpmn.adapter.outbound.codegen.builder

import io.github.emaarco.bpmn.domain.shared.EscalationDefinition
import io.github.emaarco.bpmn.domain.shared.VariableDefinition
import io.github.emaarco.bpmn.domain.testBpmnModelApi
import io.github.emaarco.bpmn.domain.testNewsletterBpmnModel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class KotlinProcessApiBuilderTest {

    private val underTest = KotlinProcessApiBuilder()

    @Test
    fun `buildApiFile generates correct process API file`() {

        // given: a BPMN model with custom service task implementations
        val modelApi = testBpmnModelApi(
            packagePath = "de.emaarco.example",
            model = testNewsletterBpmnModel(
                flowNodes = buildNewsletterFlowNodes(
                    confirmationMailImpl = "#{newsletterSendConfirmationMail}",
                    welcomeMailImpl = "\${newsletterSendWelcomeMail}",
                    registrationCompletedImpl = "newsletter.registrationCompleted",
                    extraVariables = listOf(VariableDefinition("testVariable")),
                ),
                escalations = listOf(EscalationDefinition("EndEvent_RegistrationNotPossible", "Escalation_RegistrationFailed", "100"))
            )
        )

        // when: we build the process API file
        val result = underTest.buildApiFile(modelApi)

        // then: a single model file is returned at the root package
        assertThat(result.fileName).isEqualTo("${modelApi.fileName()}.kt")
        assertThat(result.packagePath).isEqualTo("de.emaarco.example")

        val expectedFile = File(requireNotNull(javaClass.getResource("/api/NewsletterSubscriptionProcessApiKotlin.txt")).toURI())
        assertThat(result.content).isEqualToIgnoringWhitespace(expectedFile.readText())
    }

}
