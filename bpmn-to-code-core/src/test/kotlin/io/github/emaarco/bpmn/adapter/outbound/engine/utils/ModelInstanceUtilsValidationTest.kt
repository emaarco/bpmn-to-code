package io.github.emaarco.bpmn.adapter.outbound.engine.utils

import io.github.emaarco.bpmn.adapter.outbound.engine.extractor.ZeebeModelExtractor
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.findErrorEventDefinition
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.findFlowNodes
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.findMessages
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.findSignalEventDefinitions
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.findTimerEventDefinition
import io.github.emaarco.bpmn.adapter.outbound.engine.utils.ModelInstanceUtils.getProcessId
import org.assertj.core.api.Assertions.assertThatIllegalArgumentException
import org.assertj.core.api.Assertions.assertThatIllegalStateException
import org.camunda.bpm.model.bpmn.Bpmn
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream

class ModelInstanceUtilsValidationTest {

    private fun bpmnOf(content: String) = Bpmn.readModelFromStream(
        ByteArrayInputStream(
            """<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    id="Definitions_1" targetNamespace="http://bpmn.io/schema/bpmn">
  $content
</bpmn:definitions>""".toByteArray()
        )
    )

    @Test
    fun `getProcessId throws when no process definition exists`() {
        val modelInstance = bpmnOf("")
        assertThatIllegalArgumentException()
            .isThrownBy { modelInstance.getProcessId() }
            .withMessage("BPMN file contains no process definition")
    }

    @Test
    fun `findFlowNodes throws when a flow node is missing an ID`() {
        val modelInstance = bpmnOf(
            """<bpmn:process id="testProcess">
                 <bpmn:startEvent />
               </bpmn:process>"""
        )
        assertThatIllegalArgumentException()
            .isThrownBy { modelInstance.findFlowNodes() }
            .withMessageContaining("Flow node in process 'testProcess' is missing an ID attribute")
    }

    @Test
    fun `findMessages throws when a message is missing a name`() {
        val modelInstance = bpmnOf(
            """<bpmn:message id="msg1" />
               <bpmn:process id="testProcess" />"""
        )
        assertThatIllegalArgumentException()
            .isThrownBy { modelInstance.findMessages() }
            .withMessageContaining("Message element in process 'testProcess' is missing a name attribute")
    }

    @Test
    fun `findSignalEventDefinitions throws when signal reference is missing`() {
        val modelInstance = bpmnOf(
            """<bpmn:process id="testProcess">
                 <bpmn:endEvent id="end1">
                   <bpmn:signalEventDefinition id="sed1" />
                 </bpmn:endEvent>
               </bpmn:process>"""
        )
        assertThatIllegalArgumentException()
            .isThrownBy { modelInstance.findSignalEventDefinitions() }
            .withMessageContaining("Signal event definition in process 'testProcess' is missing a signal reference")
    }

    @Test
    fun `findErrorEventDefinition throws when error is missing an error code`() {
        val modelInstance = bpmnOf(
            """<bpmn:error id="err1" name="myError" />
               <bpmn:process id="testProcess">
                 <bpmn:endEvent id="end1">
                   <bpmn:errorEventDefinition id="eed1" errorRef="err1" />
                 </bpmn:endEvent>
               </bpmn:process>"""
        )
        assertThatIllegalArgumentException()
            .isThrownBy { modelInstance.findErrorEventDefinition() }
            .withMessageContaining("Error 'myError' in process 'testProcess' is missing an error code")
    }

    @Test
    fun `findTimerEventDefinition throws when timer has no valid type`() {
        val modelInstance = bpmnOf(
            """<bpmn:process id="testProcess">
                 <bpmn:startEvent id="start1">
                   <bpmn:timerEventDefinition id="tid1" />
                 </bpmn:startEvent>
               </bpmn:process>"""
        )
        assertThatIllegalStateException()
            .isThrownBy { modelInstance.findTimerEventDefinition() }
            .withMessageContaining("Timer event 'start1' is missing a valid type (expected timeDate, timeDuration, or timeCycle)")
    }

    @Test
    fun `zeebe findServiceTasks throws when service task is missing a jobType`() {
        val bpmn = """<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
    xmlns:zeebe="http://camunda.org/schema/zeebe/1.0"
    id="Definitions_1" targetNamespace="http://bpmn.io/schema/bpmn">
  <bpmn:process id="testProcess">
    <bpmn:serviceTask id="task1">
      <bpmn:extensionElements>
        <zeebe:taskDefinition />
      </bpmn:extensionElements>
    </bpmn:serviceTask>
  </bpmn:process>
</bpmn:definitions>""".toByteArray()

        assertThatIllegalArgumentException()
            .isThrownBy { ZeebeModelExtractor().extract(ByteArrayInputStream(bpmn)) }
            .withMessageContaining("Service task 'task1' in process 'testProcess' is missing a type (jobType) attribute")
    }
}
