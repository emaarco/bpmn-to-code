# validate-docs

Validate that the documentation website pages accurately reflect the current codebase. Compares what the docs claim against what the code actually does, and flags mismatches.

## When to use

- After changing engine extractors, code generators, or plugin parameters
- Before a release, to ensure docs are up to date
- When adding new features that should be documented

## Validation checklist

### 1. Engine pages vs extractors

For each engine (Zeebe, Camunda 7, Operaton), compare the doc page against its extractor:

| Engine | Doc page | Extractor |
|--------|----------|-----------|
| Zeebe | `docs/website/src/engines/zeebe.md` | `bpmn-to-code-core/src/main/kotlin/io/github/emaarco/bpmn/adapter/outbound/engine/zeebe/ZeebeModelExtractor.kt` |
| Camunda 7 | `docs/website/src/engines/camunda7.md` | `bpmn-to-code-core/src/main/kotlin/io/github/emaarco/bpmn/adapter/outbound/engine/camunda7/Camunda7ModelExtractor.kt` |
| Operaton | `docs/website/src/engines/operaton.md` | `bpmn-to-code-core/src/main/kotlin/io/github/emaarco/bpmn/adapter/outbound/engine/operaton/OperatonModelExtractor.kt` |

For each engine, verify:
- **Service task detection**: Which attributes/elements are parsed? Does the doc match?
- **Variable extraction**: Which sources (I/O mappings, multi-instance, call activity mappings, extension properties/additionalVariables)? Are all documented? Are any documented that don't exist?
- **Call activities**: How is the called element resolved? Does the doc match?
- **Any other sections**: Does the doc claim features that aren't implemented?
- **Missing documentation**: Are there implemented features the docs don't mention? Would they be relevant enough to document?

Also check the shared extractors in `bpmn-to-code-core/src/main/kotlin/io/github/emaarco/bpmn/adapter/outbound/engine/shared/` — these handle flow nodes, messages, errors, signals, and timers for all engines.

### 2. Configuration page vs plugin parameters

Compare `docs/website/src/guide/configuration.md` against:
- `bpmn-to-code-gradle/src/main/kotlin/io/github/emaarco/bpmn/adapter/GenerateBpmnModelsTask.kt`
- `bpmn-to-code-maven/src/main/kotlin/io/github/emaarco/bpmn/adapter/BpmnModelMojo.java`

Verify:
- All parameters listed in the docs exist in the code
- All parameters in the code are listed in the docs
- Types, defaults, and descriptions are accurate
- `ProcessEngine` enum values match (check `bpmn-to-code-core/src/main/kotlin/io/github/emaarco/bpmn/domain/shared/ProcessEngine.kt`)
- `OutputLanguage` enum values match

### 3. Generated API page vs code generators

Compare `docs/website/src/guide/generated-api.md` against:
- `bpmn-to-code-core/src/main/kotlin/io/github/emaarco/bpmn/adapter/outbound/codegen/`

Verify:
- All sections listed (Elements, CallActivities, Messages, TaskTypes, Timers, Errors, Signals, Variables) actually get generated
- The example code matches what the generators would produce
- No generated sections are missing from the docs

### 4. Getting started pages vs actual plugin versions

Check that version numbers in `docs/website/src/getting-started/gradle.md` and `maven.md` match the latest published versions (check `gradle.properties` or the latest release tag).

## How to run

1. Read each extractor file listed above
2. Read each doc page listed above
3. Compare feature-by-feature using the checklist
4. Report findings as a table:

```
| Page | Section | Status | Issue |
|------|---------|--------|-------|
| zeebe.md | Service Tasks | ✅ | — |
| zeebe.md | Headers | ❌ | Documented but not implemented |
```

5. Fix any issues found, or flag them for the user if the fix is ambiguous (could be missing code OR wrong docs)
