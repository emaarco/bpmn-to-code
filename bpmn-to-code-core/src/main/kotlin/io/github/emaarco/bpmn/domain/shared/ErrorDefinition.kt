package io.github.emaarco.bpmn.domain.shared

data class ErrorDefinition(
    private val id: String,
    private val name: String,
    private val code: String,
) : VariableMapping<Pair<String, String>> {
    override fun getName() = id.replace(".", "_").replace(".", "_")
    override fun getValue() = name to code
}