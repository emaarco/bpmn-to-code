package io.github.emaarco.bpmn.domain.shared

/**
 * Represents the BPMN element type of a flow node.
 *
 * The [bpmnTypeName] matches the type name returned by the Camunda BPM model API
 * (`element.elementType.typeName`), which follows the BPMN 2.0 spec element names.
 * Use [fromTypeName] to map a raw type name string to the enum constant.
 */
enum class BpmnElementType(val bpmnTypeName: String) {

    // Tasks
    SERVICE_TASK("serviceTask"),
    USER_TASK("userTask"),
    RECEIVE_TASK("receiveTask"),
    SEND_TASK("sendTask"),
    SCRIPT_TASK("scriptTask"),
    MANUAL_TASK("manualTask"),
    BUSINESS_RULE_TASK("businessRuleTask"),

    // Events
    START_EVENT("startEvent"),
    END_EVENT("endEvent"),
    INTERMEDIATE_CATCH_EVENT("intermediateCatchEvent"),
    INTERMEDIATE_THROW_EVENT("intermediateThrowEvent"),
    BOUNDARY_EVENT("boundaryEvent"),

    // Gateways
    EXCLUSIVE_GATEWAY("exclusiveGateway"),
    PARALLEL_GATEWAY("parallelGateway"),
    INCLUSIVE_GATEWAY("inclusiveGateway"),
    EVENT_BASED_GATEWAY("eventBasedGateway"),
    COMPLEX_GATEWAY("complexGateway"),

    // Containers
    SUB_PROCESS("subProcess"),
    CALL_ACTIVITY("callActivity"),
    TRANSACTION("transaction"),

    /**
     * Fallback for element types not covered by this enum (e.g. future BPMN extensions).
     * Also used as the default when a [FlowNodeDefinition] is constructed without a type —
     * for example in unit tests that build models manually without running an extractor.
     */
    UNKNOWN("unknown");

    companion object {
        fun fromTypeName(typeName: String?): BpmnElementType =
            entries.firstOrNull { it.bpmnTypeName == typeName } ?: UNKNOWN
    }
}
