package io.github.emaarco.bpmn.adapter.outbound.engine.extractor

import io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition
import io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition.Companion.IMPL_KIND_KEY
import io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition.Companion.IMPL_VALUE_KEY
import io.github.emaarco.bpmn.domain.shared.VariableDefinition
import io.github.emaarco.bpmn.domain.testNewsletterBpmnModel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class Camunda7ModelExtractorTest {

    private val underTest = Camunda7ModelExtractor()

    @Test
    fun `extract returns valid BpmnModel`() {
        val resourceUrl = requireNotNull(javaClass.getResource("/bpmn/c7-newsletter.bpmn"))
        val file = File(resourceUrl.toURI())
        val bpmnModel = underTest.extract(file.inputStream())
        assertThat(bpmnModel).usingRecursiveComparison().ignoringCollectionOrder().isEqualTo(
            testNewsletterBpmnModel(
                variables = listOf(
                    VariableDefinition("otherVariable"),
                    VariableDefinition("subscriptionId"),
                    VariableDefinition("reasonCode"),
                    VariableDefinition("abortResult")
                ),
                serviceTasks = listOf(
                    ServiceTaskDefinition("Activity_SendWelcomeMail", customProperties = mapOf(IMPL_VALUE_KEY to "\${newsletterSendWelcomeMail}", IMPL_KIND_KEY to "DELEGATE_EXPRESSION")),
                    ServiceTaskDefinition("Activity_SendConfirmationMail", customProperties = mapOf(IMPL_VALUE_KEY to "#{newsletterSendConfirmationMail}", IMPL_KIND_KEY to "EXTERNAL_TASK")),
                    ServiceTaskDefinition("EndEvent_RegistrationCompleted", customProperties = mapOf(IMPL_VALUE_KEY to "newsletter.registrationCompleted", IMPL_KIND_KEY to "EXTERNAL_TASK")),
                )
            )
        )
    }

    @Test
    fun `extract returns additionalVariables from camunda properties`() {
        val resourceUrl = requireNotNull(javaClass.getResource("/bpmn/c7-additional-variables.bpmn"))
        val file = File(resourceUrl.toURI())
        val bpmnModel = underTest.extract(file.inputStream())
        assertThat(bpmnModel.variables).containsExactlyInAnyOrder(
            VariableDefinition("orderId"),
            VariableDefinition("customerEmail"),
            VariableDefinition("amount"),
            VariableDefinition("shipmentId"),
        )
    }

    @Test
    fun `extract returns multi-instance variables`() {
        val resourceUrl = requireNotNull(javaClass.getResource("/bpmn/c7-multi-instance.bpmn"))
        val file = File(resourceUrl.toURI())
        val bpmnModel = underTest.extract(file.inputStream())
        assertThat(bpmnModel.variables).containsExactlyInAnyOrder(
            VariableDefinition("test"),
            VariableDefinition("authors"),
            VariableDefinition("author"),
            VariableDefinition("subscribers"),
            VariableDefinition("subscriber"),
        )
    }
}
