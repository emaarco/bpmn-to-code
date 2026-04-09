package io.github.emaarco.bpmn.adapter.outbound.engine.constants

/**
 * Mimics [org.camunda.bpm.model.bpmn.impl.BpmnModelConstants] for Zeebe
 * Provies constants used in BPMN models of zeebe, required to extract the model.
 */
object ZeebeModelConstants {

    const val ELEMENT_TASK_DEFINITION = "taskDefinition"
    const val ELEMENT_IO_MAPPING = "ioMapping"
    const val ELEMENT_INPUT = "input"
    const val ELEMENT_OUTPUT = "output"
    const val ELEMENT_LOOP_CHARACTERISTICS = "loopCharacteristics"
    const val ELEMENT_SUBSCRIPTION = "subscription"

    const val ATTRIBUTE_PROCESS_ID = "processId"
    const val ATTRIBUTE_CORRELATION_KEY = "correlationKey"
    const val ATTRIBUTE_TARGET = "target"
    const val ATTRIBUTE_INPUT_ELEMENT = "inputElement"
    const val ATTRIBUTE_INPUT_COLLECTION = "inputCollection"
    const val ATTRIBUTE_OUTPUT_ELEMENT = "outputElement"
    const val ATTRIBUTE_OUTPUT_COLLECTION = "outputCollection"
}
