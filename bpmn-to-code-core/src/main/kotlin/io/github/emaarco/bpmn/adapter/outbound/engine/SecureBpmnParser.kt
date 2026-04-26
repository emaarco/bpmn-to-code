package io.github.emaarco.bpmn.adapter.outbound.engine

import org.camunda.bpm.model.bpmn.Bpmn
import org.camunda.bpm.model.bpmn.BpmnModelInstance
import java.io.InputStream

internal object SecureBpmnParser {

    fun readModelFromStream(inputStream: InputStream): BpmnModelInstance {
        val bytes = inputStream.readBytes()
        // DOCTYPE declarations enable XXE attacks — reject them before parsing
        if (bytes.decodeToString().contains("<!DOCTYPE", ignoreCase = true)) {
            throw SecurityException("DOCTYPE declarations are not allowed in BPMN files")
        }
        return Bpmn.readModelFromStream(bytes.inputStream())
    }
}
