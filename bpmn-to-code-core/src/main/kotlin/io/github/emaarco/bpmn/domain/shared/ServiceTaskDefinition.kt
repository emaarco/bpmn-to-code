package io.github.emaarco.bpmn.domain.shared

import io.github.emaarco.bpmn.domain.utils.StringUtils.toUpperSnakeCase

data class ServiceTaskDefinition(
    val id: String?,
    val customProperties: Map<String, Any?> = emptyMap(),
) : VariableMapping<String> {
    override fun getName() = implementationType?.toUpperSnakeCase() ?: ""
    override fun getValue() = implementationType ?: ""
    override fun getRawName() = implementationType ?: ""
    fun hasImplementation() = implementationType != null

    private val implementationType: String? get() = customProperties[IMPL_VALUE_KEY] as? String

    companion object {
        const val IMPL_VALUE_KEY = "implementationValue"
        const val IMPL_KIND_KEY = "implementationKind"
    }
}
