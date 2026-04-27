# 📡 JSON Export

bpmn-to-code generates a structured JSON file alongside the Kotlin/Java API. It contains the full process structure — every flow node, sequence flow, message, signal, error, and compensation — in a format that both AI agents and developers can read directly.

## What Gets Generated

For each process, a `.json` file is produced with the same base name as your BPMN file. The format is stable and deterministic — same BPMN in, same JSON out, on every run.

## Example

For the newsletter subscription process:

```json
{
    "processId": "newsletterSubscription",
    "flowNodes": [
        {
            "id": "StartEvent_SubmitRegistrationForm",
            "displayName": "Submit newsletter form",
            "elementType": "START_EVENT",
            "outgoing": ["serviceTask_incrementSubscriptionCounter"],
            "variables": ["subscriptionId"]
        },
        {
            "id": "serviceTask_incrementSubscriptionCounter",
            "displayName": "Increment subscription counter",
            "elementType": "SERVICE_TASK",
            "attachedElements": ["CompensationEvent_OnSubscriptionCounter"],
            "incoming": ["StartEvent_SubmitRegistrationForm"],
            "outgoing": ["SubProcess_Confirmation"],
            "properties": {
                "type": "ServiceTask",
                "implementationValue": "counterClass"
            }
        },
        {
            "id": "SubProcess_Confirmation",
            "displayName": "Subscription Confirmation",
            "elementType": "SUB_PROCESS",
            "attachedElements": ["ErrorEvent_InvalidMail", "Timer_After3Days"],
            "incoming": ["serviceTask_incrementSubscriptionCounter"],
            "outgoing": ["Activity_SendWelcomeMail"]
        },
        {
            "id": "Activity_SendWelcomeMail",
            "displayName": "Send Welcome-Mail",
            "elementType": "SERVICE_TASK",
            "incoming": ["SubProcess_Confirmation"],
            "outgoing": ["EndEvent_RegistrationCompleted"],
            "variables": ["subscriptionId"],
            "properties": {
                "type": "ServiceTask",
                "implementationValue": "newsletter.sendWelcomeMail"
            },
            "engineSpecificProperties": {
                "asyncBefore": true,
                "asyncAfter": true,
                "exclusive": false
            }
        }
    ],
    "messages": [
        { "id": "StartEvent_SubmitRegistrationForm", "name": "Message_FormSubmitted" }
    ],
    "signals": [
        { "id": "EndEvent_RegistrationNotPossible", "name": "Signal_RegistrationNotPossible" }
    ],
    "errors": [
        { "id": "ErrorEvent_InvalidMail", "name": "Error_InvalidMail", "code": "500" }
    ],
    "compensations": [
        { "id": "CompensationEndEvent_RegistrationAborted", "activityRef": "CompensationEndEvent_RegistrationAborted" }
    ],
    "sequenceFlows": [
        {
            "id": "Flow_09cuvzp",
            "sourceRef": "SubProcess_Confirmation",
            "targetRef": "Activity_SendWelcomeMail",
            "isDefault": false
        }
    ]
}
```

## Node Ordering

Flow nodes are sorted in **process-flow order** — a depth-first traversal from the start event(s). This means the JSON reads top-to-bottom in execution order, making it easy to understand the process narrative without tracing sequence flows manually.

Boundary events appear immediately after the element they are attached to.

## Flow Node Fields

| Field | Always present | Description |
|-------|---------------|-------------|
| `id` | yes | The BPMN element ID |
| `displayName` | yes | The element's label or name from the modeler |
| `elementType` | yes | One of: `START_EVENT`, `END_EVENT`, `SERVICE_TASK`, `USER_TASK`, `GATEWAY`, `SUB_PROCESS`, `CALL_ACTIVITY`, `BOUNDARY_EVENT`, `TASK`, … |
| `incoming` | no | IDs of incoming elements (sequence flow sources or parent subprocess) |
| `outgoing` | no | IDs of outgoing elements |
| `parentId` | no | Parent subprocess ID, if nested |
| `attachedToRef` | no | Element this boundary event is attached to |
| `attachedElements` | no | Boundary events attached to this element |
| `variables` | no | Variable names extracted from I/O mappings |
| `properties` | no | Engine-specific implementation details (task type, calledElement, timer config) |
| `engineSpecificProperties` | no | Camunda 7 / Operaton async markers (`asyncBefore`, `asyncAfter`, `exclusive`) |

## Configuring the JSON Task

### Gradle

```kotlin
tasks.named("generateBpmnModelJson", GenerateBpmnJsonTask::class) {
    baseDir = projectDir.toString()
    filePattern = "src/main/resources/**/*.bpmn"
    outputFolderPath = "$projectDir/src/main/resources/bpmn-json"
    processEngine = ProcessEngine.ZEEBE
}
```

Run:

```bash
./gradlew generateBpmnModelJson
```

### Maven

```xml
<plugin>
    <groupId>io.github.emaarco</groupId>
    <artifactId>bpmn-to-code-maven</artifactId>
    <version>2.0.1</version>
    <executions>
        <execution>
            <id>generate-bpmn-json</id>
            <goals><goal>generate-bpmn-json</goal></goals>
            <configuration>
                <baseDir>${project.basedir}</baseDir>
                <filePattern>src/main/resources/**/*.bpmn</filePattern>
                <outputFolderPath>${project.basedir}/src/main/resources/bpmn-json</outputFolderPath>
                <processEngine>ZEEBE</processEngine>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `baseDir` | `String` | required | Base directory for resolving relative paths |
| `filePattern` | `String` | required | Glob pattern to locate BPMN files |
| `outputFolderPath` | `String` | required | Directory where JSON files are written |
| `processEngine` | `ProcessEngine` | required | `ZEEBE`, `CAMUNDA_7`, or `OPERATON` |

## Using the JSON with AI

The JSON is designed for use with AI coding assistants. Paste it into your assistant's context (or stream it via the [MCP Server](/mcp/)) and ask questions about your process:

- "Which service tasks in this process are async?"
- "What variables does the `Activity_SendConfirmationMail` task receive?"
- "List all boundary events and what they are attached to."

Because the JSON is produced by deterministic rules — not generated by an LLM — the assistant gets reliable process context with no hallucinated element IDs.

::: tip Keep the JSON in version control
Commit the JSON alongside your BPMN files. This makes process structure changes visible in pull request diffs, without requiring reviewers to open Camunda Modeler.
:::
