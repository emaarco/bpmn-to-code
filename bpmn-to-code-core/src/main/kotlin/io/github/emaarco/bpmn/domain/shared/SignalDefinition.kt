package io.github.emaarco.bpmn.domain.shared

import kotlinx.serialization.Serializable

@Serializable
data class SignalDefinition(
    private val id: String,
) : VariableMapping<String> {
    override fun getName() = id
    override fun getValue() = id
}