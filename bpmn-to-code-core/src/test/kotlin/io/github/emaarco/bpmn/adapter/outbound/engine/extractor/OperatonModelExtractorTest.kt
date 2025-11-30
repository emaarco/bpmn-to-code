package io.github.emaarco.bpmn.adapter.outbound.engine.extractor

import io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition
import io.github.emaarco.bpmn.domain.shared.VariableDefinition
import io.github.emaarco.bpmn.domain.testNewsletterBpmnModel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class OperatonModelExtractorTest {

    private val underTest = OperatonModelExtractor()

    @Test
    fun `extract returns valid BpmnModel with operaton namespace`() {
        val resourceUrl = requireNotNull(javaClass.getResource("/bpmn/operaton-newsletter.bpmn"))
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
                    ServiceTaskDefinition("Activity_SendWelcomeMail", "newsletter.sendWelcomeMail"),
                    ServiceTaskDefinition("Activity_SendConfirmationMail", "newsletter.sendConfirmationMail"),
                    ServiceTaskDefinition("EndEvent_RegistrationCompleted", "newsletter.registrationCompleted")
                )
            )
        )
    }

}
