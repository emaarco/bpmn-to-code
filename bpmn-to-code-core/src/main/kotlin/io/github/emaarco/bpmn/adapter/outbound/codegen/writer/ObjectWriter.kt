package io.github.emaarco.bpmn.adapter.outbound.codegen.writer

import io.github.emaarco.bpmn.domain.BpmnModel
import io.github.emaarco.bpmn.domain.shared.ApiObjectType

/**
 * This interface defines a contract for a code-generator
 * It can be used to write a given API object (f.ex. a message or service-task) to a code builder.
 */
interface ObjectWriter<T> {

    /**
     * The type of the API object this writer is responsible for.
     */
    val objectType: ApiObjectType

    /**
     * Writes the API object to the provided builder using data from the BPMN model.
     */
    fun write(builder: T, model: BpmnModel)

    /**
     * Determines if the writer should write the object to the process-api.
     */
    fun shouldWrite(model: BpmnModel): Boolean
}