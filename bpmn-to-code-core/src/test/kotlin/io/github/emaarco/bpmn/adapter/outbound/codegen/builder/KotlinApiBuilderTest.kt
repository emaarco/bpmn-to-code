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
        variables = listOf(VariableDefinition("subscriptionId")),
        incoming = listOf("Timer_After3Days"), outgoing = listOf("EndEvent_RegistrationAborted")),
    FlowNodeDefinition("Activity_ConfirmRegistration", BpmnElementType.RECEIVE_TASK,
        attachedElements = listOf("Timer_EveryDay"),
        parentId = "SubProcess_Confirmation",
        incoming = listOf("Activity_SendConfirmationMail"), outgoing = listOf("EndEvent_SubscriptionConfirmed")),
    FlowNodeDefinition("Activity_SendConfirmationMail", BpmnElementType.SERVICE_TASK,
        properties = FlowNodeProperties.ServiceTask(ServiceTaskDefinition("Activity_SendConfirmationMail", customProperties = mapOf(IMPL_VALUE_KEY to confirmationMailImpl))),
        variables = listOf(VariableDefinition("subscriptionId")) + extraVariables,
        parentId = "SubProcess_Confirmation",
        incoming = listOf("StartEvent_RequestReceived", "Timer_EveryDay"), outgoing = listOf("Activity_ConfirmRegistration")),
    FlowNodeDefinition("Activity_SendWelcomeMail", BpmnElementType.SERVICE_TASK,
        properties = FlowNodeProperties.ServiceTask(ServiceTaskDefinition("Activity_SendWelcomeMail", customProperties = mapOf(IMPL_VALUE_KEY to welcomeMailImpl))),
        variables = listOf(VariableDefinition("subscriptionId")),
        incoming = listOf("SubProcess_Confirmation"), outgoing = listOf("EndEvent_RegistrationCompleted")),
    FlowNodeDefinition("EndEvent_RegistrationAborted", BpmnElementType.END_EVENT,
        incoming = listOf("CallActivity_AbortRegistration")),
    FlowNodeDefinition("EndEvent_RegistrationCompleted", BpmnElementType.END_EVENT,
        properties = FlowNodeProperties.ServiceTask(ServiceTaskDefinition("EndEvent_RegistrationCompleted", customProperties = mapOf(IMPL_VALUE_KEY to registrationCompletedImpl))),
        variables = listOf(VariableDefinition("subscriptionId")),
        incoming = listOf("Activity_SendWelcomeMail")),
    FlowNodeDefinition("EndEvent_RegistrationNotPossible", BpmnElementType.END_EVENT,
        incoming = listOf("ErrorEvent_InvalidMail")),
    FlowNodeDefinition("EndEvent_SubscriptionConfirmed", BpmnElementType.END_EVENT,
        parentId = "SubProcess_Confirmation",
        incoming = listOf("Activity_ConfirmRegistration")),
    FlowNodeDefinition("ErrorEvent_InvalidMail", BpmnElementType.BOUNDARY_EVENT,
        attachedToRef = "SubProcess_Confirmation",
        outgoing = listOf("EndEvent_RegistrationNotPossible")),
    FlowNodeDefinition("StartEvent_RequestReceived", BpmnElementType.START_EVENT,
        variables = listOf(VariableDefinition("subscriptionId")),
        parentId = "SubProcess_Confirmation",
        outgoing = listOf("Activity_SendConfirmationMail")),
    FlowNodeDefinition("StartEvent_SubmitRegistrationForm", BpmnElementType.START_EVENT,
        variables = listOf(VariableDefinition("subscriptionId")),
        outgoing = listOf("SubProcess_Confirmation")),
    FlowNodeDefinition("SubProcess_Confirmation", BpmnElementType.SUB_PROCESS,
        attachedElements = listOf("ErrorEvent_InvalidMail", "Timer_After3Days"),
        incoming = listOf("StartEvent_SubmitRegistrationForm"), outgoing = listOf("Activity_SendWelcomeMail")),
    FlowNodeDefinition("Timer_After3Days", BpmnElementType.BOUNDARY_EVENT,
        properties = FlowNodeProperties.Timer(TimerDefinition("Timer_After3Days", "Duration", "\${testVariable}")),
        attachedToRef = "SubProcess_Confirmation",
        outgoing = listOf("CallActivity_AbortRegistration")),
    FlowNodeDefinition("Timer_EveryDay", BpmnElementType.BOUNDARY_EVENT,
        properties = FlowNodeProperties.Timer(TimerDefinition("Timer_EveryDay", "Duration", "PT1M")),
        attachedToRef = "Activity_ConfirmRegistration",
        parentId = "SubProcess_Confirmation",
        outgoing = listOf("Activity_SendConfirmationMail")),
)
