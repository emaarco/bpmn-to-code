# 🔶 Camunda 7

Engine-specific behavior when using `processEngine = CAMUNDA_7`.

## Service Task Detection

Service tasks are detected via one of these `camunda:` attributes:

- `camunda:delegateExpression` — e.g. `#{sendMailDelegate}`
- `camunda:class` — e.g. `com.example.SendMailDelegate`
- `camunda:topic` — for external tasks, e.g. `mail.send`

```xml
<bpmn:serviceTask id="Activity_SendMail" name="Send Mail"
    camunda:delegateExpression="#{sendMailDelegate}">
</bpmn:serviceTask>
```

The attribute value becomes the entry in the generated `ServiceTasks` object.

## Variable Extraction

### I/O Mappings

Variables are extracted from `camunda:inputOutput`:

```xml
<bpmn:extensionElements>
  <camunda:inputOutput>
    <camunda:inputParameter name="orderId">${orderId}</camunda:inputParameter>
    <camunda:outputParameter name="mailSent">true</camunda:outputParameter>
  </camunda:inputOutput>
</bpmn:extensionElements>
```

The `name` attribute of each parameter becomes a variable.

::: warning Message Start Events
`camunda:inputOutput` is **not supported** on message start events in Camunda 7. Use extension properties with `additionalInputVariables` / `additionalOutputVariables` instead (see below).
:::

### Call Activity Mappings

Call activity variables come from `camunda:in` and `camunda:out` mappings, **not** from `camunda:inputOutput`:

```xml
<bpmn:callActivity id="CallActivity_Process" calledElement="sub-process">
  <bpmn:extensionElements>
    <camunda:in source="orderId" target="orderId" />
    <camunda:out source="result" target="processResult" />
  </bpmn:extensionElements>
</bpmn:callActivity>
```

Variables are extracted from the `source` attribute of `camunda:in` and the `target` attribute of `camunda:out`.

### Multi-Instance

Multi-instance variables come from attributes on the `multiInstanceLoopCharacteristics` element:

```xml
<bpmn:multiInstanceLoopCharacteristics
    camunda:collection="${subscribers}"
    camunda:elementVariable="subscriber" />
```

**Extracted variables:** `subscribers` → Input (from `collection` expression), `subscriber` → Output (from `elementVariable`)

### Additional Input / Output Variables (Extension Properties)

For elements where I/O mappings aren't supported (like message start events), declare variables via `camunda:properties`. Two directional property names are recognised:

```xml
<bpmn:extensionElements>
  <camunda:properties>
    <camunda:property name="additionalInputVariables" value="orderId, customerEmail" />
    <camunda:property name="additionalOutputVariables" value="processingResult" />
  </camunda:properties>
</bpmn:extensionElements>
```

Each comma-separated value becomes a variable — values under `additionalInputVariables` land in the element's `Inputs` sub-object, values under `additionalOutputVariables` in `Outputs`. Works on any BPMN element. The legacy undirected `additionalVariables` property is no longer extracted — split your values into the two directional properties above.
