package io.github.emaarco.bpmn.adapter.outbound.codegen.builder

import io.github.emaarco.bpmn.domain.shared.BpmnElementType
import io.github.emaarco.bpmn.domain.shared.CallActivityDefinition
import io.github.emaarco.bpmn.domain.shared.FlowNodeDefinition
import io.github.emaarco.bpmn.domain.shared.FlowNodeProperties
import io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition
import io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition.Companion.IMPL_VALUE_KEY
import io.github.emaarco.bpmn.domain.shared.TimerDefinition
import io.github.emaarco.bpmn.domain.shared.VariableDefinition
import io.github.emaarco.bpmn.domain.testBpmnModelApi
import io.github.emaarco.bpmn.domain.testNewsletterBpmnModel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class KotlinApiBuilderTest {

    private val underTest = KotlinApiBuilder()

    @Test
    fun `buildApiFile generates correct API file content`() {

        // given: a BPMN model with custom service task implementations
        val modelApi = testBpmnModelApi(
            packagePath = "de.emaarco.example",
            model = testNewsletterBpmnModel(
                flowNodes = buildNewsletterFlowNodes(
                    confirmationMailImpl = "#{newsletterSendConfirmationMail}",
                    welcomeMailImpl = "\${newsletterSendWelcomeMail}",
                    registrationCompletedImpl = "newsletter.registrationCompleted",
                    extraVariables = listOf(VariableDefinition("testVariable")),
                )
            )
        )

        // when: we build the API file
        val result = underTest.buildApiFile(modelApi)

        // then: expect the generated content to match the expected content
        val expectedFile = File(javaClass.getResource("/api/NewsletterSubscriptionProcessApiKotlin.txt").toURI())
        val expectedContent = expectedFile.readText()
        assertThat(result.content).isEqualToIgnoringWhitespace(expectedContent)
        assertThat(result.fileName).isEqualTo("${modelApi.fileName()}.kt")
        assertThat(result.packagePath).isEqualTo("de.emaarco.example")
    }

}

internal fun buildNewsletterFlowNodes(
    confirmationMailImpl: String,
    welcomeMailImpl: String,
    registrationCompletedImpl: String,
    extraVariables: List<VariableDefinition> = emptyList(),
) = listOf(
    FlowNodeDefinition("CallActivity_AbortRegistration", BpmnElementType.CALL_ACTIVITY,
        properties = FlowNodeProperties.CallActivity(CallActivityDefinition("CallActivity_AbortRegistration", "abort-registration")),
        variables = listOf(VariableDefinition("subscriptionId"))),
    FlowNodeDefinition("Activity_ConfirmRegistration", BpmnElementType.RECEIVE_TASK),
    FlowNodeDefinition("Activity_SendConfirmationMail", BpmnElementType.SERVICE_TASK,
        properties = FlowNodeProperties.ServiceTask(ServiceTaskDefinition("Activity_SendConfirmationMail", customProperties = mapOf(IMPL_VALUE_KEY to confirmationMailImpl))),
        variables = listOf(VariableDefinition("subscriptionId")) + extraVariables),
    FlowNodeDefinition("Activity_SendWelcomeMail", BpmnElementType.SERVICE_TASK,
        properties = FlowNodeProperties.ServiceTask(ServiceTaskDefinition("Activity_SendWelcomeMail", customProperties = mapOf(IMPL_VALUE_KEY to welcomeMailImpl))),
        variables = listOf(VariableDefinition("subscriptionId"))),
    FlowNodeDefinition("EndEvent_RegistrationAborted", BpmnElementType.END_EVENT),
    FlowNodeDefinition("EndEvent_RegistrationCompleted", BpmnElementType.END_EVENT,
        properties = FlowNodeProperties.ServiceTask(ServiceTaskDefinition("EndEvent_RegistrationCompleted", customProperties = mapOf(IMPL_VALUE_KEY to registrationCompletedImpl))),
        variables = listOf(VariableDefinition("subscriptionId"))),
    FlowNodeDefinition("EndEvent_RegistrationNotPossible", BpmnElementType.END_EVENT),
    FlowNodeDefinition("EndEvent_SubscriptionConfirmed", BpmnElementType.END_EVENT),
    FlowNodeDefinition("ErrorEvent_InvalidMail", BpmnElementType.BOUNDARY_EVENT,
        attachedToRef = "SubProcess_Confirmation"),
    FlowNodeDefinition("StartEvent_RequestReceived", BpmnElementType.START_EVENT,
        variables = listOf(VariableDefinition("subscriptionId"))),
    FlowNodeDefinition("StartEvent_SubmitRegistrationForm", BpmnElementType.START_EVENT,
        variables = listOf(VariableDefinition("subscriptionId"))),
    FlowNodeDefinition("SubProcess_Confirmation", BpmnElementType.SUB_PROCESS),
    FlowNodeDefinition("Timer_After3Days", BpmnElementType.BOUNDARY_EVENT,
        properties = FlowNodeProperties.Timer(TimerDefinition("Timer_After3Days", "Duration", "\${testVariable}")),
        attachedToRef = "SubProcess_Confirmation"),
    FlowNodeDefinition("Timer_EveryDay", BpmnElementType.BOUNDARY_EVENT,
        properties = FlowNodeProperties.Timer(TimerDefinition("Timer_EveryDay", "Duration", "PT1M")),
        attachedToRef = "Activity_ConfirmRegistration"),
)
