# Compatibility Matrix

Use this file during Step 1 to derive the correct Kotlin, Spring Boot, and Java versions
for the chosen engine. Always prefer fetching fresh data (Initializr metadata) over hardcoded
values; fall back to the defaults below when network is unavailable.

---

## Fetching the latest Spring Boot GA version

```bash
curl -s "https://start.spring.io/actuator/info" \
  | grep -o '"default":"[^"]*"' | head -1 | grep -o '[0-9][^"]*'
```

This returns the current default Spring Boot version (e.g. `3.5.0`). Use that value as
`bootVersion` unless the engine constraints below require a different minimum.

> **Important:** `start.spring.io` rejects `bootVersion` below `3.5.0` with HTTP 400.
> If the fetch fails, always fall back to `3.5.0` — never use an older hardcoded value.

---

## Fetching the compatible Kotlin version from the BOM

For Gradle+Kotlin projects, the Kotlin version that compiled the engine BOM must be ≥ the
version in the project. Detect the required Kotlin version from the BOM's POM:

```bash
curl -s "https://repo1.maven.org/maven2/{{bom-group-path}}/{{bom-artifact}}/{{bom-version}}/{{bom-artifact}}-{{bom-version}}.pom" \
  | grep -o '<kotlin.version>[^<]*' | head -1 | sed 's/<kotlin.version>//'
```

If the POM does not declare `kotlin.version`, inspect any jar in the BOM to read the metadata
version (the `binary version` in the Kotlin error message) — then round up to that Kotlin release.

As a **known-good fallback** (recorded 2026-04-19):

| Engine BOM | BOM version | Kotlin metadata version | Min Kotlin plugin version |
|---|---|---|---|
| process-engine-adapter-camunda-platform-c8-bom | 2026.02.1 | 2.2.0 | **2.2.0** |
| process-engine-adapter-camunda-platform-c7-bom | 2026.02.1 | 2.2.0 | **2.2.0** |
| operaton-bpm-spring-boot-starter-webapp | 2.0.0 | 1.9.x | 1.9.25 (Initializr default is fine) |

> If you fetch a newer BOM version, re-derive the required Kotlin version — do not assume the
> fallback still applies.

---

## Detecting the host Java version

```bash
java -version 2>&1 | head -1
```

Extract the major version (e.g. `21`). Use that value for the Gradle/Maven toolchain config.
If no Java is found, use `AskUserQuestion` to let the user specify the version.
If the version is below the engine's minimum, warn and ask.

Engine minimums:

| Engine | Min Java |
|---|---|
| CAMUNDA_7 (embedded) | 17 |
| ZEEBE | 21 |
| OPERATON | 17 |

> Spring Boot 3.x requires Java ≥ 17 in all cases.

---

## Spring Boot + Engine compatibility

| Engine | Min Spring Boot | Notes |
|---|---|---|
| CAMUNDA_7 (process-engine-adapter-camunda-platform-c7-bom 2026.02.1) | 3.2 | 3.5.x tested OK |
| ZEEBE (process-engine-adapter-camunda-platform-c8-bom 2026.02.1) | 3.2 | 3.5.x tested OK |
| OPERATON (operaton-bpm-spring-boot-starter-webapp 2.0.0) | 3.3 | 3.5.x tested OK |

---

## Variables API structure

Since bpmn-to-code `#265`, the `Variables` object contains **only** per-element nested objects.
There are no top-level flat constants. Reference variables via:
`{{ProcessApiClass}}.Variables.{{ElementName}}.VARIABLE_NAME`

If a TaskType's service task element has no corresponding `Variables.{{ElementName}}` sub-object,
leave the worker/delegate method parameterless.

---

## Spring Boot integration test caveat (ZEEBE)

ZEEBE requires a running Zeebe broker for the Spring context to load. The generated
`@SpringBootTest contextLoads()` test will therefore fail without Docker. When verifying
compilation for ZEEBE:

- **Gradle:** `./gradlew build -x test`
- **Maven:** `mvn verify -DskipTests`

Document this in the project README and in the Step 9 summary.
