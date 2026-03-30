# AI Assistant Skills

This project ships reusable skill definitions that AI coding assistants can discover automatically.
Skills live in `.agent/skills/` and are agent-agnostic by design.

## Directory Layout

```
bpmn-to-code-skills/           # Claude Code plugin (user-facing skills)
├── .claude-plugin/
│   └── plugin.json
└── skills/
    ├── setup-bpmn-to-code-gradle/SKILL.md
    ├── setup-bpmn-to-code-maven/SKILL.md
    └── migrate-to-bpmn-to-code-apis/SKILL.md

.agent/
└── skills/
    ├── setup-bpmn-to-code-gradle -> ../../bpmn-to-code-skills/skills/setup-bpmn-to-code-gradle
    ├── setup-bpmn-to-code-maven -> ../../bpmn-to-code-skills/skills/setup-bpmn-to-code-maven
    ├── migrate-to-bpmn-to-code-apis -> ../../bpmn-to-code-skills/skills/migrate-to-bpmn-to-code-apis
    ├── clean-code/SKILL.md
    ├── create-adr/SKILL.md
    ├── create-ticket/SKILL.md
    ├── bpmn-to-code-release/SKILL.md
    └── bpmn-to-code-validate-docs/SKILL.md
.claude -> .agent              # symlink for Claude Code compatibility
```

User-facing skills (setup, migration) live in `bpmn-to-code-skills/` so they can be distributed as a Claude Code plugin. Symlinks in `.agent/skills/` ensure contributors still discover all skills when working in the repo. Internal skills (create-ticket, release, etc.) remain directly in `.agent/skills/`.

## Claude Code Compatibility

Claude Code expects its configuration under `.claude/`.
A symlink (`.claude → .agent`) lets Claude Code discover skills transparently without duplicating files.

### Windows Users

Git on Windows checks out symlinks as plain text files by default.
To get a working symlink, enable Developer Mode (Windows 10+) and run:

```bash
git config core.symlinks true
git checkout -- .claude
```

Alternatively, enable "Enable symbolic links" during Git for Windows installation.

## Adding a New Skill

1. Create a folder under `.agent/skills/<skill-name>/`
2. Add a `SKILL.md` with frontmatter (`name`, `description`, `allowed-tools`) and instructions
3. Follow the structure of existing skills for reference
