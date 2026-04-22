---
name: generate-rule-to-enforce-bpmn-styleguide
argument-hint: "[rule-slug]"
description: "Generate a Kotlin BpmnValidationRule implementation for ONE rule from BPMN_STYLE_GUIDE.md. Use when drafting / iterating on a single rule, or when the user asks to 'generate a rule for <slug>', 'create the element-id-format rule', or 'add just this one rule to the test module'. For all rules at once, use generate-rules-to-enforce-bpmn-styleguide."
allowed-tools: Read, Write, Glob, Grep, Bash(./gradlew *)
---

# Skill: generate-rule-to-enforce-bpmn-styleguide

Generate a single Kotlin `BpmnValidationRule` implementation for one entry in `BPMN_STYLE_GUIDE.md`. Same template, conventions and output directory as the bulk variant — use this when you're drafting a rule or iterating on one without regenerating the full file.

## IMPORTANT

- Everything the bulk variant (`generate-rules-to-enforce-bpmn-styleguide`) says also applies here.
- The rule must include a deterministic half (`validation: deterministic` or `validation: hybrid`). If the user asks for an `llm`-only rule, explain that it belongs to `/validate-bpmn-style` only and stop.
- For `hybrid` rules, generate the deterministic half and note in a comment that the semantic half stays for `/validate-bpmn-style`.
- Don't touch the aggregator (`BpmnStyleGuideRules.kt`) owned by the bulk variant. This skill writes its own per-rule file; the user can run the bulk skill later to regenerate the aggregator.
- Use `requireNotNull()` instead of `!!`.
- Use `bpmn-to-code-core/src/main/kotlin/io/github/emaarco/bpmn/domain/validation/rules/InvalidIdentifierRule.kt` as the structural template.

## Domain Model

For rule bodies, read the actual domain classes under `bpmn-to-code-core/src/main/kotlin/io/github/emaarco/bpmn/domain/shared/` — don't trust static summaries.

## Instructions

### Step 0 — Verify `bpmn-to-code-testing` Setup

Same check as the bulk variant — generated rules need the dependency.

1. Glob for build files at repo root and one level below: `build.gradle`, `build.gradle.kts`, `pom.xml`, `*/build.gradle*`, `*/pom.xml`.
2. Grep each for `bpmn-to-code-testing`.
3. Missing → abort with the Gradle + Maven snippets (see the bulk skill). Tell the user to add it and re-run.
4. Multiple module matches → ask which one hosts the rule.

### Step 1 — Resolve the Rule Slug

1. Locate `BPMN_STYLE_GUIDE.md` (current dir, then git root). Missing → suggest `/build-bpmn-styleguide`.
2. Parse the file to enumerate rules:
   - Find every `<!-- rule:<slug> -->` anchor.
   - For each, capture the slug + the next ``` ```yaml``` fenced block + the preceding prose.
3. If `$ARGUMENTS` is a rule slug, look it up.
4. If no argument, list all slugs and ask which one to generate.
5. If the slug doesn't exist, report and stop.

### Step 2 — Classify and Confirm

Decide based on the rule's `validation`:

- `deterministic` → proceed.
- `hybrid` → proceed (deterministic half); inform the user the semantic half stays for `/validate-bpmn-style`.
- `llm` → stop. Explain that the rule is enforced only by `/validate-bpmn-style` and there's nothing to generate.

Before generating, show the user:
- Which domain-model property the rule inspects (from the rule's `applies-to` + the YAML schema).
- The regex (or check) that will be applied.
- The classification (deterministic / hybrid).

Ask for confirmation.

### Step 3 — Generate the Rule

One Kotlin file, one class. Class name is PascalCase of the slug (e.g. `element-id-format` → `ElementIdFormatRule`).

**File header:**

```kotlin
// Generated from BPMN_STYLE_GUIDE.md by /generate-rule-to-enforce-bpmn-styleguide.
// Re-run the skill after editing the rule in the style guide.
```

**Class for a `deterministic` rule** (follow `InvalidIdentifierRule.kt` for shape):

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

**Class for a `hybrid` rule** — same shape, but the doc comment calls out the split:

```kotlin
/**
 * service-task-topic (hybrid — deterministic half only)
 * Category: technical-configuration
 *
 * This rule covers the deterministic shape check. The semantic half
 * is enforced by /validate-bpmn-style using the rule's prose as the brief.
 */
class ServiceTaskTopicRule : BpmnValidationRule { /* ... */ }
```

### Step 4 — Scan for an Existing Version of This Rule

Before asking where to write, see whether the project already has a rule for this slug:

1. Glob `**/src/test/kotlin/**/architecture/bpmn/**/*.kt`, plus a broader `**/src/test/kotlin/**/*Rule.kt` to catch alternative layouts.
2. Grep the matches for the slug as the `override val id = "<slug>"` literal, and for the expected class name.
3. If found:
   - Show the user the existing implementation.
   - Ask whether to **replace** it (overwrite the file), **skip** generation (keep the existing file as-is), or **write alongside** under a different class name (user resolves the duplicate manually).
4. If nothing matches, proceed directly to Step 5.

### Step 5 — Ask Output Location

Default to `src/test/kotlin/<package>/architecture/bpmn/<ClassName>.kt` in the module from Step 0 — same directory as the bulk skill uses. If Step 4 found an existing file, suggest that location by default. Confirm or let the user override.

### Step 6 — Write and Verify

1. Create `architecture/bpmn/` if needed.
2. Write the file.
3. Run `./gradlew compileKotlin` (or the module's test-compile task).
4. Fix and retry if it fails.
5. Show how to plug the rule in:

   ```kotlin
   BpmnValidator
       .fromClasspath("bpmn/")
       .engine(ProcessEngine.CAMUNDA_7)
       .withRules(BpmnRules.all() + listOf(ElementIdFormatRule()))
       .validate()
       .assertNoErrors()
   ```

6. Mention the bulk skill for when the user is ready to generate the full aggregator (`BpmnStyleGuideRules.kt`). If this is a `hybrid` rule, remind them that `/validate-bpmn-style` still needs to run for the semantic half.
