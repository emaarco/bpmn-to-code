# 🔷 Zeebe (Camunda 8)

Engine-specific behavior when using `processEngine = ZEEBE`.

## Service Task Detection

Service tasks are detected via the `zeebe:taskDefinition` extension element:

```xml
<bpmn:serviceTask id="Activity_SendMail" name="Send Mail">
  <bpmn:extensionElements>
    <zeebe:taskDefinition type="mail.send" />
  </bpmn:extensionElements>
</bpmn:serviceTask>
```

The `type` attribute becomes the value in the generated `ServiceTasks` object.

## Variable Extraction

### I/O Mappings

Variables are extracted from `zeebe:ioMapping` with `zeebe:input` and `zeebe:output` children:

```xml
<bpmn:extensionElements>
  <zeebe:ioMapping>
    <zeebe:input source="=orderId" target="orderId" />
    <zeebe:output source="=result" target="mailSent" />
  </zeebe:ioMapping>
</bpmn:extensionElements>
```

The `target` attribute of each mapping becomes a variable in the generated `Variables` object.

### Multi-Instance

Multi-instance variables come from `zeebe:loopCharacteristics`:

```xml
<bpmn:multiInstanceLoopCharacteristics>
  <bpmn:extensionElements>
    <zeebe:loopCharacteristics
      inputCollection="=subscribers"
      inputElement="subscriber"
      outputCollection="=results"
      outputElement="result" />
  </bpmn:extensionElements>
</bpmn:multiInstanceLoopCharacteristics>
```

All four attributes are extracted as variables. The `=` expression prefix is automatically stripped.

**Extracted variables:** `subscribers`, `subscriber`, `results`, `result`

## Call Activities

Call activities use `zeebe:calledElement` (not the standard `calledElement` attribute):

```xml
<bpmn:callActivity id="CallActivity_Abort" name="Abort Registration">
  <bpmn:extensionElements>
    <zeebe:calledElement processId="abort-registration" />
  </bpmn:extensionElements>
</bpmn:callActivity>
```

The `processId` from the extension element is extracted into the `CallActivities` object.
