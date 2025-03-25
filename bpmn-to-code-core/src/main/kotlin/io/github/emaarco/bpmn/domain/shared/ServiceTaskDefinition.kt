package io.github.emaarco.bpmn.domain.shared

import kotlinx.serialization.Serializable

@Serializable
data class ServiceTaskDefinition(
    private val id: String,
    private val type: String,
) : VariableMapping<String> {
    override fun getName() = id
    override fun getValue() = type
}