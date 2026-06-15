package io.miragon.bpmn.adapter.outbound.engine.constants

import org.camunda.bpm.model.bpmn.impl.BpmnModelConstants

/**
 * Extends [org.camunda.bpm.model.bpmn.impl.BpmnModelConstants] with additional constants,
 * relevant for engines building up on Camunda 7
 */
object CamundaModelConstants {

    const val ADDITIONAL_INPUT_VARIABLES_PROPERTY_NAME = "additionalInputVariables"
    const val ADDITIONAL_OUTPUT_VARIABLES_PROPERTY_NAME = "additionalOutputVariables"

    const val VARIABLES_ATTRIBUTE = "variables"
    const val VARIABLES_ALL_VALUE = "all"

    val callActivityMappingElements = listOf(
        BpmnModelConstants.CAMUNDA_ELEMENT_IN,
        BpmnModelConstants.CAMUNDA_ELEMENT_OUT
    )
}