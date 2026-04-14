package io.github.emaarco.bpmn.adapter.outbound.codegen.builder

import io.github.emaarco.bpmn.domain.BpmnModelApi
import io.github.emaarco.bpmn.domain.MergedBpmnModel
import io.github.emaarco.bpmn.domain.MergedBpmnModel.VariantData
import io.github.emaarco.bpmn.domain.shared.EscalationDefinition
import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.shared.VariableDefinition
import io.github.emaarco.bpmn.domain.testBpmnModelApi
import io.github.emaarco.bpmn.domain.testSendNewsletterBpmnModel
import io.github.emaarco.bpmn.domain.testSubscribeNewsletterBpmnModel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class JavaProcessApiBuilderTest {

    private val underTest = JavaProcessApiBuilder()

    @Test
    fun `buildApiFile generates correct process API file`() {

        // given: a BPMN model with custom service task implementations
        val modelApi = testBpmnModelApi(
            packagePath = "de.emaarco.example",
            model = testSubscribeNewsletterBpmnModel(
                flowNodes = buildSubscribeNewsletterFlowNodes(
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
        assertThat(result.fileName).isEqualTo("${modelApi.fileName()}.java")
        assertThat(result.packagePath).isEqualTo("de.emaarco.example")

        val expectedFile = File(requireNotNull(javaClass.getResource("/api/NewsletterSubscriptionProcessApiJava.txt")).toURI())
        assertThat(result.content).isEqualToIgnoringWhitespace(expectedFile.readText())
    }

    @Test
    fun `maps content of id to valid variable name format`() {

        // given: a model with flow nodes that have slashes in their names
        val defaultModel = testSubscribeNewsletterBpmnModel()
        val modifiedNodes = defaultModel.flowNodes.map { it.copy(id = it.getName().replace("_", "-")) }
        val modelApi = testBpmnModelApi(
            model = testSubscribeNewsletterBpmnModel(flowNodes = modifiedNodes),
            packagePath = "de.emaarco.example"
        )

        // when: we build the process API file
        val result = underTest.buildApiFile(modelApi)

        // then: expect the generated code contains valid Java
        assertThat(result.content).isNotEmpty()
    }

    @Test
    fun `buildApiFile generates variant-scoped Flows and Relations for merged model`() {

        // given: a merged model with a single variant
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
        val modelApi = BpmnModelApi(merged, OutputLanguage.JAVA, "de.emaarco.example", ProcessEngine.ZEEBE)

        // when: we build the process API file
        val result = underTest.buildApiFile(modelApi)

        // then: output contains Variants section instead of flat Flows/Relations
        val expectedFile = File(requireNotNull(javaClass.getResource("/api/MultiVariantProcessApiJava.txt")).toURI())
        assertThat(result.content).isEqualToIgnoringWhitespace(expectedFile.readText())
    }
}
