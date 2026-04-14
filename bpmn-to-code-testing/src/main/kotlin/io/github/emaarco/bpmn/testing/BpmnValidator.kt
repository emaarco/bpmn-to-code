package io.github.emaarco.bpmn.testing

import io.github.emaarco.bpmn.adapter.outbound.engine.ExtractBpmnAdapter
import io.github.emaarco.bpmn.domain.BpmnModel
import io.github.emaarco.bpmn.domain.BpmnResource
import io.github.emaarco.bpmn.domain.MergedBpmnModel
import io.github.emaarco.bpmn.domain.service.ModelMergerService
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.validation.BpmnValidationRule
import io.github.emaarco.bpmn.domain.validation.Severity
import io.github.emaarco.bpmn.domain.validation.ValidationContext
import io.github.emaarco.bpmn.domain.validation.ValidationPhase
import io.github.emaarco.bpmn.domain.validation.ValidationResult
import io.github.emaarco.bpmn.domain.validation.ValidationViolation
import java.nio.file.Path

/**
 * Fluent entry point for validating BPMN process models in tests.
 *
 * Example usage:
 * ```kotlin
 * BpmnValidator
 *     .fromClasspath("bpmn/")
 *     .engine(ProcessEngine.CAMUNDA_7)
 *     .validate()
 *     .assertNoViolations()
 * ```
 */
class BpmnValidator private constructor(
    private val resourceLoader: () -> List<BpmnResource>,
) {

    private var engine: ProcessEngine? = null
    private var rules: List<BpmnValidationRule>? = null
    private var disabledRuleIds: Set<String> = emptySet()
    private var failOnWarning: Boolean = false

    /**
     * Sets the process engine used to parse BPMN files.
     */
    fun engine(engine: ProcessEngine): BpmnValidator {
        this.engine = engine
        return this
    }

    /**
     * Sets the validation rules to use. Defaults to [BpmnRules.all] if not called.
     */
    fun withRules(vararg rules: BpmnValidationRule): BpmnValidator {
        this.rules = rules.toList()
        return this
    }

    /**
     * Sets the validation rules to use. Defaults to [BpmnRules.all] if not called.
     */
    fun withRules(rules: List<BpmnValidationRule>): BpmnValidator {
        this.rules = rules
        return this
    }

    /**
     * Disables the given rules by their IDs.
     */
    fun disableRules(vararg ruleIds: String): BpmnValidator {
        this.disabledRuleIds = ruleIds.toSet()
        return this
    }

    /**
     * Treats warnings as failures during validation.
     */
    fun failOnWarning(): BpmnValidator {
        this.failOnWarning = true
        return this
    }

    /**
     * Executes validation and returns a [BpmnValidationAssert] for fluent assertions.
     */
    fun validate(): BpmnValidationAssert {
        val selectedEngine = requireNotNull(engine) {
            "Process engine must be set. Call .engine(ProcessEngine.CAMUNDA_7) or similar before .validate()"
        }

        val extractor = ExtractBpmnAdapter()
        val resources = resourceLoader()
        val models = resources.map { extractor.extract(it, selectedEngine) }
        val activeRules = resolveRules()
        val result = runValidation(models, selectedEngine, activeRules)
        return BpmnValidationAssert.assertThat(result)
    }

    private fun applyPolicy(violations: List<ValidationViolation>): List<ValidationViolation> {
        return if (failOnWarning) {
            violations.map { if (it.severity == Severity.WARN) it.copy(severity = Severity.ERROR) else it }
        } else {
            violations
        }
    }

    private fun resolveRules(): List<BpmnValidationRule> {
        val base = rules ?: BpmnRules.all()
        return base.filterNot { it.id in disabledRuleIds }
    }

    private fun runValidation(
        models: List<BpmnModel>,
        engine: ProcessEngine,
        activeRules: List<BpmnValidationRule>,
    ): ValidationResult {
        val preMergeRules = activeRules.filter { it.phase == ValidationPhase.PRE_MERGE }
        val postMergeRules = activeRules.filter { it.phase == ValidationPhase.POST_MERGE }

        val preMergeViolations = models.flatMap { model ->
            val ctx = ValidationContext(model, engine)
            val violations = preMergeRules.flatMap { it.validate(ctx) }
            applyPolicy(violations)
        }

        if (preMergeViolations.any { it.severity == Severity.ERROR }) {
            return ValidationResult(preMergeViolations)
        }

        val mergedModels = ModelMergerService().mergeModels(models)
        val postMergeViolations = mergedModels.flatMap { merged ->
            val flat = merged.toFlatModel()
            val ctx = ValidationContext(flat, engine)
            val violations = postMergeRules.flatMap { it.validate(ctx) }
            applyPolicy(violations)
        }

        return ValidationResult(preMergeViolations + postMergeViolations)
    }

    companion object {

        /**
         * Loads BPMN files from the classpath at the given path.
         */
        @JvmStatic
        fun fromClasspath(path: String): BpmnValidator {
            return BpmnValidator { BpmnResourceLoader.fromClasspath(path) }
        }

        /**
         * Loads BPMN files from a filesystem directory.
         */
        @JvmStatic
        fun fromDirectory(directory: Path): BpmnValidator {
            return BpmnValidator { BpmnResourceLoader.fromDirectory(directory) }
        }
    }
}
