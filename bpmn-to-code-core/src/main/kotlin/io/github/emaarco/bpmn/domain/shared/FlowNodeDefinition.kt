package io.github.emaarco.bpmn.domain.shared

data class FlowNodeDefinition(
    private val id: String,
) : VariableMapping<String> {
    override fun getName() = id.replace("-", "_").replace(".", "_")
    override fun getValue() = id
}