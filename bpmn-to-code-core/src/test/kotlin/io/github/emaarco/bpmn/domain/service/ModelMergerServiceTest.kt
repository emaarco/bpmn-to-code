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
import io.github.emaarco.bpmn.domain.testBpmnModel
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class ModelMergerServiceTest {

    private val underTest = ModelMergerService()

    @Test
    fun `should merge processes with same id`() {

        // given
        val firstMessage = MessageDefinition(id = "firstMessageId", name = "firstMessageName")
        val secondMessage = MessageDefinition(id = "secondMessageId", name = "secondMessageName")
        val thirdMessage = MessageDefinition(id = "thirdMessageId", name = "thirdMessageName")
        val firstFlowNode = FlowNodeDefinition(id = "create-order",
            properties = FlowNodeProperties.ServiceTask(ServiceTaskDefinition(id = "create-order", customProperties = mapOf(IMPL_VALUE_KEY to "firstTaskType"))))
        val secondFlowNode = FlowNodeDefinition(id = "update-order",
            properties = FlowNodeProperties.ServiceTask(ServiceTaskDefinition(id = "update-order", customProperties = mapOf(IMPL_VALUE_KEY to "secondTaskType"))))
        val thirdFlowNode = FlowNodeDefinition(id = "delete-order",
            properties = FlowNodeProperties.ServiceTask(ServiceTaskDefinition(id = "delete-order", customProperties = mapOf(IMPL_VALUE_KEY to "thirdTaskType"))))

        val firstModel = testBpmnModel(
            processId = "order-process",
            flowNodes = listOf(firstFlowNode, secondFlowNode),
            messages = listOf(firstMessage, secondMessage),
        )

        val secondModel = testBpmnModel(
            processId = "order-process",
            flowNodes = listOf(secondFlowNode, thirdFlowNode),
            messages = listOf(secondMessage, thirdMessage),
        )

        val otherModel = testBpmnModel(
            processId = "other-order-process",
            flowNodes = listOf(firstFlowNode, secondFlowNode),
            messages = listOf(firstMessage, secondMessage),
        )

        // when
        val result = underTest.mergeModels(listOf(firstModel, secondModel, otherModel))

        // then
        Assertions.assertThat(result).containsExactlyInAnyOrder(
            testBpmnModel(
                processId = "order-process",
                flowNodes = listOf(firstFlowNode, thirdFlowNode, secondFlowNode),
                messages = listOf(firstMessage, secondMessage, thirdMessage),
            ),
            testBpmnModel(
                processId = "other-order-process",
                flowNodes = listOf(firstFlowNode, secondFlowNode),
                messages = listOf(firstMessage, secondMessage),
            )
        )
    }

    @Test
    fun `should sort all collections alphabetically by raw name`() {

        // given: model with unsorted elements
        val model = testBpmnModel(
            processId = "test-process",
            flowNodes = listOf(
                FlowNodeDefinition(id = "z-node", variables = listOf(VariableDefinition("alphaVar"))),
                FlowNodeDefinition(id = "a-node", variables = listOf(VariableDefinition("zetaVar"))),
                FlowNodeDefinition(id = "m-node")
            ),
        )

        // when
        val result = underTest.mergeModels(listOf(model))

        // then: collections should be sorted independently by their own raw name
        val sortedModel = result.first()
        val actualFlowNodes = sortedModel.flowNodes.map { it.getRawName() }
        val actualVariables = sortedModel.variables.map { it.getRawName() }
        Assertions.assertThat(actualFlowNodes).containsExactly("a-node", "m-node", "z-node")
        Assertions.assertThat(actualVariables).containsExactly("alphaVar", "zetaVar")
    }

    @Test
    fun `should deduplicate all elements within single BPMN model`() {

        // given: a single model with duplicates of various element types
        val timerFlowNode = FlowNodeDefinition(id = "TIMER_1", properties = FlowNodeProperties.Timer(TimerDefinition(id = "TIMER_1", type = "Date", value = "2024-01-01")))
        val model = testBpmnModel(
            processId = "test-process",
            errors = listOf(
                ErrorDefinition(id = "TEST_ERROR", name = "TEST_ERROR", code = "400"),
                ErrorDefinition(id = "TEST_ERROR", name = "TEST_ERROR", code = "400") // duplicate
            ),
            signals = listOf(
                SignalDefinition(id = "TEST_SIGNAL", name = "TEST_SIGNAL"),
                SignalDefinition(id = "TEST_SIGNAL", name = "TEST_SIGNAL") // duplicate
            ),
            messages = listOf(
                MessageDefinition(id = "TEST_MESSAGE", name = "TEST_MESSAGE"),
                MessageDefinition(id = "TEST_MESSAGE", name = "TEST_MESSAGE") // duplicate
            ),
            flowNodes = listOf(
                FlowNodeDefinition(id = "node-1"),
                FlowNodeDefinition(id = "node-1"), // duplicate
                timerFlowNode,
                timerFlowNode // duplicate
            )
        )

        // when: merging models
        val result = underTest.mergeModels(listOf(model))

        // then: duplicates should be removed from all element types (sorted alphabetically)
        Assertions.assertThat(result).containsExactly(
            testBpmnModel(
                processId = "test-process",
                errors = listOf(ErrorDefinition(id = "TEST_ERROR", name = "TEST_ERROR", code = "400")),
                signals = listOf(SignalDefinition(id = "TEST_SIGNAL", name = "TEST_SIGNAL")),
                messages = listOf(MessageDefinition(id = "TEST_MESSAGE", name = "TEST_MESSAGE")),
                flowNodes = listOf(
                    timerFlowNode,
                    FlowNodeDefinition(id = "node-1")
                )
            )
        )
    }

    @Test
    fun `should deduplicate all elements across multiple BPMN models with same process ID`() {

        // given: two models with the same process ID and overlapping elements
        val firstModel = testBpmnModel(
            processId = "test-process",
            errors = listOf(
                ErrorDefinition(id = "ERROR_1", name = "ERROR_1", code = "400"),
                ErrorDefinition(id = "ERROR_2", name = "ERROR_2", code = "500")
            ),
            signals = listOf(
                SignalDefinition(id = "SIGNAL_1", name = "SIGNAL_1"),
                SignalDefinition(id = "SIGNAL_2", name = "SIGNAL_2")
            ),
            messages = listOf(
                MessageDefinition(id = "MSG_1", name = "MSG_1"),
                MessageDefinition(id = "MSG_2", name = "MSG_2")
            ),
            flowNodes = listOf(
                FlowNodeDefinition(id = "node-1"),
                FlowNodeDefinition(id = "node-2")
            )
        )

        val secondModel = testBpmnModel(
            processId = "test-process",
            errors = listOf(
                ErrorDefinition(id = "ERROR_2", name = "ERROR_2", code = "500"), // duplicate from first model
                ErrorDefinition(id = "ERROR_3", name = "ERROR_3", code = "600")
            ),
            signals = listOf(
                SignalDefinition(id = "SIGNAL_2", name = "SIGNAL_2"), // duplicate from the first model
                SignalDefinition(id = "SIGNAL_3", name = "SIGNAL_3")
            ),
            messages = listOf(
                MessageDefinition(id = "MSG_2", name = "MSG_2"), // duplicate from first model
                MessageDefinition(id = "MSG_3", name = "MSG_3")
            ),
            flowNodes = listOf(
                FlowNodeDefinition(id = "node-2"), // duplicate from first model
                FlowNodeDefinition(id = "node-3")
            )
        )

        // when: merging models
        val result = underTest.mergeModels(listOf(firstModel, secondModel))

        // then: should merge into a single model with deduplicated elements
        Assertions.assertThat(result).containsExactly(
            testBpmnModel(
                processId = "test-process",
                errors = listOf(
                    ErrorDefinition(id = "ERROR_1", name = "ERROR_1", code = "400"),
                    ErrorDefinition(id = "ERROR_2", name = "ERROR_2", code = "500"),
                    ErrorDefinition(id = "ERROR_3", name = "ERROR_3", code = "600")
                ),
                signals = listOf(
                    SignalDefinition(id = "SIGNAL_1", name = "SIGNAL_1"),
                    SignalDefinition(id = "SIGNAL_2", name = "SIGNAL_2"),
                    SignalDefinition(id = "SIGNAL_3", name = "SIGNAL_3")
                ),
                messages = listOf(
                    MessageDefinition(id = "MSG_1", name = "MSG_1"),
                    MessageDefinition(id = "MSG_2", name = "MSG_2"),
                    MessageDefinition(id = "MSG_3", name = "MSG_3")
                ),
                flowNodes = listOf(
                    FlowNodeDefinition(id = "node-1"),
                    FlowNodeDefinition(id = "node-2"),
                    FlowNodeDefinition(id = "node-3")
                )
            )
        )
    }

}
