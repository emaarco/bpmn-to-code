---
name: generate-rules-to-enforce-bpmn-styleguide
description: "Generate Kotlin BpmnValidationRule implementations for every deterministic (or hybrid) rule in BPMN_STYLE_GUIDE.md, so they can be enforced at test time by the bpmn-to-code-testing module. llm-only rules stay in the style guide for /validate-bpmn-style. Use when the user asks to 'generate validation rules from the style guide', 'enforce BPMN conventions in CI', or 'automate style checks in tests'. For a single rule, use generate-rule-to-enforce-bpmn-styleguide."
allowed-tools: Read, Write, Glob, Grep, Bash(./gradlew *)
---

# Skill: generate-rules-to-enforce-bpmn-styleguide

Generate Kotlin `BpmnValidationRule` implementations for the deterministic rules in `BPMN_STYLE_GUIDE.md`. The output is a single file that plugs into `bpmn-to-code-testing`'s `BpmnValidator`.

## IMPORTANT

- Only generate rules where `validation` includes a deterministic half (`deterministic` or `hybrid`). Pure-LLM rules stay in the style guide for `/validate-bpmn-style`.
- For `hybrid` rules, generate the deterministic half only. Mention in a comment on the generated class that the full rule also has a semantic half enforced by `/validate-bpmn-style`.
- Never modify existing source files — only create new files.
- Always present the classification table to the user before generating anything.
- Use `requireNotNull()` instead of `!!` (project convention).
- Use `bpmn-to-code-core/src/main/kotlin/io/github/emaarco/bpmn/domain/validation/rules/InvalidIdentifierRule.kt` as the structural template.
- After generating, verify compilation with `./gradlew compileKotlin` (or the user's module-specific task).

## Domain Model

For rule bodies, read the actual domain classes under `bpmn-to-code-core/src/main/kotlin/io/github/emaarco/bpmn/domain/shared/` — don't trust static summaries, the model evolves. `ProcessModel` is the entry point.

## Instructions

### Step 0 — Verify `bpmn-to-code-testing` Setup

Generated rules depend on `io.github.emaarco:bpmn-to-code-testing`. If it's not on the user's classpath, the code won't compile — abort before writing anything.

1. Glob for build files at repo root and one level below: `build.gradle`, `build.gradle.kts`, `pom.xml`, `*/build.gradle*`, `*/pom.xml`.
2. Grep each for `bpmn-to-code-testing`.
3. No match → abort and show how to add it:

   ```kotlin
   // build.gradle.kts
   dependencies {
       testImplementation("io.github.emaarco:bpmn-to-code-testing:<latest>")
   }
   ```

   ```xml
   <!-- pom.xml -->
   <dependency>
     <groupId>io.github.emaarco</groupId>
     <artifactId>bpmn-to-code-testing</artifactId>
     <version><latest></version>
     <scope>test</scope>
   </dependency>
   ```

   Tell the user to add it and re-run.
4. Multiple module matches → ask which one hosts the rules.

### Step 1 — Read and Parse the Style Guide

1. Locate `BPMN_STYLE_GUIDE.md` (current dir, then git root). Missing → suggest `/build-bpmn-styleguide`.
2. Parse the file:
   - Find every `<!-- rule:<slug> -->` anchor.
   - For each, take the next ``` ```yaml``` fenced block as the rule metadata.
   - Extract `category`, `severity`, `applies-to`, `validation`, `pattern`, `engine` (optional).
   - Ignore YAML blocks without a preceding anchor.

### Step 2 — Classify Rules

For each rule, decide whether to generate Kotlin for it:

| `validation` | Generate? | Note |
|---|---|---|
| `deterministic` | Yes | Full rule goes into Kotlin. |
| `hybrid` | Partial | Generate the deterministic half; document that the semantic half stays for `/validate-bpmn-style`. |
| `llm` | No | Skip. Rule stays in the style guide. |

Present the classification as a table and pause for user confirmation:

```
| Rule                | Category                 | Validation    | Generate? | Reason |
|---------------------|--------------------------|---------------|-----------|--------|
| element-id-format   | technical-configuration  | deterministic | Yes       | Regex on FlowNodeDefinition.id |
| service-task-topic  | technical-configuration  | hybrid        | Partial   | Regex half generated; semantic half stays in style guide |
| task-naming         | business-modeling        | llm           | No        | Semantic only — use /validate-bpmn-style |
| gateway-flow-labels | technical-configuration  | deterministic | Yes       | Presence check on SequenceFlowDefinition.flowName |
```

### Step 3 — Generate the File

Produce a single Kotlin file: one class per generated rule + an aggregator.

**File header:**

```kotlin
// Generated from BPMN_STYLE_GUIDE.md by /generate-rules-to-enforce-bpmn-styleguide.
// Re-run the skill after editing the style guide to keep this file in sync.
```

**Per-rule class** (template — follow `InvalidIdentifierRule.kt` for shape):

```kotlin
/**
 * element-id-format (deterministic)
 * Category: technical-configuration
 */
class ElementIdFormatRule : BpmnValidationRule {
    override val id = "element-id-format"
    override val severity = Severity.ERROR

    override fun validate(context: ValidationContext): List<ValidationViolation> {
        val pattern = Regex("^(serviceTask|userTask|...)_[A-Z][a-zA-Z0-9]*$")
        return context.model.flowNodes
            .filter { node ->
                val nodeId = requireNotNull(node.id) { "FlowNode missing id" }
                !pattern.matches(nodeId)
            }
            .map { node ->
                ValidationViolation(
                    ruleId = id,
                    severity = severity,
                    elementId = node.id,
                    processId = context.model.processId,
                    message = "Element ID '${node.id}' does not match format type_DescriptionInCamelCase",
                )
            }
    }
}
```

**For `hybrid` rules**, the class comment calls out the semantic half:

```kotlin
/**
 * service-task-topic (hybrid — deterministic half only)
 * Category: technical-configuration
 *
 * This rule covers the deterministic shape check. The semantic half
 * ('<elementIdWithoutPrefix>' actually matches the element ID minus
 * 'serviceTask_') is enforced by /validate-bpmn-style.
 */
class ServiceTaskTopicRule : BpmnValidationRule { /* ... */ }
```

**Aggregator:**

```kotlin
object BpmnStyleGuideRules {
    @JvmField val ELEMENT_ID_FORMAT = ElementIdFormatRule()
    @JvmField val SERVICE_TASK_TOPIC = ServiceTaskTopicRule()
    // …

    @JvmStatic
    fun all(): List<BpmnValidationRule> = listOf(
        ELEMENT_ID_FORMAT,
        SERVICE_TASK_TOPIC,
        // …
    )
}
```

### Step 4 — Scan for Existing Rules

Before asking for an output location, see what the project already has:

1. Glob `**/src/test/kotlin/**/architecture/bpmn/**/*.kt`, plus a broader `**/src/test/kotlin/**/*Rule.kt` to catch alternative layouts.
2. Grep the matches for `: BpmnValidationRule` (class declarations) and for each slug from the style guide (as the `override val id = "<slug>"` literal).
3. Use what you find:
   - If a `BpmnStyleGuideRules.kt` file already exists at the expected path, read it. Show the user the rules that are already implemented and ask whether to **overwrite**, **merge** (add the new rules and keep existing ones that aren't in the style guide), or **abort**.
   - If rules with the same `id` exist in different files or under different class names, list them and ask the user how to resolve: **replace**, **skip** (don't regenerate this rule), or **write alongside** (let the user deduplicate manually).
   - If nothing matches, proceed directly to Step 5.

### Step 5 — Ask Output Location

Default to `src/test/kotlin/<package>/architecture/bpmn/BpmnStyleGuideRules.kt` inside the module that holds `bpmn-to-code-testing` (from Step 0). `<package>` should match the project's test package convention. If Step 4 found an existing aggregator or rule files, suggest the same location. Confirm or let the user override.

### Step 6 — Write and Verify

1. Create `architecture/bpmn/` if it doesn't exist.
2. Write the file.
3. Run `./gradlew compileKotlin` (or the module's test-compile task).
4. If compilation fails, fix and retry.
5. Show how to integrate:

   ```kotlin
   BpmnValidator
       .fromClasspath("bpmn/")
       .engine(ProcessEngine.CAMUNDA_7)
       .withRules(BpmnRules.all() + BpmnStyleGuideRules.all())
       .validate()
       .assertNoErrors()
   ```

6. Remind the user that LLM-only rules and the semantic half of hybrid rules still need `/validate-bpmn-style` — the test suite alone won't cover them.
