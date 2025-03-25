package io.github.emaarco.bpmn.domain.service

import io.github.emaarco.bpmn.domain.BpmnModel
import io.github.emaarco.bpmn.domain.shared.VariableMapping

class ModelMergerService {

    /**
     * Merges BPMN models by process ID.
     * If multiple models have the same ID, their elements are combined into a single model.
     */
    fun mergeModels(models: List<BpmnModel>): List<BpmnModel> {
        return models.groupBy { it.processId }.map { (processId, modelsWithSameId) ->
            mergeModelsWithSameId(processId, modelsWithSameId)
        }
    }

    /**
     * Merges multiple models that share the same process ID into a single model.
     */
    private fun mergeModelsWithSameId(processId: String, models: List<BpmnModel>): BpmnModel {
        val mergedFlowNodes = mergeDistinctBy(models) { it.flowNodes }
        val mergedMessages = mergeDistinctBy(models) { it.messages }
        val mergedServiceTasks = mergeDistinctBy(models) { it.serviceTasks }
        val mergedSignals = mergeDistinctBy(models) { it.signals }
        val mergedErrors = mergeDistinctBy(models) { it.errors }
        val mergedTimers = mergeDistinctBy(models) { it.timers }
        return BpmnModel(
            processId = processId,
            flowNodes = mergedFlowNodes,
            messages = mergedMessages,
            serviceTasks = mergedServiceTasks,
            signals = mergedSignals,
            errors = mergedErrors,
            timers = mergedTimers
        )
    }

    private fun <T : VariableMapping<*>> mergeDistinctBy(
        models: List<BpmnModel>,
        selector: (BpmnModel) -> List<T>,
    ): List<T> {
        return models.flatMap(selector).distinctBy { it.getName() }
    }
}
