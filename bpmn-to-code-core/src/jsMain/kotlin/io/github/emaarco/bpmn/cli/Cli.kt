package io.github.emaarco.bpmn.cli

import io.github.emaarco.bpmn.adapter.outbound.engine.ZeebeBpmnParser
import io.github.emaarco.bpmn.adapter.outbound.filesystem.BpmnFileLoader
import io.github.emaarco.bpmn.adapter.outbound.filesystem.ProcessApiFileSaver
import io.github.emaarco.bpmn.adapter.outbound.filesystem.ProcessJsonFileSaver
import io.github.emaarco.bpmn.application.ProcessApiGeneration
import io.github.emaarco.bpmn.application.ProcessJsonGeneration
import io.github.emaarco.bpmn.application.ProcessValidation
import io.github.emaarco.bpmn.domain.BpmnModel
import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import io.github.emaarco.bpmn.domain.validation.model.Severity
import io.github.emaarco.bpmn.domain.validation.model.ValidationConfig
import io.github.emaarco.bpmn.domain.validation.model.ValidationViolation
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise

/**
 * Node CLI for the Kotlin/JS build. Lives outside the hexagonal layers (package `cli`, not
 * `adapter.inbound`) so it may use outbound adapters directly without breaking the konsist arch test.
 */

data class CliConfig(
  val command: String,
  val baseDir: String = ".",
  val filePattern: String,
  val outputFolderPath: String,
  val packagePath: String,
  val outputLanguage: OutputLanguage = OutputLanguage.KOTLIN,
  val engine: ProcessEngine = ProcessEngine.ZEEBE,
)

@OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
fun main(args: Array<String>) {
  // Kotlin/JS calls main([]), so read args from process.argv (falling back to args for direct callers).
  val effectiveArgs = if (args.isNotEmpty()) args else nodeArgv()
  val command = effectiveArgs.firstOrNull()
  val flags = parseFlags(effectiveArgs)
  GlobalScope.promise {
    runCommand(command, flags)
  }.catch { error ->
    printErr("Error: ${error.message}")
    setExitCode(1)
    null
  }
}

private fun nodeArgv(): Array<String> {
  val argv = js("(typeof process !== 'undefined' && process.argv) ? process.argv.slice(2) : []")
  return argv.unsafeCast<Array<String>>()
}

suspend fun runCommand(command: String?, flags: Map<String, String>) {
  when (command) {
    "generate" -> {
      val written = runGenerate(toConfig(command, flags))
      println("Generated ${written.size} file(s).")
      written.forEach { println("  $it") }
    }

    "json" -> {
      val written = runJson(toConfig(command, flags))
      println("Wrote ${written.size} JSON file(s).")
      written.forEach { println("  $it") }
    }

    "validate" -> {
      val violations = runValidate(toConfig(command, flags))
      printValidationSummary(violations)
      if (violations.any { it.severity == Severity.ERROR }) setExitCode(1)
    }

    else -> {
      printUsage()
      setExitCode(1)
    }
  }
}

suspend fun runGenerate(config: CliConfig): List<String> {
  val models = parseModels(config)
  val generated = ProcessApiGeneration.generate(
    models = models,
    config = ProcessApiGeneration.Config(
      packagePath = config.packagePath,
      outputLanguage = config.outputLanguage,
      engine = config.engine,
      validationConfig = ValidationConfig(),
    ),
  )
  ProcessApiFileSaver().writeFiles(generated, config.outputFolderPath)
  return generated.map { joinPath(config.outputFolderPath, it.packagePath.replace('.', '/'), it.fileName) }
}

suspend fun runJson(config: CliConfig): List<String> {
  val models = parseModels(config)
  val files = ProcessJsonGeneration.generate(models, config.engine)
  ProcessJsonFileSaver().writeFiles(files, config.outputFolderPath)
  return files.map { joinPath(config.outputFolderPath, it.fileName) }
}

suspend fun runValidate(config: CliConfig): List<ValidationViolation> {
  val models = parseModels(config)
  return ProcessValidation.validate(models, config.engine).violations
}

private suspend fun parseModels(config: CliConfig): List<BpmnModel> {
  val resources = BpmnFileLoader().loadFrom(config.baseDir, config.filePattern)
  if (resources.isEmpty()) {
    printErr("Warning: no BPMN files matched '${config.filePattern}' under '${config.baseDir}'.")
  }
  val parser = ZeebeBpmnParser()
  return resources.map { parser.parse(it.content.decodeToString()) }
}

private fun printValidationSummary(violations: List<ValidationViolation>) {
  if (violations.isEmpty()) {
    println("Validation passed: no violations found.")
    return
  }
  val errors = violations.count { it.severity == Severity.ERROR }
  val warnings = violations.count { it.severity == Severity.WARN }
  println("Validation found $errors error(s), $warnings warning(s):")
  violations.forEach { violation ->
    val location = if (violation.elementId != null) {
      "${violation.processId}/${violation.elementId}"
    } else {
      violation.processId
    }
    println("  [${violation.severity}] $location: ${violation.message} (rule: ${violation.ruleId})")
  }
}

private fun toConfig(command: String, flags: Map<String, String>): CliConfig {
  return CliConfig(
    command = command,
    baseDir = flags["baseDir"] ?: ".",
    filePattern = flags["filePattern"] ?: "*.bpmn",
    outputFolderPath = flags["outputFolderPath"] ?: ".",
    packagePath = flags["packagePath"] ?: "",
    outputLanguage = parseLanguage(flags["outputLanguage"]),
    engine = parseEngine(flags["processEngine"]),
  )
}

private fun parseLanguage(value: String?): OutputLanguage {
  return when (value?.uppercase()) {
    "JAVA" -> OutputLanguage.JAVA
    else -> OutputLanguage.KOTLIN
  }
}

private fun parseEngine(value: String?): ProcessEngine {
  // JS supports only the Zeebe parser; reject other engines rather than silently using Zeebe.
  require(value == null || value.uppercase() == "ZEEBE") {
    "Kotlin/JS supports only --processEngine ZEEBE (got '$value')."
  }
  return ProcessEngine.ZEEBE
}

private fun parseFlags(args: Array<String>): Map<String, String> {
  val flags = mutableMapOf<String, String>()
  var index = 0
  while (index < args.size) {
    val token = args[index]
    if (token.startsWith("--")) {
      val key = token.removePrefix("--")
      val value = args.getOrNull(index + 1)
      if (value != null && !value.startsWith("--")) {
        flags[key] = value
        index += 2
      } else {
        flags[key] = "true"
        index += 1
      }
    } else {
      index += 1
    }
  }
  return flags
}

private fun printUsage() {
  println(
    """
    |bpmn-to-code (Node CLI)
    |
    |Usage: bpmn-to-code <command> [options]
    |
    |Commands:
    |  generate   Generate type-safe process API files from BPMN models
    |  json       Generate one JSON descriptor file per BPMN model
    |  validate   Validate BPMN models and report violations
    |
    |Options:
    |  --filePattern <pattern>     BPMN file name/suffix to match (default: *.bpmn)
    |  --baseDir <dir>             Base directory to scan (default: .)
    |  --outputFolderPath <dir>    Output directory for generated files
    |  --packagePath <pkg>         Package path for generated API code
    |  --outputLanguage <lang>     KOTLIN or JAVA (default: KOTLIN)
    |  --processEngine <engine>    ZEEBE (JS supports Zeebe only)
    """.trimMargin()
  )
}

private fun joinPath(vararg parts: String): String {
  return parts.filter { it.isNotEmpty() }.joinToString("/")
}

private fun setExitCode(code: Int) {
  js("process.exitCode = code")
}

private fun printErr(message: String) {
  js("console.error(message)")
}
