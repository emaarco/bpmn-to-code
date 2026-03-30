---
name: docs-dev
argument-hint: "\"<page or component to work on>\""
allowed-tools: Read, Write, Edit, Glob, Grep, Bash(npm *), Bash(cd *), mcp__playwright__*
description: "Develop and visually verify changes to the documentation site (VitePress). Use when working on doc pages, Vue components, VitePress config, or validating docs UI with Playwright MCP."
---

# Skill: docs-dev

Develop and visually verify changes to the bpmn-to-code documentation site.

## IMPORTANT

- **VitePress conventions**: Content is Markdown with VitePress extensions. Follow existing page structure.
- **Vue components**: Custom components live in `.vitepress/theme/`. Use Vue 3 Composition API.
- **Read before editing**: Always read the target files before making changes to understand existing patterns.
- **Build check**: Always run `npm run build` to catch broken links or build errors before finishing.

## Instructions

### Step 1 — Understand the architecture

The documentation site uses VitePress at `docs/website/`.

Key facts:
- **Content**: Markdown files in `docs/website/src/` (e.g. `index.md` for landing page)
- **Config**: `docs/website/src/.vitepress/config.mts` — site title, navigation, sidebar, footer, social links
- **Theme**: `docs/website/src/.vitepress/theme/` — custom theme extending DefaultTheme
- **Vue components**: e.g. `VersionBadge.vue` (fetches latest GitHub release, displays in nav bar)
- **Custom CSS**: `docs/website/src/.vitepress/theme/style.css`
- **Dependencies**: Read versions from `docs/website/package.json` (do not hardcode)

### Step 2 — Review current state

Read the files relevant to `$ARGUMENTS`. Key files and their purposes:

| File | Purpose |
|------|---------|
| `docs/website/src/index.md` | Landing page: hero section, feature cards, code comparison |
| `docs/website/src/.vitepress/config.mts` | Site config: nav bar, sidebar, footer, social links |
| `docs/website/src/.vitepress/theme/index.ts` | Theme setup: extends DefaultTheme, registers components |
| `docs/website/src/.vitepress/theme/style.css` | Custom CSS overrides |
| `docs/website/src/.vitepress/theme/components/*.vue` | Custom Vue components |
| `docs/website/src/web/index.md` | "Try in Browser" page linking to web module |
| `docs/website/src/getting-started/*.md` | Gradle and Maven setup guides |
| `docs/website/src/engines/*.md` | Engine-specific documentation |
| `docs/website/src/guide/*.md` | Configuration and generated API guides |

### Step 3 — Make changes

Follow these conventions:
- **Markdown**: Use VitePress frontmatter (`layout`, `hero`, `features`). Use `:::` containers for tips/warnings.
- **Vue components**: Vue 3 Composition API (`<script setup>`). Register in `theme/index.ts`.
- **Navigation**: Update `config.mts` `themeConfig.nav` and `themeConfig.sidebar` for new pages.
- **Links**: Use relative paths for internal links. External links get `target: _blank`.
- **Images**: Store in `docs/website/src/public/` and reference with absolute paths.

### Step 4 — Build and run locally

```bash
# Start dev server (with hot reload)
cd docs/website && npm run dev

# Build for production (catches broken links)
cd docs/website && npm run build

# Preview production build
cd docs/website && npm run preview
```

### Step 5 — Visual verification (only on request)

**Do not run visual verification automatically.** Use `AskUserQuestion` to ask the user what should be verified. Only proceed with the steps below if the user or `$ARGUMENTS` explicitly request visual testing.

**Prerequisite**: Playwright MCP must be configured in the agent's MCP settings.

1. Ensure the docs site is running (`cd docs/website && npm run dev` in the background)
2. Navigate to the local dev server URL
3. Perform only the verification steps the user requested
4. Store screenshots in `.tmp/` (gitignored). **Never check screenshots into git.**
5. Name screenshots descriptively: `docs-{page}-{state}.png`
6. Clean up `.tmp/` when the skill terminates

To attach screenshots to a PR, upload them as draft release assets:
```bash
gh release create screenshots-pr-<NUMBER> --draft --title "PR #<NUMBER> Screenshots" --notes "Temporary" .tmp/*.png
gh release view screenshots-pr-<NUMBER> --json assets --jq '.assets[] | .url'
gh pr comment <NUMBER> --body "![description](<asset-url>)"
```
The draft release can be deleted after the PR is merged.

### Step 6 — Report

Summarize:
- What was changed and why
- Which files were modified
- Build result (`npm run build` passed/failed)
- Verification results (screenshots taken, issues found)
- Any follow-up items
