package io.github.emaarco.bpmn.adapter.outbound.json

import io.github.emaarco.bpmn.domain.MergedBpmnModel
import io.github.emaarco.bpmn.domain.MergedBpmnModel.VariantData
import io.github.emaarco.bpmn.domain.shared.EscalationDefinition
import io.github.emaarco.bpmn.domain.testSendNewsletterBpmnModel
import io.github.emaarco.bpmn.domain.testSubscribeNewsletterBpmnModel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class BpmnJsonGeneratorTest {

    private val underTest = BpmnJsonGenerator()

    @Test
    fun `generates correct JSON for single model`() {

        // given: the subscribe newsletter BPMN model
        val model = testSubscribeNewsletterBpmnModel(
            escalations = listOf(EscalationDefinition("EndEvent_RegistrationNotPossible", "Escalation_RegistrationFailed", "100"))
        )

        // when: generating JSON
        val result = underTest.generate(model)

        // then: expect the generated JSON to match the expected snapshot
        val expectedFile = File(javaClass.getResource("/json/NewsletterSubscriptionProcess.json")!!.toURI())
        assertThat(result).isEqualToIgnoringWhitespace(expectedFile.readText())
    }

    @Test
    fun `generates JSON with variants for merged model`() {

        // given: a merged model combining subscribe and send newsletter
        val subscribe = testSubscribeNewsletterBpmnModel(variantName = "subscribe")
        val send = testSendNewsletterBpmnModel(variantName = "send")
        val merged = MergedBpmnModel(
            processId = "newsletterSubscription",
            flowNodes = (subscribe.flowNodes + send.flowNodes).distinctBy { it.getRawName() },
            messages = (subscribe.messages + send.messages).distinctBy { it.getRawName() },
            signals = subscribe.signals + send.signals,
            errors = subscribe.errors + send.errors,
            escalations = (subscribe.escalations + send.escalations).distinctBy { it.getRawName() },
            variants = listOf(
                VariantData("subscribe", subscribe.sequenceFlows, subscribe.flowNodes),
                VariantData("send", send.sequenceFlows, send.flowNodes),
            ),
        )

        // when: generating JSON
        val result = underTest.generate(merged)

        // then: expect the generated JSON to match the expected snapshot
        val expectedFile = File(javaClass.getResource("/json/MultiVariantNewsletterProcess.json")!!.toURI())
        assertThat(result).isEqualToIgnoringWhitespace(expectedFile.readText())
    }

    @Test
    fun `adapter always uses processId as filename`() {

        // given: a model
        val model = testSubscribeNewsletterBpmnModel()
        val adapter = BpmnJsonGenerationAdapter()

        // when: generating JSON via adapter
        val result = adapter.generateJson(model)

        // then: filename is processId.json
        assertThat(result.fileName).isEqualTo("newsletterSubscription.json")
    }
}
