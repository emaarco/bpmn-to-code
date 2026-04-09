package io.github.emaarco.bpmn.adapter.outbound.engine.extractor

import io.github.emaarco.bpmn.domain.shared.MessageDefinition
import io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition
import io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition.Companion.IMPL_KIND_KEY
import io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition.Companion.IMPL_VALUE_KEY
import io.github.emaarco.bpmn.domain.shared.VariableDefinition
import io.github.emaarco.bpmn.domain.testNewsletterBpmnModel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class ZeebeModelExtractorTest {

    private val underTest = ZeebeModelExtractor()

    @Test
    fun `extract returns valid BpmnModel`() {

        // given: prepare bpmn file to be extracted
        val resourceUrl = requireNotNull(javaClass.getResource("/bpmn/c8-newsletter.bpmn"))
        val file = File(resourceUrl.toURI())

        // when: extracting file to bpmn-model
        val bpmnModel = underTest.extract(file.inputStream())

        // then: assert that the model has expected content
        assertThat(bpmnModel).isNotNull()
        assertThat(bpmnModel).usingRecursiveComparison().ignoringCollectionOrder().isEqualTo(
            testNewsletterBpmnModel(
                testVariableForTimer = "=testVariable",
                variables = listOf(
                    VariableDefinition("subscriptionId"),
                    VariableDefinition("testVariable")
                ),
                serviceTasks = listOf(
                    ServiceTaskDefinition("Activity_SendConfirmationMail", customProperties = mapOf(IMPL_VALUE_KEY to "newsletter.sendConfirmationMail", IMPL_KIND_KEY to "JOB_WORKER")),
                    ServiceTaskDefinition("Activity_SendWelcomeMail", customProperties = mapOf(IMPL_VALUE_KEY to "newsletter.sendWelcomeMail", IMPL_KIND_KEY to "JOB_WORKER")),
                    ServiceTaskDefinition("EndEvent_RegistrationCompleted", customProperties = mapOf(IMPL_VALUE_KEY to "newsletter.registrationCompleted", IMPL_KIND_KEY to "JOB_WORKER")),
                ),
                messages = listOf(
                    MessageDefinition("StartEvent_SubmitRegistrationForm", "Message_FormSubmitted"),
                    MessageDefinition("Activity_ConfirmRegistration", "Message_SubscriptionConfirmed", customProperties = mapOf("correlationKey" to "=subscriptionId")),
                )
            )
        )
    }

    @Test
    fun `extract returns multi-instance variables`() {
        val resourceUrl = requireNotNull(javaClass.getResource("/bpmn/c8-multi-instance.bpmn"))
        val file = File(resourceUrl.toURI())
        val bpmnModel = underTest.extract(file.inputStream())
        assertThat(bpmnModel.variables).containsExactlyInAnyOrder(
            VariableDefinition("test"),
            VariableDefinition("authors"),
            VariableDefinition("author"),
            VariableDefinition("subscribers"),
            VariableDefinition("subscriber"),
            VariableDefinition("results"),
            VariableDefinition("result")
        )
    }

}
