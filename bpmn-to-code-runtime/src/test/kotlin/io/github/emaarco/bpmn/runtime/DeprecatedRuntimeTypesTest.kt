package io.github.emaarco.bpmn.runtime

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Guards the deprecated io.github.emaarco.bpmn.runtime compatibility shim: Process APIs generated
 * before the io.miragon migration import these types, so they must keep compiling and behaving
 * identically until they are removed in 4.0. See [[io.miragon.bpmn.runtime]] for the canonical types.
 */
@Suppress("DEPRECATION")
class DeprecatedRuntimeTypesTest {

    @Test
    fun `old identifier wrappers still expose value via toString`() {
        assertThat(ProcessId("order-process").toString()).isEqualTo("order-process")
        assertThat(ElementId("place-order").toString()).isEqualTo("place-order")
        assertThat(MessageName("OrderPlaced").toString()).isEqualTo("OrderPlaced")
        assertThat(SignalName("CancelRequested").toString()).isEqualTo("CancelRequested")
    }

    @Test
    fun `old VariableName subtypes preserve direction and value`() {
        val input: VariableName = VariableName.Input("customerId")
        val output: VariableName = VariableName.Output("orderId")
        val inOut: VariableName = VariableName.InOut("ticket")

        assertThat(input.value).isEqualTo("customerId")
        assertThat(output.value).isEqualTo("orderId")
        assertThat(inOut.value).isEqualTo("ticket")
    }

    @Test
    fun `old BpmnEngine covers all supported dialects`() {
        assertThat(BpmnEngine.entries).containsExactly(
            BpmnEngine.ZEEBE,
            BpmnEngine.CAMUNDA_7,
            BpmnEngine.OPERATON,
        )
    }

    @Test
    fun `old value types carry their fields`() {
        assertThat(BpmnFlow(id = "f1", sourceRef = "s", targetRef = "t").isDefault).isFalse()
        assertThat(BpmnTimer("Duration", "PT5M").timerValue).isEqualTo("PT5M")
        assertThat(BpmnError("NotFound", "E_404").code).isEqualTo("E_404")
        assertThat(BpmnEscalation("OutOfHours", "E_HRS").name).isEqualTo("OutOfHours")
        assertThat(
            BpmnRelations(
                previousElements = listOf("start"),
                followingElements = listOf("end"),
                parentId = null,
                attachedToRef = null,
                attachedElements = emptyList(),
            ).previousElements,
        ).containsExactly("start")
    }

    @Test
    fun `old and new types are distinct classes`() {
        assertThat(ProcessId::class.java.name).isEqualTo("io.github.emaarco.bpmn.runtime.ProcessId")
        assertThat(ProcessId::class.java).isNotEqualTo(io.miragon.bpmn.runtime.ProcessId::class.java)
    }
}
