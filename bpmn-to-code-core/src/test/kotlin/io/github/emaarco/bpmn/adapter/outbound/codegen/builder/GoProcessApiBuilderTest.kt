package io.github.emaarco.bpmn.adapter.outbound.codegen.builder

import io.github.emaarco.bpmn.domain.BpmnModelApi
import io.github.emaarco.bpmn.domain.MergedBpmnModel
import io.github.emaarco.bpmn.domain.MergedBpmnModel.VariantData
import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.shared.VariableDefinition
import io.github.emaarco.bpmn.domain.testBpmnModelApi
import io.github.emaarco.bpmn.domain.testSendNewsletterBpmnModel
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

    @Test
    fun `buildApiFile generates variant-scoped Flows and Relations for merged model`() {

        // given: a merged model with a single variant (includes escalations)
        val send = testSendNewsletterBpmnModel(variantName = "send")
        val merged = MergedBpmnModel(
            processId = send.processId,
            flowNodes = send.flowNodes,
            messages = send.messages,
            signals = send.signals,
            errors = send.errors,
            escalations = send.escalations,
            variants = listOf(
                VariantData("send", send.sequenceFlows, send.flowNodes),
            ),
        )
        val modelApi = BpmnModelApi(merged, OutputLanguage.GO, "de.emaarco.example", ProcessEngine.ZEEBE)

        // when
        val result = underTest.buildApiFile(modelApi)

        // then
        val expectedFile = File(requireNotNull(javaClass.getResource("/api/MultiVariantProcessApiGo.txt")).toURI())
        assertThat(result.content).isEqualToIgnoringWhitespace(expectedFile.readText())
    }
}
