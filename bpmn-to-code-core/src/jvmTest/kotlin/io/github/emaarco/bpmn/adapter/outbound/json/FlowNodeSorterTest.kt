package io.github.emaarco.bpmn.adapter.outbound.json

import io.github.emaarco.bpmn.domain.shared.BpmnElementType
import io.github.emaarco.bpmn.domain.shared.FlowNodeDefinition
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class FlowNodeSorterTest {

    private fun node(
        id: String,
        type: BpmnElementType = BpmnElementType.SERVICE_TASK,
        previousElements: List<String> = emptyList(),
        followingElements: List<String> = emptyList(),
        parentId: String? = null,
        attachedToRef: String? = null,
    ) = FlowNodeDefinition(
        id = id,
        elementType = type,
        previousElements = previousElements,
        followingElements = followingElements,
        parentId = parentId,
        attachedToRef = attachedToRef,
    )

    @Test
    fun `linear chain is sorted start to end`() {

        // given: a linear start → task → end chain
        val start = node(id = "Start", type = BpmnElementType.START_EVENT, followingElements = listOf("Task"))
        val task = node(id = "Task", previousElements = listOf("Start"), followingElements = listOf("End"))
        val end = node(id = "End", type = BpmnElementType.END_EVENT, previousElements = listOf("Task"))

        // when: sorting the unsorted list
        val result = FlowNodeSorter.sort(listOf(task, end, start))

        // then: nodes appear in process order
        assertThat(result.map { it.id }).containsExactly("Start", "Task", "End")
    }

    @Test
    fun `start events are visited before other top-level nodes`() {

        // given: two start events feeding the same task
        val startA = node(id = "Start_A", type = BpmnElementType.START_EVENT, followingElements = listOf("Task"))
        val startB = node(id = "Start_B", type = BpmnElementType.START_EVENT, followingElements = listOf("Task"))
        val task = node(id = "Task", previousElements = listOf("Start_A", "Start_B"), followingElements = listOf("End"))
        val end = node(id = "End", type = BpmnElementType.END_EVENT, previousElements = listOf("Task"))

        // when: sorting
        val result = FlowNodeSorter.sort(listOf(task, end, startB, startA))
        val ids = result.map { it.id }

        // then: Start_A (alphabetically first) leads, all nodes appear exactly once
        assertThat(ids.first()).isEqualTo("Start_A")
        assertThat(ids).containsExactlyInAnyOrder("Start_A", "Start_B", "Task", "End")
    }

    @Test
    fun `boundary event appears after its parent`() {

        // given: a task with an attached boundary event
        val start = node(id = "Start", type = BpmnElementType.START_EVENT, followingElements = listOf("Task"))
        val task = node(id = "Task", previousElements = listOf("Start"), followingElements = listOf("End"))
        val boundary = node(id = "Boundary", type = BpmnElementType.BOUNDARY_EVENT, attachedToRef = "Task", followingElements = listOf("ErrorEnd"))
        val end = node(id = "End", type = BpmnElementType.END_EVENT, previousElements = listOf("Task"))
        val errorEnd = node(id = "ErrorEnd", type = BpmnElementType.END_EVENT, previousElements = listOf("Boundary"))

        // when: sorting
        val result = FlowNodeSorter.sort(listOf(end, errorEnd, boundary, task, start))
        val ids = result.map { it.id }

        // then: Boundary comes after Task, ErrorEnd comes after Boundary
        assertThat(ids.indexOf("Boundary")).isGreaterThan(ids.indexOf("Task"))
        assertThat(ids.indexOf("ErrorEnd")).isGreaterThan(ids.indexOf("Boundary"))
    }

    @Test
    fun `subprocess children are inlined after subprocess`() {

        // given: a subprocess with child nodes
        val start = node(id = "Start", type = BpmnElementType.START_EVENT, followingElements = listOf("Sub"))
        val sub = node(id = "Sub", type = BpmnElementType.SUB_PROCESS, previousElements = listOf("Start"), followingElements = listOf("End"))
        val subStart = node(id = "SubStart", type = BpmnElementType.START_EVENT, parentId = "Sub", followingElements = listOf("SubTask"))
        val subTask = node(id = "SubTask", parentId = "Sub", previousElements = listOf("SubStart"), followingElements = listOf("SubEnd"))
        val subEnd = node(id = "SubEnd", type = BpmnElementType.END_EVENT, parentId = "Sub", previousElements = listOf("SubTask"))
        val end = node(id = "End", type = BpmnElementType.END_EVENT, previousElements = listOf("Sub"))

        // when: sorting
        val result = FlowNodeSorter.sort(listOf(end, subEnd, subTask, subStart, sub, start))

        // then: subprocess children are inlined immediately after the subprocess
        assertThat(result.map { it.id }).containsExactly("Start", "Sub", "SubStart", "SubTask", "SubEnd", "End")
    }

    @Test
    fun `cycles do not cause infinite loops`() {

        // given: a cyclic A ↔ B loop
        val start = node(id = "Start", type = BpmnElementType.START_EVENT, followingElements = listOf("A"))
        val a = node(id = "A", previousElements = listOf("Start", "B"), followingElements = listOf("B"))
        val b = node(id = "B", previousElements = listOf("A"), followingElements = listOf("A", "End"))
        val end = node(id = "End", type = BpmnElementType.END_EVENT, previousElements = listOf("B"))

        // when: sorting
        val result = FlowNodeSorter.sort(listOf(b, a, end, start))

        // then: no exception; each node appears exactly once
        assertThat(result.map { it.id }).containsExactlyInAnyOrder("Start", "A", "B", "End")
        assertThat(result).hasSize(4)
    }

    @Test
    fun `already sorted input is idempotent`() {

        // given: nodes already in correct order
        val start = node(id = "Start", type = BpmnElementType.START_EVENT, followingElements = listOf("Task"))
        val task = node(id = "Task", previousElements = listOf("Start"), followingElements = listOf("End"))
        val end = node(id = "End", type = BpmnElementType.END_EVENT, previousElements = listOf("Task"))

        // when: sorting
        val result = FlowNodeSorter.sort(listOf(start, task, end))

        // then: order is unchanged
        assertThat(result.map { it.id }).containsExactly("Start", "Task", "End")
    }

    @Test
    fun `exclusive gateway branches appear after gateway`() {

        // given: a gateway splitting into two branches
        val start = node(id = "Start", type = BpmnElementType.START_EVENT, followingElements = listOf("GW"))
        val gw = node(id = "GW", type = BpmnElementType.EXCLUSIVE_GATEWAY, previousElements = listOf("Start"), followingElements = listOf("Branch_A", "Branch_B"))
        val branchA = node(id = "Branch_A", previousElements = listOf("GW"), followingElements = listOf("End"))
        val branchB = node(id = "Branch_B", previousElements = listOf("GW"), followingElements = listOf("End"))
        val end = node(id = "End", type = BpmnElementType.END_EVENT, previousElements = listOf("Branch_A", "Branch_B"))

        // when: sorting
        val result = FlowNodeSorter.sort(listOf(end, branchB, gw, branchA, start))
        val ids = result.map { it.id }

        // then: both branches appear after their gateway
        assertThat(ids.indexOf("GW")).isGreaterThan(ids.indexOf("Start"))
        assertThat(ids.indexOf("Branch_A")).isGreaterThan(ids.indexOf("GW"))
        assertThat(ids.indexOf("Branch_B")).isGreaterThan(ids.indexOf("GW"))
    }
}
