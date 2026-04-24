package io.github.emaarco.bpmn.runtime

/**
 * Identifier of an element (task, gateway, event, ...) inside a BPMN process.
 *
 * `toString()` returns the underlying string, so `.value` is optional in string contexts.
 */
data class ElementId(val value: String) {
    override fun toString(): String = value
}
