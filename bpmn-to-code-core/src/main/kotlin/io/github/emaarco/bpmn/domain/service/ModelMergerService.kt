package io.github.emaarco.bpmn.domain.service

import io.github.emaarco.bpmn.domain.BpmnModel
import io.github.emaarco.bpmn.domain.shared.VariableMapping

class ModelMergerService {

    /**
     * Merges BPMN models by process ID.
     * If multiple models have the same ID, their elements are combined into a single model.
     *
     * @return A list of merged BPMN models with sorted elements
     */
    fun mergeModels(models: List<BpmnModel>): List<BpmnModel> {
        val modelsPerProcessId = models.groupBy { it.processId }
        val (singleModels, modelsThatRequireMerging) = modelsPerProcessId.entries.partition { it.value.size == 1 }
        val modelsThatDontRequireMerging = singleModels.flatMap { it.value }.map { it.sortContent() }
        val mergedModels = modelsThatRequireMerging.map { mergeModelsWithSameProcessId(it.key, it.value).sortContent() }
        val allModels = mergedModels + modelsThatDontRequireMerging
        return allModels.sortContent()
    }

    private fun mergeModelsWithSameProcessId(processId: String, models: List<BpmnModel>): BpmnModel {
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

    private fun List<BpmnModel>.sortContent(): List<BpmnModel> {
        return this.map { it.sortContent() }
    }

    private fun BpmnModel.sortContent() = this.copy(
        flowNodes = flowNodes.sortedBy { it.getRawName() },
        serviceTasks = serviceTasks.sortedBy { it.getRawName() },
        messages = messages.sortedBy { it.getRawName() },
        signals = signals.sortedBy { it.getRawName() },
        errors = errors.sortedBy { it.getRawName() },
        timers = timers.sortedBy { it.getRawName() },
        variables = variables.sortedBy { it.getRawName() }
    )
}
