package io.github.emaarco.bpmn.runtime

/**
 * A BPMN timer definition.
 *
 * @param type One of `Duration`, `Date`, or `Cycle`.
 * @param timerValue The timer expression (ISO 8601 duration, date, or cycle).
 */
data class BpmnTimer(
    val type: String,
    val timerValue: String,
)
