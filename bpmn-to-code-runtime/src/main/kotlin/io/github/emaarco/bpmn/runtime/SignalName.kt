package io.github.emaarco.bpmn.runtime

/**
 * Name of a BPMN signal broadcast to all matching catch events.
 *
 * `toString()` returns the underlying string, so `.value` is optional in string contexts.
 */
data class SignalName(val value: String) {
    override fun toString(): String = value
}
