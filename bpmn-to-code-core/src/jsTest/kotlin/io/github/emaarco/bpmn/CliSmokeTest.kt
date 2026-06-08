package io.github.emaarco.bpmn

import io.github.emaarco.bpmn.adapter.outbound.filesystem.nodeRequire
import io.github.emaarco.bpmn.cli.CliConfig
import io.github.emaarco.bpmn.cli.runGenerate
import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * End-to-end smoke test for the Node CLI's `generate` orchestration: it points [runGenerate] at the
 * shared `c8-subscribe-newsletter.bpmn` fixture, writes into a throwaway temp dir, and asserts the
 * expected Kotlin API file was produced with real content.
 *
 * Like [ZeebeParityTest] it recovers the repo root from the Node runner's cwd (`<repoRoot>/build/...`).
 */
class CliSmokeTest {

  @Suppress("FunctionNaming")
  @Test
  fun `generate writes the newsletter process api file`() = runTest {
    val fs = nodeRequire()("fs")
    val os = nodeRequire()("os")
    val path = nodeRequire()("path")
    val repoRoot = (js("process").cwd() as String).substringBefore("/build/")
    val fixtureDir = "$repoRoot/shared/bpmn"
    val outputDir = fs.mkdtempSync("${os.tmpdir() as String}/bpmn-to-code-cli-") as String

    val written = runGenerate(
      CliConfig(
        command = "generate",
        baseDir = fixtureDir,
        filePattern = "c8-subscribe-newsletter.bpmn",
        outputFolderPath = outputDir,
        packagePath = "de.emaarco.example",
        outputLanguage = OutputLanguage.KOTLIN,
      ),
    )

    assertEquals(1, written.size, "expected exactly one generated file")
    val expectedFile = path.join(
      outputDir,
      "de",
      "emaarco",
      "example",
      "NewsletterSubscriptionProcessApi.kt",
    ) as String
    assertTrue(fs.existsSync(expectedFile) as Boolean, "expected generated file to exist: $expectedFile")
    val content = fs.readFileSync(expectedFile, "utf8") as String
    assertTrue(content.isNotEmpty(), "generated file should not be empty")
    assertTrue(
      content.contains("object NewsletterSubscriptionProcessApi"),
      "generated file should declare the process api object",
    )

    fs.rmSync(outputDir, js("({ recursive: true, force: true })"))
  }
}
