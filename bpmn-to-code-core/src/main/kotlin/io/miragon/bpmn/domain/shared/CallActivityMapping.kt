package io.miragon.bpmn.domain.shared

data class CallActivityMapping(
    val direction: VariableDirection,
    val source: String? = null,
    val sourceExpression: String? = null,
    val target: String? = null,
)
