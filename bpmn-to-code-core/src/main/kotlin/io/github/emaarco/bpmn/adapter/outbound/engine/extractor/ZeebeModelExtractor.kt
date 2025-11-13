package io.github.emaarco.bpmn.adapter.outbound.engine.extractor

import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.findErrorEventDefinition
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.findFlowNodes
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.findMessages
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.findSignalEventDefinitions
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.findTimerEventDefinition
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.getProcessId
import io.github.emaarco.bpmn.domain.BpmnModel
import io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition
import io.github.emaarco.bpmn.domain.shared.VariableDefinition
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
        val allVariables = extractVariables(modelInstance)
        return BpmnModel(
            processId = processId,
            flowNodes = allFlowNodes,
            serviceTasks = allServiceTasks,
            messages = allMessages,
            signals = allSignalEvents,
            errors = allErrorEvents,
            timers = allTimerEvents,
            variables = allVariables
        )
    }

    private fun findServiceTasks(modelInstance: ModelInstance): List<ServiceTaskDefinition> {
        val flowNodes = modelInstance.getModelElementsByType(FlowNode::class.java)
        val flowNodesWithServiceTasks = findAllServiceTaskDefinitions(flowNodes)
        return flowNodesWithServiceTasks.map { (event, taskDefinition) ->
            val id = event.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_ID)
            val type = taskDefinition.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_TYPE)
            ServiceTaskDefinition(id = id, type = type)
        }
    }

    private fun findAllServiceTaskDefinitions(flowNodes: Collection<FlowNode>): List<Pair<FlowNode, ModelElementInstance>> {
        return flowNodes.mapNotNull { node ->
            val extensionElements = node.extensionElements?.elementsQuery?.list() ?: emptyList()
            val taskDefinition = extensionElements.firstOrNull { it.elementType.typeName == "taskDefinition" }
            if (taskDefinition != null) Pair(node, taskDefinition) else null
        }
    }

    // TODO: Analyze refactoring demand
    private fun extractVariables(modelInstance: ModelInstance): List<VariableDefinition> {
        val flowNodes = modelInstance.getModelElementsByType(FlowNode::class.java)
        val variableNames = mutableSetOf<String>()

        flowNodes.forEach { flowNode ->
            val extensionElements = flowNode.extensionElements?.elementsQuery?.list() ?: emptyList()
            val ioMappings = extensionElements.filter { it.elementType.typeName == "ioMapping" }

            ioMappings.forEach { ioMapping ->
                val domElement = ioMapping.domElement
                domElement.childElements.forEach { childDom ->
                    val localName = childDom.localName
                    if (localName == "input" || localName == "output") {
                        val target = childDom.getAttribute("target")
                        if (target != null && target.isNotBlank()) {
                            variableNames.add(target)
                        }
                    }
                }
            }
        }

        return variableNames.map { VariableDefinition(it) }
    }

}