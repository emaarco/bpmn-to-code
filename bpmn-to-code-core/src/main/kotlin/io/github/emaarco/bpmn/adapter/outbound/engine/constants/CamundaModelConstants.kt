package io.github.emaarco.bpmn.adapter.outbound.engine.constants

import org.camunda.bpm.model.bpmn.impl.BpmnModelConstants

/**
 * Extends [org.camunda.bpm.model.bpmn.impl.BpmnModelConstants] with additional constants,
 * relevant for engines building up on Camunda 7
 */
object CamundaModelConstants {

    const val ADDITIONAL_INPUT_VARIABLES_PROPERTY_NAME = "additionalInputVariables"
    const val ADDITIONAL_OUTPUT_VARIABLES_PROPERTY_NAME = "additionalOutputVariables"

    val inputOutputParameters = listOf(
        BpmnModelConstants.CAMUNDA_ELEMENT_INPUT_PARAMETER,
        BpmnModelConstants.CAMUNDA_ELEMENT_OUTPUT_PARAMETER
    )

    val callActivityMappingElements = listOf(
        BpmnModelConstants.CAMUNDA_ELEMENT_IN,
        BpmnModelConstants.CAMUNDA_ELEMENT_OUT
    )
}