---
name: bpmn-to-code-create-adr
argument-hint: "\"<short description of the decision>\""
allowed-tools: Read, Write, Glob
description: Write a new Architectural Decision Record (ADR) for this project. Use when the user wants to document an architectural or design decision.
---

# Skill: create-adr

Write a new Architectural Decision Record following the format used in `docs/adr/`.

## Instructions

### Step 1: Determine the next ADR number

Use Glob to list all files matching `docs/adr/*.md` (excluding `README.md`). 
Extract the three-digit numeric prefix from each filename (e.g. `011` from `011-variable-name-collision-detection.md`). 

The next number is the highest existing prefix plus one, zero-padded to three digits.
If no ADR files exist, start at `001`.

### Step 2: Derive the filename slug

Convert `$ARGUMENTS` to kebab-case: lowercase, replace spaces/underscores with hyphens, remove non-alphanumeric characters (except hyphens), collapse consecutive hyphens.

Target: `docs/adr/{NNN}-{slug}.md`

### Step 3: Read existing ADRs for content style

Read 1-2 recent ADRs to understand **content style and level of detail**:
- `docs/adr/011-variable-name-collision-detection.md`
- `docs/adr/010-operaton-namespace-only-extractor.md`

**Note**: Read these ONLY for writing style and technical detail level. Structure is defined in Step 5.

### Step 4: Gather content from user

Ask the user (all at once, skip if `$ARGUMENTS` already answers):
1. **Context**: What problem or situation motivated this decision?
2. **Decision drivers**: Key factors (extensibility, testability, simplicity, etc.)?
3. **Options considered**: Alternatives evaluated (description, pros, cons for each)?
4. **Decision**: Which option was chosen and why?
5. **Consequences**: Positive impacts, negative impacts, trade-offs?
6. **Technical implications**: How does this affect the architecture (domain/application/adapter layers, new services, etc.)?

### Step 5: Draft the ADR

Follow the structure from existing ADRs:

```
# ADR {NNN}: {Title}

## Status
Accepted

## Context
{Situation that motivated the decision}

## Decision
{What was decided and how it works}

### Implementation
{How this affects the codebase — new services, adapters, patterns}

## Consequences

### Positive
- {benefit}

### Negative
- {trade-off}
```

Add extra sections (e.g. `## Example`, `## Alternatives Considered`) only when they add meaningful value — follow the style of existing ADRs.

Show the complete draft to the user before writing.

### Step 6: Confirm and write

Ask: "Write this ADR to `{target-path}`? (yes / edit / cancel)"
- **yes** → write file, then update the index in `docs/adr/README.md` by adding a link under the appropriate category
- **edit** → apply changes, show updated draft, ask again
- **cancel** → stop without writing

### Step 7: Report

Output the created file path and remind the user to commit the ADR in the same commit as the related code change for traceability.
