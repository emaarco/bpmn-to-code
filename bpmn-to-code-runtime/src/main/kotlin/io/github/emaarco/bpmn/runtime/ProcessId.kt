package io.github.emaarco.bpmn.runtime

/**
 * Identifier of a BPMN process definition.
 *
 * `toString()` returns the underlying string, so `.value` is optional in string contexts.
 */
data class ProcessId(val value: String) {
    override fun toString(): String = value
}
