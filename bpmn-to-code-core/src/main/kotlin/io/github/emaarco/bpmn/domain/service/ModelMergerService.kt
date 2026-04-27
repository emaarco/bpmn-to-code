package io.github.emaarco.bpmn.domain.service

import io.github.emaarco.bpmn.domain.BpmnModel
import io.github.emaarco.bpmn.domain.MergedBpmnModel
import io.github.emaarco.bpmn.domain.ProcessModel
import io.github.emaarco.bpmn.domain.MergedBpmnModel.VariantData
import io.github.emaarco.bpmn.domain.shared.FlowNodeDefinition
import io.github.emaarco.bpmn.domain.shared.VariableMapping

class ModelMergerService {

    /**
     * Merges BPMN models by process ID.
     * Single-model processes are returned as-is.
     * Multi-model processes are merged into a [MergedBpmnModel] with variant-scoped flows/relations.
     */
    fun mergeModels(models: List<BpmnModel>): List<ProcessModel> {
        val modelsPerProcess = models.groupBy { it.processId }
        return modelsPerProcess.map { (processId, modelList) ->
            if (modelList.size == 1) {
                deduplicateSingleModel(modelList.first()).sortContent()
            } else {
                mergeModelsWithSameProcessId(processId, modelList).sortContent()
            }
        }
    }

    private fun deduplicateSingleModel(model: BpmnModel): BpmnModel {
        val models = listOf(model)
        return model.copy(
            flowNodes = mergeFlowNodes(models),
            messages = mergeDistinctBy(models) { it.messages },
            signals = mergeDistinctBy(models) { it.signals },
            errors = mergeDistinctBy(models) { it.errors },
            escalations = mergeDistinctBy(models) { it.escalations },
            compensations = mergeDistinctBy(models) { it.compensations },
        )
    }

    private fun mergeModelsWithSameProcessId(processId: String, models: List<BpmnModel>): MergedBpmnModel {
        val modelsWithoutVariant = models.filter { it.variantName.isNullOrBlank() }
        require(modelsWithoutVariant.isEmpty()) {
            "Multiple BPMN files share process ID '$processId' but not all define a variantName. " +
                "Add a variantName extension property to each process."
        }
        return MergedBpmnModel(
            processId = processId,
            flowNodes = mergeFlowNodes(models),
            messages = mergeDistinctBy(models) { it.messages },
            signals = mergeDistinctBy(models) { it.signals },
            errors = mergeDistinctBy(models) { it.errors },
            escalations = mergeDistinctBy(models) { it.escalations },
            compensations = mergeDistinctBy(models) { it.compensations },
            variants = models.map { model ->
                VariantData(
                    variantName = requireNotNull(model.variantName),
                    sequenceFlows = model.sequenceFlows,
                    flowNodes = model.flowNodes,
                )
            },
        )
    }

    /**
     * Merges flow nodes across models by id, unioning additive list fields like `variables`
     * and `attachedElements` so that variant-specific extension data (e.g. additionalInputVariables)
     * is preserved instead of being dropped by simple deduplication.
     */
    private fun mergeFlowNodes(models: List<BpmnModel>): List<FlowNodeDefinition> {
        return models.flatMap { it.flowNodes }
            .filter { it.getRawName().isNotEmpty() }
            .groupBy { it.getRawName() }
            .map { (_, duplicates) ->
                duplicates.first().copy(
                    variables = duplicates.flatMap { it.variables }.distinct(),
                    attachedElements = duplicates.flatMap { it.attachedElements }.distinct(),
                )
            }
    }

    private fun <T : VariableMapping<*>> mergeDistinctBy(
        models: List<BpmnModel>,
        selector: (BpmnModel) -> List<T>,
    ): List<T> {
        return models.flatMap(selector)
            .filter { it.getRawName().isNotEmpty() }
            .distinctBy { it.getRawName() }
    }

    private fun BpmnModel.sortContent(): BpmnModel {
        return this.copy(
            flowNodes = flowNodes.sortedBy { it.getRawName() },
            sequenceFlows = sequenceFlows.sortedBy { it.getRawName() },
            messages = messages.sortedBy { it.getRawName() },
            signals = signals.sortedBy { it.getRawName() },
            errors = errors.sortedBy { it.getRawName() },
            escalations = escalations.sortedBy { it.getRawName() },
            compensations = compensations.sortedBy { it.getRawName() },
        )
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
