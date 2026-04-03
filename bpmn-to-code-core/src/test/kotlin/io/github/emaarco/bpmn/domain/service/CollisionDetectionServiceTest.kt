package io.github.emaarco.bpmn.domain.service

import io.github.emaarco.bpmn.domain.BpmnModel
import io.github.emaarco.bpmn.domain.shared.ErrorDefinition
import io.github.emaarco.bpmn.domain.shared.FlowNodeDefinition
import io.github.emaarco.bpmn.domain.shared.MessageDefinition
import io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition
import io.github.emaarco.bpmn.domain.shared.SignalDefinition
import io.github.emaarco.bpmn.domain.shared.TimerDefinition
import io.github.emaarco.bpmn.domain.shared.VariableDefinition
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CollisionDetectionServiceTest {

    private val service = CollisionDetectionService()

    @Test
    fun `findCollisions returns empty when no collisions exist`() {
        val model = testBpmnModel(
            processId = "TestProcess",
            flowNodes = listOf(
                FlowNodeDefinition("Activity_Task1"),
                FlowNodeDefinition("Activity_Task2"),
            ),
            serviceTasks = listOf(
                ServiceTaskDefinition("Task1", "newsletter.sendMail"),
                ServiceTaskDefinition("Task2", "newsletter.sendConfirmationMail"),
            ),
            messages = listOf(
                MessageDefinition("Message_FormSubmitted", "Message_FormSubmitted"),
                MessageDefinition("Message_SubscriptionConfirmed", "Message_SubscriptionConfirmed"),
            ),
        )

        assertThat(service.findCollisions(model)).isEmpty()
    }

    @Test
    fun `findCollisions allows true duplicates with same original ID`() {
        val model = testBpmnModel(
            processId = "TestProcess",
            messages = listOf(
                MessageDefinition("Message_Test", "Message_Test"),
                MessageDefinition("Message_Test", "Message_Test"),
            ),
            flowNodes = listOf(
                FlowNodeDefinition("Activity_SendMail"),
                FlowNodeDefinition("Activity_SendMail"),
            ),
        )

        assertThat(service.findCollisions(model)).isEmpty()
    }

    @Test
    fun `findCollisions detects collision with case variation in FlowNodes`() {
        val model = testBpmnModel(
            processId = "TestProcess",
            flowNodes = listOf(
                FlowNodeDefinition("eventData"),
                FlowNodeDefinition("EventData"),
            ),
        )

        val collisions = service.findCollisions(model)
        assertThat(collisions).hasSize(1)
        assertThat(collisions[0].variableType).isEqualTo("FlowNode")
        assertThat(collisions[0].constantName).isEqualTo("EVENT_DATA")
        assertThat(collisions[0].conflictingIds).containsExactlyInAnyOrder("EventData", "eventData")
        assertThat(collisions[0].processId).isEqualTo("TestProcess")
    }

    @Test
    fun `findCollisions detects collision with separator variation in FlowNodes`() {
        val model = testBpmnModel(
            processId = "TestProcess",
            flowNodes = listOf(
                FlowNodeDefinition("endEvent_dataProcessed"),
                FlowNodeDefinition("endEvent-dataProcessed"),
            ),
        )

        val collisions = service.findCollisions(model)
        assertThat(collisions).hasSize(1)
        assertThat(collisions[0].variableType).isEqualTo("FlowNode")
        assertThat(collisions[0].constantName).isEqualTo("END_EVENT_DATA_PROCESSED")
        assertThat(collisions[0].conflictingIds).containsExactlyInAnyOrder(
            "endEvent-dataProcessed",
            "endEvent_dataProcessed",
        )
    }

    @Test
    fun `findCollisions detects collision with mixed case and separator variation`() {
        val model = testBpmnModel(
            processId = "TestProcess",
            flowNodes = listOf(
                FlowNodeDefinition("eventData"),
                FlowNodeDefinition("event-data"),
                FlowNodeDefinition("event_Data"),
            ),
        )

        val collisions = service.findCollisions(model)
        assertThat(collisions).hasSize(1)
        assertThat(collisions[0].variableType).isEqualTo("FlowNode")
        assertThat(collisions[0].constantName).isEqualTo("EVENT_DATA")
        assertThat(collisions[0].conflictingIds).containsExactlyInAnyOrder(
            "event-data",
            "eventData",
            "event_Data",
        )
    }

    @Test
    fun `findCollisions detects collisions in Messages`() {
        val model = testBpmnModel(
            processId = "TestProcess",
            messages = listOf(
                MessageDefinition("message_formSubmitted", "FormSubmitted"),
                MessageDefinition("message-formSubmitted", "FormSubmitted"),
            ),
        )

        val collisions = service.findCollisions(model)
        assertThat(collisions).hasSize(1)
        assertThat(collisions[0].variableType).isEqualTo("Message")
        assertThat(collisions[0].constantName).isEqualTo("MESSAGE_FORM_SUBMITTED")
    }

    @Test
    fun `findCollisions detects collisions in ServiceTasks`() {
        val model = testBpmnModel(
            processId = "TestProcess",
            serviceTasks = listOf(
                ServiceTaskDefinition("task1", "newsletter.sendMail"),
                ServiceTaskDefinition("task2", "newsletter_sendMail"),
            ),
        )

        val collisions = service.findCollisions(model)
        assertThat(collisions).hasSize(1)
        assertThat(collisions[0].variableType).isEqualTo("ServiceTask")
        assertThat(collisions[0].constantName).isEqualTo("NEWSLETTER_SEND_MAIL")
    }

    @Test
    fun `findCollisions detects collisions in Signals`() {
        val model = testBpmnModel(
            processId = "TestProcess",
            signals = listOf(
                SignalDefinition("signal.complete"),
                SignalDefinition("signal_complete"),
            ),
        )

        val collisions = service.findCollisions(model)
        assertThat(collisions).hasSize(1)
        assertThat(collisions[0].variableType).isEqualTo("Signal")
        assertThat(collisions[0].constantName).isEqualTo("SIGNAL_COMPLETE")
    }

    @Test
    fun `findCollisions detects collisions in Errors`() {
        val model = testBpmnModel(
            processId = "TestProcess",
            errors = listOf(
                ErrorDefinition("Error_InvalidMail", "InvalidMail", "400"),
                ErrorDefinition("Error-InvalidMail", "InvalidMail", "400"),
            ),
        )

        val collisions = service.findCollisions(model)
        assertThat(collisions).hasSize(1)
        assertThat(collisions[0].variableType).isEqualTo("Error")
        assertThat(collisions[0].constantName).isEqualTo("ERROR_INVALID_MAIL")
    }

    @Test
    fun `findCollisions detects collisions in Timers`() {
        val model = testBpmnModel(
            processId = "TestProcess",
            timers = listOf(
                TimerDefinition("Duration", "Duration", "PT1M"),
                TimerDefinition("duration", "Duration", "PT2M"),
            ),
        )

        val collisions = service.findCollisions(model)
        assertThat(collisions).hasSize(1)
        assertThat(collisions[0].variableType).isEqualTo("Timer")
        assertThat(collisions[0].constantName).isEqualTo("DURATION")
    }

    @Test
    fun `findCollisions detects collisions in Variables`() {
        val model = testBpmnModel(
            processId = "TestProcess",
            variables = listOf(
                VariableDefinition("userId"),
                VariableDefinition("user_id"),
            ),
        )

        val collisions = service.findCollisions(model)
        assertThat(collisions).hasSize(1)
        assertThat(collisions[0].variableType).isEqualTo("Variable")
        assertThat(collisions[0].constantName).isEqualTo("USER_ID")
    }

    @Test
    fun `findCollisions detects multiple collisions across different variable types`() {
        val model = testBpmnModel(
            processId = "TestProcess",
            flowNodes = listOf(
                FlowNodeDefinition("endEvent_complete"),
                FlowNodeDefinition("endEvent-complete"),
            ),
            messages = listOf(
                MessageDefinition("message_sent", "SentMessage1"),
                MessageDefinition("message-sent", "SentMessage2"),
            ),
            signals = listOf(
                SignalDefinition("signal_ready"),
                SignalDefinition("signal-ready"),
            ),
        )

        val collisions = service.findCollisions(model)
        assertThat(collisions).hasSize(3)
        assertThat(collisions.map { it.variableType }).containsExactlyInAnyOrder(
            "FlowNode",
            "Message",
            "Signal",
        )
    }

    @Test
    fun `findCollisions handles mixed valid and collision cases`() {
        val model = testBpmnModel(
            processId = "TestProcess",
            flowNodes = listOf(
                FlowNodeDefinition("Activity_Task1"),
                FlowNodeDefinition("Activity_Task2"),
                FlowNodeDefinition("Activity_Task3"),
                FlowNodeDefinition("endEvent_complete"),
                FlowNodeDefinition("endEvent-complete"),
            ),
        )

        val collisions = service.findCollisions(model)
        assertThat(collisions).hasSize(1)
        assertThat(collisions[0].constantName).isEqualTo("END_EVENT_COMPLETE")
    }

    private fun testBpmnModel(
        processId: String,
        flowNodes: List<FlowNodeDefinition> = emptyList(),
        serviceTasks: List<ServiceTaskDefinition> = emptyList(),
        messages: List<MessageDefinition> = emptyList(),
        signals: List<SignalDefinition> = emptyList(),
        errors: List<ErrorDefinition> = emptyList(),
        timers: List<TimerDefinition> = emptyList(),
        variables: List<VariableDefinition> = emptyList(),
    ) = BpmnModel(
        processId = processId,
        flowNodes = flowNodes,
        serviceTasks = serviceTasks,
        messages = messages,
        signals = signals,
        errors = errors,
        timers = timers,
        variables = variables,
    )
}
