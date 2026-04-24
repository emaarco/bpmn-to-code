package io.github.emaarco.bpmn.runtime

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class RuntimeTypesTest {

    @Test
    fun `identifier wrappers expose value via toString`() {
        assertThat(ProcessId("order-process").toString()).isEqualTo("order-process")
        assertThat(ElementId("place-order").toString()).isEqualTo("place-order")
        assertThat(MessageName("OrderPlaced").toString()).isEqualTo("OrderPlaced")
        assertThat(SignalName("CancelRequested").toString()).isEqualTo("CancelRequested")
    }

    @Test
    fun `identifier wrappers implement value equality`() {
        assertThat(ProcessId("a")).isEqualTo(ProcessId("a"))
        assertThat(ProcessId("a")).isNotEqualTo(ProcessId("b"))
        assertThat(ProcessId("a").hashCode()).isEqualTo(ProcessId("a").hashCode())
    }

    @Test
    fun `VariableName subtypes preserve direction and value`() {
        val input: VariableName = VariableName.Input("customerId")
        val output: VariableName = VariableName.Output("orderId")
        val inOut: VariableName = VariableName.InOut("ticket")

        assertThat(input.value).isEqualTo("customerId")
        assertThat(output.value).isEqualTo("orderId")
        assertThat(inOut.value).isEqualTo("ticket")

        assertThat(input.toString()).isEqualTo("customerId")
        assertThat(output.toString()).isEqualTo("orderId")
        assertThat(inOut.toString()).isEqualTo("ticket")
    }

    @Test
    fun `VariableName direction subtypes are distinct types for the same value`() {
        assertThat(VariableName.Input("x")).isNotEqualTo(VariableName.Output("x"))
        assertThat(VariableName.Input("x")).isNotEqualTo(VariableName.InOut("x"))
    }

    @Test
    fun `BpmnFlow defaults nullable fields to null`() {
        val flow = BpmnFlow(id = "f1", sourceRef = "s", targetRef = "t")
        assertThat(flow.name).isNull()
        assertThat(flow.condition).isNull()
        assertThat(flow.isDefault).isFalse()
    }

    @Test
    fun `BpmnRelations retains list and nullable fields`() {
        val relations = BpmnRelations(
            name = "Approve",
            previousElements = listOf("start"),
            followingElements = listOf("end"),
            parentId = null,
            attachedToRef = null,
            attachedElements = listOf("boundary-timer"),
        )
        assertThat(relations.previousElements).containsExactly("start")
        assertThat(relations.followingElements).containsExactly("end")
        assertThat(relations.attachedElements).containsExactly("boundary-timer")
    }

    @Test
    fun `BpmnEngine covers all supported dialects`() {
        assertThat(BpmnEngine.entries).containsExactly(
            BpmnEngine.ZEEBE,
            BpmnEngine.CAMUNDA_7,
            BpmnEngine.OPERATON,
        )
    }

    @Test
    fun `BpmnTimer, BpmnError, BpmnEscalation carry their pair of strings`() {
        assertThat(BpmnTimer("Duration", "PT5M").timerValue).isEqualTo("PT5M")
        assertThat(BpmnError("NotFound", "E_404").code).isEqualTo("E_404")
        assertThat(BpmnEscalation("OutOfHours", "E_HRS").name).isEqualTo("OutOfHours")
    }
}
