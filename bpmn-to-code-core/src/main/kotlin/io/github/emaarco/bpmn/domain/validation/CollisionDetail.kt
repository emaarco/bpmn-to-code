package io.github.emaarco.bpmn.domain.validation

/**
 * Represents a single variable name collision within a BPMN model.
 * A collision occurs when different IDs (e.g., "endEvent_dataProcessed" and "endEvent-dataProcessed")
 * are normalized to the same constant name (e.g., "END_EVENT_DATA_PROCESSED").
 */
data class CollisionDetail(
    val variableType: String,
    val constantName: String,
    val conflictingIds: List<String>,
    val processId: String,
)