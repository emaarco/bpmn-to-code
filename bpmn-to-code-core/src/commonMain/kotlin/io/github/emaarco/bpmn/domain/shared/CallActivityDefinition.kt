package io.github.emaarco.bpmn.domain.shared

import io.github.emaarco.bpmn.domain.utils.StringUtils.toUpperSnakeCase

data class CallActivityDefinition(
    val id: String?,
    private val calledElement: String?,
    val engineSpecificProperties: Map<String, Any?> = emptyMap(),
) : VariableMapping<String> {
    override fun getName() = id?.toUpperSnakeCase() ?: ""
    override fun getValue() = calledElement ?: ""
    override fun getRawName() = id ?: ""
    fun hasCalledElement() = calledElement != null
}
