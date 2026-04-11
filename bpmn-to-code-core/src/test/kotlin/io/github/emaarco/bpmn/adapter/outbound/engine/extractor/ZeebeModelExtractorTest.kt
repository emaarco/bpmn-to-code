package io.github.emaarco.bpmn.adapter.outbound.engine.extractor

import io.github.emaarco.bpmn.domain.shared.BpmnElementType
import io.github.emaarco.bpmn.domain.shared.CallActivityDefinition
import io.github.emaarco.bpmn.domain.shared.FlowNodeDefinition
import io.github.emaarco.bpmn.domain.shared.FlowNodeProperties
import io.github.emaarco.bpmn.domain.shared.MessageDefinition
import io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition
import io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition.Companion.IMPL_KIND_KEY
import io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition.Companion.IMPL_VALUE_KEY
import io.github.emaarco.bpmn.domain.shared.TimerDefinition
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
        val zeebeServiceTasks = listOf(
            ServiceTaskDefinition("Activity_SendConfirmationMail", customProperties = mapOf(IMPL_VALUE_KEY to "newsletter.sendConfirmationMail", IMPL_KIND_KEY to "JOB_WORKER")),
            ServiceTaskDefinition("Activity_SendWelcomeMail", customProperties = mapOf(IMPL_VALUE_KEY to "newsletter.sendWelcomeMail", IMPL_KIND_KEY to "JOB_WORKER")),
            ServiceTaskDefinition("EndEvent_RegistrationCompleted", customProperties = mapOf(IMPL_VALUE_KEY to "newsletter.registrationCompleted", IMPL_KIND_KEY to "JOB_WORKER")),
        )
        assertThat(bpmnModel).usingRecursiveComparison().ignoringCollectionOrder().isEqualTo(
            testNewsletterBpmnModel(
                flowNodes = listOf(
                    FlowNodeDefinition("CallActivity_AbortRegistration", BpmnElementType.CALL_ACTIVITY,
                        properties = FlowNodeProperties.CallActivity(CallActivityDefinition("CallActivity_AbortRegistration", "abort-registration")),
                        variables = listOf(VariableDefinition("subscriptionId")),
                        incoming = listOf("Timer_After3Days"),
                        outgoing = listOf("EndEvent_RegistrationAborted")),
                    FlowNodeDefinition("Activity_ConfirmRegistration", BpmnElementType.RECEIVE_TASK,
                        parentId = "SubProcess_Confirmation",
                        incoming = listOf("Activity_SendConfirmationMail"),
                        outgoing = listOf("EndEvent_SubscriptionConfirmed")),
                    FlowNodeDefinition("Activity_SendConfirmationMail", BpmnElementType.SERVICE_TASK,
                        properties = FlowNodeProperties.ServiceTask(zeebeServiceTasks[0]),
                        variables = listOf(VariableDefinition("testVariable"), VariableDefinition("subscriptionId")),
                        parentId = "SubProcess_Confirmation",
                        incoming = listOf("StartEvent_RequestReceived", "Timer_EveryDay"),
                        outgoing = listOf("Activity_ConfirmRegistration")),
                    FlowNodeDefinition("Activity_SendWelcomeMail", BpmnElementType.SERVICE_TASK,
                        properties = FlowNodeProperties.ServiceTask(zeebeServiceTasks[1]),
                        variables = listOf(VariableDefinition("subscriptionId")),
                        incoming = listOf("SubProcess_Confirmation"),
                        outgoing = listOf("EndEvent_RegistrationCompleted")),
                    FlowNodeDefinition("EndEvent_RegistrationAborted", BpmnElementType.END_EVENT,
                        incoming = listOf("CallActivity_AbortRegistration")),
                    FlowNodeDefinition("EndEvent_RegistrationCompleted", BpmnElementType.END_EVENT,
                        properties = FlowNodeProperties.ServiceTask(zeebeServiceTasks[2]),
                        variables = listOf(VariableDefinition("subscriptionId")),
                        incoming = listOf("Activity_SendWelcomeMail")),
                    FlowNodeDefinition("EndEvent_RegistrationNotPossible", BpmnElementType.END_EVENT,
                        incoming = listOf("ErrorEvent_InvalidMail")),
                    FlowNodeDefinition("EndEvent_SubscriptionConfirmed", BpmnElementType.END_EVENT,
                        parentId = "SubProcess_Confirmation",
                        incoming = listOf("Activity_ConfirmRegistration")),
                    FlowNodeDefinition("ErrorEvent_InvalidMail", BpmnElementType.BOUNDARY_EVENT,
                        attachedToRef = "SubProcess_Confirmation",
                        variables = listOf(VariableDefinition("subscriptionId")),
                        outgoing = listOf("EndEvent_RegistrationNotPossible")),
                    FlowNodeDefinition("StartEvent_RequestReceived", BpmnElementType.START_EVENT,
                        variables = listOf(VariableDefinition("subscriptionId")),
                        parentId = "SubProcess_Confirmation",
                        outgoing = listOf("Activity_SendConfirmationMail")),
                    FlowNodeDefinition("StartEvent_SubmitRegistrationForm", BpmnElementType.START_EVENT,
                        variables = listOf(VariableDefinition("subscriptionId")),
                        outgoing = listOf("SubProcess_Confirmation")),
                    FlowNodeDefinition("SubProcess_Confirmation", BpmnElementType.SUB_PROCESS,
                        incoming = listOf("StartEvent_SubmitRegistrationForm"),
                        outgoing = listOf("Activity_SendWelcomeMail")),
                    FlowNodeDefinition("Timer_After3Days", BpmnElementType.BOUNDARY_EVENT,
                        properties = FlowNodeProperties.Timer(TimerDefinition("Timer_After3Days", "Duration", "=testVariable")),
                        attachedToRef = "SubProcess_Confirmation",
                        outgoing = listOf("CallActivity_AbortRegistration")),
                    FlowNodeDefinition("Timer_EveryDay", BpmnElementType.BOUNDARY_EVENT,
                        properties = FlowNodeProperties.Timer(TimerDefinition("Timer_EveryDay", "Duration", "PT1M")),
                        attachedToRef = "Activity_ConfirmRegistration",
                        parentId = "SubProcess_Confirmation",
                        outgoing = listOf("Activity_SendConfirmationMail")),
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
