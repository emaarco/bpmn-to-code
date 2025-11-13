package io.github.emaarco.bpmn.domain.shared

data class VariableDefinition(
    private val name: String,
) : VariableMapping<String> {
    override fun getName() = name.replace("-", "_").replace(".", "_")
    override fun getValue() = name
}
