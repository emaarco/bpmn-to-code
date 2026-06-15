package io.miragon.bpmn.domain.shared

import io.miragon.bpmn.domain.utils.StringUtils.toUpperSnakeCase

data class CallActivityDefinition(
    val id: String?,
    private val calledElement: String?,
    val mappings: List<CallActivityMapping> = emptyList(),
    val engineSpecificProperties: Map<String, Any?> = emptyMap(),
) : VariableMapping<String> {
    override fun getName() = id?.toUpperSnakeCase() ?: ""
    override fun getValue() = calledElement ?: ""
    override fun getRawName() = id ?: ""
    fun hasCalledElement() = calledElement != null

    val inputMappings get() = mappings.filter { it.direction == VariableDirection.INPUT }
    val outputMappings get() = mappings.filter { it.direction == VariableDirection.OUTPUT }
    val propagateAllInputVariables: Boolean? get() = engineSpecificProperties[PROPAGATE_ALL_INPUT_KEY] as? Boolean
    val propagateAllOutputVariables: Boolean? get() = engineSpecificProperties[PROPAGATE_ALL_OUTPUT_KEY] as? Boolean

    companion object {
        const val PROPAGATE_ALL_INPUT_KEY = "propagateAllInputVariables"
        const val PROPAGATE_ALL_OUTPUT_KEY = "propagateAllOutputVariables"
    }
}
