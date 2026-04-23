package io.github.emaarco.bpmn.domain.service

import io.github.emaarco.bpmn.domain.BpmnModel
import io.github.emaarco.bpmn.domain.MergedBpmnModel
import io.github.emaarco.bpmn.domain.shared.ErrorDefinition
import io.github.emaarco.bpmn.domain.shared.EscalationDefinition
import io.github.emaarco.bpmn.domain.shared.FlowNodeDefinition
import io.github.emaarco.bpmn.domain.shared.FlowNodeProperties
import io.github.emaarco.bpmn.domain.shared.MessageDefinition
import io.github.emaarco.bpmn.domain.shared.SequenceFlowDefinition
import io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition
import io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition.Companion.IMPL_VALUE_KEY
import io.github.emaarco.bpmn.domain.shared.SignalDefinition
import io.github.emaarco.bpmn.domain.shared.TimerDefinition
import io.github.emaarco.bpmn.domain.shared.VariableDefinition
import io.github.emaarco.bpmn.domain.testBpmnModel
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class ModelMergerServiceTest {

    private val underTest = ModelMergerService()

    @Test
    fun `merges processes with same id into MergedBpmnModel`() {

        // given: two models with same processId and one with different processId
        val firstFlowNode = FlowNodeDefinition(
            id = "create-order",
            properties = FlowNodeProperties.ServiceTask(
                ServiceTaskDefinition(id = "create-order", engineSpecificProperties = mapOf(IMPL_VALUE_KEY to "firstTaskType")),
            ),
        )
        val secondFlowNode = FlowNodeDefinition(
            id = "update-order",
            properties = FlowNodeProperties.ServiceTask(
                ServiceTaskDefinition(id = "update-order", engineSpecificProperties = mapOf(IMPL_VALUE_KEY to "secondTaskType")),
            ),
        )
        val thirdFlowNode = FlowNodeDefinition(
            id = "delete-order",
            properties = FlowNodeProperties.ServiceTask(
                ServiceTaskDefinition(id = "delete-order", engineSpecificProperties = mapOf(IMPL_VALUE_KEY to "thirdTaskType")),
            ),
        )
        val firstMessage = MessageDefinition(id = "firstMessageId", name = "firstMessageName")
        val secondMessage = MessageDefinition(id = "secondMessageId", name = "secondMessageName")
        val thirdMessage = MessageDefinition(id = "thirdMessageId", name = "thirdMessageName")
        val firstEscalation = EscalationDefinition(id = "ESC_1", name = "firstEscalation", code = "100")
        val secondEscalation = EscalationDefinition(id = "ESC_2", name = "secondEscalation", code = "200")
        val thirdEscalation = EscalationDefinition(id = "ESC_3", name = "thirdEscalation", code = "300")

        val firstModel = testBpmnModel(
            processId = "order-process",
            variantName = "variantA",
            flowNodes = listOf(firstFlowNode, secondFlowNode),
            messages = listOf(firstMessage, secondMessage),
            escalations = listOf(firstEscalation, secondEscalation),
        )
        val secondModel = testBpmnModel(
            processId = "order-process",
            variantName = "variantB",
            flowNodes = listOf(secondFlowNode, thirdFlowNode),
            messages = listOf(secondMessage, thirdMessage),
            escalations = listOf(secondEscalation, thirdEscalation),
        )
        val otherModel = testBpmnModel(
            processId = "other-order-process",
            flowNodes = listOf(firstFlowNode, secondFlowNode),
            messages = listOf(firstMessage, secondMessage),
            escalations = listOf(firstEscalation),
        )

        // when: merging all models
        val result = underTest.mergeModels(listOf(firstModel, secondModel, otherModel))

        // then: multi-model group produces MergedBpmnModel with deduplicated shared elements
        assertThat(result).hasSize(2)

        val orderProcess = result.first { it.processId == "order-process" }
        assertThat(orderProcess).isInstanceOf(MergedBpmnModel::class.java)
        assertThat(orderProcess.flowNodes).containsExactly(firstFlowNode, thirdFlowNode, secondFlowNode)
        assertThat(orderProcess.messages).containsExactly(firstMessage, secondMessage, thirdMessage)
        assertThat(orderProcess.escalations).containsExactly(firstEscalation, secondEscalation, thirdEscalation)
        assertThat((orderProcess as MergedBpmnModel).variants).hasSize(2)

        // and: single-model group stays as BpmnModel
        val otherProcess = result.first { it.processId == "other-order-process" }
        assertThat(otherProcess).isInstanceOf(BpmnModel::class.java)
        assertThat(otherProcess.flowNodes).containsExactly(firstFlowNode, secondFlowNode)
        assertThat(otherProcess.messages).containsExactly(firstMessage, secondMessage)
        assertThat(otherProcess.escalations).containsExactly(firstEscalation)
    }

    @Test
    fun `sorts all collections alphabetically by raw name`() {

        // given: model with unsorted elements
        val model = testBpmnModel(
            processId = "test-process",
            flowNodes = listOf(
                FlowNodeDefinition(id = "z-node", variables = listOf(VariableDefinition("alphaVar"))),
                FlowNodeDefinition(id = "a-node", variables = listOf(VariableDefinition("zetaVar"))),
                FlowNodeDefinition(id = "m-node"),
            ),
            escalations = listOf(
                EscalationDefinition(id = "ESC_Z", name = "zEscalation", code = "300"),
                EscalationDefinition(id = "ESC_A", name = "aEscalation", code = "100"),
                EscalationDefinition(id = "ESC_M", name = "mEscalation", code = "200"),
            ),
        )

        // when: merging a single model
        val result = underTest.mergeModels(listOf(model))

        // then: collections should be sorted independently by their own raw name
        val sortedModel = result.first()
        assertThat(sortedModel.flowNodes.map { it.getRawName() }).containsExactly("a-node", "m-node", "z-node")
        assertThat(sortedModel.variables.map { it.getRawName() }).containsExactly("alphaVar", "zetaVar")
        assertThat(sortedModel.escalations.map { it.getRawName() }).containsExactly("aEscalation", "mEscalation", "zEscalation")
    }

    @Test
    fun `deduplicates all elements within single BPMN model`() {

        // given: a single model with duplicates of various element types
        val timerFlowNode = FlowNodeDefinition(
            id = "TIMER_1",
            properties = FlowNodeProperties.Timer(TimerDefinition(id = "TIMER_1", type = "Date", value = "2024-01-01")),
        )
        val model = testBpmnModel(
            processId = "test-process",
            errors = listOf(
                ErrorDefinition(id = "TEST_ERROR", name = "TEST_ERROR", code = "400"),
                ErrorDefinition(id = "TEST_ERROR", name = "TEST_ERROR", code = "400"),
            ),
            signals = listOf(
                SignalDefinition(id = "TEST_SIGNAL", name = "TEST_SIGNAL"),
                SignalDefinition(id = "TEST_SIGNAL", name = "TEST_SIGNAL"),
            ),
            messages = listOf(
                MessageDefinition(id = "TEST_MESSAGE", name = "TEST_MESSAGE"),
                MessageDefinition(id = "TEST_MESSAGE", name = "TEST_MESSAGE"),
            ),
            flowNodes = listOf(
                FlowNodeDefinition(id = "node-1"),
                FlowNodeDefinition(id = "node-1"),
                timerFlowNode,
                timerFlowNode,
            ),
            escalations = listOf(
                EscalationDefinition(id = "TEST_ESC", name = "TEST_ESC", code = "500"),
                EscalationDefinition(id = "TEST_ESC", name = "TEST_ESC", code = "500"),
            ),
        )

        // when: merging a single model
        val result = underTest.mergeModels(listOf(model))

        // then: duplicates should be removed from all element types
        val merged = result.first()
        assertThat(merged.errors).containsExactly(ErrorDefinition(id = "TEST_ERROR", name = "TEST_ERROR", code = "400"))
        assertThat(merged.signals).containsExactly(SignalDefinition(id = "TEST_SIGNAL", name = "TEST_SIGNAL"))
        assertThat(merged.messages).containsExactly(MessageDefinition(id = "TEST_MESSAGE", name = "TEST_MESSAGE"))
        assertThat(merged.flowNodes).containsExactly(timerFlowNode, FlowNodeDefinition(id = "node-1"))
        assertThat(merged.escalations).containsExactly(EscalationDefinition(id = "TEST_ESC", name = "TEST_ESC", code = "500"))
    }

    @Test
    fun `deduplicates shared elements across multiple BPMN models with same process ID`() {

        // given: two models with overlapping elements
        val firstModel = testBpmnModel(
            processId = "test-process",
            variantName = "variantA",
            errors = listOf(
                ErrorDefinition(id = "ERROR_1", name = "ERROR_1", code = "400"),
                ErrorDefinition(id = "ERROR_2", name = "ERROR_2", code = "500"),
            ),
            signals = listOf(
                SignalDefinition(id = "SIGNAL_1", name = "SIGNAL_1"),
                SignalDefinition(id = "SIGNAL_2", name = "SIGNAL_2"),
            ),
            messages = listOf(
                MessageDefinition(id = "MSG_1", name = "MSG_1"),
                MessageDefinition(id = "MSG_2", name = "MSG_2"),
            ),
            flowNodes = listOf(
                FlowNodeDefinition(id = "node-1"),
                FlowNodeDefinition(id = "node-2"),
            ),
            escalations = listOf(
                EscalationDefinition(id = "ESC_1", name = "ESC_1", code = "100"),
                EscalationDefinition(id = "ESC_2", name = "ESC_2", code = "200"),
            ),
        )
        val secondModel = testBpmnModel(
            processId = "test-process",
            variantName = "variantB",
            errors = listOf(
                ErrorDefinition(id = "ERROR_2", name = "ERROR_2", code = "500"),
                ErrorDefinition(id = "ERROR_3", name = "ERROR_3", code = "600"),
            ),
            signals = listOf(
                SignalDefinition(id = "SIGNAL_2", name = "SIGNAL_2"),
                SignalDefinition(id = "SIGNAL_3", name = "SIGNAL_3"),
            ),
            messages = listOf(
                MessageDefinition(id = "MSG_2", name = "MSG_2"),
                MessageDefinition(id = "MSG_3", name = "MSG_3"),
            ),
            flowNodes = listOf(
                FlowNodeDefinition(id = "node-2"),
                FlowNodeDefinition(id = "node-3"),
            ),
            escalations = listOf(
                EscalationDefinition(id = "ESC_2", name = "ESC_2", code = "200"),
                EscalationDefinition(id = "ESC_3", name = "ESC_3", code = "300"),
            ),
        )

        // when: merging models
        val result = underTest.mergeModels(listOf(firstModel, secondModel))

        // then: should produce MergedBpmnModel with deduplicated shared elements
        assertThat(result).hasSize(1)
        val merged = result.first()
        assertThat(merged).isInstanceOf(MergedBpmnModel::class.java)
        assertThat(merged.errors.map { it.getRawName() }).containsExactly("ERROR_1", "ERROR_2", "ERROR_3")
        assertThat(merged.signals.map { it.getRawName() }).containsExactly("SIGNAL_1", "SIGNAL_2", "SIGNAL_3")
        assertThat(merged.messages.map { it.getRawName() }).containsExactly("MSG_1", "MSG_2", "MSG_3")
        assertThat(merged.flowNodes.map { it.getRawName() }).containsExactly("node-1", "node-2", "node-3")
        assertThat(merged.escalations.map { it.getRawName() }).containsExactly("ESC_1", "ESC_2", "ESC_3")
        assertThat((merged as MergedBpmnModel).variants).hasSize(2)
    }

    @Test
    fun `preserves per-variant sequence flows and flow nodes`() {

        // given: two models with the same processId but different flows
        val sharedNode = FlowNodeDefinition(id = "Gateway_Route")
        val flowDeOnly = SequenceFlowDefinition("Flow_DE", "Gateway_Route", "Task_DE", conditionExpression = "country=DE")
        val flowAtOnly = SequenceFlowDefinition("Flow_AT", "Gateway_Route", "Task_AT", conditionExpression = "country=AT")
        val deModel = testBpmnModel(
            processId = "order-process",
            variantName = "prodDe",
            flowNodes = listOf(sharedNode, FlowNodeDefinition(id = "Task_DE", previousElements = listOf("Gateway_Route"))),
            sequenceFlows = listOf(flowDeOnly),
        )
        val atModel = testBpmnModel(
            processId = "order-process",
            variantName = "prodAt",
            flowNodes = listOf(sharedNode, FlowNodeDefinition(id = "Task_AT", previousElements = listOf("Gateway_Route"))),
            sequenceFlows = listOf(flowAtOnly),
        )

        // when: merging models
        val result = underTest.mergeModels(listOf(deModel, atModel))

        // then: result is a MergedBpmnModel with per-variant data
        assertThat(result).hasSize(1)
        val merged = result.first()
        assertThat(merged).isInstanceOf(MergedBpmnModel::class.java)
        val mergedModel = merged as MergedBpmnModel

        // and: shared flow nodes are deduplicated
        assertThat(mergedModel.flowNodes.map { it.getRawName() }).containsExactly("Gateway_Route", "Task_AT", "Task_DE")

        // and: each variant preserves its own flows and flow nodes
        val deVariant = mergedModel.variants.first { it.variantName == "prodDe" }
        assertThat(deVariant.sequenceFlows).containsExactly(flowDeOnly)
        assertThat(deVariant.flowNodes.map { it.getRawName() }).containsExactly("Gateway_Route", "Task_DE")

        val atVariant = mergedModel.variants.first { it.variantName == "prodAt" }
        assertThat(atVariant.sequenceFlows).containsExactly(flowAtOnly)
        assertThat(atVariant.flowNodes.map { it.getRawName() }).containsExactly("Gateway_Route", "Task_AT")

        // and: top-level sequenceFlows is empty on MergedBpmnModel
        assertThat(mergedModel.sequenceFlows).isEmpty()
    }

    @Test
    fun `returns single model as BpmnModel`() {

        // given: a single model
        val flow = SequenceFlowDefinition("Flow_1", "Start", "End")
        val model = testBpmnModel(
            processId = "simple-process",
            flowNodes = listOf(FlowNodeDefinition(id = "Start"), FlowNodeDefinition(id = "End")),
            sequenceFlows = listOf(flow),
        )

        // when: merging a single model
        val result = underTest.mergeModels(listOf(model))

        // then: single model is returned as BpmnModel, not wrapped
        assertThat(result).hasSize(1)
        assertThat(result.first()).isInstanceOf(BpmnModel::class.java)
        assertThat(result.first().sequenceFlows).containsExactly(flow)
    }

    @Test
    fun `throws when multiple models share processId without variantName`() {

        // given: two models with same processId but no variantName
        val model1 = testBpmnModel(processId = "order-process")
        val model2 = testBpmnModel(processId = "order-process")

        // when / then
        assertThatThrownBy { underTest.mergeModels(listOf(model1, model2)) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("order-process")
            .hasMessageContaining("variantName")
    }

    @Test
    fun `throws when some models have variantName and some do not`() {

        // given: mixed variantName presence
        val model1 = testBpmnModel(processId = "order-process", variantName = "prodDe")
        val model2 = testBpmnModel(processId = "order-process")

        // when / then
        assertThatThrownBy { underTest.mergeModels(listOf(model1, model2)) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("variantName")
    }
}
