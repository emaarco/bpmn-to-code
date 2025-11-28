package io.github.emaarco.bpmn.adapter.outbound.engine.extractor

import io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition
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
                    VariableDefinition("subscriptionId")
                ),
                serviceTasks = listOf(
                    ServiceTaskDefinition("Activity_AbortRegistration", "newsletter.abortRegistration"),
                    ServiceTaskDefinition("Activity_SendWelcomeMail", "\${newsletterSendWelcomeMail}"),
                    ServiceTaskDefinition("Activity_SendConfirmationMail", "#{newsletterSendConfirmationMail}"),
                    ServiceTaskDefinition("EndEvent_RegistrationCompleted", "newsletter.registrationCompleted")
                )
            )
        )
    }
}
