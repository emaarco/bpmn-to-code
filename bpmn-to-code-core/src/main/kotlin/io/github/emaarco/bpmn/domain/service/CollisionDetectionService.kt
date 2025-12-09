package io.github.emaarco.bpmn.domain.service

import io.github.emaarco.bpmn.domain.BpmnModel
import io.github.emaarco.bpmn.domain.shared.VariableMapping
import io.github.emaarco.bpmn.domain.validation.CollisionDetail
import io.github.emaarco.bpmn.domain.validation.VariableNameCollisionException

/**
 * Domain service responsible for detecting name collisions in BPMN models.
 * Such a collision occurs when different configs (for example, for serviceTasks)
 * would normalize to the same constant name in the processApi
 *
 * If this is the case, it cannot be guaranteed that the Process API is complete,
 * because the variables could have different values and only the first would be included.
 *
 * Thus, this class is responsible for detecting such collisions.
 * It throws an exception if any collisions are found.
 */
class CollisionDetectionService {

    /**
     * Validates that no variable name collisions exist in the provided models.
     *
     * @param models The BPMN models to validate
     * @throws VariableNameCollisionException if any collisions are detected
     */
    fun detectCollisions(models: List<BpmnModel>) {
        val allCollisions = models.flatMap { this.findCollisions(it) }
        if (allCollisions.isNotEmpty()) {
            throw VariableNameCollisionException(allCollisions)
        }
    }

    private fun findCollisions(model: BpmnModel): List<CollisionDetail> {
        val modelId = model.processId
        val collisions = mutableListOf<CollisionDetail>()
        collisions.addAll(findCollisionsIn(modelId, model.flowNodes, "FlowNode"))
        collisions.addAll(findCollisionsIn(modelId, model.serviceTasks, "ServiceTask"))
        collisions.addAll(findCollisionsIn(modelId, model.messages, "Message"))
        collisions.addAll(findCollisionsIn(modelId, model.signals, "Signal"))
        collisions.addAll(findCollisionsIn(modelId, model.errors, "Error"))
        collisions.addAll(findCollisionsIn(modelId, model.timers, "Timer"))
        collisions.addAll(findCollisionsIn(modelId, model.variables, "Variable"))
        return collisions
    }

    private fun <T : VariableMapping<*>> findCollisionsIn(
        processId: String,
        items: List<T>,
        variableType: String,
    ): List<CollisionDetail> {
        val distinctItems = items.distinctBy { it.getRawName() }
        val itemsPerVariableName = distinctItems.groupBy { it.getName() }
        val collisions = itemsPerVariableName.filterValues { it.size > 1 }
        return collisions.mapNotNull { (constantName, itemsWithSameName) ->
            val rawNames = itemsWithSameName.map { it.getRawName() }
            if (rawNames.size > 1) {
                CollisionDetail(
                    processId = processId,
                    variableType = variableType,
                    constantName = constantName,
                    conflictingIds = rawNames.sorted(),
                )
            } else {
                null
            }
        }
    }
}
