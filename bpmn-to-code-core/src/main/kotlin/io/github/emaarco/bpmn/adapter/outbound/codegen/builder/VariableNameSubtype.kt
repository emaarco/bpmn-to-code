package io.github.emaarco.bpmn.adapter.outbound.codegen.builder

import io.github.emaarco.bpmn.domain.shared.FlowNodeDefinition
import io.github.emaarco.bpmn.domain.shared.VariableDefinition
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
            require(directions.isNotEmpty()) { "cannot choose a VariableNameSubtype from an empty direction set" }
            val hasInput = VariableDirection.INPUT in directions
            val hasOutput = VariableDirection.OUTPUT in directions
            return when {
                hasInput && hasOutput -> IN_OUT
                hasInput -> INPUT
                else -> OUTPUT
            }
        }
    }
}

/**
 * Variable ready to be emitted by a ProcessApiBuilder: the representative definition
 * (for name and value) plus the direction-aware subtype chosen from all of the element's
 * declarations of that name.
 */
internal data class TypedVariable(
    val definition: VariableDefinition,
    val subtype: VariableNameSubtype,
)

/**
 * Groups a flow node's variables by name, collapses each group's directions into a
 * single `VariableNameSubtype`, and returns the entries sorted by name so builders
 * emit stable output.
 */
internal fun groupVariablesByName(node: FlowNodeDefinition): List<TypedVariable> {
    val variablesByName = node.variables.groupBy { it.getRawName() }
    val sortedNames = variablesByName.keys.sorted()
    return sortedNames.map { rawName ->
        val group = variablesByName.getValue(rawName)
        val directions = group.map { it.direction }.toSet()
        TypedVariable(group.first(), VariableNameSubtype.chooseFor(directions))
    }
}
