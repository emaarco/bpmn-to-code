---
name: build-bpmn-styleguide
description: "Interactively author a BPMN_STYLE_GUIDE.md for your project — a handbook that explains how the team models BPMN processes (naming, allowed elements, IDs, topics, engine-specific special cases). The output is a document humans can read; embedded YAML annotations let downstream skills enforce the rules. Use when the user asks to 'create a BPMN style guide', 'set up BPMN conventions', or 'define BPMN naming rules'."
allowed-tools: Read, Write, Glob, Grep, AskUserQuestion
---

# Skill: build-bpmn-styleguide

Interactively author `BPMN_STYLE_GUIDE.md` — a **human-readable handbook** that documents how the team models BPMN processes. The document teaches developers what good models look like; embedded YAML annotations make the same document machine-parseable so `/validate-bpmn-style` and `/generate-rules-to-enforce-bpmn-styleguide` can enforce the rules.

## IMPORTANT

- This skill describes **BPMN conventions only** — NOT code patterns (workers, adapters, frameworks are out of scope).
- The output is **prose first, YAML second**. A reader should be able to skim the document and know how to model; tooling reads the YAML annotations to enforce the rules.
- The skill knows nothing about downstream enforcement details (which built-in rules exist in `bpmn-to-code-testing`, which CI runs what). Consumers decide.
- Never modify existing BPMN files or source code.
- If `BPMN_STYLE_GUIDE.md` already exists, ask whether to overwrite, extend, or abort.
- Use `AskUserQuestion` for every user decision. Default to `[RECOMMENDED]` answers; let the user accept in one keystroke.
- Always load `resources/example-bpmn-style-guide.md` before emitting. It is the canonical shape to copy.

## Reference Material

Load these files as needed during the wizard. They're part of the skill, not the project.

- `resources/example-bpmn-style-guide.md` — canonical output shape. Emit files that match its structure.
- `resources/engines/camunda-7.md` — C7 supported elements, service-task kinds, async triple, variables, timers, transaction-boundary best practices.
- `resources/engines/operaton.md` — Operaton deltas vs. C7 (namespace).
- `resources/engines/cib-7.md` — CIB-7 deltas vs. C7 (none; uses C7 namespace).
- `resources/engines/zeebe.md` — Zeebe support matrix, job-worker model, correlation keys, FEEL timer expressions.

## Output Format

The emitted file is Markdown that reads like documentation. Each rule block has:

1. **Prose**: the convention, the rationale, examples of good and bad.
2. **A YAML annotation** immediately after the prose:

   ```markdown
   <!-- rule:<kebab-case-slug> -->
   ```yaml
   category: business-modeling | technical-configuration
   severity: error | warning | info
   applies-to: [<BpmnElementType...>]
   validation: deterministic | llm | hybrid
   pattern: "<regex>"           # required when validation includes deterministic
   engine: [CAMUNDA_7, ...]      # optional; defaults to the file's top-level Engine:
   ```
   ```

The `<!-- rule:slug -->` HTML comment is invisible when the Markdown is rendered but survives in source — tools find rules by this anchor. Any YAML block not preceded by such a comment is ignored.

### The `validation` field

- `deterministic` — fully checkable by regex or a structural check on the domain model. `/generate-rules-to-enforce-bpmn-styleguide` picks it up; `/validate-bpmn-style` runs the regex.
- `llm` — requires semantic judgement (naming quality, layout, notation choices). Only `/validate-bpmn-style` enforces it.
- `hybrid` — has both. The regex catches the shape; the AI linter also checks the semantic half using the rule's prose as the brief. Code generation emits only the deterministic half.

## Instructions

### Step 1 — Scan Existing Conventions and Load Engine Metadata

1. Glob for `**/*.bpmn` (exclude `build/`, `target/`, `node_modules/`).
2. Read up to three sample files. Detect:
   - Element ID patterns (e.g. `serviceTask_Xxx`, `Activity_Xxx`).
   - Process ID format (kebab-case, camelCase, etc.).
   - Whether elements have display names.
   - Message / signal / error naming patterns.
   - Engine from XML namespaces: `xmlns:camunda` → CAMUNDA_7 / CIB-7; `xmlns:zeebe` → ZEEBE; `xmlns:operaton` → OPERATON.
3. Load the matching `resources/engines/<engine>.md`. This tells you which elements, implementation kinds, and attributes the engine supports — don't propose rules for features the engine lacks (e.g. `asyncBefore` on Zeebe; `JAVA_DELEGATE` on Zeebe).
4. If no BPMN files are found, ask the user which engine to target.

### Step 2 — Phase 1: Context (one question)

`AskUserQuestion` for:
- Confirm the detected engine (or choose one; treat CIB-7 as CAMUNDA_7 for the `Engine:` header but mention the distinction in prose).
- Language: English or German.
- Any team context worth capturing in the introduction (optional).

Write the document header: title, `Engine: …`, `Language: …`, and an intro paragraph that previews the three sections.

### Step 3 — Phase 2: Business Modeling (🎨)

Elicit the mental model. These rules are mostly `validation: llm`.

Ask about (grouped into one or two `AskUserQuestion` calls):

- **Naming conventions** — tasks (verb + noun, present tense), events (noun + past/passive), gateways (short question), pools/lanes (speaking names). Show detected patterns as defaults.
- **Allowed elements** — which BPMN elements does the team use, and when to reach for each? This is the single most important section for a newcomer. Write it as a bulleted list like the example (`User Task` for human work, `Message Catch Event` for external input, etc.).
- **Layout** — left-to-right, spacing, crossing avoidance. Default `[RECOMMENDED]` is to include these as `severity: info` rules.

For each rule, emit prose + `<!-- rule:slug -->` + YAML.

### Step 4 — Phase 3: Technical Configuration (🔧)

Mostly `validation: deterministic` or `hybrid`.

Ask about (one `AskUserQuestion` is usually enough, since most teams have a fixed opinion):

- **Element ID format** — default `type_DescriptionInCamelCase`.
- **Process ID format** — default kebab-case, no `process_` prefix, no version suffix.
- **Message ID schema** — default `<serviceName>.<myState>`. Offer `hybrid` if the team wants the semantic half ("serviceName matches bounded context") enforced too.
- **Service task topic schema** — default `<serviceName>.<elementIdWithoutPrefix>`. Same `hybrid` offer.
- **Gateway outgoing flow labels** — default required.
- **Implementation-kind consistency** (C7 / Operaton / CIB-7 only) — offer the four kinds; most teams pick `EXTERNAL_TASK`. Skip for Zeebe.
- **Process variables minimization** — include as `llm` info-level guidance.

Emit each rule with prose (schema + good/bad examples) + YAML annotation.

### Step 5 — Phase 4: Special Cases (⚠️)

Engine-specific patterns. Don't just *offer* — analyze the user's models first, then ask them to choose.

#### For CAMUNDA_7 / OPERATON / CIB-7 — async continuations

1. **Load the best-practice default.** Read the "Transaction Boundaries — Async Continuation Best Practices" section in `resources/engines/camunda-7.md`. That table (external service tasks: no async; start / user / boundary / intermediate-catch: `asyncAfter=true`) is the default. Non-external service-task kinds (`JAVA_DELEGATE`, `DELEGATE_EXPRESSION`, `EXPRESSION`) have no default — don't emit a rule for them.
2. **Analyze existing BPMN files.** For every BPMN file found in Step 1, tally each element type in the default table and count how many elements match / violate the default. Present a short "current state" summary.
3. **Ask the user what to do** via `AskUserQuestion`. Four options:
   - **Adopt best-practice default** — emit the rule exactly as in the engine reference.
   - **Use detected conventions** — emit the rule with the team's current patterns baked in (e.g. if the team uses `asyncBefore` on service tasks, encode that).
   - **Customize** — walk through each element type and confirm the default or specify the team's preference.
   - **Skip** — don't include the rule.
4. **Emit** a single `<!-- rule:async-continuations -->` block in the Special Cases section with `validation: deterministic` (per-element attribute check). Use the example file's Special Cases section as the prose template.

#### For ZEEBE — message correlation keys

Default: every `<bpmn:message>` has a `<zeebe:subscription correlationKey="…">`. Analyze existing messages, then ask via `AskUserQuestion`: adopt default / use detected (maybe the team has a relaxed rule) / skip.

#### No applicable special cases

If the user declines everything, omit the `## ⚠️ Special Cases` section entirely.

### Step 6 — Phase 5: Review & Generate

1. Assemble the full document, following the example shape:
   - Title + `Engine:` + `Language:` header
   - Introductory paragraph
   - `## 🎨 Business Modeling` with the emitted rule blocks
   - `## 🔧 Technical Configuration` with its rule blocks
   - `## ⚠️ Special Cases` (if any)
   - `## Validation Reference` (the field schema table — copy from the example verbatim)
2. Show a preview. `AskUserQuestion` asks for confirmation or adjustments.
3. On approval, write `BPMN_STYLE_GUIDE.md` to the project root.
4. Suggest next steps:
   - `/validate-bpmn-style` to lint BPMN files against the guide.
   - `/generate-rules-to-enforce-bpmn-styleguide` to generate deterministic rules for the testing module.

## Authoring Tips

- Favour narrative over enumeration. A modeler reading the guide should understand *why*, not just *what*.
- Include good/bad examples under every rule where it helps — lifted straight from the user's domain if you have enough context.
- When a rule is `validation: llm`, the prose IS the rule (there's no regex). Make the prose concrete enough that `/validate-bpmn-style` has a clear brief.
- When a rule is `validation: hybrid`, the prose should make the two halves explicit ("The shape is enforced by regex; the semantics are checked by the AI linter — it verifies that…").
