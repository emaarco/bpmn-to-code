---
name: web-dev
argument-hint: "\"<component or page to work on>\""
allowed-tools: Read, Write, Edit, Glob, Grep, Bash(./gradlew *), mcp__playwright__*
description: "Develop and visually verify changes to the web module (bpmn-to-code-web). Use when working on the vanilla JS frontend, Ktor backend, or validating UI changes with Playwright MCP."
---

# Skill: web-dev

Develop and visually verify changes to the bpmn-to-code web module.

## IMPORTANT

- **No frameworks**: The frontend uses vanilla HTML/CSS/JS per ADR 009. Never introduce React, Vue, or any JS framework.
- **No npm/build tooling**: External libraries must be loaded via CDN `<script>` tags only.
- **Single files**: All JS lives in `app.js`, all CSS in `styles.css`. Do not split into multiple files.
- **Read before editing**: Always read the target files before making changes to understand existing patterns.

## Instructions

### Step 1 — Understand the architecture

Read ADR 009 (`docs/adr/009-ktor-static-frontend-single-module.md`) for architectural context.

Key facts:
- **Backend**: Ktor 3.x (Kotlin) serving static files + REST API
- **Frontend**: Vanilla HTML/CSS/JS — no frameworks, no npm, no build pipeline
- **Deployment**: Single fat JAR with embedded frontend (`./gradlew :bpmn-to-code-web:buildFatJar`)
- **External libs**: CDN only (e.g. highlight.js for syntax highlighting, bpmn.js for diagram rendering)
- **Static files location**: `bpmn-to-code-web/src/main/resources/static/`

### Step 2 — Review current state

Read the files relevant to `$ARGUMENTS`. The key files and their purposes:

| File | Purpose |
|------|---------|
| `bpmn-to-code-web/src/main/resources/static/index.html` | Page structure, sections, CDN script tags |
| `bpmn-to-code-web/src/main/resources/static/css/styles.css` | All styling, CSS custom properties in `:root`, responsive breakpoints |
| `bpmn-to-code-web/src/main/resources/static/js/app.js` | All logic: state management, API calls, DOM manipulation |
| `bpmn-to-code-web/src/main/kotlin/io/github/emaarco/bpmn/web/Application.kt` | Ktor routing, static resource serving, plugin installation |
| `bpmn-to-code-web/src/main/kotlin/io/github/emaarco/bpmn/web/routes/GenerateRoutes.kt` | API endpoint for BPMN code generation |
| `bpmn-to-code-web/src/main/kotlin/io/github/emaarco/bpmn/web/service/WebGenerationService.kt` | Backend generation logic |

API endpoints:
- `POST /api/generate` — accepts base64-encoded BPMN files, returns generated code
- `GET /api/config` — returns version and legal links for the frontend
- `GET /health` — health check

### Step 3 — Make changes

Follow these conventions:
- **CSS**: Use existing custom properties from `:root` (e.g. `--primary-color`, `--text-primary`, `--bg-gray`). Add new styles to `styles.css`.
- **JS**: Follow the existing pattern in `app.js` — state object, DOM references, event listeners, helper functions. Use `const`/`let`, template literals, async/await.
- **HTML**: Follow existing section structure (`.card` wrapper, heading, content). Use semantic elements.
- **Responsive**: Use existing media query breakpoints (`640px`, `768px`). Test mobile layout.

### Step 4 — Build and run locally

```bash
# Run backend tests
./gradlew :bpmn-to-code-web:test

# Start the web module (port 8080)
./gradlew :bpmn-to-code-web:run
```

The app will be available at `http://localhost:8080/static/index.html`.

### Step 5 — Visual verification with Playwright MCP

**Prerequisite**: Playwright MCP must be configured in the agent's MCP settings.

Use Playwright MCP to visually verify the changes:

1. Ensure the web module is running (`./gradlew :bpmn-to-code-web:run` in the background)
2. Navigate to `http://localhost:8080/static/index.html`
3. Interact with the page as needed (upload files, click buttons, fill forms)
4. Take screenshots at key states
5. Store screenshots in `.agent/.tmp/screenshots/` (already gitignored)
6. Name screenshots descriptively: `web-{page}-{state}.png`

Common verification scenarios:
- **Initial load**: Hero section, feature badges, upload area visible
- **File uploaded**: File list populated, config section visible, BPMN diagram rendered
- **Code generated**: Results section with syntax-highlighted code, CTA section visible
- **Mobile viewport**: Responsive layout at 375px and 640px widths
- **Error state**: Error message display after invalid input

To attach screenshots to a PR:
```bash
gh pr comment <PR_NUMBER> --body "![screenshot description](path/to/screenshot.png)"
```

### Step 6 — Report

Summarize:
- What was changed and why
- Which files were modified
- Verification results (screenshots taken, issues found)
- Any follow-up items
