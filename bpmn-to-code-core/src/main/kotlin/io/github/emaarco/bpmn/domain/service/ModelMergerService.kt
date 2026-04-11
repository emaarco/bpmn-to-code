package io.github.emaarco.bpmn.domain.service

import io.github.emaarco.bpmn.domain.BpmnModel
import io.github.emaarco.bpmn.domain.shared.SequenceFlowDefinition
import io.github.emaarco.bpmn.domain.shared.VariableMapping

class ModelMergerService {

    /**
     * Merges BPMN models by process ID.
     * If multiple models have the same ID, their elements are combined into a single model.
     *
     * @return A list of merged BPMN models with sorted elements
     */
    fun mergeModels(models: List<BpmnModel>): List<BpmnModel> {
        val modelsPerProcess = models.groupBy { it.processId }
        return modelsPerProcess.map { (processId, modelList) ->
            val mergedModels = mergeModelsWithSameProcessId(processId, modelList)
            mergedModels.sortContent()
        }
    }

    private fun mergeModelsWithSameProcessId(processId: String, models: List<BpmnModel>): BpmnModel {
        val mergedFlowNodes = mergeDistinctBy(models) { it.flowNodes }
        val mergedSequenceFlows = mergeSequenceFlows(models)
        val mergedMessages = mergeDistinctBy(models) { it.messages }
        val mergedSignals = mergeDistinctBy(models) { it.signals }
        val mergedErrors = mergeDistinctBy(models) { it.errors }
        return BpmnModel(
            processId = processId,
            flowNodes = mergedFlowNodes,
            sequenceFlows = mergedSequenceFlows,
            messages = mergedMessages,
            signals = mergedSignals,
            errors = mergedErrors,
        )
    }

    private fun mergeSequenceFlows(models: List<BpmnModel>): List<SequenceFlowDefinition> {
        return models.flatMap { it.sequenceFlows }
            .distinctBy { it.sourceRef to it.targetRef }
    }

    private fun <T : VariableMapping<*>> mergeDistinctBy(
        models: List<BpmnModel>,
        selector: (BpmnModel) -> List<T>,
    ): List<T> {
        return models.flatMap(selector)
            .filter { it.getRawName().isNotEmpty() }
            .distinctBy { it.getRawName() }
    }

    private fun List<BpmnModel>.sortContent(): List<BpmnModel> {
        return this.map { it.sortContent() }
    }

    private fun BpmnModel.sortContent() = this.copy(
        flowNodes = flowNodes.sortedBy { it.getRawName() },
        sequenceFlows = sequenceFlows.sortedBy { it.getRawName() },
        messages = messages.sortedBy { it.getRawName() },
        signals = signals.sortedBy { it.getRawName() },
        errors = errors.sortedBy { it.getRawName() },
    )
}
