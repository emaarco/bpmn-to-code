package io.miragon.bpmn.domain.shared

import io.miragon.bpmn.domain.utils.StringUtils.toUpperSnakeCase

data class EscalationDefinition(
    val id: String?,
    private val name: String?,
    private val code: String?,
    val engineSpecificProperties: Map<String, Any?> = emptyMap(),
) : VariableMapping<Pair<String, String>> {
    override fun getName() = name?.toUpperSnakeCase() ?: ""
    override fun getValue() = (name ?: "") to (code ?: "")
    override fun getRawName() = name ?: ""
    fun hasRequiredFields() = name != null && code != null
}
