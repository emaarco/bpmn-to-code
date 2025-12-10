package io.github.emaarco.bpmn.domain.shared

import io.github.emaarco.bpmn.domain.utils.StringUtils.toUpperSnakeCase

data class TimerDefinition(
    private val id: String,
    private val type: String,
    private val value: String,
) : VariableMapping<Pair<String, String>> {
    override fun getName() = id.toUpperSnakeCase()
    override fun getValue() = (type to value)
    override fun getRawName() = id
}