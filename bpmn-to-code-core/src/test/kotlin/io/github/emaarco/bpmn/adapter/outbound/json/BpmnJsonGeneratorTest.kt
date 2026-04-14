package io.github.emaarco.bpmn.adapter.outbound.json

import io.github.emaarco.bpmn.domain.testBpmnModel
import io.github.emaarco.bpmn.domain.shared.*
import io.github.emaarco.bpmn.domain.testSubscribeNewsletterBpmnModel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class BpmnJsonGeneratorTest {

    private val underTest = BpmnJsonGenerator()

    @Test
    fun `generates correct JSON for newsletter model`() {

        // given: the newsletter BPMN model
        val model = testSubscribeNewsletterBpmnModel(
            escalations = listOf(EscalationDefinition("EndEvent_RegistrationNotPossible", "Escalation_RegistrationFailed", "100"))
        )

        // when: generating JSON
        val result = underTest.generate(model)

        // then: expect the generated JSON to match the expected snapshot
        val expectedFile = File(javaClass.getResource("/json/NewsletterSubscriptionProcess.json")!!.toURI())
        val expectedContent = expectedFile.readText()
        assertThat(result).isEqualToIgnoringWhitespace(expectedContent)
    }

    @Test
    fun `generates JSON with variantName when set`() {

        // given: a minimal model with variantName
        val model = testBpmnModel(
            processId = "order-process",
            variantName = "prodDe",
            flowNodes = listOf(FlowNodeDefinition(id = "StartEvent_1", elementType = BpmnElementType.START_EVENT)),
            sequenceFlows = listOf(SequenceFlowDefinition("Flow_1", "StartEvent_1", "EndEvent_1")),
            messages = listOf(MessageDefinition("StartEvent_1", "Message_Order")),
            signals = listOf(SignalDefinition("EndEvent_1", "Signal_Done")),
            errors = listOf(ErrorDefinition("Error_1", "Error_Timeout", "408")),
            escalations = listOf(EscalationDefinition("Esc_1", "Escalation_Retry", "100")),
        )

        // when: generating JSON
        val result = underTest.generate(model)

        // then: expect the generated JSON to match the expected snapshot
        val expectedFile = File(javaClass.getResource("/json/MinimalProcessWithVariant.json")!!.toURI())
        assertThat(result).isEqualToIgnoringWhitespace(expectedFile.readText())
    }

    @Test
    fun `adapter uses variant-aware filename when variantName is set`() {

        // given: a model with variantName
        val model = testSubscribeNewsletterBpmnModel(variantName = "withApproval")
        val adapter = BpmnJsonGenerationAdapter()

        // when: generating JSON via adapter
        val result = adapter.generateJson(model)

        // then: filename includes variantName
        assertThat(result.fileName).isEqualTo("newsletterSubscription-withApproval.json")
    }

    @Test
    fun `adapter uses plain filename when variantName is not set`() {

        // given: a model without variantName
        val model = testSubscribeNewsletterBpmnModel()
        val adapter = BpmnJsonGenerationAdapter()

        // when: generating JSON via adapter
        val result = adapter.generateJson(model)

        // then: filename is just processId
        assertThat(result.fileName).isEqualTo("newsletterSubscription.json")
    }
}
