package io.github.emaarco.bpmn.domain.shared

sealed interface FlowNodeProperties {
    object None : FlowNodeProperties
    data class ServiceTask(val definition: ServiceTaskDefinition) : FlowNodeProperties
    data class Timer(val definition: TimerDefinition) : FlowNodeProperties
    data class CallActivity(val definition: CallActivityDefinition) : FlowNodeProperties
}
