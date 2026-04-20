# Code Templates

Use this file when scaffolding the hexagonal layer structure. Templates are split by approach.

Placeholders:
- `{{packageName}}` — base package (e.g. `com.example.newsletter`)
- `{{enginePackage}}` — `camunda` for CAMUNDA_7, `zeebe` for ZEEBE, `delegate` for OPERATON
- `{{ProcessApiClass}}` — generated API object/class name
- `{{TaskName}}` — PascalCase task name derived from the TaskType constant name
- `{{methodName}}` — camelCase method name derived from the TaskType constant name
- `{{TASK_CONST}}` — the TaskType constant name as-is (e.g. `SEND_CONFIRMATION_MAIL`)
- `{{ProcessName}}` — PascalCase process name derived from `PROCESS_ID`
- `{{ElementName}}` — PascalCase element name from the BPMN element ID for this task
  (used to look up per-element variables in `Variables.{{ElementName}}`)

Generate one file per TaskType for Workers/Delegates, UseCases, and Services.
Generate one file per process for the outbound port and adapter.

## Variable injection

The generated ProcessApi exposes variables **only** as per-element nested objects inside
`Variables` — there are no top-level flat constants. Each nested object is named in PascalCase
from the BPMN element ID:

```kotlin
object Variables {
    object ActivitySendWelcomeMail {        // variables scoped to this service task
        const val SUBSCRIPTION_ID: String = "subscriptionId"
    }
    object ActivitySendConfirmationMail {
        const val SUBSCRIPTION_ID: String = "subscriptionId"
        const val TEST_VARIABLE: String = "testVariable"
    }
}
```

When generating workers/delegates, look up `Variables.{{ElementName}}` for the service task's
element name. Use those constants for `@Variable` parameters or `getVariable()` calls.
If no sub-object exists for that task, leave the method parameterless.

---

## Section A — process-engine-api path (CAMUNDA_7 or ZEEBE)

### `adapter/inbound/{{enginePackage}}/{{TaskName}}Worker.kt`

The `@ProcessEngineWorker` and `@Variable` annotations come from
`dev.bpmcrafters.processengine.worker` (artifact: `dev.bpm-crafters.process-engine-worker`).
The logging import is `io.github.oshai.kotlinlogging.KotlinLogging` (via `io.github.oshai:kotlin-logging-jvm`).

For each variable in `{{ProcessApiClass}}.Variables.{{ElementName}}`, add a corresponding
`@Variable` parameter referencing the per-element constant:
`@Variable(name = {{ProcessApiClass}}.Variables.{{ElementName}}.VARIABLE_NAME) variableName: String`

If no per-element sub-object exists for this task, leave the method parameterless.

```kotlin
package {{packageName}}.adapter.inbound.{{enginePackage}}

import dev.bpmcrafters.processengine.worker.ProcessEngineWorker
import dev.bpmcrafters.processengine.worker.Variable
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import {{packageName}}.api.{{ProcessApiClass}}
import {{packageName}}.application.port.inbound.{{TaskName}}UseCase

private val log = KotlinLogging.logger {}

@Component
class {{TaskName}}Worker(private val useCase: {{TaskName}}UseCase) {

    @ProcessEngineWorker(topic = {{ProcessApiClass}}.TaskTypes.{{TASK_CONST}})
    fun {{methodName}}(
        // @Variable(name = {{ProcessApiClass}}.Variables.{{ElementName}}.VARIABLE_NAME) variableName: String,
    ) {
        log.debug { "Received task {{TASK_CONST}}" }
        useCase.{{methodName}}()
    }
}
```

### `application/port/inbound/{{TaskName}}UseCase.kt`

```kotlin
package {{packageName}}.application.port.inbound

interface {{TaskName}}UseCase {
    fun {{methodName}}()
}
```

### `application/service/{{TaskName}}Service.kt`

```kotlin
package {{packageName}}.application.service

import org.springframework.stereotype.Service
import {{packageName}}.application.port.inbound.{{TaskName}}UseCase
import {{packageName}}.application.port.outbound.{{ProcessName}}Process

@Service
class {{TaskName}}Service(
    private val process: {{ProcessName}}Process,
) : {{TaskName}}UseCase {

    override fun {{methodName}}() {
        TODO("Implement")
    }
}
```

### `application/port/outbound/{{ProcessName}}Process.kt` *(one per process)*

```kotlin
package {{packageName}}.application.port.outbound

interface {{ProcessName}}Process
```

### `adapter/outbound/{{ProcessName}}ProcessAdapter.kt` *(one per process)*

```kotlin
package {{packageName}}.adapter.outbound

import org.springframework.stereotype.Component
import {{packageName}}.application.port.outbound.{{ProcessName}}Process

@Component
class {{ProcessName}}ProcessAdapter : {{ProcessName}}Process
```

---

## Section B — plain JavaDelegate path (OPERATON)

For CAMUNDA_7 plain: replace `org.operaton.bpm` with `org.camunda.bpm` throughout.

### `adapter/inbound/delegate/BaseDelegate.kt` *(once per project)*

```kotlin
package {{packageName}}.adapter.inbound.delegate

import io.github.oshai.kotlinlogging.KotlinLogging
import org.operaton.bpm.engine.delegate.DelegateExecution
import org.operaton.bpm.engine.delegate.JavaDelegate

private val log = KotlinLogging.logger {}

abstract class BaseDelegate : JavaDelegate {

    final override fun execute(execution: DelegateExecution) {
        try {
            executeTask(execution)
        } catch (e: Exception) {
            log.error(e) { "Error in delegate ${this::class.simpleName}" }
            throw e
        }
    }

    abstract fun executeTask(execution: DelegateExecution)
}
```

### `adapter/inbound/delegate/{{TaskName}}Delegate.kt`

For each variable in `{{ProcessApiClass}}.Variables.{{ElementName}}`, add a `getVariable()`
call using the per-element constant as the key — no raw strings.

```kotlin
package {{packageName}}.adapter.inbound.delegate

import io.github.oshai.kotlinlogging.KotlinLogging
import org.operaton.bpm.engine.delegate.DelegateExecution
import org.springframework.stereotype.Component
import {{packageName}}.api.{{ProcessApiClass}}
import {{packageName}}.application.port.inbound.{{TaskName}}UseCase

private val log = KotlinLogging.logger {}

@Component
class {{TaskName}}Delegate(
    private val useCase: {{TaskName}}UseCase,
) : BaseDelegate() {

    override fun executeTask(execution: DelegateExecution) {
        log.debug { "Executing {{TASK_CONST}}" }
        // Example variable read using per-element constant — remove if not needed:
        // val value = execution.getVariable({{ProcessApiClass}}.Variables.{{ElementName}}.VARIABLE_NAME) as String
        useCase.{{methodName}}()
    }
}
```

### `application/port/inbound/{{TaskName}}UseCase.kt`

Same as Section A.

### `application/service/{{TaskName}}Service.kt`

```kotlin
package {{packageName}}.application.service

import org.springframework.stereotype.Service
import {{packageName}}.application.port.inbound.{{TaskName}}UseCase

@Service
class {{TaskName}}Service : {{TaskName}}UseCase {

    override fun {{methodName}}() {
        TODO("Implement")
    }
}
```

*(No process port injected — use `DelegateExecution` in the delegate for engine operations.)*
