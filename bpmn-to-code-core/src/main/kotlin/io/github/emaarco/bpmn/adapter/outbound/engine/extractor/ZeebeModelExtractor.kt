package io.github.emaarco.bpmn.adapter.outbound.engine.extractor

import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.findErrorEventDefinition
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.findFlowNodes
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.findMessages
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.findSignalEventDefinitions
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.findTimerEventDefinition
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.getProcessId
import io.github.emaarco.bpmn.domain.BpmnModel
import io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition
import org.camunda.bpm.model.bpmn.Bpmn
import org.camunda.bpm.model.bpmn.impl.BpmnModelConstants
import org.camunda.bpm.model.bpmn.instance.FlowNode
import org.camunda.bpm.model.xml.ModelInstance
import org.camunda.bpm.model.xml.instance.ModelElementInstance
import java.io.File

class ZeebeModelExtractor : EngineSpecificExtractor {

    override fun extract(file: File): BpmnModel {
        val modelInstance = Bpmn.readModelFromFile(file)
        val processId = modelInstance.getProcessId()
        val allFlowNodes = modelInstance.findFlowNodes()
        val allMessages = modelInstance.findMessages()
        val allErrorEvents = modelInstance.findErrorEventDefinition()
        val allTimerEvents = modelInstance.findTimerEventDefinition()
        val allSignalEvents = modelInstance.findSignalEventDefinitions()
        val allServiceTasks = findServiceTasks(modelInstance)
        return BpmnModel(
            processId = processId,
            flowNodes = allFlowNodes,
            serviceTasks = allServiceTasks,
            messages = allMessages,
            signals = allSignalEvents,
            errors = allErrorEvents,
            timers = allTimerEvents
        )
    }

    private fun findServiceTasks(modelInstance: ModelInstance): List<ServiceTaskDefinition> {
        val flowNodes = modelInstance.getModelElementsByType(FlowNode::class.java)
        val flowNodesWithServiceTasks = findAllServiceTaskDefinitions(flowNodes)
        return flowNodesWithServiceTasks.map { (event, taskDefinition) ->
            ServiceTaskDefinition(
                id = event.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_ID),
                type = taskDefinition.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_TYPE)
            )
        }
    }

    private fun findAllServiceTaskDefinitions(flowNodes: Collection<FlowNode>): List<Pair<FlowNode, ModelElementInstance>> {
        return flowNodes.mapNotNull { node ->
            val extensionElements = node.extensionElements?.elementsQuery?.list() ?: emptyList()
            val taskDefinition = extensionElements.firstOrNull { it.elementType.typeName == "taskDefinition" }
            if (taskDefinition != null) Pair(node, taskDefinition) else null
        }
    }

}