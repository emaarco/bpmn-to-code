package io.github.emaarco.bpmn.adapter.outbound.engine.extractor

import io.github.emaarco.bpmn.domain.shared.BpmnElementType
import io.github.emaarco.bpmn.domain.shared.CallActivityDefinition
import io.github.emaarco.bpmn.domain.shared.CompensationDefinition
import io.github.emaarco.bpmn.domain.shared.CompensationType
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
import io.github.emaarco.bpmn.domain.testSubscribeNewsletterBpmnModel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class OperatonModelExtractorTest {

    private val underTest = OperatonModelExtractor()

    @Test
    fun `extract returns valid BpmnModel with operaton namespace`() {

        // given: the Operaton newsletter BPMN file from classpath
        val resourceUrl = requireNotNull(javaClass.getResource("/bpmn/operaton-subscribe-newsletter.bpmn"))
        val file = File(resourceUrl.toURI())

        // when: extracting the model
        val bpmnModel = underTest.extract(file.inputStream())

        // then: the model matches the expected structure
        val opServiceTasks = listOf(
            ServiceTaskDefinition("Activity_SendWelcomeMail", engineSpecificProperties = mapOf(IMPL_VALUE_KEY to "newsletter.sendWelcomeMail", IMPL_KIND_KEY to "DELEGATE_EXPRESSION")),
            ServiceTaskDefinition("Activity_SendConfirmationMail", engineSpecificProperties = mapOf(IMPL_VALUE_KEY to "newsletter.sendConfirmationMail", IMPL_KIND_KEY to "EXTERNAL_TASK")),
            ServiceTaskDefinition("EndEvent_RegistrationCompleted", engineSpecificProperties = mapOf(IMPL_VALUE_KEY to "newsletter.registrationCompleted", IMPL_KIND_KEY to "EXTERNAL_TASK")),
            ServiceTaskDefinition("serviceTask_incrementSubscriptionCounter", engineSpecificProperties = mapOf(IMPL_VALUE_KEY to "counterClass", IMPL_KIND_KEY to "DELEGATE_EXPRESSION")),
        )
        val opServiceTaskById = opServiceTasks.associateBy { it.id }

        assertThat(bpmnModel).usingRecursiveComparison().ignoringCollectionOrder().isEqualTo(
            testSubscribeNewsletterBpmnModel(
                variantName = "withApproval",
                flowNodes = listOf(
                    FlowNodeDefinition("CallActivity_AbortRegistration", BpmnElementType.CALL_ACTIVITY,
                        displayName = "Abort registration",
                        properties = FlowNodeProperties.CallActivity(CallActivityDefinition("CallActivity_AbortRegistration", "abort-registration")),
                        variables = listOf(VariableDefinition("subscriptionId"), VariableDefinition("reasonCode"), VariableDefinition("abortResult")),
                        previousElements = listOf("Timer_After3Days"),
                        followingElements = listOf("CompensationEndEvent_RegistrationAborted"),
                        engineSpecificProperties = mapOf(ASYNC_BEFORE_KEY to true, ASYNC_AFTER_KEY to true)),
                    FlowNodeDefinition("Activity_ConfirmRegistration", BpmnElementType.USER_TASK,
                        displayName = "Confirm subscription",
                        variables = listOf(VariableDefinition("subscriptionId")),
                        attachedElements = listOf("Timer_EveryDay"),
                        parentId = "SubProcess_Confirmation",
                        previousElements = listOf("Activity_SendConfirmationMail"),
                        followingElements = listOf("EndEvent_SubscriptionConfirmed"),
                        engineSpecificProperties = mapOf(ASYNC_AFTER_KEY to true)),
                    FlowNodeDefinition("Activity_SendConfirmationMail", BpmnElementType.SERVICE_TASK,
                        displayName = "Send confirmation mail",
                        properties = FlowNodeProperties.ServiceTask(opServiceTaskById["Activity_SendConfirmationMail"]!!),
                        variables = listOf(VariableDefinition("subscriptionId"), VariableDefinition("otherVariable")),
                        parentId = "SubProcess_Confirmation",
                        previousElements = listOf("StartEvent_RequestReceived", "Timer_EveryDay"),
                        followingElements = listOf("Activity_ConfirmRegistration")),
                    FlowNodeDefinition("Activity_SendWelcomeMail", BpmnElementType.SERVICE_TASK,
                        displayName = "Send Welcome-Mail",
                        properties = FlowNodeProperties.ServiceTask(opServiceTaskById["Activity_SendWelcomeMail"]!!),
                        variables = listOf(VariableDefinition("subscriptionId")),
                        previousElements = listOf("SubProcess_Confirmation"),
                        followingElements = listOf("EndEvent_RegistrationCompleted"),
                        engineSpecificProperties = mapOf(ASYNC_BEFORE_KEY to true, ASYNC_AFTER_KEY to true, EXCLUSIVE_KEY to false)),
                    FlowNodeDefinition("CompensationEndEvent_RegistrationAborted", BpmnElementType.END_EVENT,
                        displayName = "Registration aborted",
                        previousElements = listOf("CallActivity_AbortRegistration")),
                    FlowNodeDefinition("CompensationEvent_OnSubscriptionCounter", BpmnElementType.BOUNDARY_EVENT,
                        displayName = "Registration aborted",
                        attachedToRef = "serviceTask_incrementSubscriptionCounter"),
                    FlowNodeDefinition("CompensationTask_DecrementSubscriptionCounter", BpmnElementType.TASK,
                        displayName = "Decrement subscription counter",
                        variables = listOf(VariableDefinition("subscriptionId"))),
                    FlowNodeDefinition("EndEvent_RegistrationCompleted", BpmnElementType.END_EVENT,
                        displayName = "Registration completed",
                        properties = FlowNodeProperties.ServiceTask(opServiceTaskById["EndEvent_RegistrationCompleted"]!!),
                        variables = listOf(VariableDefinition("subscriptionId")),
                        previousElements = listOf("Activity_SendWelcomeMail")),
                    FlowNodeDefinition("EndEvent_RegistrationNotPossible", BpmnElementType.END_EVENT,
                        displayName = "Registration not possible",
                        previousElements = listOf("ErrorEvent_InvalidMail"),
                        engineSpecificProperties = mapOf(ASYNC_BEFORE_KEY to true, EXCLUSIVE_KEY to false)),
                    FlowNodeDefinition("EndEvent_SubscriptionConfirmed", BpmnElementType.END_EVENT,
                        displayName = "Subscription confirmed",
                        parentId = "SubProcess_Confirmation",
                        previousElements = listOf("Activity_ConfirmRegistration")),
                    FlowNodeDefinition("ErrorEvent_InvalidMail", BpmnElementType.BOUNDARY_EVENT,
                        displayName = "Invalid Mail",
                        attachedToRef = "SubProcess_Confirmation",
                        followingElements = listOf("EndEvent_RegistrationNotPossible")),
                    FlowNodeDefinition("serviceTask_incrementSubscriptionCounter", BpmnElementType.SERVICE_TASK,
                        displayName = "Increment subscription counter",
                        properties = FlowNodeProperties.ServiceTask(opServiceTaskById["serviceTask_incrementSubscriptionCounter"]!!),
                        attachedElements = listOf("CompensationEvent_OnSubscriptionCounter"),
                        previousElements = listOf("StartEvent_SubmitRegistrationForm"),
                        followingElements = listOf("SubProcess_Confirmation")),
                    FlowNodeDefinition("StartEvent_RequestReceived", BpmnElementType.START_EVENT,
                        displayName = "Subscription requested",
                        variables = listOf(VariableDefinition("subscriptionId")),
                        parentId = "SubProcess_Confirmation",
                        followingElements = listOf("Activity_SendConfirmationMail"),
                        engineSpecificProperties = mapOf(ASYNC_BEFORE_KEY to true)),
                    FlowNodeDefinition("StartEvent_SubmitRegistrationForm", BpmnElementType.START_EVENT,
                        displayName = "Submit newsletter form",
                        variables = listOf(VariableDefinition("subscriptionId")),
                        followingElements = listOf("serviceTask_incrementSubscriptionCounter")),
                    FlowNodeDefinition("SubProcess_Confirmation", BpmnElementType.SUB_PROCESS,
                        displayName = "Subscription Confirmation",
                        attachedElements = listOf("ErrorEvent_InvalidMail", "Timer_After3Days"),
                        previousElements = listOf("serviceTask_incrementSubscriptionCounter"),
                        followingElements = listOf("Activity_SendWelcomeMail")),
                    FlowNodeDefinition("Timer_After3Days", BpmnElementType.BOUNDARY_EVENT,
                        displayName = "After 3 days",
                        properties = FlowNodeProperties.Timer(TimerDefinition("Timer_After3Days", "Duration", "\${testVariable}")),
                        attachedToRef = "SubProcess_Confirmation",
                        followingElements = listOf("CallActivity_AbortRegistration")),
                    FlowNodeDefinition("Timer_EveryDay", BpmnElementType.BOUNDARY_EVENT,
                        displayName = "Every day",
                        properties = FlowNodeProperties.Timer(TimerDefinition("Timer_EveryDay", "Duration", "PT1M")),
                        attachedToRef = "Activity_ConfirmRegistration",
                        parentId = "SubProcess_Confirmation",
                        followingElements = listOf("Activity_SendConfirmationMail")),
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
                    CompensationDefinition("CompensationEndEvent_RegistrationAborted", CompensationType.THROWING, engineSpecificProperties = mapOf("activityRef" to "serviceTask_incrementSubscriptionCounter", "waitForCompletion" to false)),
                    CompensationDefinition("CompensationEvent_OnSubscriptionCounter", CompensationType.CATCHING, engineSpecificProperties = mapOf("waitForCompletion" to false)),
                ),
            )
        )
    }

    @Test
    fun `extract returns variantName from process-level extension properties`() {
        val resourceUrl = requireNotNull(javaClass.getResource("/bpmn/operaton-subscribe-newsletter.bpmn"))
        val file = File(resourceUrl.toURI())
        val bpmnModel = underTest.extract(file.inputStream())
        assertThat(bpmnModel.variantName).isEqualTo("withApproval")
    }

    @Test
    fun `extract returns null variantName when not specified`() {
        val resourceUrl = requireNotNull(javaClass.getResource("/bpmn/operaton-send-newsletter.bpmn"))
        val file = File(resourceUrl.toURI())
        val bpmnModel = underTest.extract(file.inputStream())
        assertThat(bpmnModel.variantName).isNull()
    }

    @Test
    fun `extract returns additionalVariables from operaton properties`() {
        val resourceUrl = requireNotNull(javaClass.getResource("/bpmn/operaton-additional-variables.bpmn"))
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
        val resourceUrl = requireNotNull(javaClass.getResource("/bpmn/operaton-send-newsletter.bpmn"))
        val file = File(resourceUrl.toURI())
        val bpmnModel = underTest.extract(file.inputStream())
        assertThat(bpmnModel.variables).containsExactlyInAnyOrder(
            VariableDefinition("authors"),
            VariableDefinition("author"),
            VariableDefinition("subscribers"),
            VariableDefinition("subscriber"),
        )
    }

    @Test
    fun `extract detects event subprocess type and extracts escalations`() {
        val resourceUrl = requireNotNull(javaClass.getResource("/bpmn/operaton-send-newsletter.bpmn"))
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
        val resourceUrl = requireNotNull(javaClass.getResource("/bpmn/operaton-send-newsletter.bpmn"))
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
