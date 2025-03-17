package io.github.emaarco.bpmn.domain.shared

data class TimerDefinition(
    private val id: String,
    private val type: String,
    private val value: String,
) : VariableMapping<Pair<String, String>> {
    override fun getName() = id
    override fun getValue() = (type to value)
}