package io.github.emaarco.bpmn.domain.shared

data class MessageDefinition(
    private val id: String,
    private val name: String,
) : VariableMapping {
    override fun getName() = id
    override fun getValue() = name
}