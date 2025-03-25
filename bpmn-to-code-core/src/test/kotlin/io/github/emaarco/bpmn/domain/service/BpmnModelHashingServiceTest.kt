package io.github.emaarco.bpmn.domain.service

import io.github.emaarco.bpmn.domain.service.BpmnModelHashingService.calculateHash
import io.github.emaarco.bpmn.domain.shared.FlowNodeDefinition
import io.github.emaarco.bpmn.domain.testBpmnModel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BpmnModelHashingServiceTest {

    @Test
    fun hashingDetectsEqualModels() {
        assertThat(firstProcess).usingRecursiveComparison().isEqualTo(sameThanFirstProcess)
        assertThat(firstProcess).usingRecursiveComparison().isNotEqualTo(secondProcess)
        assertThat(firstProcess).usingRecursiveComparison().isNotEqualTo(thirdProcess)
        assertThat(firstProcess.calculateHash()).isEqualTo(sameThanFirstProcess.calculateHash())
        assertThat(firstProcess.calculateHash()).isNotEqualTo(secondProcess.calculateHash())
        assertThat(firstProcess.calculateHash()).isNotEqualTo(thirdProcess.calculateHash())
    }

    private val firstProcess = testBpmnModel(
        processId = "firstProcess",
        flowNodes = listOf(FlowNodeDefinition(id = "create-order"), FlowNodeDefinition(id = "update-order"))
    )

    private val sameThanFirstProcess = testBpmnModel(
        processId = "firstProcess",
        flowNodes = listOf(FlowNodeDefinition(id = "create-order"), FlowNodeDefinition(id = "update-order"))
    )

    private val secondProcess = testBpmnModel(
        processId = "secondProcess",
        flowNodes = listOf(FlowNodeDefinition(id = "update-order"), FlowNodeDefinition(id = "delete-order"))
    )

    private val thirdProcess = testBpmnModel(
        processId = "firstProcess",
        flowNodes = listOf(FlowNodeDefinition(id = "create-order"), FlowNodeDefinition(id = "delete-order"))
    )
}