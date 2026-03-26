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

The attribute value becomes the entry in the generated `TaskTypes` object.

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
`camunda:inputOutput` is **not supported** on message start events in Camunda 7. Use extension properties with `additionalVariables` instead (see below).
:::

### Additional Variables (Extension Properties)

For elements where I/O mappings aren't supported (like message start events), you can declare variables via `camunda:properties`:

```xml
<bpmn:extensionElements>
  <camunda:properties>
    <camunda:property name="additionalVariables" value="orderId, customerEmail, amount" />
  </camunda:properties>
</bpmn:extensionElements>
```

The comma-separated list is parsed and each value becomes a variable. This works on any BPMN element.

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

**Extracted variables:** `subscribers` (from collection expression), `subscriber` (element variable)
