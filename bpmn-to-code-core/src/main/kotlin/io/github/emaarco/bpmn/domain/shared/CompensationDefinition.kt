package io.github.emaarco.bpmn.domain.shared

import io.github.emaarco.bpmn.domain.utils.StringUtils.toUpperSnakeCase

data class CompensationDefinition(
    val id: String?,
    private val activityRef: String?,
    val customProperties: Map<String, Any?> = emptyMap(),
) : VariableMapping<String> {
    override fun getName() = activityRef?.toUpperSnakeCase() ?: ""
    override fun getValue() = activityRef ?: ""
    override fun getRawName() = activityRef ?: ""
    fun hasActivityRef() = activityRef != null
}
