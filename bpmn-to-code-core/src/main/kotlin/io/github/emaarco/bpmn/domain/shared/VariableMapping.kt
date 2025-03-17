package io.github.emaarco.bpmn.domain.shared

interface VariableMapping<T : Any> {
    fun getName(): String
    fun getValue(): T
}
