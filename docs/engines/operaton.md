# 🟢 Operaton

Engine-specific behavior when using `processEngine = OPERATON`.

[Operaton](https://operaton.org/) is an open-source fork of Camunda 7. It follows the same extraction patterns as Camunda 7, but uses its own XML namespace (`http://operaton.org/schema/1.0/bpmn`) with the `operaton:` prefix instead of `camunda:`.

::: tip Migrating from Camunda 7?
If your Operaton models were originally created with Camunda 7 Modeler and still use `camunda:` namespace attributes, use `processEngine = CAMUNDA_7` instead until you migrate the namespace.
:::

## Service Task Detection

Service tasks are detected via one of these `operaton:` attributes:

- `operaton:delegateExpression` — e.g. `#{sendMailDelegate}`
- `operaton:class` — e.g. `com.example.SendMailDelegate`
- `operaton:topic` — for external tasks, e.g. `mail.send`

```xml
<bpmn:serviceTask id="Activity_SendMail" name="Send Mail"
    operaton:delegateExpression="#{sendMailDelegate}">
</bpmn:serviceTask>
```

The attribute value becomes the entry in the generated `ServiceTasks` object.

## Variable Extraction

### I/O Mappings

Variables are extracted from `operaton:inputOutput`:

```xml
<bpmn:extensionElements>
  <operaton:inputOutput>
    <operaton:inputParameter name="orderId">${orderId}</operaton:inputParameter>
    <operaton:outputParameter name="mailSent">true</operaton:outputParameter>
  </operaton:inputOutput>
</bpmn:extensionElements>
```

The `name` attribute of each parameter becomes a variable.

::: warning Message Start Events
`operaton:inputOutput` is **not supported** on message start events. Use extension properties with `additionalInputVariables` / `additionalOutputVariables` instead (see below).
:::

### Call Activity Mappings

Call activity variables come from `operaton:in` and `operaton:out` mappings, **not** from `operaton:inputOutput`:

```xml
<bpmn:callActivity id="CallActivity_Process" calledElement="sub-process">
  <bpmn:extensionElements>
    <operaton:in source="orderId" target="orderId" />
    <operaton:out source="result" target="processResult" />
  </bpmn:extensionElements>
</bpmn:callActivity>
```

Variables are extracted from the `source` attribute of `operaton:in` and the `target` attribute of `operaton:out`.

### Multi-Instance

Multi-instance variables come from attributes on the `multiInstanceLoopCharacteristics` element:

```xml
<bpmn:multiInstanceLoopCharacteristics
    operaton:collection="${subscribers}"
    operaton:elementVariable="subscriber" />
```

**Extracted variables:** `subscribers` → Input (from `collection` expression), `subscriber` → Output (from `elementVariable`)

### Additional Input / Output Variables (Extension Properties)

For elements where I/O mappings aren't supported (like message start events), declare variables via `operaton:properties`. Two directional property names are recognised:

```xml
<bpmn:extensionElements>
  <operaton:properties>
    <operaton:property name="additionalInputVariables" value="orderId, customerEmail" />
    <operaton:property name="additionalOutputVariables" value="processingResult" />
  </operaton:properties>
</bpmn:extensionElements>
```

Each comma-separated value becomes a variable — values under `additionalInputVariables` land in the element's `Inputs` sub-object, values under `additionalOutputVariables` in `Outputs`. Works on any BPMN element. The legacy undirected `additionalVariables` property is no longer extracted — split your values into the two directional properties above.
