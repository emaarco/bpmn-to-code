package io.github.emaarco.bpmn.adapter.outbound.engine.extractor

import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.getFlowNodes
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.getMessages
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.getProcessId
import io.github.emaarco.bpmn.domain.BpmnModel
import io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition
import org.camunda.bpm.model.bpmn.Bpmn
import org.camunda.bpm.model.bpmn.impl.BpmnModelConstants
import org.camunda.bpm.model.bpmn.instance.ServiceTask
import org.camunda.bpm.model.xml.ModelInstance
import java.io.File

class Camunda7ModelExtractor : EngineSpecificExtractor {

    override fun extract(file: File): BpmnModel {
        val modelInstance = Bpmn.readModelFromFile(file)
        val processId = modelInstance.getProcessId()
        val messages = modelInstance.getMessages()
        val flowNodes = modelInstance.getFlowNodes()
        val serviceTasks = getServiceTaskTypes(modelInstance)
        return BpmnModel(processId, flowNodes, serviceTasks, messages)
    }

    private fun getServiceTaskTypes(modelInstance: ModelInstance): List<ServiceTaskDefinition> {
        val serviceTasks = modelInstance.getModelElementsByType(ServiceTask::class.java)
        return serviceTasks.map { it.toServiceTask() }
    }

    private fun ServiceTask.toServiceTask(): ServiceTaskDefinition {
        val taskId = this.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_ID)
        val camundaTopic = this.camundaTopic ?: throw IllegalStateException("Service task '$taskId' has no topic")
        return ServiceTaskDefinition(id = taskId, type = camundaTopic)
    }
}