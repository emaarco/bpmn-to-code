package io.github.emaarco.bpmn.adapter.outbound.codegen.builder

import io.github.emaarco.bpmn.domain.shared.VariableDirection

/**
 * Nested subtype of the generated `VariableName` sealed interface chosen per variable.
 * `simpleName` is the nested class/record name emitted by the shared-types builders
 * (`KotlinSharedTypesBuilder` / `JavaSharedTypesBuilder`).
 */
internal enum class VariableNameSubtype(val simpleName: String) {
    INPUT("Input"),
    OUTPUT("Output"),
    IN_OUT("InOut");

    companion object {
        fun chooseFor(directions: Set<VariableDirection>): VariableNameSubtype {
            val hasInput = VariableDirection.INPUT in directions
            val hasOutput = VariableDirection.OUTPUT in directions
            return when {
                hasInput && hasOutput -> IN_OUT
                hasInput -> INPUT
                hasOutput -> OUTPUT
                else -> error("Unexpected variable directions: $directions")
            }
        }
    }
}
