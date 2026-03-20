package io.github.emaarco.bpmn.mcp.tools

object GenerateProcessApiToolSchema {

    const val TOOL_NAME = "generate_process_api"

    const val PARAM_BPMN_XML = "bpmnXml"
    const val PARAM_PROCESS_NAME = "processName"
    const val PARAM_OUTPUT_LANGUAGE = "outputLanguage"
    const val PARAM_PROCESS_ENGINE = "processEngine"
    const val PARAM_PACKAGE_PATH = "packagePath"

    const val ASK_USER_HINT = "Ask the user if not already clear from context. Do not assume a default."

    const val DESC_BPMN_XML = "Raw BPMN XML content"
    const val DESC_PROCESS_NAME = "Process identifier used for naming generated classes (e.g. 'newsletter-subscription')"
    const val DESC_OUTPUT_LANGUAGE = "Output language: KOTLIN or JAVA. $ASK_USER_HINT"
    const val DESC_PROCESS_ENGINE = "Target process engine: ZEEBE, CAMUNDA_7, or OPERATON. $ASK_USER_HINT"
    const val DESC_PACKAGE_PATH = "Target package for generated code (e.g. 'com.example.process'). $ASK_USER_HINT"

    val TOOL_DESCRIPTION = """
        Generate type-safe process API code from BPMN XML. \
        Returns generated source files for interacting with BPMN process tasks and variables. \
        IMPORTANT: Before calling this tool, you MUST confirm the following with the user if not already \
        known from context: (1) outputLanguage — KOTLIN or JAVA, (2) processEngine — ZEEBE, CAMUNDA_7, \
        or OPERATON, (3) packagePath — the target package for generated code. \
        Do not assume defaults for these parameters. Ask the user to specify them.
    """.trimIndent()
}
