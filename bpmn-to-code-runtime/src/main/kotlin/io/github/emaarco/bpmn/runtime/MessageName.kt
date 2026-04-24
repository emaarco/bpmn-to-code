package io.github.emaarco.bpmn.runtime

/**
 * Name of a BPMN message used for correlation with message catch events.
 *
 * `toString()` returns the underlying string, so `.value` is optional in string contexts.
 */
data class MessageName(val value: String) {
    override fun toString(): String = value
}
