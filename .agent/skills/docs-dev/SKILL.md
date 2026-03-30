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
- **Dependencies**: VitePress 1.6.4, Vue 3.5.31 (in `docs/website/package.json`)

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

### Step 5 — Visual verification with Playwright MCP

**Prerequisite**: Playwright MCP must be configured in the agent's MCP settings.

Use Playwright MCP to visually verify the changes:

1. Ensure the docs site is running (`cd docs/website && npm run dev` in the background)
2. Navigate to the local dev server URL
3. Check affected pages: layout, content, navigation, links
4. Take screenshots at key states
5. Store screenshots in `.agent/.tmp/screenshots/` (already gitignored)
6. Name screenshots descriptively: `docs-{page}-{state}.png`

Common verification scenarios:
- **Landing page**: Hero section, feature cards, code comparison
- **Navigation**: Sidebar links, nav bar, breadcrumbs
- **Content pages**: Headings, code blocks, tables, callouts
- **Mobile viewport**: Responsive layout, hamburger menu
- **Links**: External links open correctly, internal links navigate properly

To attach screenshots to a PR:
```bash
gh pr comment <PR_NUMBER> --body "![screenshot description](path/to/screenshot.png)"
```

### Step 6 — Report

Summarize:
- What was changed and why
- Which files were modified
- Build result (`npm run build` passed/failed)
- Verification results (screenshots taken, issues found)
- Any follow-up items
