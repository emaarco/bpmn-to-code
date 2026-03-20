# AI Assistant Skills

This project ships reusable skill definitions that AI coding assistants can discover automatically.
Skills live in `.agent/skills/` and are agent-agnostic by design.

## Directory Layout

```
.agent/
└── skills/
    ├── clean-code/SKILL.md
    ├── create-adr/SKILL.md
    ├── create-ticket/SKILL.md
    ├── migrate-to-bpmn-to-code-apis/SKILL.md
    ├── setup-bpmn-to-code-gradle/SKILL.md
    └── setup-bpmn-to-code-maven/SKILL.md
.claude -> .agent              # symlink for Claude Code compatibility
```

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
