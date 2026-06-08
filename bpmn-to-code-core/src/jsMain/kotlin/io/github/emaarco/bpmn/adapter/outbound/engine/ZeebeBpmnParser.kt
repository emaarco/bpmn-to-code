package io.github.emaarco.bpmn.adapter.outbound.engine

import io.github.emaarco.bpmn.domain.BpmnModel
import io.github.emaarco.bpmn.domain.shared.BpmnElementType
import io.github.emaarco.bpmn.domain.shared.CallActivityDefinition
import io.github.emaarco.bpmn.domain.shared.CompensationDefinition
import io.github.emaarco.bpmn.domain.shared.CompensationType
import io.github.emaarco.bpmn.domain.shared.ErrorDefinition
import io.github.emaarco.bpmn.domain.shared.EscalationDefinition
import io.github.emaarco.bpmn.domain.shared.FlowNodeDefinition
import io.github.emaarco.bpmn.domain.shared.FlowNodeProperties
import io.github.emaarco.bpmn.domain.shared.MessageDefinition
import io.github.emaarco.bpmn.domain.shared.SequenceFlowDefinition
import io.github.emaarco.bpmn.domain.shared.SignalDefinition
import io.github.emaarco.bpmn.domain.shared.TimerDefinition
import io.github.emaarco.bpmn.domain.shared.VariableDefinition
import io.github.emaarco.bpmn.domain.shared.VariableDirection
import kotlinx.coroutines.await
import kotlin.js.Promise

/**
 * Kotlin/JS Zeebe BPMN parser (bpmn-moddle + zeebe-bpmn-moddle). Mirrors the JVM `ZeebeModelExtractor`
 * to produce an equivalent [BpmnModel]. Async, hence `suspend`; the generate pipeline stays sync.
 */
class ZeebeBpmnParser {

  suspend fun parse(xml: String): BpmnModel {
    val moddle = createModdle()
    val result = moddle.fromXML(xml).unsafeCast<Promise<dynamic>>().await()
    val definitions = result.rootElement
    val rootElements = definitions.rootElements.unsafeCast<Array<dynamic>>()

    val process = rootElements.first { it["\$type"] == "bpmn:Process" }
    val messagesById = collectRootMessages(rootElements)
    val signalsById = collectRootByType(rootElements, "bpmn:Signal")
    val errorsById = collectRootByType(rootElements, "bpmn:Error")
    val escalationsById = collectRootByType(rootElements, "bpmn:Escalation")

    val elements = mutableListOf<ElementContext>()
    collectElements(process, parentSubProcessId = null, out = elements)

    val defaultFlowIds = collectDefaultFlowIds(elements)

    return BpmnModel(
      processId = process.id as String,
      variantName = extractVariantName(process),
      flowNodes = buildFlowNodes(elements),
      sequenceFlows = buildSequenceFlows(elements, defaultFlowIds),
      messages = buildMessages(elements, messagesById),
      signals = buildSignals(elements, signalsById),
      errors = buildErrors(elements, errorsById),
      escalations = buildEscalations(elements, escalationsById),
      compensations = buildCompensations(elements),
    )
  }

  // --- traversal -----------------------------------------------------------------------------

  private class ElementContext(val element: dynamic, val parentSubProcessId: String?) {
    val type: String get() = element["\$type"] as String
    val id: String? get() = element.id as String?
  }

  private fun collectElements(container: dynamic, parentSubProcessId: String?, out: MutableList<ElementContext>) {
    val flowElements = container.flowElements
    if (flowElements == null || flowElements == undefined) return
    for (element in flowElements.unsafeCast<Array<dynamic>>()) {
      out.add(ElementContext(element, parentSubProcessId))
      val type = element["\$type"] as String
      if (type == "bpmn:SubProcess" || type == "bpmn:Transaction" || type == "bpmn:AdHocSubProcess") {
        collectElements(element, parentSubProcessId = element.id as String?, out = out)
      }
    }
  }

  // --- flow nodes ----------------------------------------------------------------------------

  private fun buildFlowNodes(elements: List<ElementContext>): List<FlowNodeDefinition> {
    val flowNodeElements = elements.filter { isFlowNode(it.type) }
    val attachedElementsById = flowNodeElements
      .mapNotNull { ctx -> attachedToRefId(ctx.element)?.let { it to ctx.id } }
      .filter { it.second != null }
      .groupBy({ it.first }, { it.second!! })

    return flowNodeElements.map { ctx ->
      val element = ctx.element
      val id = ctx.id
      FlowNodeDefinition(
        id = id,
        elementType = resolveElementType(element),
        displayName = (element.name as String?)?.normalizeWhitespace(),
        properties = resolveProperties(element),
        variables = extractVariables(element),
        attachedToRef = attachedToRefId(element),
        attachedElements = (id?.let { attachedElementsById[it] }) ?: emptyList(),
        parentId = ctx.parentSubProcessId,
        previousElements = incomingSourceIds(element),
        followingElements = outgoingTargetIds(element),
      )
    }
  }

  private fun isFlowNode(type: String): Boolean = type !in NON_FLOW_NODE_TYPES && type.startsWith("bpmn:")

  private fun resolveElementType(element: dynamic): BpmnElementType {
    val type = element["\$type"] as String
    if (type == "bpmn:SubProcess" && (element.triggeredByEvent as Boolean? == true)) {
      return BpmnElementType.EVENT_SUB_PROCESS
    }
    return BpmnElementType.fromTypeName(typeName(type))
  }

  private fun typeName(type: String): String {
    val local = type.substringAfter(':')
    return local.replaceFirstChar { it.lowercase() }
  }

  private fun attachedToRefId(element: dynamic): String? {
    if (element["\$type"] != "bpmn:BoundaryEvent") return null
    val ref = element.attachedToRef
    return if (ref == null || ref == undefined) null else ref.id as String?
  }

  private fun incomingSourceIds(element: dynamic): List<String> {
    val incoming = element.incoming
    if (incoming == null || incoming == undefined) return emptyList()
    return incoming.unsafeCast<Array<dynamic>>().mapNotNull { flow ->
      val source = flow.sourceRef
      if (source == null || source == undefined) null else source.id as String?
    }
  }

  private fun outgoingTargetIds(element: dynamic): List<String> {
    val outgoing = element.outgoing
    if (outgoing == null || outgoing == undefined) return emptyList()
    return outgoing.unsafeCast<Array<dynamic>>().mapNotNull { flow ->
      val target = flow.targetRef
      if (target == null || target == undefined) null else target.id as String?
    }
  }

  private fun resolveProperties(element: dynamic): FlowNodeProperties {
    serviceTaskDefinition(element)?.let { return FlowNodeProperties.ServiceTask(it) }
    callActivityDefinition(element)?.let { return FlowNodeProperties.CallActivity(it) }
    timerDefinition(element)?.let { return FlowNodeProperties.Timer(it) }
    return FlowNodeProperties.None
  }

  // --- service tasks / call activities -------------------------------------------------------

  private fun serviceTaskDefinition(element: dynamic): io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition? {
    val taskDefinition = extensionValues(element).firstOrNull { it["\$type"] == "zeebe:TaskDefinition" }
      ?: return null
    val type = (taskDefinition.type as String?)?.takeIf { it.isNotBlank() }
    return io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition(
      id = element.id as String?,
      engineSpecificProperties = buildMap {
        put(io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition.IMPL_VALUE_KEY, type)
        put(io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition.IMPL_KIND_KEY, "JOB_WORKER")
      },
    )
  }

  private fun callActivityDefinition(element: dynamic): CallActivityDefinition? {
    if (element["\$type"] != "bpmn:CallActivity") return null
    val calledElement = extensionValues(element).firstOrNull { it["\$type"] == "zeebe:CalledElement" }
    val processId = if (calledElement == null) null else calledElement.processId as String?
    return CallActivityDefinition(id = element.id as String?, calledElement = processId)
  }

  private fun timerDefinition(element: dynamic): TimerDefinition? {
    val timerEvent = eventDefinitions(element).firstOrNull { it["\$type"] == "bpmn:TimerEventDefinition" }
      ?: return null
    val typeAndValue = detectTimerType(timerEvent) ?: return TimerDefinition(element.id as String?, null, null)
    return TimerDefinition(element.id as String?, typeAndValue.first, typeAndValue.second)
  }

  private fun detectTimerType(timerEvent: dynamic): Pair<String, String>? {
    timeExpression(timerEvent.timeDate)?.let { return "Date" to it }
    timeExpression(timerEvent.timeDuration)?.let { return "Duration" to it }
    timeExpression(timerEvent.timeCycle)?.let { return "Cycle" to it }
    return null
  }

  private fun timeExpression(node: dynamic): String? {
    if (node == null || node == undefined) return null
    return node.body as String?
  }

  // --- sequence flows ------------------------------------------------------------------------

  private fun collectDefaultFlowIds(elements: List<ElementContext>): Set<String> {
    return elements
      .filter { it.type == "bpmn:ExclusiveGateway" || it.type == "bpmn:InclusiveGateway" }
      .mapNotNull { ctx ->
        val default = ctx.element.default
        if (default == null || default == undefined) null else default.id as String?
      }
      .toSet()
  }

  private fun buildSequenceFlows(elements: List<ElementContext>, defaultFlowIds: Set<String>): List<SequenceFlowDefinition> {
    return elements
      .filter { it.type == "bpmn:SequenceFlow" }
      .mapNotNull { ctx ->
        val flow = ctx.element
        val source = flow.sourceRef
        val target = flow.targetRef
        if (source == null || source == undefined || target == null || target == undefined) return@mapNotNull null
        val sourceRef = source.id as String? ?: return@mapNotNull null
        val targetRef = target.id as String? ?: return@mapNotNull null
        val id = flow.id as String?
        val flowName = (flow.name as String?)?.normalizeWhitespace()?.takeIf { it.isNotBlank() }
        val condition = conditionExpression(flow)
        SequenceFlowDefinition(
          id = id,
          sourceRef = sourceRef,
          targetRef = targetRef,
          flowName = flowName,
          conditionExpression = condition,
          isDefault = id != null && id in defaultFlowIds,
        )
      }
  }

  private fun conditionExpression(flow: dynamic): String? {
    val expression = flow.conditionExpression
    if (expression == null || expression == undefined) return null
    return (expression.body as String?)?.takeIf { it.isNotBlank() }
  }

  // --- messages / signals / errors / escalations / compensations -----------------------------

  private fun collectRootMessages(rootElements: Array<dynamic>): Map<String, dynamic> {
    val byId = mutableMapOf<String, dynamic>()
    for (element in rootElements) {
      if (element["\$type"] == "bpmn:Message") {
        val id = element.id as String?
        if (id != null) byId[id] = element
      }
    }
    return byId
  }

  private fun collectRootByType(rootElements: Array<dynamic>, type: String): Map<String, dynamic> {
    val byId = mutableMapOf<String, dynamic>()
    for (element in rootElements) {
      if (element["\$type"] == type) {
        val id = element.id as String?
        if (id != null) byId[id] = element
      }
    }
    return byId
  }

  private fun buildMessages(elements: List<ElementContext>, messagesById: Map<String, dynamic>): List<MessageDefinition> {
    val eventBased = elements.flatMap { ctx ->
      eventDefinitions(ctx.element)
        .filter { it["\$type"] == "bpmn:MessageEventDefinition" }
        .mapNotNull { med ->
          val message = resolvedMessage(med, messagesById) ?: return@mapNotNull null
          messageDefinition(ctx.id, message)
        }
    }
    val taskBased = elements
      .filter { it.type == "bpmn:ReceiveTask" }
      .map { ctx ->
        val message = resolvedReceiveTaskMessage(ctx.element, messagesById)
        messageDefinition(ctx.id, message)
      }
    return eventBased + taskBased
  }

  private fun resolvedMessage(eventDefinition: dynamic, messagesById: Map<String, dynamic>): dynamic {
    val ref = eventDefinition.messageRef
    return resolveRef(ref, messagesById)
  }

  private fun resolvedReceiveTaskMessage(task: dynamic, messagesById: Map<String, dynamic>): dynamic {
    val ref = task.messageRef
    return resolveRef(ref, messagesById)
  }

  private fun resolveRef(ref: dynamic, byId: Map<String, dynamic>): dynamic {
    if (ref == null || ref == undefined) return null
    // bpmn-moddle usually resolves IDREFs to the object; fall back to id lookup if it is a string.
    if (jsTypeOf(ref) == "string") return byId[ref as String]
    return ref
  }

  private fun messageDefinition(elementId: String?, message: dynamic): MessageDefinition {
    val name = if (message == null || message == undefined) null else message.name as String?
    val engineSpecificProperties = zeebeSubscriptionProperties(message)
    return MessageDefinition(id = elementId, name = name, engineSpecificProperties = engineSpecificProperties)
  }

  private fun zeebeSubscriptionProperties(message: dynamic): Map<String, Any?> {
    if (message == null || message == undefined) return emptyMap()
    val subscription = extensionValues(message).firstOrNull { it["\$type"] == "zeebe:Subscription" }
      ?: return emptyMap()
    val correlationKey = subscription.correlationKey as String? ?: return emptyMap()
    return mapOf("correlationKey" to correlationKey)
  }

  private fun buildSignals(elements: List<ElementContext>, signalsById: Map<String, dynamic>): List<SignalDefinition> {
    return elements.flatMap { ctx ->
      eventDefinitions(ctx.element)
        .filter { it["\$type"] == "bpmn:SignalEventDefinition" }
        .map { med ->
          val signal = resolveRef(med.signalRef, signalsById)
          val name = if (signal == null || signal == undefined) null else signal.name as String?
          SignalDefinition(id = ctx.id, name = name)
        }
    }
  }

  private fun buildErrors(elements: List<ElementContext>, errorsById: Map<String, dynamic>): List<ErrorDefinition> {
    return elements.flatMap { ctx ->
      eventDefinitions(ctx.element)
        .filter { it["\$type"] == "bpmn:ErrorEventDefinition" }
        .map { med ->
          val error = resolveRef(med.errorRef, errorsById)
          val name = if (error == null || error == undefined) null else error.name as String?
          val code = if (error == null || error == undefined) null else error.errorCode as String?
          ErrorDefinition(id = ctx.id, name = name, code = code)
        }
    }
  }

  private fun buildEscalations(elements: List<ElementContext>, escalationsById: Map<String, dynamic>): List<EscalationDefinition> {
    return elements.flatMap { ctx ->
      eventDefinitions(ctx.element)
        .filter { it["\$type"] == "bpmn:EscalationEventDefinition" }
        .map { med ->
          val escalation = resolveRef(med.escalationRef, escalationsById)
          val name = if (escalation == null || escalation == undefined) null else escalation.name as String?
          val code = if (escalation == null || escalation == undefined) null else escalation.escalationCode as String?
          EscalationDefinition(id = ctx.id, name = name, code = code)
        }
    }
  }

  private fun buildCompensations(elements: List<ElementContext>): List<CompensationDefinition> {
    return elements.flatMap { ctx ->
      eventDefinitions(ctx.element)
        .filter { it["\$type"] == "bpmn:CompensateEventDefinition" }
        .map { med ->
          val type = if (ctx.type == "bpmn:BoundaryEvent") CompensationType.CATCHING else CompensationType.THROWING
          CompensationDefinition(
            id = ctx.id,
            type = type,
            engineSpecificProperties = buildMap {
              val activity = med.activityRef
              if (activity != null && activity != undefined) {
                (activity.id as String?)?.let { put(CompensationDefinition.ACTIVITY_REF_KEY, it) }
              }
              put(CompensationDefinition.WAIT_FOR_COMPLETION_KEY, waitForCompletion(med))
            },
          )
        }
    }
  }

  private fun waitForCompletion(eventDefinition: dynamic): Boolean {
    val value = eventDefinition.waitForCompletion
    // bpmn:CompensateEventDefinition#waitForCompletion defaults to true in the BPMN schema.
    return if (value == null || value == undefined) true else value as Boolean
  }

  // --- variables -----------------------------------------------------------------------------

  private fun extractVariables(element: dynamic): List<VariableDefinition> {
    val ioMappings = extensionValues(element).filter { it["\$type"] == "zeebe:IoMapping" }
    val inputs = ioVariables(ioMappings, "inputParameters", VariableDirection.INPUT)
    val outputs = ioVariables(ioMappings, "outputParameters", VariableDirection.OUTPUT)
    val multiInstance = multiInstanceVariables(element)
    val all = inputs + outputs + multiInstance
    return all.distinct().map { (name, direction, expression) -> VariableDefinition(name, direction, expression) }
  }

  private fun ioVariables(
    ioMappings: List<dynamic>,
    parametersField: String,
    direction: VariableDirection,
  ): List<Triple<String, VariableDirection, String?>> {
    val parameters = ioMappings.flatMap { mapping ->
      val params = mapping[parametersField]
      if (params == null || params == undefined) emptyList() else params.unsafeCast<Array<dynamic>>().toList()
    }
    return parameters.mapNotNull { parameter ->
      val target = (parameter.target as String?)?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
      val source = (parameter.source as String?)?.takeIf { it.isNotBlank() }
      Triple(target, direction, source)
    }
  }

  private fun multiInstanceVariables(element: dynamic): List<Triple<String, VariableDirection, String?>> {
    val loop = element.loopCharacteristics
    if (loop == null || loop == undefined) return emptyList()
    if (loop["\$type"] != "bpmn:MultiInstanceLoopCharacteristics") return emptyList()
    val zeebeLoop = extensionValues(loop).firstOrNull { it["\$type"] == "zeebe:LoopCharacteristics" }
      ?: return emptyList()

    val inputs = listOfNotNull(
      attributeValue(zeebeLoop, "inputElement"),
      attributeValue(zeebeLoop, "inputCollection"),
    ).map { Triple(it.removePrefix("="), VariableDirection.INPUT, it) }
    val outputs = listOfNotNull(
      attributeValue(zeebeLoop, "outputElement"),
      attributeValue(zeebeLoop, "outputCollection"),
    ).map { Triple(it.removePrefix("="), VariableDirection.OUTPUT, it) }
    return inputs + outputs
  }

  private fun attributeValue(node: dynamic, field: String): String? {
    val value = node[field]
    return if (value == null || value == undefined) null else value as String?
  }

  // --- extension / event-definition helpers --------------------------------------------------

  private fun extensionValues(element: dynamic): List<dynamic> {
    val extensions = element.extensionElements
    if (extensions == null || extensions == undefined) return emptyList()
    val values = extensions.values
    if (values == null || values == undefined) return emptyList()
    return values.unsafeCast<Array<dynamic>>().toList()
  }

  private fun eventDefinitions(element: dynamic): List<dynamic> {
    val definitions = element.eventDefinitions
    if (definitions == null || definitions == undefined) return emptyList()
    return definitions.unsafeCast<Array<dynamic>>().toList()
  }

  private fun extractVariantName(process: dynamic): String? {
    val propertiesContainers = extensionValues(process).filter { it["\$type"] == "zeebe:Properties" }
    val allProperties = propertiesContainers.flatMap { container ->
      val props = container.properties
      if (props == null || props == undefined) emptyList() else props.unsafeCast<Array<dynamic>>().toList()
    }
    val variantProperty = allProperties.firstOrNull { it.name == "variantName" }
      ?: return null
    return (variantProperty.value as String?)?.takeIf { it.isNotBlank() }
  }

  private fun String.normalizeWhitespace(): String = this.replace(Regex("\\s+"), " ").trim()

  private fun createModdle(): dynamic {
    val require = io.github.emaarco.bpmn.adapter.outbound.filesystem.nodeRequire()
    val moddleCtor = require("bpmn-moddle")
    val zeebeSchema = require("zeebe-bpmn-moddle/resources/zeebe.json")
    return js("new moddleCtor({ zeebe: zeebeSchema })")
  }

  private companion object {
    private val NON_FLOW_NODE_TYPES = setOf(
      "bpmn:SequenceFlow",
      "bpmn:Association",
      "bpmn:DataObject",
      "bpmn:DataObjectReference",
      "bpmn:DataStoreReference",
      "bpmn:TextAnnotation",
      "bpmn:Group",
    )
  }
}
