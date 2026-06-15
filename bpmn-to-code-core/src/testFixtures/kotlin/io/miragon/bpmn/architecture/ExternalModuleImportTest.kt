package io.miragon.bpmn.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.jupiter.api.Test

/**
 * Enforces that plugin wrapper modules only import from:
 * - `io.miragon.bpmn.domain.*` — domain objects
 * - `io.miragon.bpmn.adapter.inbound.*` — inbound adapters (the public API surface)
 *
 * Extend this class in each plugin module (Gradle, Maven, Web) and pass the module
 * directory name as [modulePath]. The test will then be scoped to that module's source files.
 *
 * Note: `bpmn-to-code-testing` is excluded — it is a BPMN parsing utility library that
 * legitimately uses the BPMN extraction out-adapter directly to implement its validation logic.
 */
abstract class ExternalModuleImportTest(private val modulePath: String) {

    private val forbiddenImportPrefixes = listOf(
        "io.miragon.bpmn.application.",      // services and ports
        "io.miragon.bpmn.adapter.outbound.", // out-adapters
    )

    @Test
    fun `module only imports domain objects or inbound adapters from core`() {
        Konsist
            .scopeFromProject()
            .files
            .filter { file -> file.path.contains(modulePath) }
            .assertTrue { file ->
                file.imports.none { import ->
                    forbiddenImportPrefixes.any { import.name.startsWith(it) }
                }
            }
    }
}
