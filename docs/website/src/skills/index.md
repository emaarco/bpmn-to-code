# 🧠 AI Agent Skills

bpmn-to-code ships with reusable skills for AI coding assistants. Skills work with [Claude Code](https://docs.anthropic.com/en/docs/claude-code) out of the box.

## Available Skills

| Skill | Description |
|-------|-------------|
| `setup-bpmn-to-code-gradle` | Set up the Gradle plugin in an existing project. Detects project structure, BPMN files, and output language. |
| `setup-bpmn-to-code-maven` | Set up the Maven plugin in an existing project. Adds plugin configuration to `pom.xml`. |
| `migrate-to-bpmn-to-code-apis` | Replace hardcoded BPMN strings with references to the generated Process API. Scans source code and shows a migration plan before applying changes. |

## Installation

### Plugin (recommended)

Install as a [Claude Code](https://docs.anthropic.com/en/docs/claude-code) plugin:

```bash
/plugin marketplace add emaarco/bpmn-to-code
/plugin install bpmn-to-code@bpmn-to-code
```

### Alternative: npx skills

Install individual skills using [`npx skills`](https://github.com/vercel-labs/skills):

```bash
# Install all skills at once
npx skills add https://github.com/emaarco/bpmn-to-code

# Or pick only what you need
npx skills add https://github.com/emaarco/bpmn-to-code/tree/main/.claude/skills/setup-bpmn-to-code-gradle
npx skills add https://github.com/emaarco/bpmn-to-code/tree/main/.claude/skills/setup-bpmn-to-code-maven
npx skills add https://github.com/emaarco/bpmn-to-code/tree/main/.claude/skills/migrate-to-bpmn-to-code-apis
```

## How Skills Work

Skills are Markdown-based prompt definitions stored in `.agent/skills/`. When an AI assistant recognizes a matching task, it loads the skill and follows its instructions. Skills provide:

- Step-by-step workflows for common tasks
- Context about the project's conventions and patterns
- Safety checks (e.g. read-only until confirmation)
- References to canonical examples in the repository
