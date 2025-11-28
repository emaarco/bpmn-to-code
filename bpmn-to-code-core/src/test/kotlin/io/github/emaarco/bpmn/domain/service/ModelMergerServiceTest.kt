package io.github.emaarco.bpmn.domain.service

import io.github.emaarco.bpmn.domain.shared.FlowNodeDefinition
import io.github.emaarco.bpmn.domain.shared.MessageDefinition
import io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition
import io.github.emaarco.bpmn.domain.testBpmnModel
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class ModelMergerServiceTest {

    private val underTest = ModelMergerService()

    @Test
    fun `should merge processes with same id`() {

        // given
        val firstTask = ServiceTaskDefinition(id = "firstTaskId", type = "firstTaskType")
        val secondTask = ServiceTaskDefinition(id = "secondTaskId", type = "secondTaskType")
        val thirdTask = ServiceTaskDefinition(id = "thirdTaskId", type = "thirdTaskType")
        val firstMessage = MessageDefinition(id = "firstMessageId", name = "firstMessageName")
        val secondMessage = MessageDefinition(id = "secondMessageId", name = "secondMessageName")
        val thirdMessage = MessageDefinition(id = "thirdMessageId", name = "thirdMessageName")
        val firstFlowNode = FlowNodeDefinition(id = "create-order")
        val secondFlowNode = FlowNodeDefinition(id = "update-order")
        val thirdFlowNode = FlowNodeDefinition(id = "delete-order")

        val firstModel = testBpmnModel(
            processId = "order-process",
            flowNodes = listOf(firstFlowNode, secondFlowNode),
            messages = listOf(firstMessage, secondMessage),
            serviceTasks = listOf(firstTask, secondTask)
        )

        val secondModel = testBpmnModel(
            processId = "order-process",
            flowNodes = listOf(secondFlowNode, thirdFlowNode),
            messages = listOf(secondMessage, thirdMessage),
            serviceTasks = listOf(secondTask, thirdTask)
        )

        val otherModel = testBpmnModel(
            processId = "other-order-process",
            flowNodes = listOf(firstFlowNode, secondFlowNode),
            messages = listOf(firstMessage, secondMessage),
            serviceTasks = listOf(firstTask, secondTask)
        )

        // when
        val result = underTest.mergeModels(listOf(firstModel, secondModel, otherModel))

        // then
        Assertions.assertThat(result).containsExactly(
            testBpmnModel(
                processId = "order-process",
                flowNodes = listOf(firstFlowNode, secondFlowNode, thirdFlowNode),
                messages = listOf(firstMessage, secondMessage, thirdMessage),
                serviceTasks = listOf(firstTask, secondTask, thirdTask)
            ),
            testBpmnModel(
                processId = "other-order-process",
                flowNodes = listOf(firstFlowNode, secondFlowNode),
                messages = listOf(firstMessage, secondMessage),
                serviceTasks = listOf(firstTask, secondTask)
            )
        )
    }

}
