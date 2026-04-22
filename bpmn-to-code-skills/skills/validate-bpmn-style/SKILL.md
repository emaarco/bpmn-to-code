---
name: validate-bpmn-style
argument-hint: "[path/to/file.bpmn]"
description: "Check BPMN files against a team-defined BPMN_STYLE_GUIDE.md and report violations with explanations and suggested fixes. Routes each rule through the right check: deterministic (regex), llm (semantic judgement), or hybrid (both). Use when the user asks to 'check BPMN style', 'validate BPMN conventions', 'review BPMN naming', or 'lint BPMN files'."
allowed-tools: Read, Glob, Grep
---

# Skill: validate-bpmn-style

Check BPMN files against a project's `BPMN_STYLE_GUIDE.md` and report violations. The style guide is a handbook — prose explains each rule, YAML annotations inside the prose define how to enforce it. Route each rule by its `validation` field: `deterministic` (regex), `llm` (semantic judgement), or `hybrid` (regex + semantics).

## IMPORTANT

- Read-only. Never modify BPMN files or any other files.
- Advisory only — never a CI gate. Present results as recommendations.
- If `BPMN_STYLE_GUIDE.md` is missing, abort and suggest `/build-bpmn-styleguide`.
- Process files one at a time to manage context.
- **Always read the BPMN XML directly.** The JSON export is ignored — BPMN XML is the source of truth, covers every engine consistently, and carries everything needed (including the `name` attribute for display names and DI data for layout rules).
- When reporting a violation, quote the rule's prose so the user understands *why* — that's the value this skill adds over a regex linter.

## Instructions

### Step 1 — Locate and Parse the Style Guide

1. Look for `BPMN_STYLE_GUIDE.md` in the current directory first, then the git root.
2. If missing: `"No BPMN_STYLE_GUIDE.md found. Run /build-bpmn-styleguide to create one."`
3. Parse the file:
   - Find every anchor matching `<!-- rule:<slug> -->` in the Markdown source.
   - For each anchor, take the next ``` ```yaml ... ``` ``` fenced block as the rule's YAML metadata.
   - Take the surrounding prose (from the nearest preceding `###` or `##` heading down to the anchor) as the rule's brief — you'll reference this in violation messages.
   - Ignore YAML blocks that aren't preceded by a rule anchor.
4. Parse the top-level `Engine:` header (and any rule-level `engine:` overrides) to filter rules per file.

### Step 2 — Select Input

1. If `$ARGUMENTS` is a `.bpmn` file path, use only that file.
2. Otherwise, glob for `**/*.bpmn` (exclude `build/`, `target/`, `node_modules/`).
3. Read each BPMN file's XML directly.

### Step 3 — Route Each Rule by `validation`

For each input + each rule (filtered by engine):

**`validation: deterministic`**

Apply the `pattern` regex (or structural check — e.g. set-membership for `allowed-notation-choices`) to the relevant XML path — see the mapping table below. A miss is a violation.

**`validation: llm`**

No regex. Use the rule's prose as the brief. Inspect the element(s) identified by `applies-to` and judge whether they satisfy the convention. Examples:
- `task-naming` → look at task-type elements' `name` attribute; judge verb+noun, present-tense.
- `layout-left-to-right` → use the `<bpmndi:BPMNShape>` coordinates to check flow direction.

**`validation: hybrid`**

Run the regex first. If it fails, report a deterministic violation and stop for that rule/element.
If the regex passes, *additionally* run the LLM judgement on the semantic half described in the prose. Example for `service-task-topic` (hybrid):
- Regex: topic matches `^[a-z][a-zA-Z0-9]*\.[a-z][a-zA-Z0-9]*$` → pass.
- Semantic: is `<elementIdWithoutPrefix>` actually the element's ID minus `serviceTask_`? → LLM check.

Report the two halves separately when they disagree.

### XML Path Mapping

| Rule target | BPMN XML path |
|---|---|
| Process ID | `<bpmn:process id>` |
| Flow node ID | `<bpmn:serviceTask id>`, `<bpmn:userTask id>`, … |
| Flow node display name | `<bpmn:* name>` on the flow-node element |
| Service task implementation (Camunda 7 / Operaton) | `camunda:topic` / `operaton:topic` / `camunda:class` / `camunda:delegateExpression` / `camunda:expression` (and `operaton:` equivalents) |
| Service task implementation (Zeebe) | `<zeebe:taskDefinition type="…">` inside the serviceTask's `<bpmn:extensionElements>` |
| Sequence flow label | `<bpmn:sequenceFlow name>` |
| Message name | `<bpmn:message name>` |
| Signal name | `<bpmn:signal name>` |
| Error name / code | `<bpmn:error name errorCode>` |
| Escalation name / code | `<bpmn:escalation name escalationCode>` |
| Async triple (Camunda 7 / Operaton / CIB-7) | `camunda:asyncBefore`, `camunda:asyncAfter`, `camunda:exclusive` (or `operaton:*`) on the flow-node element |
| Zeebe correlation key | `<zeebe:subscription correlationKey="…">` inside the message's `<bpmn:extensionElements>` |
| Layout coordinates | `<bpmndi:BPMNShape bpmnElement="…">` with `<dc:Bounds x="…" y="…" width="…" height="…">` |

### Step 4 — Report Results

Group by file, sort by severity (errors → warnings → info). For each violation, include a short quote of the rule's prose so the user understands the convention.

```
## Results

### `path/to/process.bpmn`

| Severity | Element | Rule | Half | Issue | Suggested Fix |
|----------|---------|------|------|-------|---------------|
| error   | `Activity_0x7f3a` | element-id-format   | deterministic | ID does not match `type_CamelCase` format | Rename to `serviceTask_SendEmail` |
| warning | `Activity_0x7f3a` | task-naming         | llm           | Name "do stuff" isn't a clear verb+noun    | "Send confirmation mail" |
| error   | `serviceTask_X`    | service-task-topic | semantic      | Topic `billing.x` doesn't match the element ID `X` | Use `billing.x` |

> **element-id-format**: Every element that matters for automation has an ID in the format `type_DescriptionInCamelCase`.

> **task-naming**: Describe what has to be done. One noun + one verb is usually enough.

### `path/to/other-process.bpmn`

No violations found.

---

**Summary**: 2 files checked, 2 errors, 1 warning.
```

### Step 5 — Provide Context

Beyond the table, add a short note for each distinct rule that was violated explaining *why* the convention matters (readability, maintainability, tooling compatibility). The goal is teaching, not shaming — this is the advantage AI-powered checking has over a regex linter.

## Orchestration

For fan-out validation across a large project, invoke the `bpmn-styleguide-validator` subagent once per BPMN file in parallel. Each subagent runs this skill on a single file; the orchestrator aggregates the reports. See `bpmn-to-code-skills/agents/bpmn-styleguide-validator.md`.
