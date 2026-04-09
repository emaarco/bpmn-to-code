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

    @JvmField
    val MISSING_SERVICE_TASK_IMPLEMENTATION: BpmnValidationRule = MissingServiceTaskImplementationRule()

    @JvmField
    val MISSING_MESSAGE_NAME: BpmnValidationRule = MissingMessageNameRule()

    @JvmField
    val MISSING_ERROR_DEFINITION: BpmnValidationRule = MissingErrorDefinitionRule()

    @JvmField
    val MISSING_SIGNAL_NAME: BpmnValidationRule = MissingSignalNameRule()

    @JvmField
    val MISSING_TIMER_DEFINITION: BpmnValidationRule = MissingTimerDefinitionRule()

    @JvmField
    val MISSING_CALLED_ELEMENT: BpmnValidationRule = MissingCalledElementRule()

    @JvmField
    val MISSING_ELEMENT_ID: BpmnValidationRule = MissingElementIdRule()

    @JvmField
    val INVALID_IDENTIFIER: BpmnValidationRule = InvalidIdentifierRule()

    @JvmField
    val EMPTY_PROCESS: BpmnValidationRule = EmptyProcessRule()

    @JvmField
    val MISSING_PROCESS_ID: BpmnValidationRule = MissingProcessIdRule()

    @JvmField
    val COLLISION_DETECTION: BpmnValidationRule = CollisionDetectionRule()

    /**
     * Returns all built-in BPMN validation rules.
     */
    @JvmStatic
    fun all(): List<BpmnValidationRule> = listOf(
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
