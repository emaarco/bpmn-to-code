# 🏷️ API Versioning

The `useVersioning` parameter controls whether generated class names include a version suffix.

## How it works

When `useVersioning = true`, the generated class name includes a version number derived from the BPMN model:

- `NewsletterSubscriptionProcessApiV1`
- `NewsletterSubscriptionProcessApiV2`

When `useVersioning = false` (the default), no suffix is added:

- `NewsletterSubscriptionProcessApi`

## Recommended: Fail Fast (no versioning)

In most cases, **versioning is not worth the overhead**. Every time you bump the version, you must migrate all imports to the new version. If you don't, your codebase accumulates imports spread across multiple versions.

Instead, keep `useVersioning = false` and let breaking changes surface as compile errors:

1. Make breaking changes to your BPMN model (rename or delete elements)
2. Regenerate the Process API
3. Compilation errors immediately show where code needs updating
4. Fix the errors and move on

**Benefits:**
- Immediate feedback on what needs updating
- No version management overhead
- Single source of truth (always the latest API)
- Forces code to stay in sync with models

## When versioning makes sense

Versioning can be useful when:
- Multiple teams consume the same Process API and can't update simultaneously
- You need a migration period where old and new versions coexist
- Your deployment process requires backwards compatibility across versions
