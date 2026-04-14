package io.github.emaarco.bpmn.testing

import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class BpmnValidatorTest {

    @Test
    fun `valid bpmn passes assertNoErrors`() {

        // when / then: a valid BPMN file produces no errors
        BpmnValidator
            .fromClasspath("bpmn/valid-process.bpmn")
            .engine(ProcessEngine.CAMUNDA_7)
            .withRules(BpmnRules.MISSING_SERVICE_TASK_IMPLEMENTATION)
            .validate()
            .assertNoErrors()
    }

    @Test
    fun `invalid bpmn detects violations`() {

        // when / then: an invalid BPMN file produces at least one violation
        BpmnValidator
            .fromClasspath("bpmn/invalid-process.bpmn")
            .engine(ProcessEngine.CAMUNDA_7)
            .withRules(BpmnRules.MISSING_SERVICE_TASK_IMPLEMENTATION, BpmnRules.MISSING_MESSAGE_NAME)
            .validate()
            .assertHasViolations()
    }

    @Test
    fun `custom rules are applied via withRules`() {

        // when / then: the empty-process rule does not trigger for a valid process
        BpmnValidator
            .fromClasspath("bpmn/valid-process.bpmn")
            .engine(ProcessEngine.CAMUNDA_7)
            .withRules(BpmnRules.EMPTY_PROCESS)
            .validate()
            .assertNoViolations("empty-process")
    }

    @Test
    fun `disableRules filters out specified rules`() {

        // given: an invalid process with the implementation rule disabled

        // when / then: the disabled rule produces no violations
        BpmnValidator
            .fromClasspath("bpmn/invalid-process.bpmn")
            .engine(ProcessEngine.CAMUNDA_7)
            .withRules(BpmnRules.MISSING_SERVICE_TASK_IMPLEMENTATION, BpmnRules.MISSING_MESSAGE_NAME)
            .disableRules("missing-service-task-implementation")
            .validate()
            .assertNoViolations("missing-service-task-implementation")
    }

    @Test
    fun `missing engine throws clear error`() {

        // when / then: validating without setting an engine throws with a clear message
        assertThatThrownBy {
            BpmnValidator
                .fromClasspath("bpmn/valid-process.bpmn")
                .validate()
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Process engine must be set")
    }

    @Test
    fun `fromDirectory loads bpmn files`(@TempDir tempDir: Path) {

        // given: a BPMN file copied into a temp directory
        val bpmnContent = javaClass.classLoader.getResourceAsStream("bpmn/valid-process.bpmn")!!
        Files.copy(bpmnContent, tempDir.resolve("test.bpmn"))

        // when / then: validation succeeds when loading from the directory
        BpmnValidator
            .fromDirectory(tempDir)
            .engine(ProcessEngine.CAMUNDA_7)
            .withRules(BpmnRules.MISSING_SERVICE_TASK_IMPLEMENTATION)
            .validate()
            .assertNoErrors()
    }

    @Test
    fun `defaults to all rules when withRules is not called`() {

        // when / then: a valid process passes all default rules without errors
        BpmnValidator
            .fromClasspath("bpmn/valid-process.bpmn")
            .engine(ProcessEngine.CAMUNDA_7)
            .validate()
            .assertNoErrors()
    }
}
