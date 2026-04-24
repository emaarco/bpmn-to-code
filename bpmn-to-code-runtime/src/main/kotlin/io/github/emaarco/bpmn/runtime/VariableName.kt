package io.github.emaarco.bpmn.runtime

/**
 * Name of a process variable declared by a BPMN element.
 *
 * Direction is encoded in the subtype so consumer APIs can enforce it at compile time
 * (e.g. `fun setOutput(v: VariableName.Output, value: Any)`).
 * `toString()` returns the underlying string, so `.value` is optional in string contexts.
 */
sealed interface VariableName {
    val value: String

    /**
     * A variable read by the declaring element (BPMN input mapping).
     */
    data class Input(override val value: String) : VariableName {
        override fun toString(): String = value
    }

    /**
     * A variable written by the declaring element (BPMN output mapping).
     */
    data class Output(override val value: String) : VariableName {
        override fun toString(): String = value
    }

    /**
     * A variable read AND written by the declaring element.
     */
    data class InOut(override val value: String) : VariableName {
        override fun toString(): String = value
    }
}
