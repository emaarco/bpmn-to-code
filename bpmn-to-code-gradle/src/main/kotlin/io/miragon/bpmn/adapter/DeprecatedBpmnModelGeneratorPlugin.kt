package io.miragon.bpmn.adapter

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Backwards-compatible alias for the old `io.github.emaarco.bpmn-to-code-gradle` plugin id.
 * Applies the canonical [BpmnModelGeneratorPlugin] and warns users to switch ids. Removed in 4.0.
 */
@Suppress("unused")
class DeprecatedBpmnModelGeneratorPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.logger.warn(
            "[bpmn-to-code] Plugin id 'io.github.emaarco.bpmn-to-code-gradle' is deprecated and will " +
                "be removed in 4.0 — switch to 'io.miragon.bpmn-to-code-gradle'.",
        )
        project.plugins.apply(BpmnModelGeneratorPlugin::class.java)
    }
}
