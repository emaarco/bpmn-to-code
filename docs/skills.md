# AI Agent Skills

bpmn-to-code ships with reusable skills for AI coding assistants.
Skills are stored under [`.agent/skills/`](../.agent/skills/) and work with [Claude Code](https://docs.anthropic.com/en/docs/claude-code) out of the box.

## Setup

| Skill | Description |
|-------|-------------|
| [setup-gradle](../.agent/skills/setup-gradle/SKILL.md) | Set up the bpmn-to-code Gradle plugin in an existing project |
| [setup-maven](../.agent/skills/setup-maven/SKILL.md) | Set up the bpmn-to-code Maven plugin in an existing project |
| [migrate-to-api](../.agent/skills/migrate-to-api/SKILL.md) | Replace hardcoded BPMN strings with generated Process API references |

## Project Management

| Skill | Description |
|-------|-------------|
| [create-ticket](../.agent/skills/create-ticket/SKILL.md) | Create or update GitHub issues with structured templates |
| [create-adr](../.agent/skills/create-adr/SKILL.md) | Write Architectural Decision Records (ADRs) |

## Code Quality

| Skill | Description |
|-------|-------------|
| [clean-code](../.agent/skills/clean-code/SKILL.md) | Review code against Clean Code principles |

## Installation

Install skills in [Claude Code](https://docs.anthropic.com/en/docs/claude-code) using [`npx skills`](https://github.com/vercel-labs/skills):

```bash
# Install all skills at once
npx skills add https://github.com/emaarco/bpmn-to-code

# Or pick only what you need
npx skills add https://github.com/emaarco/bpmn-to-code/tree/main/.claude/skills/setup-gradle
npx skills add https://github.com/emaarco/bpmn-to-code/tree/main/.claude/skills/setup-maven
npx skills add https://github.com/emaarco/bpmn-to-code/tree/main/.claude/skills/migrate-to-api
```
