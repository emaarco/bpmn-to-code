# ADR 015: Directional Variable Extraction (`Inputs` / `Outputs`)

## Status
Accepted

## Context
Per [ADR 007](./007-variable-extraction-scope.md), variables are extracted only from explicit BPMN declarations. The v2 API already scopes each variable to the element it originates from (`Variables.<Element>.<CONSTANT>`). However, a consumer reading `Variables.ServiceTaskClaimMembership.HAS_EMPTY_SPOTS` cannot tell from the API shape whether the variable is consumed by the task, produced by it, or both.

Direction is present in the source BPMN — every supported extraction path (`<zeebe:input>`/`<zeebe:output>`, `<camunda:inputParameter>`/`<camunda:outputParameter>`, `<camunda:in>`/`<camunda:out>`, multi-instance `inputElement`/`outputCollection`) has an unambiguous input-or-output semantic. The only previously supported source without direction was the `<camunda:property name="additionalVariables">` extension property, which lumped arbitrary names into a comma-separated list.

For AI-agent consumers reasoning about a process, and for humans writing a worker that needs to know what it receives vs. what it returns, the missing direction is a real friction point. GitHub issue [#290](https://github.com/emaarco/bpmn-to-code/issues/290) proposed surfacing direction in the generated API.

## Decision
Propagate direction from the BPMN source into the generated API. Every extracted variable carries a `VariableDirection` (either `INPUT` or `OUTPUT`). The generator splits `Variables.<Element>` into nested `Inputs` / `Outputs` sub-objects:

```kotlin
object Variables {
    object ActivitySendConfirmationMail {
        object Inputs {
            val SUBSCRIPTION_ID: VariableName = VariableName("subscriptionId")
        }
    }
    object StartEventSubmitRegistrationForm {
        object Outputs {
            val SUBSCRIPTION_ID: VariableName = VariableName("subscriptionId")
        }
    }
}
```

- **No undirected tier**: every source we extract from is directional.
- **No flat fallback**: `Variables.<Element>` always uses the split form. Empty `Inputs` or `Outputs` sub-objects are never emitted — one-sided splits are legal.
- **Legacy `additionalVariables` removed**: the single undirected `<camunda:property name="additionalVariables">` property is no longer extracted. It is replaced by two directional variants, `additionalInputVariables` and `additionalOutputVariables`, each working on any BPMN element.

Direction mapping per source:

| Source | Direction |
|--------|-----------|
| `<zeebe:input>`, `<camunda:inputParameter>`, `<camunda:in source="…">` or `sourceExpression="…">` | INPUT |
| `<zeebe:output>`, `<camunda:outputParameter>`, `<camunda:out target="…">` | OUTPUT |
| Multi-instance `inputElement` / `inputCollection` (Zeebe), `camunda:collection` / `camunda:elementVariable` (C7/Operaton) | INPUT |
| Multi-instance `outputElement` / `outputCollection` (Zeebe) | OUTPUT |
| `additionalInputVariables` property | INPUT |
| `additionalOutputVariables` property | OUTPUT |

## Consequences

### Positive
- Consumers can statically tell whether a variable flows into or out of an element.
- AI-agent consumers have an unambiguous contract for reasoning about task dataflow.
- No ambiguous/undirected surface on the API — the shape is always `Inputs` / `Outputs`.

### Negative / Breaking
- **API breaking change**: every `Variables.<Element>.X` reference in consumer code must be rewritten to `Variables.<Element>.Inputs.X` or `.Outputs.X`. The migration skill flags these for manual review.
- **BPMN breaking change for C7/Operaton users** relying on `additionalVariables`: existing `<camunda:property name="additionalVariables" value="..."/>` declarations are silently ignored. BPMN files must be updated to use `additionalInputVariables` / `additionalOutputVariables`.
- **Mixed-direction variables** (same name as INPUT on one element, OUTPUT on another) are preserved faithfully — they appear in both sub-objects as separate constants.

## Alternatives Considered

**Fallback-to-flat (issue's original wording)** — keep the flat `Variables.<Element>.X` shape when no IO mappings are present. Rejected: every supported source is now directional, so a flat fallback would only exist to accommodate the legacy `additionalVariables` property, which we are removing anyway. Uniform split shape is simpler to document and consume.

**Add an `UNDIRECTED` direction value and a third sub-object** — rejected for the same reason: once we remove the single undirected source, nothing needs this tier. Keeping the enum binary avoids a degenerate branch everywhere.

**Keep `additionalVariables` as an alias for `additionalInputVariables`** — rejected. Silent migration is worse than an explicit rename for a small feature; the migration skill surfaces the change, and the old property was sparsely used.

## Implementation
- Domain: `VariableDirection { INPUT, OUTPUT }`; `VariableDefinition(name, direction)` with direction required.
- Extractors: Zeebe / Camunda 7 / Operaton tag every source; dedup happens on `(name, direction)`.
- Writers: `KotlinProcessApiBuilder.VariablesWriter` and `JavaProcessApiBuilder.VariablesWriter` partition a node's variables and emit the `Inputs` / `Outputs` sub-objects (each only when non-empty).
- Migration skill: `migrate-bpmn-to-code-v1-to-v2` flags `Variables.<Element>.X` references and `additionalVariables` BPMN declarations for manual review.
