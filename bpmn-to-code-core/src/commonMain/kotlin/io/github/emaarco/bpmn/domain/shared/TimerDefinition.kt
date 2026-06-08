package io.github.emaarco.bpmn.domain.shared

import io.github.emaarco.bpmn.domain.utils.StringUtils.toUpperSnakeCase

data class TimerDefinition(
    val id: String?,
    private val type: String?,
    private val value: String?,
    val engineSpecificProperties: Map<String, Any?> = emptyMap(),
) : VariableMapping<Pair<String, String>> {
    override fun getName() = id?.toUpperSnakeCase() ?: ""
    override fun getValue() = (type ?: "") to (value ?: "")
    override fun getRawName() = id ?: ""
    fun hasTimerType() = type != null && value != null
}