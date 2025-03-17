package io.github.emaarco.bpmn.domain.service

import io.github.emaarco.bpmn.domain.BpmnModel

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
        val mergedFlowNodes = models.flatMap { it.flowNodes }.distinctBy { it.getName() }
        val mergedMessages = models.flatMap { it.messages }.distinctBy { it.getName() }
        val mergedServiceTasks = models.flatMap { it.serviceTasks }.distinctBy { it.getName() }
        val mergedSignals = models.flatMap { it.signals }.distinctBy { it.getName() }
        val mergedErrors = models.flatMap { it.errors }.distinctBy { it.getName() }
        val mergedTimers = models.flatMap { it.timers }.distinctBy { it.getName() }
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
}
