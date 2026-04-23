package io.github.emaarco.bpmn.domain.service

import io.github.emaarco.bpmn.domain.shared.ErrorDefinition
import io.github.emaarco.bpmn.domain.shared.FlowNodeDefinition
import io.github.emaarco.bpmn.domain.shared.FlowNodeProperties
import io.github.emaarco.bpmn.domain.shared.MessageDefinition
import io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition
import io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition.Companion.IMPL_VALUE_KEY
import io.github.emaarco.bpmn.domain.shared.SignalDefinition
import io.github.emaarco.bpmn.domain.shared.TimerDefinition
import io.github.emaarco.bpmn.domain.shared.VariableDefinition
import io.github.emaarco.bpmn.domain.shared.VariableDirection
import io.github.emaarco.bpmn.domain.testBpmnModel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CollisionDetectionServiceTest {

    private val underTest = CollisionDetectionService()

    @Test
    fun `findCollisions returns empty when no collisions exist`() {

        // given: a model with distinct constant names across all element types
        val model = testBpmnModel(
            processId = "TestProcess",
            flowNodes = listOf(
                FlowNodeDefinition(id = "Activity_Task1"),
                FlowNodeDefinition(id = "Activity_Task2"),
                FlowNodeDefinition(
                    id = "Task1",
                    properties = FlowNodeProperties.ServiceTask(
                        ServiceTaskDefinition(id = "Task1", engineSpecificProperties = mapOf(IMPL_VALUE_KEY to "newsletter.sendMail"))
                    ),
                ),
                FlowNodeDefinition(
                    id = "Task2",
                    properties = FlowNodeProperties.ServiceTask(
                        ServiceTaskDefinition(id = "Task2", engineSpecificProperties = mapOf(IMPL_VALUE_KEY to "newsletter.sendConfirmationMail"))
                    ),
                ),
            ),
            messages = listOf(
                MessageDefinition(id = "Message_FormSubmitted", name = "Message_FormSubmitted"),
                MessageDefinition(id = "Message_SubscriptionConfirmed", name = "Message_SubscriptionConfirmed"),
            ),
        )

        // when / then: no collisions are detected
        assertThat(underTest.findCollisions(model)).isEmpty()
    }

    @Test
    fun `findCollisions allows true duplicates with same original ID`() {

        // given: a model with exact duplicate elements (same id)
        val model = testBpmnModel(
            processId = "TestProcess",
            messages = listOf(
                MessageDefinition(id = "Message_Test", name = "Message_Test"),
                MessageDefinition(id = "Message_Test", name = "Message_Test"),
            ),
            flowNodes = listOf(
                FlowNodeDefinition(id = "Activity_SendMail"),
                FlowNodeDefinition(id = "Activity_SendMail"),
            ),
        )

        // when / then: true duplicates are not treated as collisions
        assertThat(underTest.findCollisions(model)).isEmpty()
    }

    @Test
    fun `findCollisions detects collision with case variation in FlowNodes`() {

        // given: two flow nodes that differ only in case
        val model = testBpmnModel(
            processId = "TestProcess",
            flowNodes = listOf(
                FlowNodeDefinition(id = "eventData"),
                FlowNodeDefinition(id = "EventData"),
            ),
        )

        // when: checking for collisions
        val collisions = underTest.findCollisions(model)

        // then: one collision is reported with the expected constant name
        assertThat(collisions).hasSize(1)
        assertThat(collisions[0].variableType).isEqualTo("FlowNode")
        assertThat(collisions[0].constantName).isEqualTo("EVENT_DATA")
        assertThat(collisions[0].conflictingIds).containsExactlyInAnyOrder("EventData", "eventData")
        assertThat(collisions[0].processId).isEqualTo("TestProcess")
    }

    @Test
    fun `findCollisions detects collision with separator variation in FlowNodes`() {

        // given: two flow nodes that differ only in separator character
        val model = testBpmnModel(
            processId = "TestProcess",
            flowNodes = listOf(
                FlowNodeDefinition(id = "endEvent_dataProcessed"),
                FlowNodeDefinition(id = "endEvent-dataProcessed"),
            ),
        )

        // when: checking for collisions
        val collisions = underTest.findCollisions(model)

        // then: one collision is reported
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

        // given: three flow nodes that all normalize to the same constant
        val model = testBpmnModel(
            processId = "TestProcess",
            flowNodes = listOf(
                FlowNodeDefinition(id = "eventData"),
                FlowNodeDefinition(id = "event-data"),
                FlowNodeDefinition(id = "event_Data"),
            ),
        )

        // when: checking for collisions
        val collisions = underTest.findCollisions(model)

        // then: one collision groups all three IDs
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

        // given: two messages that normalize to the same constant
        val model = testBpmnModel(
            processId = "TestProcess",
            messages = listOf(
                MessageDefinition(id = "msg1", name = "message_formSubmitted"),
                MessageDefinition(id = "msg2", name = "message-formSubmitted"),
            ),
        )

        // when: checking for collisions
        val collisions = underTest.findCollisions(model)

        // then: one Message collision is reported
        assertThat(collisions).hasSize(1)
        assertThat(collisions[0].variableType).isEqualTo("Message")
        assertThat(collisions[0].constantName).isEqualTo("MESSAGE_FORM_SUBMITTED")
    }

    @Test
    fun `findCollisions detects collisions in ServiceTasks`() {

        // given: two service tasks with implementations that normalize to the same constant
        val model = testBpmnModel(
            processId = "TestProcess",
            flowNodes = listOf(
                FlowNodeDefinition(
                    id = "task1",
                    properties = FlowNodeProperties.ServiceTask(
                        ServiceTaskDefinition(id = "task1", engineSpecificProperties = mapOf(IMPL_VALUE_KEY to "newsletter.sendMail"))
                    ),
                ),
                FlowNodeDefinition(
                    id = "task2",
                    properties = FlowNodeProperties.ServiceTask(
                        ServiceTaskDefinition(id = "task2", engineSpecificProperties = mapOf(IMPL_VALUE_KEY to "newsletter_sendMail"))
                    ),
                ),
            ),
        )

        // when: checking for collisions
        val collisions = underTest.findCollisions(model)

        // then: one ServiceTask collision is reported
        assertThat(collisions).hasSize(1)
        assertThat(collisions[0].variableType).isEqualTo("ServiceTask")
        assertThat(collisions[0].constantName).isEqualTo("NEWSLETTER_SEND_MAIL")
    }

    @Test
    fun `findCollisions detects collisions in Signals`() {

        // given: two signals that normalize to the same constant
        val model = testBpmnModel(
            processId = "TestProcess",
            signals = listOf(
                SignalDefinition(id = "sig1", name = "signal.complete"),
                SignalDefinition(id = "sig2", name = "signal_complete"),
            ),
        )

        // when: checking for collisions
        val collisions = underTest.findCollisions(model)

        // then: one Signal collision is reported
        assertThat(collisions).hasSize(1)
        assertThat(collisions[0].variableType).isEqualTo("Signal")
        assertThat(collisions[0].constantName).isEqualTo("SIGNAL_COMPLETE")
    }

    @Test
    fun `findCollisions detects collisions in Errors`() {

        // given: two errors that normalize to the same constant
        val model = testBpmnModel(
            processId = "TestProcess",
            errors = listOf(
                ErrorDefinition(id = "err1", name = "Error_InvalidMail", code = "400"),
                ErrorDefinition(id = "err2", name = "Error-InvalidMail", code = "400"),
            ),
        )

        // when: checking for collisions
        val collisions = underTest.findCollisions(model)

        // then: one Error collision is reported
        assertThat(collisions).hasSize(1)
        assertThat(collisions[0].variableType).isEqualTo("Error")
        assertThat(collisions[0].constantName).isEqualTo("ERROR_INVALID_MAIL")
    }

    @Test
    fun `findCollisions detects collisions in Timers`() {

        // given: two timer flow nodes that normalize to the same constant
        val model = testBpmnModel(
            processId = "TestProcess",
            flowNodes = listOf(
                FlowNodeDefinition(
                    id = "timer1",
                    properties = FlowNodeProperties.Timer(TimerDefinition(id = "Duration", type = "Duration", value = "PT1M")),
                ),
                FlowNodeDefinition(
                    id = "timer2",
                    properties = FlowNodeProperties.Timer(TimerDefinition(id = "duration", type = "Duration", value = "PT2M")),
                ),
            ),
        )

        // when: checking for collisions
        val collisions = underTest.findCollisions(model)

        // then: one Timer collision is reported
        assertThat(collisions).hasSize(1)
        assertThat(collisions[0].variableType).isEqualTo("Timer")
        assertThat(collisions[0].constantName).isEqualTo("DURATION")
    }

    @Test
    fun `findCollisions detects collisions in Variables`() {

        // given: two nodes with variables that normalize to the same constant
        val model = testBpmnModel(
            processId = "TestProcess",
            flowNodes = listOf(
                FlowNodeDefinition(id = "node1", variables = listOf(VariableDefinition(name = "userId", direction = VariableDirection.INPUT))),
                FlowNodeDefinition(id = "node2", variables = listOf(VariableDefinition(name = "user_id", direction = VariableDirection.INPUT))),
            ),
        )

        // when: checking for collisions
        val collisions = underTest.findCollisions(model)

        // then: one Variable collision is reported
        assertThat(collisions).hasSize(1)
        assertThat(collisions[0].variableType).isEqualTo("Variable")
        assertThat(collisions[0].constantName).isEqualTo("USER_ID")
    }

    @Test
    fun `findCollisions detects multiple collisions across different variable types`() {

        // given: a model with collisions in FlowNodes, Messages, and Signals simultaneously
        val model = testBpmnModel(
            processId = "TestProcess",
            flowNodes = listOf(
                FlowNodeDefinition(id = "endEvent_complete"),
                FlowNodeDefinition(id = "endEvent-complete"),
            ),
            messages = listOf(
                MessageDefinition(id = "msg1", name = "message_sent"),
                MessageDefinition(id = "msg2", name = "message-sent"),
            ),
            signals = listOf(
                SignalDefinition(id = "sig1", name = "signal_ready"),
                SignalDefinition(id = "sig2", name = "signal-ready"),
            ),
        )

        // when: checking for collisions
        val collisions = underTest.findCollisions(model)

        // then: three collisions are detected, one per type
        assertThat(collisions).hasSize(3)
        assertThat(collisions.map { it.variableType }).containsExactlyInAnyOrder(
            "FlowNode",
            "Message",
            "Signal",
        )
    }

    @Test
    fun `findCollisions handles mixed valid and collision cases`() {

        // given: a model where most nodes are unique but two share a constant name
        val model = testBpmnModel(
            processId = "TestProcess",
            flowNodes = listOf(
                FlowNodeDefinition(id = "Activity_Task1"),
                FlowNodeDefinition(id = "Activity_Task2"),
                FlowNodeDefinition(id = "Activity_Task3"),
                FlowNodeDefinition(id = "endEvent_complete"),
                FlowNodeDefinition(id = "endEvent-complete"),
            ),
        )

        // when: checking for collisions
        val collisions = underTest.findCollisions(model)

        // then: only the colliding pair is reported
        assertThat(collisions).hasSize(1)
        assertThat(collisions[0].constantName).isEqualTo("END_EVENT_COMPLETE")
    }
}
