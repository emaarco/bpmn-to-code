# AI Skills Architecture

Skills are stored in `.agent/skills/` and symlinked into `.claude/` for Claude Code compatibility.
See [Agent Skills](/skills/) for the full list of available skills and installation instructions.

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
