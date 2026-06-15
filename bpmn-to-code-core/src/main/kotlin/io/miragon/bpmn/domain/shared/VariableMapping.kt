package io.miragon.bpmn.domain.shared

interface VariableMapping<T : Any> {
    fun getName(): String
    fun getValue(): T
    fun getRawName(): String = getName()
}
