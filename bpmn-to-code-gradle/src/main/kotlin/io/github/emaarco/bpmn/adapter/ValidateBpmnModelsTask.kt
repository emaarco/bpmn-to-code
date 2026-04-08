package io.github.emaarco.bpmn.adapter

import io.github.emaarco.bpmn.adapter.inbound.ValidateBpmnFilesystemPlugin
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.validation.ValidationConfig
import io.github.emaarco.bpmn.domain.validation.ValidationViolation
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(
    because = "Validation depends on BPMN files that can change at any time without the plugin knowing about it"
)
abstract class ValidateBpmnModelsTask : DefaultTask() {

    @Input
    lateinit var baseDir: String

    @Input
    lateinit var filePattern: String

    @Input
    lateinit var processEngine: ProcessEngine

    @Input
    var failOnWarning: Boolean = false

    @Input
    var disabledRules: Set<String> = emptySet()

    @TaskAction
    fun execute() {
        val plugin = ValidateBpmnFilesystemPlugin()
        val config = ValidationConfig(
            failOnWarning = failOnWarning,
            disabledRules = disabledRules,
        )
        val result = plugin.execute(baseDir, filePattern, processEngine, config)

        result.warnings.forEach { v ->
            logger.warn("[BPMN VALIDATION WARN] ${formatLocation(v)}: ${v.message} (rule: ${v.ruleId})")
        }
        result.errors.forEach { v ->
            logger.error("[BPMN VALIDATION ERROR] ${formatLocation(v)}: ${v.message} (rule: ${v.ruleId})")
        }

        if (result.hasFailures(failOnWarning)) {
            throw GradleException(
                "BPMN validation failed: ${result.errors.size} error(s), ${result.warnings.size} warning(s)"
            )
        }
        logger.lifecycle("BPMN validation passed")
    }

    private fun formatLocation(v: ValidationViolation): String =
        if (v.elementId != null) "${v.processId}/${v.elementId}" else v.processId
}
