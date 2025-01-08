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
import org.camunda.bpm.model.xml.instance.ModelElementInstance
import java.io.File

class ZeebeModelExtractor : EngineSpecificExtractor {

    override fun extract(file: File): BpmnModel {
        val modelInstance = Bpmn.readModelFromFile(file)
        val processId = modelInstance.getProcessId()
        val allFlowNodes = modelInstance.getFlowNodes()
        val allMessages = modelInstance.getMessages()
        val allServiceTaskTypes = getServiceTaskTypes(modelInstance)
        return BpmnModel(processId, allFlowNodes, allServiceTaskTypes, allMessages)
    }

    private fun getServiceTaskTypes(modelInstance: ModelInstance): List<ServiceTaskDefinition> {
        val serviceTasks: Collection<ServiceTask> = modelInstance.getModelElementsByType(ServiceTask::class.java)
        return serviceTasks.map { task ->
            val taskId = task.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_ID)
            val taskType = task.findTaskType(taskId)
            ServiceTaskDefinition(id = taskId, type = taskType)
        }
    }

    private fun ServiceTask.findTaskType(taskId: String): String {
        val taskDefinition = this.findTaskDefinition(taskId)
        val taskType = taskDefinition.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_TYPE)
        return taskType ?: throw IllegalStateException("Service task '$taskId' has no type")
    }

    private fun ServiceTask.findTaskDefinition(taskId: String): ModelElementInstance {
        val extensionElements = this.extensionElements?.elementsQuery?.list() ?: emptyList()
        return extensionElements.firstOrNull { it.elementType.typeName == "taskDefinition" }
            ?: throw IllegalStateException("Service task '$taskId' has no task definition")
    }
}