# AI Agent Skills

bpmn-to-code ships with reusable skills for AI coding assistants.
Skills are stored under [`.agent/skills/`](../.agent/skills/) and work with [Claude Code](https://docs.anthropic.com/en/docs/claude-code) out of the box.

## Setup

| Skill | Description |
|-------|-------------|
| [setup-bpmn-to-code-gradle](../.agent/skills/setup-bpmn-to-code-gradle/SKILL.md) | Set up the bpmn-to-code Gradle plugin in an existing project |
| [setup-bpmn-to-code-maven](../.agent/skills/setup-bpmn-to-code-maven/SKILL.md) | Set up the bpmn-to-code Maven plugin in an existing project |
| [migrate-to-bpmn-to-code-apis](../.agent/skills/migrate-to-bpmn-to-code-apis/SKILL.md) | Replace hardcoded BPMN strings with generated Process API references |

## Project Management

| Skill | Description |
|-------|-------------|
| [create-ticket](../.agent/skills/create-ticket/SKILL.md) | Create or update GitHub issues with structured templates |
| [create-adr](../.agent/skills/create-adr/SKILL.md) | Write Architectural Decision Records (ADRs) |
| [bpmn-to-code-release](../.agent/skills/bpmn-to-code-release/SKILL.md) | Create a versioned release and publish to Maven, Gradle, and Docker |

## Code Quality

| Skill | Description |
|-------|-------------|
| [clean-code](../.agent/skills/clean-code/SKILL.md) | Review code against Clean Code principles |

## Installation

### Plugin (recommended)

Install as a [Claude Code](https://docs.anthropic.com/en/docs/claude-code) plugin:

```bash
/plugin marketplace add emaarco/bpmn-to-code
/plugin install bpmn-to-code@bpmn-to-code
```

### Alternative: npx skills

Install skills using [`npx skills`](https://github.com/vercel-labs/skills):

```bash
# Install all skills at once
npx skills add https://github.com/emaarco/bpmn-to-code

# Or pick only what you need
npx skills add https://github.com/emaarco/bpmn-to-code/tree/main/.claude/skills/setup-bpmn-to-code-gradle
npx skills add https://github.com/emaarco/bpmn-to-code/tree/main/.claude/skills/setup-bpmn-to-code-maven
npx skills add https://github.com/emaarco/bpmn-to-code/tree/main/.claude/skills/migrate-to-bpmn-to-code-apis
```
