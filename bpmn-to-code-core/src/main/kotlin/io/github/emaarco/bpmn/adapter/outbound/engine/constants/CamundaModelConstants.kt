package io.github.emaarco.bpmn.adapter.outbound.engine.constants

import org.camunda.bpm.model.bpmn.impl.BpmnModelConstants

/**
 * Extends [org.camunda.bpm.model.bpmn.impl.BpmnModelConstants] with additional constants,
 * relevant for engines building up on Camunda 7
 */
object CamundaModelConstants {

    val inputOutputParameters = listOf(
        BpmnModelConstants.CAMUNDA_ELEMENT_INPUT_PARAMETER,
        BpmnModelConstants.CAMUNDA_ELEMENT_OUTPUT_PARAMETER
    )
}