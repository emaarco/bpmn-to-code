# bpmn-to-code Skills Plugin

Claude Code plugin with AI skills for setting up and using the [bpmn-to-code](https://github.com/emaarco/bpmn-to-code) Gradle/Maven plugin.

## Installation

```bash
# 1. Register the marketplace (one-time)
/plugin marketplace add emaarco/bpmn-to-code

# 2. Install the plugin
/plugin install bpmn-to-code@bpmn-to-code
```

## Included Skills

| Skill | Description |
|-------|-------------|
| `setup-bpmn-to-code-gradle` | Set up the Gradle plugin in an existing project. Detects project structure, BPMN files, and output language. |
| `setup-bpmn-to-code-maven` | Set up the Maven plugin in an existing project. Adds plugin configuration to `pom.xml`. |
| `migrate-to-bpmn-to-code-apis` | Replace hardcoded BPMN strings with references to the generated Process API. |
| `build-bpmn-styleguide` | Interactively create a `BPMN_STYLE_GUIDE.md` with team conventions for naming, IDs, and layout. |
| `validate-bpmn-style` | Check BPMN files against a `BPMN_STYLE_GUIDE.md` and report violations with explanations. |
| `generate-rules-to-enforce-bpmn-styleguide` | Generate Kotlin `BpmnValidationRule` implementations for all automatable style guide rules. |
| `generate-rule-to-enforce-bpmn-styleguide` | Generate a Kotlin `BpmnValidationRule` for a single rule — useful while drafting or iterating. |

## Usage

Once installed, skills are available as slash commands:

```
/bpmn-to-code:setup-bpmn-to-code-gradle
/bpmn-to-code:setup-bpmn-to-code-maven
/bpmn-to-code:migrate-to-bpmn-to-code-apis
/bpmn-to-code:build-bpmn-styleguide
/bpmn-to-code:validate-bpmn-style
/bpmn-to-code:generate-rules-to-enforce-bpmn-styleguide
/bpmn-to-code:generate-rule-to-enforce-bpmn-styleguide
```

Or describe what you need and Claude will invoke the appropriate skill automatically.

## Included Subagents

| Subagent | Description |
|----------|-------------|
| `bpmn-styleguide-validator` | Wraps `/validate-bpmn-style` so the orchestrator can keep XML parsing and rule-by-rule judgement out of its main context, or fan out validation in parallel across many BPMN files (one subagent per file). |

Invoke with `@bpmn-to-code:bpmn-styleguide-validator` or let Claude delegate automatically.
