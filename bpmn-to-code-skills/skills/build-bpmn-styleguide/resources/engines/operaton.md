# Operaton — Supported BPMN Elements and Configuration

Operaton is a fork of Camunda 7. Element support, service-task implementation kinds, the async triple, variables, messages/signals/errors/escalations, and timers are **identical** to Camunda 7.

**Read `camunda-7.md` first for the full list.** This file only records deltas.

## Differences vs. Camunda 7

### XML namespace

- C7: `xmlns:camunda="http://camunda.org/schema/1.0/bpmn"` with prefix `camunda:`
- Operaton: `xmlns:operaton="http://operaton.org/schema/1.0/bpmn"` with prefix `operaton:`

All engine-specific attributes use the `operaton:` prefix (e.g. `operaton:type`, `operaton:topic`, `operaton:delegateExpression`, `operaton:asyncBefore`).

### Implementation kinds

Same four as C7: `EXTERNAL_TASK`, `JAVA_DELEGATE`, `DELEGATE_EXPRESSION`, `EXPRESSION`.

Source: `bpmn-to-code-core/src/main/kotlin/io/github/emaarco/bpmn/adapter/outbound/engine/extractor/OperatonImplementationKind.kt`

### Example XML snippet

```xml
<bpmn:serviceTask id="serviceTask_SendEmail"
                  name="Send email"
                  operaton:type="external"
                  operaton:topic="newsletter.sendEmail"
                  operaton:asyncBefore="true" />
```

## Transaction Boundaries

Same conventions as Camunda 7 — see the "Transaction Boundaries — Async Continuation Best Practices" section in `camunda-7.md`. The attribute prefix is `operaton:` instead of `camunda:`, but the rules are identical.

## Style Guide Implications

Patterns and rule shapes used for Camunda 7 apply unchanged — only the attribute prefix differs. When generating a style guide for Operaton, reuse C7 conventions and swap `camunda:` → `operaton:` in any example XML.
