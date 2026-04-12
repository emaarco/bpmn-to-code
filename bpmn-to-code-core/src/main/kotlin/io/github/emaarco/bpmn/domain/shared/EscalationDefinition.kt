package io.github.emaarco.bpmn.domain.shared

import io.github.emaarco.bpmn.domain.utils.StringUtils.toUpperSnakeCase

data class EscalationDefinition(
    val id: String?,
    private val name: String?,
    private val code: String?,
    val customProperties: Map<String, Any?> = emptyMap(),
) : VariableMapping<Pair<String, String>> {
    override fun getName() = name?.toUpperSnakeCase() ?: ""
    override fun getValue() = (name ?: "") to (code ?: "")
    override fun getRawName() = name ?: ""
    fun hasRequiredFields() = name != null && code != null
}
