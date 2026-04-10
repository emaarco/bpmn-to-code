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
        BpmnValidator
            .fromClasspath("bpmn/valid-process.bpmn")
            .engine(ProcessEngine.CAMUNDA_7)
            .withRules(BpmnRules.MISSING_SERVICE_TASK_IMPLEMENTATION)
            .validate()
            .assertNoErrors()
    }

    @Test
    fun `invalid bpmn detects violations`() {
        BpmnValidator
            .fromClasspath("bpmn/invalid-process.bpmn")
            .engine(ProcessEngine.CAMUNDA_7)
            .withRules(BpmnRules.MISSING_SERVICE_TASK_IMPLEMENTATION, BpmnRules.MISSING_MESSAGE_NAME)
            .validate()
            .assertHasViolations()
    }

    @Test
    fun `custom rules are applied via withRules`() {
        BpmnValidator
            .fromClasspath("bpmn/valid-process.bpmn")
            .engine(ProcessEngine.CAMUNDA_7)
            .withRules(BpmnRules.EMPTY_PROCESS)
            .validate()
            .assertNoViolations("empty-process")
    }

    @Test
    fun `disableRules filters out specified rules`() {
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
        assertThatThrownBy {
            BpmnValidator
                .fromClasspath("bpmn/valid-process.bpmn")
                .validate()
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Process engine must be set")
    }

    @Test
    fun `fromDirectory loads bpmn files`(@TempDir tempDir: Path) {
        val bpmnContent = javaClass.classLoader.getResourceAsStream("bpmn/valid-process.bpmn")!!
        Files.copy(bpmnContent, tempDir.resolve("test.bpmn"))

        BpmnValidator
            .fromDirectory(tempDir)
            .engine(ProcessEngine.CAMUNDA_7)
            .withRules(BpmnRules.MISSING_SERVICE_TASK_IMPLEMENTATION)
            .validate()
            .assertNoErrors()
    }

    @Test
    fun `defaults to all rules when withRules is not called`() {
        BpmnValidator
            .fromClasspath("bpmn/valid-process.bpmn")
            .engine(ProcessEngine.CAMUNDA_7)
            .validate()
            .assertNoErrors()
    }
}
