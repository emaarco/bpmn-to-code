package io.github.emaarco.bpmn

import io.github.emaarco.bpmn.adapter.outbound.engine.ZeebeBpmnParser
import io.github.emaarco.bpmn.adapter.outbound.filesystem.nodeRequire
import io.github.emaarco.bpmn.domain.shared.BpmnElementType
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SendNewsletterSmokeTest {

  @Test
  fun coversMultiInstanceEscalationDefaultFlowAndEventSubProcess() = runTest {
    val process = js("process")
    val cwd = process.cwd() as String
    val repoRoot = cwd.substringBefore("/build/")
    val fs = nodeRequire()("fs")
    val xml = fs.readFileSync("$repoRoot/shared/bpmn/c8-send-newsletter.bpmn", "utf8") as String

    val model = ZeebeBpmnParser().parse(xml)

    // event sub-process detection
    val esp = model.flowNodes.first { it.id == "eventSubProcess_errorHandling" }
    assertEquals(BpmnElementType.EVENT_SUB_PROCESS, esp.elementType)

    // escalations
    assertTrue(model.escalations.any { it.getRawName() == "escalation_notifySupport" })

    // default sequence flow
    val defaultFlow = model.sequenceFlows.first { it.id == "Flow_1jogut0" }
    assertTrue(defaultFlow.isDefault)

    // multi-instance variables surfaced
    assertTrue(model.variables.any { it.getRawName() == "subscriber" })
    assertTrue(model.variables.any { it.getRawName() == "authors" })
  }
}
