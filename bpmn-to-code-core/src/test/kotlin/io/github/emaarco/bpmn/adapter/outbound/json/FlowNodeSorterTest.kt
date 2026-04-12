package io.github.emaarco.bpmn.adapter.outbound.json

import io.github.emaarco.bpmn.domain.shared.BpmnElementType
import io.github.emaarco.bpmn.domain.shared.FlowNodeDefinition
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class FlowNodeSorterTest {

    private fun node(
        id: String,
        type: BpmnElementType = BpmnElementType.SERVICE_TASK,
        incoming: List<String> = emptyList(),
        outgoing: List<String> = emptyList(),
        parentId: String? = null,
        attachedToRef: String? = null,
    ) = FlowNodeDefinition(id = id, elementType = type, incoming = incoming, outgoing = outgoing, parentId = parentId, attachedToRef = attachedToRef)

    @Test
    fun `linear chain is sorted start to end`() {
        val start = node("Start", BpmnElementType.START_EVENT, outgoing = listOf("Task"))
        val task = node("Task", incoming = listOf("Start"), outgoing = listOf("End"))
        val end = node("End", BpmnElementType.END_EVENT, incoming = listOf("Task"))

        val result = FlowNodeSorter.sort(listOf(task, end, start))

        assertThat(result.map { it.id }).containsExactly("Start", "Task", "End")
    }

    @Test
    fun `start events are visited before other top-level nodes`() {
        val startA = node("Start_A", BpmnElementType.START_EVENT, outgoing = listOf("Task"))
        val startB = node("Start_B", BpmnElementType.START_EVENT, outgoing = listOf("Task"))
        val task = node("Task", incoming = listOf("Start_A", "Start_B"), outgoing = listOf("End"))
        val end = node("End", BpmnElementType.END_EVENT, incoming = listOf("Task"))

        val result = FlowNodeSorter.sort(listOf(task, end, startB, startA))
        val ids = result.map { it.id }

        // Start_A (alphabetically first) is the first element
        assertThat(ids.first()).isEqualTo("Start_A")
        // All nodes appear exactly once
        assertThat(ids).containsExactlyInAnyOrder("Start_A", "Start_B", "Task", "End")
    }

    @Test
    fun `boundary event appears after its parent`() {
        val start = node("Start", BpmnElementType.START_EVENT, outgoing = listOf("Task"))
        val task = node("Task", incoming = listOf("Start"), outgoing = listOf("End"))
        val boundary = node("Boundary", BpmnElementType.BOUNDARY_EVENT, attachedToRef = "Task", outgoing = listOf("ErrorEnd"))
        val end = node("End", BpmnElementType.END_EVENT, incoming = listOf("Task"))
        val errorEnd = node("ErrorEnd", BpmnElementType.END_EVENT, incoming = listOf("Boundary"))

        val result = FlowNodeSorter.sort(listOf(end, errorEnd, boundary, task, start))
        val ids = result.map { it.id }

        assertThat(ids.indexOf("Boundary")).isGreaterThan(ids.indexOf("Task"))
        assertThat(ids.indexOf("ErrorEnd")).isGreaterThan(ids.indexOf("Boundary"))
    }

    @Test
    fun `subprocess children are inlined after subprocess`() {
        val start = node("Start", BpmnElementType.START_EVENT, outgoing = listOf("Sub"))
        val sub = node("Sub", BpmnElementType.SUB_PROCESS, incoming = listOf("Start"), outgoing = listOf("End"))
        val subStart = node("SubStart", BpmnElementType.START_EVENT, parentId = "Sub", outgoing = listOf("SubTask"))
        val subTask = node("SubTask", parentId = "Sub", incoming = listOf("SubStart"), outgoing = listOf("SubEnd"))
        val subEnd = node("SubEnd", BpmnElementType.END_EVENT, parentId = "Sub", incoming = listOf("SubTask"))
        val end = node("End", BpmnElementType.END_EVENT, incoming = listOf("Sub"))

        val result = FlowNodeSorter.sort(listOf(end, subEnd, subTask, subStart, sub, start))
        val ids = result.map { it.id }

        assertThat(ids).containsExactly("Start", "Sub", "SubStart", "SubTask", "SubEnd", "End")
    }

    @Test
    fun `cycles do not cause infinite loops`() {
        val start = node("Start", BpmnElementType.START_EVENT, outgoing = listOf("A"))
        val a = node("A", incoming = listOf("Start", "B"), outgoing = listOf("B"))
        val b = node("B", incoming = listOf("A"), outgoing = listOf("A", "End"))
        val end = node("End", BpmnElementType.END_EVENT, incoming = listOf("B"))

        val result = FlowNodeSorter.sort(listOf(b, a, end, start))

        // Should not throw or loop; each node appears exactly once
        assertThat(result.map { it.id }).containsExactlyInAnyOrder("Start", "A", "B", "End")
        assertThat(result).hasSize(4)
    }

    @Test
    fun `already sorted input is idempotent`() {
        val start = node("Start", BpmnElementType.START_EVENT, outgoing = listOf("Task"))
        val task = node("Task", incoming = listOf("Start"), outgoing = listOf("End"))
        val end = node("End", BpmnElementType.END_EVENT, incoming = listOf("Task"))

        val result = FlowNodeSorter.sort(listOf(start, task, end))

        assertThat(result.map { it.id }).containsExactly("Start", "Task", "End")
    }

    @Test
    fun `exclusive gateway branches appear after gateway`() {
        val start = node("Start", BpmnElementType.START_EVENT, outgoing = listOf("GW"))
        val gw = node("GW", BpmnElementType.EXCLUSIVE_GATEWAY, incoming = listOf("Start"), outgoing = listOf("Branch_A", "Branch_B"))
        val branchA = node("Branch_A", incoming = listOf("GW"), outgoing = listOf("End"))
        val branchB = node("Branch_B", incoming = listOf("GW"), outgoing = listOf("End"))
        val end = node("End", BpmnElementType.END_EVENT, incoming = listOf("Branch_A", "Branch_B"))

        val result = FlowNodeSorter.sort(listOf(end, branchB, gw, branchA, start))
        val ids = result.map { it.id }

        assertThat(ids.indexOf("GW")).isGreaterThan(ids.indexOf("Start"))
        assertThat(ids.indexOf("Branch_A")).isGreaterThan(ids.indexOf("GW"))
        assertThat(ids.indexOf("Branch_B")).isGreaterThan(ids.indexOf("GW"))
    }
}
