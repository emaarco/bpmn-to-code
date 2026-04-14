package io.github.emaarco.bpmn.adapter.outbound.json

import io.github.emaarco.bpmn.domain.shared.EscalationDefinition
import io.github.emaarco.bpmn.domain.testNewsletterBpmnModel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class BpmnJsonGeneratorTest {

    private val underTest = BpmnJsonGenerator()

    @Test
    fun `generates correct JSON for newsletter model`() {

        // given: the newsletter BPMN model
        val model = testNewsletterBpmnModel(
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
    fun `includes variantName in JSON when set`() {

        // given: a model with variantName
        val model = testNewsletterBpmnModel(
            variantName = "withApproval",
            escalations = listOf(EscalationDefinition("EndEvent_RegistrationNotPossible", "Escalation_RegistrationFailed", "100"))
        )

        // when: generating JSON
        val result = underTest.generate(model)

        // then: variantName appears in the JSON output
        assertThat(result).contains("\"variantName\": \"withApproval\"")
    }

    @Test
    fun `adapter uses variant-aware filename when variantName is set`() {

        // given: a model with variantName
        val model = testNewsletterBpmnModel(variantName = "withApproval")
        val adapter = BpmnJsonGenerationAdapter()

        // when: generating JSON via adapter
        val result = adapter.generateJson(model)

        // then: filename includes variantName
        assertThat(result.fileName).isEqualTo("newsletterSubscription-withApproval.json")
    }

    @Test
    fun `adapter uses plain filename when variantName is not set`() {

        // given: a model without variantName
        val model = testNewsletterBpmnModel()
        val adapter = BpmnJsonGenerationAdapter()

        // when: generating JSON via adapter
        val result = adapter.generateJson(model)

        // then: filename is just processId
        assertThat(result.fileName).isEqualTo("newsletterSubscription.json")
    }
}
