package io.github.emaarco.bpmn.adapter.outbound.engine.extractor

import io.github.emaarco.bpmn.adapter.outbound.engine.constants.CamundaModelConstants
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.BaseElementUtils.findExtensionElements
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.DomElementUtils.withAttribute
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.DomElementUtils.withElementName
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelElementInstanceUtils.extractAttribute
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelElementInstanceUtils.filterByType
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.MessageUtils.findAllMessagesWithSource
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.findErrorEventDefinition
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.findFlowNodes
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.findSignalEventDefinitions
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.findTimerEventDefinition
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.getProcessId
import io.github.emaarco.bpmn.domain.BpmnModel
import io.github.emaarco.bpmn.domain.shared.CallActivityDefinition
import io.github.emaarco.bpmn.domain.shared.MessageDefinition
import io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition
import io.github.emaarco.bpmn.domain.shared.VariableDefinition
import io.github.emaarco.bpmn.domain.utils.StringUtils.removeExpressionSyntax
import org.camunda.bpm.model.bpmn.Bpmn
import org.camunda.bpm.model.bpmn.impl.BpmnModelConstants
import org.camunda.bpm.model.bpmn.instance.CallActivity
import org.camunda.bpm.model.bpmn.instance.FlowNode
import org.camunda.bpm.model.bpmn.instance.MessageEventDefinition
import org.camunda.bpm.model.bpmn.instance.MultiInstanceLoopCharacteristics
import org.camunda.bpm.model.bpmn.instance.ServiceTask
import org.camunda.bpm.model.xml.ModelInstance
import org.camunda.bpm.model.xml.instance.ModelElementInstance
import java.io.InputStream

/**
 * Model extractor for Operaton BPMN engine
 * If you are using operaton, but your models are still camunda-7 based, you cannot use this extractor.
 * Instead, you must use the [Camunda7ModelExtractor].
 */
class OperatonModelExtractor : EngineSpecificExtractor {

    private val implKindKey = ServiceTaskDefinition.IMPL_KIND_KEY
    private val implValueKey = ServiceTaskDefinition.IMPL_VALUE_KEY

    companion object {
        private const val NAMESPACE = "http://operaton.org/schema/1.0/bpmn"
    }

    override fun extract(inputStream: InputStream): BpmnModel {
        val modelInstance = Bpmn.readModelFromStream(inputStream)
        val processId = modelInstance.getProcessId()
        val messages = findMessages(modelInstance)
        val flowNodes = modelInstance.findFlowNodes()
        val serviceTasks = getServiceTaskTypes(modelInstance)
        val callActivities = findCallActivities(modelInstance)
        val messageSendEvents = findMessageSendEvents(modelInstance)
        val signals = modelInstance.findSignalEventDefinitions()
        val errors = modelInstance.findErrorEventDefinition()
        val timers = modelInstance.findTimerEventDefinition()
        val variables = extractVariables(modelInstance)
        return BpmnModel(
            processId = processId,
            flowNodes = flowNodes,
            callActivities = callActivities,
            serviceTasks = serviceTasks + messageSendEvents,
            messages = messages,
            signals = signals,
            errors = errors,
            timers = timers,
            variables = variables
        )
    }

    private fun findMessages(modelInstance: ModelInstance): List<MessageDefinition> {
        return modelInstance.findAllMessagesWithSource().map { (elementId, name, _) ->
            MessageDefinition(id = elementId, name = name)
        }
    }

    private fun findCallActivities(modelInstance: ModelInstance): List<CallActivityDefinition> {
        val callActivities = modelInstance.getModelElementsByType(CallActivity::class.java)
        return callActivities.map {
            val id = it.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_ID)
            val calledElement = it.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_CALLED_ELEMENT)
            CallActivityDefinition(id, calledElement)
        }
    }

    private fun getServiceTaskTypes(modelInstance: ModelInstance): List<ServiceTaskDefinition> {
        val serviceTasks = modelInstance.getModelElementsByType(ServiceTask::class.java)
        return serviceTasks.map { it.toServiceTask() }
    }

    private fun ServiceTask.toServiceTask(): ServiceTaskDefinition {
        val taskId = this.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_ID)
        val (kind, implValue) = this.detectImplementation()
        return ServiceTaskDefinition(
            id = taskId,
            customProperties = buildMap {
                put(implValueKey, implValue)
                put(implKindKey, kind)
            }
        )
    }

    private fun ServiceTask.detectImplementation(): Pair<String?, String?> {
        val extractor = { attrName: String -> this.getAttributeValueNs(NAMESPACE, attrName) }
        val delegateExpression = extractor(BpmnModelConstants.CAMUNDA_ATTRIBUTE_DELEGATE_EXPRESSION)
        val javaClass = extractor(BpmnModelConstants.CAMUNDA_ATTRIBUTE_CLASS)
        val topic = extractor(BpmnModelConstants.CAMUNDA_ATTRIBUTE_TOPIC)
        val expression = extractor(BpmnModelConstants.CAMUNDA_ATTRIBUTE_EXPRESSION)
        return when {
            delegateExpression != null -> OperatonImplementationKind.DELEGATE_EXPRESSION.name to delegateExpression
            javaClass != null -> OperatonImplementationKind.JAVA_DELEGATE.name to javaClass
            topic != null -> OperatonImplementationKind.EXTERNAL_TASK.name to topic
            expression != null -> OperatonImplementationKind.EXPRESSION.name to expression
            else -> null to null
        }
    }

    private fun findMessageSendEvents(modelInstance: ModelInstance): List<ServiceTaskDefinition> {
        val messageEvents = modelInstance.getModelElementsByType(MessageEventDefinition::class.java)
        return messageEvents.mapNotNull { event ->
            val (kind, implValue) = event.detectImplementation()
            if (implValue == null) return@mapNotNull null
            val taskId = event.parentElement?.getAttributeValue(BpmnModelConstants.BPMN_ATTRIBUTE_ID)
            ServiceTaskDefinition(
                id = taskId,
                customProperties = buildMap {
                    put(implValueKey, implValue)
                    put(implKindKey, kind)
                }
            )
        }
    }

    private fun MessageEventDefinition.detectImplementation(): Pair<String?, String?> {
        val extractor = { attrName: String -> this.getAttributeValueNs(NAMESPACE, attrName) }
        val topic = extractor(BpmnModelConstants.CAMUNDA_ATTRIBUTE_TOPIC)
        val delegateExpression = extractor(BpmnModelConstants.CAMUNDA_ATTRIBUTE_DELEGATE_EXPRESSION)
        val javaClass = extractor(BpmnModelConstants.CAMUNDA_ATTRIBUTE_CLASS)
        val expression = extractor(BpmnModelConstants.CAMUNDA_ATTRIBUTE_EXPRESSION)
        return when {
            topic != null -> OperatonImplementationKind.EXTERNAL_TASK.name to topic
            delegateExpression != null -> OperatonImplementationKind.DELEGATE_EXPRESSION.name to delegateExpression
            javaClass != null -> OperatonImplementationKind.JAVA_DELEGATE.name to javaClass
            expression != null -> OperatonImplementationKind.EXPRESSION.name to expression
            else -> null to null
        }
    }

    private fun extractVariables(modelInstance: ModelInstance): List<VariableDefinition> {
        val flowNodes = modelInstance.getModelElementsByType(FlowNode::class.java)
        val allExtensions = flowNodes.flatMap { it.findExtensionElements() }
        val ioExtensions = allExtensions.filterByType(BpmnModelConstants.CAMUNDA_ELEMENT_INPUT_OUTPUT)
        val ioVariableNames = extractInputAndOutputVariables(ioExtensions)
        val multiInstanceVariableNames = extractMultiInstanceVariables(flowNodes)
        val callActivityMappingVars = extractCallActivityMappingVariables(allExtensions)
        val propertiesExtensions = allExtensions.filterByType(BpmnModelConstants.CAMUNDA_ELEMENT_PROPERTIES)
        val additionalVariableNames = extractAdditionalVariables(propertiesExtensions)
        val allVariableNames = ioVariableNames + multiInstanceVariableNames + callActivityMappingVars + additionalVariableNames
        return allVariableNames.map { it.removeExpressionSyntax() }.distinct().map { VariableDefinition(it) }
    }

    private fun extractInputAndOutputVariables(
        extensions: List<ModelElementInstance>
    ): List<String> {
        val allChildElements = extensions.flatMap { it.domElement.childElements }
        val ioElements = allChildElements.withElementName(*CamundaModelConstants.inputOutputParameters.toTypedArray())
        val variableNames = ioElements.map { it.getAttribute(BpmnModelConstants.CAMUNDA_ATTRIBUTE_NAME) }
        return variableNames.filterNot { it.isNullOrBlank() }
    }

    private fun extractAdditionalVariables(
        extensions: List<ModelElementInstance>
    ): List<String> {
        val allChildElements = extensions.flatMap { it.domElement.childElements }
        val propertyElements = allChildElements.withElementName(BpmnModelConstants.CAMUNDA_ELEMENT_PROPERTY)
        val filter = BpmnModelConstants.CAMUNDA_ATTRIBUTE_NAME to CamundaModelConstants.ADDITIONAL_VARIABLES_PROPERTY_NAME
        val matchingProperties = propertyElements.withAttribute(filter)
        val rawValues = matchingProperties.map { it.getAttribute(BpmnModelConstants.CAMUNDA_ATTRIBUTE_VALUE) }
        return rawValues.flatMap { it?.split(",") ?: emptyList() }.map { it.trim() }.filter { it.isNotBlank() }
    }

    /**
     * Extracts parent-scope variables from Call Activity in/out mappings:
     * - operaton:in `source` / `sourceExpression`: variables read from the parent and sent to the child
     * - operaton:out `target`: variables written back into the parent after the child completes
     */
    private fun extractCallActivityMappingVariables(
        extensions: List<ModelElementInstance>
    ): List<String> {
        val inElements = extensions.filterByType(BpmnModelConstants.CAMUNDA_ELEMENT_IN)
        val outElements = extensions.filterByType(BpmnModelConstants.CAMUNDA_ELEMENT_OUT)
        val sourceVars = inElements.extractAttribute(BpmnModelConstants.CAMUNDA_ATTRIBUTE_SOURCE)
        val sourceExprVars = inElements.extractAttribute(BpmnModelConstants.CAMUNDA_ATTRIBUTE_SOURCE_EXPRESSION)
        val targetVars = outElements.extractAttribute(BpmnModelConstants.CAMUNDA_ATTRIBUTE_TARGET)
        return sourceVars + sourceExprVars + targetVars
    }

    private fun extractMultiInstanceVariables(
        nodes: Collection<FlowNode>
    ): List<String> {
        val loops = nodes.flatMap { it.getChildElementsByType(MultiInstanceLoopCharacteristics::class.java) }
        val elementVariables = loops.extractVariablesFromLoops(BpmnModelConstants.CAMUNDA_ATTRIBUTE_ELEMENT_VARIABLE)
        val collectionVariables = loops.extractVariablesFromLoops(BpmnModelConstants.CAMUNDA_ATTRIBUTE_COLLECTION)
        val allVariables = elementVariables + collectionVariables
        return allVariables
    }

    private fun List<MultiInstanceLoopCharacteristics>.extractVariablesFromLoops(
        variableType: String
    ): List<String> {
        return this.mapNotNull { it.getAttributeValueNs(NAMESPACE, variableType) }
    }

}
