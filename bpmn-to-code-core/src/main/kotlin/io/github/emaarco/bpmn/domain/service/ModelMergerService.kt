package io.github.emaarco.bpmn.domain.service

import io.github.emaarco.bpmn.domain.BpmnModel
import io.github.emaarco.bpmn.domain.shared.VariableMapping

class ModelMergerService {

    /**
     * Merges BPMN models by process ID.
     * If multiple models have the same ID, their elements are combined into a single model.
     */
    fun mergeModels(models: List<BpmnModel>): List<BpmnModel> {
        val modelsPerProcessId = models.groupBy { it.processId }
        val (singleModels, modelsThatRequireMerging) = modelsPerProcessId.entries.partition { it.value.size == 1 }
        val modelsThatDontRequireMerging = singleModels.flatMap { it.value }
        val mergedModels = modelsThatRequireMerging.map { mergeModelsWithSameId(it.key, it.value) }
        return modelsThatDontRequireMerging + mergedModels
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
        val mergedVariables = mergeDistinctBy(models) { it.variables }
        return BpmnModel(
            processId = processId,
            flowNodes = mergedFlowNodes,
            messages = mergedMessages,
            serviceTasks = mergedServiceTasks,
            signals = mergedSignals,
            errors = mergedErrors,
            timers = mergedTimers,
            variables = mergedVariables
        )
    }

    private fun <T : VariableMapping<*>> mergeDistinctBy(
        models: List<BpmnModel>,
        selector: (BpmnModel) -> List<T>,
    ): List<T> {
        return models.flatMap(selector).distinctBy { it.getRawName() }
    }
}
