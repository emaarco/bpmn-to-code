package io.github.emaarco.bpmn.domain.service

import io.github.emaarco.bpmn.domain.BpmnModel
import io.github.emaarco.bpmn.domain.MergedBpmnModel
import io.github.emaarco.bpmn.domain.VariantData
import io.github.emaarco.bpmn.domain.shared.VariableMapping

class ModelMergerService {

    /**
     * Merges BPMN models by process ID.
     * If multiple models have the same ID, their shared elements are combined
     * and variant-specific data (flows, relations) is preserved per variant.
     *
     * @return A list of merged BPMN models with sorted elements
     */
    fun mergeModels(models: List<BpmnModel>): List<MergedBpmnModel> {
        val modelsPerProcess = models.groupBy { it.processId }
        return modelsPerProcess.map { (processId, modelList) ->
            mergeModelsWithSameProcessId(processId, modelList).sortContent()
        }
    }

    private fun mergeModelsWithSameProcessId(processId: String, models: List<BpmnModel>): MergedBpmnModel {
        if (models.size > 1) {
            val modelsWithoutVariant = models.filter { it.variantName.isNullOrBlank() }
            require(modelsWithoutVariant.isEmpty()) {
                "Multiple BPMN files share process ID '$processId' but not all define a variantName. " +
                    "Add a variantName extension property to each process."
            }
        }
        val mergedFlowNodes = mergeDistinctBy(models) { it.flowNodes }
        val mergedMessages = mergeDistinctBy(models) { it.messages }
        val mergedSignals = mergeDistinctBy(models) { it.signals }
        val mergedErrors = mergeDistinctBy(models) { it.errors }
        val mergedEscalations = mergeDistinctBy(models) { it.escalations }
        val mergedCompensations = mergeDistinctBy(models) { it.compensations }
        val variants = models.map { model ->
            VariantData(
                variantName = model.variantName ?: processId,
                sequenceFlows = model.sequenceFlows,
                flowNodes = model.flowNodes,
            )
        }
        return MergedBpmnModel(
            processId = processId,
            flowNodes = mergedFlowNodes,
            messages = mergedMessages,
            signals = mergedSignals,
            errors = mergedErrors,
            escalations = mergedEscalations,
            compensations = mergedCompensations,
            variants = variants,
        )
    }

    private fun <T : VariableMapping<*>> mergeDistinctBy(
        models: List<BpmnModel>,
        selector: (BpmnModel) -> List<T>,
    ): List<T> {
        return models.flatMap(selector)
            .filter { it.getRawName().isNotEmpty() }
            .distinctBy { it.getRawName() }
    }

    private fun MergedBpmnModel.sortContent(): MergedBpmnModel {
        return this.copy(
            flowNodes = flowNodes.sortedBy { it.getRawName() },
            messages = messages.sortedBy { it.getRawName() },
            signals = signals.sortedBy { it.getRawName() },
            errors = errors.sortedBy { it.getRawName() },
            escalations = escalations.sortedBy { it.getRawName() },
            compensations = compensations.sortedBy { it.getRawName() },
            variants = variants.map { variant ->
                variant.copy(
                    sequenceFlows = variant.sequenceFlows.sortedBy { it.getRawName() },
                    flowNodes = variant.flowNodes.sortedBy { it.getRawName() },
                )
            },
        )
    }
}
