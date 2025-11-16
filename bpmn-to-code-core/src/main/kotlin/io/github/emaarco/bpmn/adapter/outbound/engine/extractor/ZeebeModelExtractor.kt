package io.github.emaarco.bpmn.adapter.outbound.engine.extractor

import io.github.emaarco.bpmn.adapter.outbound.engine.utils.FlowNodeUtils.findExtensionElements
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.FlowNodeUtils.findExtensionElementsWithType
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
import java.io.InputStream

class ZeebeModelExtractor : EngineSpecificExtractor {

    override fun extract(inputStream: InputStream): BpmnModel {
        val modelInstance = Bpmn.readModelFromStream(inputStream)
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
            val extensionElements = node.findExtensionElements()
            val taskDefinition = extensionElements.firstOrNull { it.elementType.typeName == "taskDefinition" }
            if (taskDefinition != null) Pair(node, taskDefinition) else null
        }
    }

    private fun extractVariables(modelInstance: ModelInstance): List<VariableDefinition> {
        val flowNodes = modelInstance.getModelElementsByType(FlowNode::class.java)
        val extensions = flowNodes.flatMap { it.findExtensionElementsWithType(type = "ioMapping") }
        val variableNames = extractInputAndOutputVariables(extensions)
        return variableNames.distinct().map { VariableDefinition(it) }
    }

    private fun extractInputAndOutputVariables(
        extensions: List<ModelElementInstance>
    ): List<String> {
        val allowedDefinitions = listOf("input", "output")
        val allElementsInContainer = extensions.flatMap { it.domElement.childElements }
        val eitherInputOrOutput = allElementsInContainer.filter { allowedDefinitions.contains(it.localName) }
        val variableNames = eitherInputOrOutput.map { it.getAttribute("target") }
        return variableNames.filterNot { it.isNullOrBlank() }
    }

}