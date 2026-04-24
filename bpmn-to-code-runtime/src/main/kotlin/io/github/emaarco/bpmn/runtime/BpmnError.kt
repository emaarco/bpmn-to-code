package io.github.emaarco.bpmn.runtime

/**
 * A BPMN error definition referenced by error catch events and error end events.
 *
 * @param name The error name as declared in the BPMN model.
 * @param code The error code used to match catch events at runtime.
 */
data class BpmnError(
    val name: String,
    val code: String,
)
