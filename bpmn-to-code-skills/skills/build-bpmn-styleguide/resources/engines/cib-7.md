# CIB-7 — Supported BPMN Elements and Configuration

CIB-7 is a community-maintained fork of Camunda 7 (continuation of C7 after Camunda Services ended C7 support). Element support, service-task implementation kinds, the async triple, variables, messages/signals/errors/escalations, and timers are **identical** to Camunda 7.

**Read `camunda-7.md` first for the full list.** This file only records deltas.

## Differences vs. Camunda 7

### XML namespace

CIB-7 keeps the Camunda namespace for backwards compatibility:

- `xmlns:camunda="http://camunda.org/schema/1.0/bpmn"` with prefix `camunda:`

Existing C7 BPMN files open and run unchanged on CIB-7.

### Implementation kinds

Same four as C7: `EXTERNAL_TASK`, `JAVA_DELEGATE`, `DELEGATE_EXPRESSION`, `EXPRESSION`.

bpmn-to-code treats CIB-7 as `CAMUNDA_7` in `ProcessEngine`. When a user says "CIB-7", select the `CAMUNDA_7` engine in this skill.

## Transaction Boundaries

Same conventions as Camunda 7 — see the "Transaction Boundaries — Async Continuation Best Practices" section in `camunda-7.md`. CIB-7 keeps the `camunda:` prefix, so the XML examples there apply verbatim.

## Style Guide Implications

Same as C7. Generate C7-style guides unchanged for CIB-7 projects; there is nothing to configure differently.
