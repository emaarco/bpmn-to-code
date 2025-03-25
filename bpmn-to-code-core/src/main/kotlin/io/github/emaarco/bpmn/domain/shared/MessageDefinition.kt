package io.github.emaarco.bpmn.domain.shared

import kotlinx.serialization.Serializable

@Serializable
data class MessageDefinition(
    private val id: String,
    private val name: String,
) : VariableMapping<String> {
    override fun getName() = id
    override fun getValue() = name
}