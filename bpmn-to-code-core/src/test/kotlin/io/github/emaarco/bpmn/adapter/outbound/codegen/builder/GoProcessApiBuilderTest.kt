package io.github.emaarco.bpmn.adapter.outbound.codegen.builder

import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import io.github.emaarco.bpmn.domain.shared.VariableDefinition
import io.github.emaarco.bpmn.domain.testBpmnModelApi
import io.github.emaarco.bpmn.domain.testSubscribeNewsletterBpmnModel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class GoProcessApiBuilderTest {

    private val underTest = GoProcessApiBuilder()

    @Test
    fun `buildApiFile generates correct process API file`() {

        // given: a BPMN model with custom service task implementations
        val modelApi = testBpmnModelApi(
            packagePath = "de.emaarco.example",
            language = OutputLanguage.GO,
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

        // then: a single Go file is returned at the root package
        assertThat(result.fileName).endsWith(".go")
        assertThat(result.packagePath).isEqualTo("de.emaarco.example")

        val expectedFile = File(requireNotNull(javaClass.getResource("/api/NewsletterSubscriptionProcessApiGo.txt")).toURI())
        assertThat(result.content).isEqualToIgnoringWhitespace(expectedFile.readText())
    }
}
