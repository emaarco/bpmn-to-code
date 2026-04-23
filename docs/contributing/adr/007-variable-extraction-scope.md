# ADR 007: Variable Extraction Scope - Explicit Definitions Only

## Status
Accepted

## Context
BPMN processes use variables in multiple ways:
1. **Explicit variable definitions** via `camunda:inputOutput` (C7) or `zeebe:ioMapping` (Zeebe) on tasks/events
2. **Implicit variable references** in expressions within sequence flow conditions, gateways, or task configurations

Example of implicit variable reference:
```xml
<bpmn:conditionExpression xsi:type="bpmn:tFormalExpression">
  ${execution.getVariable('decision') == 'ACCEPTED'}
</bpmn:conditionExpression>
```

Example of explicit variable definition:
```xml
<camunda:inputOutput>
  <camunda:inputParameter name="subscriptionId">${subscriptionId}</camunda:inputParameter>
  <camunda:outputParameter name="mailSent">true</camunda:outputParameter>
</camunda:inputOutput>
```

The plugin must decide which variables to include in generated Process APIs.

## Decision
Extract **only explicitly defined variables** from `camunda:inputOutput` / `zeebe:ioMapping` elements. **Ignore** variables referenced in expressions (sequence flows, gateway conditions, script tasks, etc.).

### Rationale
1. **BPMN as Single Source of Truth**: Implicit variables in expressions often depend on external worker/message implementations that inject variables not defined in the model itself
2. **Model Integrity**: If a variable isn't explicitly defined in the BPMN model's I/O mappings, it's a hidden contract between the process and external code
3. **Maintainability**: Relying on implicit variables makes models incomplete and harder to understand without inspecting worker code
4. **Best Practice Alignment**: Encourages modelers to declare all process variables explicitly at their source (tasks, messages, events)

## Consequences

### Positive
- **Clear API surface**: Generated API only includes variables the model explicitly declares
- **Enforces good modeling**: Encourages teams to define variables at their origin points
- **Reduces coupling**: Process API doesn't leak implementation details from worker code
- **Easier refactoring**: All variable contracts visible in BPMN model

### Negative
- **Incomplete extraction**: Valid processes may use variables not appearing in generated API
- **Learning curve**: Users must understand why expression variables aren't extracted
- **Migration impact**: Existing models with poor variable hygiene need updates

## Alternatives Considered

**Extract variables from expressions** (Rejected)
- Pros: More complete variable list
- Cons:
  - Parses complex expression languages (JUEL, FEEL)
  - Includes variables that may not actually exist in process context
  - Breaks BPMN as single source of truth
  - Hides poor modeling practices

**Hybrid approach - explicit + annotated expressions** (Deferred)
- Could add optional expression parsing behind feature flag
- Complexity doesn't justify benefit currently
- Can revisit if user demand emerges

## Implementation

The scope of "explicit definitions" has expanded since the initial decision. All supported sources remain intentional BPMN declarations — no expression parsing is performed.

- **Camunda 7** (`Camunda7ModelExtractor.extractVariables()`):
  - `camunda:inputOutput` → `inputParameter`/`outputParameter` (I/O mappings)
  - `camunda:in`/`camunda:out` on call activities (call activity mappings)
  - `camunda:properties` with `name="additionalInputVariables"` / `name="additionalOutputVariables"` (directional extension properties, for elements like message start events that don't support I/O mappings)
  - `multiInstanceLoopCharacteristics` `camunda:collection` expression and `camunda:elementVariable` attribute
- **Zeebe** (`ZeebeModelExtractor.extractVariables()`):
  - `zeebe:ioMapping` → `input`/`output` elements
  - `zeebe:loopCharacteristics` `inputElement`/`inputCollection`/`outputElement`/`outputCollection` attributes
- **Operaton** (`OperatonModelExtractor.extractVariables()`): same as Camunda 7 using the `operaton:` namespace
- Expression parsing is not performed

## Subsequent decision (issue #290)

Direction is propagated from the BPMN source into the generated API: the `Variables.<Element>` object is split into `Inputs` / `Outputs` sub-objects. To keep every extracted variable unambiguously directional, the legacy undirected `additionalVariables` extension property was **removed** from the extraction rules and replaced by two directional variants — `additionalInputVariables` and `additionalOutputVariables`. See [ADR 015](./015-directional-variable-extraction.md).