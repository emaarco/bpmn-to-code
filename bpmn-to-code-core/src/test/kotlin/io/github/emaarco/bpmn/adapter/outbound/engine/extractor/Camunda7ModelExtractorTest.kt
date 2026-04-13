package io.github.emaarco.bpmn.adapter.outbound.engine.extractor

import io.github.emaarco.bpmn.domain.shared.BpmnElementType
import io.github.emaarco.bpmn.domain.shared.CallActivityDefinition
import io.github.emaarco.bpmn.domain.shared.CompensationDefinition
import io.github.emaarco.bpmn.domain.shared.EscalationDefinition
import io.github.emaarco.bpmn.domain.shared.FlowNodeDefinition
import io.github.emaarco.bpmn.domain.shared.FlowNodeDefinition.Companion.ASYNC_AFTER_KEY
import io.github.emaarco.bpmn.domain.shared.FlowNodeDefinition.Companion.ASYNC_BEFORE_KEY
import io.github.emaarco.bpmn.domain.shared.FlowNodeDefinition.Companion.EXCLUSIVE_KEY
import io.github.emaarco.bpmn.domain.shared.FlowNodeProperties
import io.github.emaarco.bpmn.domain.shared.SequenceFlowDefinition
import io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition
import io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition.Companion.IMPL_KIND_KEY
import io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition.Companion.IMPL_VALUE_KEY
import io.github.emaarco.bpmn.domain.shared.TimerDefinition
import io.github.emaarco.bpmn.domain.shared.VariableDefinition
import io.github.emaarco.bpmn.domain.testNewsletterBpmnModel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class Camunda7ModelExtractorTest {

    private val underTest = Camunda7ModelExtractor()

    @Test
    fun `extract returns valid BpmnModel`() {
        val resourceUrl = requireNotNull(javaClass.getResource("/bpmn/c7-subscribe-newsletter.bpmn"))
        val file = File(resourceUrl.toURI())
        val bpmnModel = underTest.extract(file.inputStream())

        val c7ServiceTasks = listOf(
            ServiceTaskDefinition("Activity_SendWelcomeMail", customProperties = mapOf(IMPL_VALUE_KEY to "\${newsletterSendWelcomeMail}", IMPL_KIND_KEY to "DELEGATE_EXPRESSION")),
            ServiceTaskDefinition("Activity_SendConfirmationMail", customProperties = mapOf(IMPL_VALUE_KEY to "#{newsletterSendConfirmationMail}", IMPL_KIND_KEY to "EXTERNAL_TASK")),
            ServiceTaskDefinition("EndEvent_RegistrationCompleted", customProperties = mapOf(IMPL_VALUE_KEY to "newsletter.registrationCompleted", IMPL_KIND_KEY to "EXTERNAL_TASK")),
            ServiceTaskDefinition("serviceTask_incrementSubscriptionCounter", customProperties = mapOf(IMPL_VALUE_KEY to "counterClass", IMPL_KIND_KEY to "DELEGATE_EXPRESSION")),
        )
        val c7ServiceTaskById = c7ServiceTasks.associateBy { it.id }

        assertThat(bpmnModel).usingRecursiveComparison().ignoringCollectionOrder().isEqualTo(
            testNewsletterBpmnModel(
                flowNodes = listOf(
                    FlowNodeDefinition("CallActivity_AbortRegistration", BpmnElementType.CALL_ACTIVITY,
                        properties = FlowNodeProperties.CallActivity(CallActivityDefinition("CallActivity_AbortRegistration", "abort-registration")),
                        variables = listOf(VariableDefinition("subscriptionId"), VariableDefinition("reasonCode"), VariableDefinition("abortResult")),
                        incoming = listOf("Timer_After3Days"),
                        outgoing = listOf("CompensationEndEvent_RegistrationAborted"),
                        customProperties = mapOf(ASYNC_BEFORE_KEY to true, ASYNC_AFTER_KEY to true)),
                    FlowNodeDefinition("Activity_ConfirmRegistration", BpmnElementType.USER_TASK,
                        variables = listOf(VariableDefinition("subscriptionId")),
                        attachedElements = listOf("Timer_EveryDay"),
                        parentId = "SubProcess_Confirmation",
                        incoming = listOf("Activity_SendConfirmationMail"),
                        outgoing = listOf("EndEvent_SubscriptionConfirmed"),
                        customProperties = mapOf(ASYNC_AFTER_KEY to true)),
                    FlowNodeDefinition("Activity_SendConfirmationMail", BpmnElementType.SERVICE_TASK,
                        properties = FlowNodeProperties.ServiceTask(c7ServiceTaskById["Activity_SendConfirmationMail"]!!),
                        variables = listOf(VariableDefinition("subscriptionId"), VariableDefinition("otherVariable")),
                        parentId = "SubProcess_Confirmation",
                        incoming = listOf("StartEvent_RequestReceived", "Timer_EveryDay"),
                        outgoing = listOf("Activity_ConfirmRegistration")),
                    FlowNodeDefinition("Activity_SendWelcomeMail", BpmnElementType.SERVICE_TASK,
                        properties = FlowNodeProperties.ServiceTask(c7ServiceTaskById["Activity_SendWelcomeMail"]!!),
                        variables = listOf(VariableDefinition("subscriptionId")),
                        incoming = listOf("SubProcess_Confirmation"),
                        outgoing = listOf("EndEvent_RegistrationCompleted"),
                        customProperties = mapOf(ASYNC_BEFORE_KEY to true, ASYNC_AFTER_KEY to true, EXCLUSIVE_KEY to false)),
                    FlowNodeDefinition("CompensationEndEvent_RegistrationAborted", BpmnElementType.END_EVENT,
                        incoming = listOf("CallActivity_AbortRegistration")),
                    FlowNodeDefinition("compensationEvent_onSubscriptionCounter", BpmnElementType.BOUNDARY_EVENT,
                        attachedToRef = "serviceTask_incrementSubscriptionCounter"),
                    FlowNodeDefinition("compensationTask_decrementSubscriptionCounter", BpmnElementType.TASK,
                        variables = listOf(VariableDefinition("subscriptionId"))),
                    FlowNodeDefinition("EndEvent_RegistrationCompleted", BpmnElementType.END_EVENT,
                        properties = FlowNodeProperties.ServiceTask(c7ServiceTaskById["EndEvent_RegistrationCompleted"]!!),
                        variables = listOf(VariableDefinition("subscriptionId")),
                        incoming = listOf("Activity_SendWelcomeMail")),
                    FlowNodeDefinition("EndEvent_RegistrationNotPossible", BpmnElementType.END_EVENT,
                        incoming = listOf("ErrorEvent_InvalidMail"),
                        customProperties = mapOf(ASYNC_BEFORE_KEY to true, EXCLUSIVE_KEY to false)),
                    FlowNodeDefinition("EndEvent_SubscriptionConfirmed", BpmnElementType.END_EVENT,
                        parentId = "SubProcess_Confirmation",
                        incoming = listOf("Activity_ConfirmRegistration")),
                    FlowNodeDefinition("ErrorEvent_InvalidMail", BpmnElementType.BOUNDARY_EVENT,
                        attachedToRef = "SubProcess_Confirmation",
                        outgoing = listOf("EndEvent_RegistrationNotPossible")),
                    FlowNodeDefinition("serviceTask_incrementSubscriptionCounter", BpmnElementType.SERVICE_TASK,
                        properties = FlowNodeProperties.ServiceTask(c7ServiceTaskById["serviceTask_incrementSubscriptionCounter"]!!),
                        attachedElements = listOf("compensationEvent_onSubscriptionCounter"),
                        incoming = listOf("StartEvent_SubmitRegistrationForm"),
                        outgoing = listOf("SubProcess_Confirmation")),
                    FlowNodeDefinition("StartEvent_RequestReceived", BpmnElementType.START_EVENT,
                        variables = listOf(VariableDefinition("subscriptionId")),
                        parentId = "SubProcess_Confirmation",
                        outgoing = listOf("Activity_SendConfirmationMail"),
                        customProperties = mapOf(ASYNC_BEFORE_KEY to true)),
                    FlowNodeDefinition("StartEvent_SubmitRegistrationForm", BpmnElementType.START_EVENT,
                        variables = listOf(VariableDefinition("subscriptionId")),
                        outgoing = listOf("serviceTask_incrementSubscriptionCounter")),
                    FlowNodeDefinition("SubProcess_Confirmation", BpmnElementType.SUB_PROCESS,
                        attachedElements = listOf("ErrorEvent_InvalidMail", "Timer_After3Days"),
                        incoming = listOf("serviceTask_incrementSubscriptionCounter"),
                        outgoing = listOf("Activity_SendWelcomeMail")),
                    FlowNodeDefinition("Timer_After3Days", BpmnElementType.BOUNDARY_EVENT,
                        properties = FlowNodeProperties.Timer(TimerDefinition("Timer_After3Days", "Duration", "\${testVariable}")),
                        attachedToRef = "SubProcess_Confirmation",
                        outgoing = listOf("CallActivity_AbortRegistration")),
                    FlowNodeDefinition("Timer_EveryDay", BpmnElementType.BOUNDARY_EVENT,
                        properties = FlowNodeProperties.Timer(TimerDefinition("Timer_EveryDay", "Duration", "PT1M")),
                        attachedToRef = "Activity_ConfirmRegistration",
                        parentId = "SubProcess_Confirmation",
                        outgoing = listOf("Activity_SendConfirmationMail")),
                ),
                sequenceFlows = listOf(
                    SequenceFlowDefinition("Flow_05i3x1y", "StartEvent_RequestReceived", "Activity_SendConfirmationMail"),
                    SequenceFlowDefinition("Flow_09cuvzp", "SubProcess_Confirmation", "Activity_SendWelcomeMail"),
                    SequenceFlowDefinition("Flow_0i2ctuv", "ErrorEvent_InvalidMail", "EndEvent_RegistrationNotPossible"),
                    SequenceFlowDefinition("Flow_0x4ewvb", "Timer_EveryDay", "Activity_SendConfirmationMail"),
                    SequenceFlowDefinition("Flow_0zdmt0t", "serviceTask_incrementSubscriptionCounter", "SubProcess_Confirmation"),
                    SequenceFlowDefinition("Flow_1bckm43", "Activity_SendConfirmationMail", "Activity_ConfirmRegistration"),
                    SequenceFlowDefinition("Flow_1bsb8no", "CallActivity_AbortRegistration", "CompensationEndEvent_RegistrationAborted"),
                    SequenceFlowDefinition("Flow_1cpwe57", "Activity_ConfirmRegistration", "EndEvent_SubscriptionConfirmed"),
                    SequenceFlowDefinition("Flow_1csfyyz", "StartEvent_SubmitRegistrationForm", "serviceTask_incrementSubscriptionCounter"),
                    SequenceFlowDefinition("Flow_1i7hjid", "Activity_SendWelcomeMail", "EndEvent_RegistrationCompleted"),
                    SequenceFlowDefinition("Flow_1l1lj4m", "Timer_After3Days", "CallActivity_AbortRegistration"),
                ),
                compensations = listOf(
                    CompensationDefinition("CompensationEndEvent_RegistrationAborted", "serviceTask_incrementSubscriptionCounter", customProperties = mapOf("waitForCompletion" to false)),
                    CompensationDefinition("compensationEvent_onSubscriptionCounter", null, customProperties = mapOf("waitForCompletion" to false)),
                ),
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
        val resourceUrl = requireNotNull(javaClass.getResource("/bpmn/c7-send-newsletter.bpmn"))
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

    @Test
    fun `extract detects event subprocess type and extracts escalations`() {
        val resourceUrl = requireNotNull(javaClass.getResource("/bpmn/c7-send-newsletter.bpmn"))
        val file = File(resourceUrl.toURI())
        val bpmnModel = underTest.extract(file.inputStream())

        val eventSubProcess = bpmnModel.flowNodes.first { it.id == "eventSubProcess_errorHandling" }
        assertThat(eventSubProcess.elementType).isEqualTo(BpmnElementType.EVENT_SUB_PROCESS)

        assertThat(bpmnModel.escalations).containsExactlyInAnyOrder(
            EscalationDefinition("escalationEndEvent_nofitySupport", "escalation_notifySupport", "200"),
            EscalationDefinition("escalationEndEvent_nofitySupportAfterRepeatedError", "escalation_notifySupport", "200"),
        )
    }

    @Test
    fun `extract marks default sequence flow correctly`() {
        val resourceUrl = requireNotNull(javaClass.getResource("/bpmn/c7-send-newsletter.bpmn"))
        val file = File(resourceUrl.toURI())
        val bpmnModel = underTest.extract(file.inputStream())

        val flowsById = bpmnModel.sequenceFlows.associateBy { it.id }
        assertThat(flowsById["Flow_1jogut0"]).isEqualTo(
            SequenceFlowDefinition("Flow_1jogut0", "gateway_hasSubscribers", "serviceTask_sendToSubscriber", flowName = "Yes", isDefault = true)
        )
        assertThat(flowsById["Flow_1gsz7wd"]).isEqualTo(
            SequenceFlowDefinition("Flow_1gsz7wd", "gateway_hasSubscribers", "endEvent_noSubscribers", flowName = "No", conditionExpression = "\${subscribers.size() > 0}")
        )
    }
}
