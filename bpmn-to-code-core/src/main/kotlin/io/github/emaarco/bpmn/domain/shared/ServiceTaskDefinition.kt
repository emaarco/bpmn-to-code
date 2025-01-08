package io.github.emaarco.bpmn.domain.shared

data class ServiceTaskDefinition(
    private val id: String,
    private val type: String,
) : VariableMapping {
    override fun getName() = id
    override fun getValue() = type
}