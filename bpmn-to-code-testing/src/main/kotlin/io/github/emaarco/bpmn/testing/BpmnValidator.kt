package io.github.emaarco.bpmn.testing

import io.github.emaarco.bpmn.adapter.outbound.engine.ExtractBpmnAdapter
import io.github.emaarco.bpmn.domain.BpmnModel
import io.github.emaarco.bpmn.domain.BpmnResource
import io.github.emaarco.bpmn.domain.service.ModelMergerService
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.validation.BpmnValidationRule
import io.github.emaarco.bpmn.domain.validation.Severity
import io.github.emaarco.bpmn.domain.validation.ValidationContext
import io.github.emaarco.bpmn.domain.validation.ValidationPhase
import io.github.emaarco.bpmn.domain.validation.ValidationResult
import java.nio.file.Path

/**
 * Fluent entry point for validating BPMN process models in tests.
 *
 * Example usage:
 * ```kotlin
 * BpmnValidator
 *     .fromClasspath("processes/")
 *     .engine(ProcessEngine.CAMUNDA_7)
 *     .validate()
 *     .assertNoViolations()
 * ```
 */
class BpmnValidator private constructor(
    private val resourceLoader: (ProcessEngine) -> List<BpmnResource>,
) {

    private var engine: ProcessEngine? = null
    private var rules: List<BpmnValidationRule>? = null
    private var disabledRuleIds: Set<String> = emptySet()
    private var failOnWarning: Boolean = false

    /**
     * Sets the process engine used to parse BPMN files.
     */
    fun engine(engine: ProcessEngine): BpmnValidator = apply {
        this.engine = engine
    }

    /**
     * Sets the validation rules to use. Defaults to [BpmnRules.all] if not called.
     */
    fun withRules(vararg rules: BpmnValidationRule): BpmnValidator = apply {
        this.rules = rules.toList()
    }

    /**
     * Sets the validation rules to use. Defaults to [BpmnRules.all] if not called.
     */
    fun withRules(rules: List<BpmnValidationRule>): BpmnValidator = apply {
        this.rules = rules
    }

    /**
     * Disables the given rules by their IDs.
     */
    fun disableRules(vararg ruleIds: String): BpmnValidator = apply {
        this.disabledRuleIds = ruleIds.toSet()
    }

    /**
     * Treats warnings as failures during validation.
     */
    fun failOnWarning(): BpmnValidator = apply {
        this.failOnWarning = true
    }

    /**
     * Executes validation and returns a [BpmnValidationAssert] for fluent assertions.
     */
    fun validate(): BpmnValidationAssert {
        val selectedEngine = requireNotNull(engine) {
            "Process engine must be set. Call .engine(ProcessEngine.CAMUNDA_7) or similar before .validate()"
        }

        val extractor = ExtractBpmnAdapter()
        val resources = resourceLoader(selectedEngine)
        val models = resources.map { extractor.extract(it) }
        val activeRules = resolveRules()
        val result = runValidation(models, selectedEngine, activeRules)
        return BpmnValidationAssert.assertThat(result)
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
            preMergeRules.flatMap { it.validate(ctx) }
        }

        if (preMergeViolations.any { it.severity == Severity.ERROR }) {
            return ValidationResult(preMergeViolations)
        }

        val mergedModels = ModelMergerService().mergeModels(models)
        val postMergeViolations = mergedModels.flatMap { model ->
            val ctx = ValidationContext(model, engine)
            postMergeRules.flatMap { it.validate(ctx) }
        }

        return ValidationResult(preMergeViolations + postMergeViolations)
    }

    companion object {

        /**
         * Loads BPMN files from the classpath at the given path.
         */
        @JvmStatic
        fun fromClasspath(path: String): BpmnValidator =
            BpmnValidator { engine -> ClasspathBpmnLoader.load(path, engine) }

        /**
         * Loads BPMN files from a filesystem directory.
         */
        @JvmStatic
        fun fromDirectory(directory: Path): BpmnValidator =
            BpmnValidator { engine -> FilesystemBpmnLoader.load(directory, engine) }
    }
}
