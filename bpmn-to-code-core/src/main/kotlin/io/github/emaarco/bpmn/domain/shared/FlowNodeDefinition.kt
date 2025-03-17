package io.github.emaarco.bpmn.domain.shared

data class FlowNodeDefinition(
    private val id: String,
) : VariableMapping<String> {
    override fun getName() = id
    override fun getValue() = id
}