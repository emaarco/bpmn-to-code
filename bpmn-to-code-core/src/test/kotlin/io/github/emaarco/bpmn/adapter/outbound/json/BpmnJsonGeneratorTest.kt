package io.github.emaarco.bpmn.adapter.outbound.json

import io.github.emaarco.bpmn.domain.MergedBpmnModel
import io.github.emaarco.bpmn.domain.MergedBpmnModel.VariantData
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
        val model = testSubscribeNewsletterBpmnModel()

        // when: generating JSON
        val result = underTest.generate(model)

        // then: expect the generated JSON to match the expected snapshot
        val expectedFile = File(javaClass.getResource("/json/NewsletterSubscriptionProcess.json")!!.toURI())
        assertThat(result).isEqualToIgnoringWhitespace(expectedFile.readText())
    }

    @Test
    fun `generates JSON with variants for merged model`() {

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
