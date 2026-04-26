package io.github.emaarco.bpmn.testing

import io.github.emaarco.bpmn.domain.validation.BpmnValidationRule
import io.github.emaarco.bpmn.domain.validation.rules.CollisionDetectionRule
import io.github.emaarco.bpmn.domain.validation.rules.EmptyProcessRule
import io.github.emaarco.bpmn.domain.validation.rules.InvalidIdentifierRule
import io.github.emaarco.bpmn.domain.validation.rules.MissingCalledElementRule
import io.github.emaarco.bpmn.domain.validation.rules.MissingElementIdRule
import io.github.emaarco.bpmn.domain.validation.rules.MissingErrorDefinitionRule
import io.github.emaarco.bpmn.domain.validation.rules.MissingMessageNameRule
import io.github.emaarco.bpmn.domain.validation.rules.MissingProcessIdRule
import io.github.emaarco.bpmn.domain.validation.rules.MissingServiceTaskImplementationRule
import io.github.emaarco.bpmn.domain.validation.rules.MissingSignalNameRule
import io.github.emaarco.bpmn.domain.validation.rules.MissingTimerDefinitionRule

/**
 * Provides access to all built-in BPMN validation rules.
 *
 * Each rule is exposed as a `@JvmField` constant for convenient use from both Kotlin and Java.
 * Use [all] to get a list of all built-in rules.
 */
object BpmnRules {

    /**
     * Without a job type (Zeebe) or delegate expression (Camunda 7), the engine has no worker
     * to route the task to — the process hangs silently at runtime.
     */
    @JvmField
    val MISSING_SERVICE_TASK_IMPLEMENTATION: BpmnValidationRule = MissingServiceTaskImplementationRule()

    /**
     * Message correlation relies on the name as the subscription key; without it the engine
     * can't match incoming messages to catching events.
     */
    @JvmField
    val MISSING_MESSAGE_NAME: BpmnValidationRule = MissingMessageNameRule()

    /**
     * Error boundary events need both name and code to be distinguishable; a missing code means
     * the engine can't differentiate this error from others and will swallow it.
     */
    @JvmField
    val MISSING_ERROR_DEFINITION: BpmnValidationRule = MissingErrorDefinitionRule()

    /**
     * Signals are broadcast by name; without it, no catching event can subscribe
     * — broadcasts are silently lost.
     */
    @JvmField
    val MISSING_SIGNAL_NAME: BpmnValidationRule = MissingSignalNameRule()

    /**
     * Timers without a valid type (Date/Duration/Cycle) are a deployment-time error on most engines.
     */
    @JvmField
    val MISSING_TIMER_DEFINITION: BpmnValidationRule = MissingTimerDefinitionRule()

    /**
     * Call activities resolve their subprocess by process ID at runtime; a missing reference
     * causes a deployment failure or runtime exception.
     */
    @JvmField
    val MISSING_CALLED_ELEMENT: BpmnValidationRule = MissingCalledElementRule()

    /**
     * The generated API derives its constant names from element IDs; elements without an ID
     * are silently omitted from the API.
     */
    @JvmField
    val MISSING_ELEMENT_ID: BpmnValidationRule = MissingElementIdRule()

    /**
     * Element IDs that can't be converted to valid SCREAMING_SNAKE_CASE produce malformed or
     * missing constants — the generated API becomes inconsistent.
     * Reported as WARN to allow gradual migration of existing processes.
     */
    @JvmField
    val INVALID_IDENTIFIER: BpmnValidationRule = InvalidIdentifierRule()

    /**
     * A process with no flow nodes produces an empty generated API, which is almost always
     * a modeling mistake. Reported as WARN since it is technically valid BPMN.
     */
    @JvmField
    val EMPTY_PROCESS: BpmnValidationRule = EmptyProcessRule()

    /**
     * The process ID is the deployment key and primary correlation handle;
     * a process without one can't be deployed.
     */
    @JvmField
    val MISSING_PROCESS_ID: BpmnValidationRule = MissingProcessIdRule()

    /**
     * When multiple BPMN files declare the same process ID (e.g., engine variants), conflicting
     * element definitions are silently overwritten during merge — this rule surfaces those
     * conflicts before code generation.
     */
    @JvmField
    val COLLISION_DETECTION: BpmnValidationRule = CollisionDetectionRule()

    /**
     * Returns all built-in BPMN validation rules.
     */
    @JvmStatic
    fun all(): List<BpmnValidationRule> {
        return listOf(
            MISSING_SERVICE_TASK_IMPLEMENTATION,
            MISSING_MESSAGE_NAME,
            MISSING_ERROR_DEFINITION,
            MISSING_SIGNAL_NAME,
            MISSING_TIMER_DEFINITION,
            MISSING_CALLED_ELEMENT,
            MISSING_ELEMENT_ID,
            INVALID_IDENTIFIER,
            EMPTY_PROCESS,
            MISSING_PROCESS_ID,
            COLLISION_DETECTION,
        )
    }
}
