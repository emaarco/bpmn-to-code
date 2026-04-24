package io.github.emaarco.bpmn.runtime

/**
 * A BPMN escalation definition referenced by escalation catch events and escalation end events.
 *
 * @param name The escalation name as declared in the BPMN model.
 * @param code The escalation code used to match catch events at runtime.
 */
data class BpmnEscalation(
    val name: String,
    val code: String,
)
