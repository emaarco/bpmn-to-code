package io.github.emaarco.bpmn.runtime

/**
 * A BPMN timer definition.
 *
 * @param type The BPMN timer type: `Duration` (fires after elapsed time, e.g. `PT10M`),
 *   `Date` (fires at a specific instant, e.g. `2024-01-01T00:00:00Z`), or
 *   `Cycle` (fires repeatedly, e.g. `R3/PT10H`). Kept as a String because the value
 *   is extracted verbatim from the BPMN XML — no engine-level normalization happens.
 * @param timerValue The timer expression (ISO 8601 duration, date, or cycle).
 */
data class BpmnTimer(
    val type: String,
    val timerValue: String,
)
