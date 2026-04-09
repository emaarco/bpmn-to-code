package io.github.emaarco.bpmn.testing

import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class BpmnValidatorTest {

    @Test
    fun `valid bpmn passes assertNoErrors`() {
        BpmnValidator
            .fromClasspath("processes/valid-process.bpmn")
            .engine(ProcessEngine.CAMUNDA_7)
            .withRules(BpmnRules.MISSING_SERVICE_TASK_IMPLEMENTATION)
            .validate()
            .assertNoErrors()
    }

    @Test
    fun `invalid bpmn detects violations`() {
        val assert = BpmnValidator
            .fromClasspath("processes/invalid-process.bpmn")
            .engine(ProcessEngine.CAMUNDA_7)
            .withRules(BpmnRules.MISSING_SERVICE_TASK_IMPLEMENTATION, BpmnRules.MISSING_MESSAGE_NAME)
            .validate()

        assertThat(assert.result().violations).isNotEmpty
    }

    @Test
    fun `custom rules are applied via withRules`() {
        val assert = BpmnValidator
            .fromClasspath("processes/valid-process.bpmn")
            .engine(ProcessEngine.CAMUNDA_7)
            .withRules(BpmnRules.EMPTY_PROCESS)
            .validate()

        assert.assertNoViolations("empty-process")
    }

    @Test
    fun `disableRules filters out specified rules`() {
        val assert = BpmnValidator
            .fromClasspath("processes/invalid-process.bpmn")
            .engine(ProcessEngine.CAMUNDA_7)
            .withRules(BpmnRules.MISSING_SERVICE_TASK_IMPLEMENTATION, BpmnRules.MISSING_MESSAGE_NAME)
            .disableRules("missing-service-task-implementation")
            .validate()

        assert.assertNoViolations("missing-service-task-implementation")
    }

    @Test
    fun `missing engine throws clear error`() {
        assertThatThrownBy {
            BpmnValidator
                .fromClasspath("processes/valid-process.bpmn")
                .validate()
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Process engine must be set")
    }

    @Test
    fun `fromDirectory loads bpmn files`(@TempDir tempDir: Path) {
        val bpmnContent = javaClass.classLoader.getResourceAsStream("processes/valid-process.bpmn")!!
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
        val assert = BpmnValidator
            .fromClasspath("processes/valid-process.bpmn")
            .engine(ProcessEngine.CAMUNDA_7)
            .validate()

        // Valid process should pass all built-in rules without errors
        assert.assertNoErrors()
    }
}
