package io.github.emaarco.bpmn

import io.github.emaarco.bpmn.adapter.outbound.engine.ZeebeBpmnParser
import io.github.emaarco.bpmn.adapter.outbound.filesystem.nodeRequire
import io.github.emaarco.bpmn.application.ProcessApiGeneration
import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import io.github.emaarco.bpmn.domain.shared.ProcessEngine
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Cross-target parity gate: the Kotlin/JS parser + [ProcessApiGeneration] must produce byte-for-byte
 * the JVM reference. (The committed jvmTest golden is a synthetic fixture, so a separate JVM-parse
 * reference is used as the oracle.)
 */
class ZeebeParityTest {

  @Test
  fun jsParseAndGenerateMatchesJvmReference() = runTest {
    val fixtureXml = readRepoFile("shared/bpmn/c8-subscribe-newsletter.bpmn")
    val jvmReference = readRepoFile(
      "bpmn-to-code-core/src/jsTest/resources/api/NewsletterSubscriptionProcessApiKotlin.jvm-reference.txt"
    )

    val model = ZeebeBpmnParser().parse(fixtureXml)
    val generated = ProcessApiGeneration.generate(
      models = listOf(model),
      config = ProcessApiGeneration.Config(
        packagePath = "de.emaarco.example",
        outputLanguage = OutputLanguage.KOTLIN,
        engine = ProcessEngine.ZEEBE,
      ),
    )

    assertEquals(1, generated.size, "expected exactly one generated file")
    assertEquals(jvmReference, generated.single().content)
  }

  // Recover the repo root from the node runner's cwd (<repoRoot>/build/js/packages/...).
  private fun readRepoFile(repoRelativePath: String): String {
    val process = js("process")
    val cwd = process.cwd() as String
    val repoRoot = cwd.substringBefore("/build/")
    val fs = nodeRequire()("fs")
    return fs.readFileSync("$repoRoot/$repoRelativePath", "utf8") as String
  }
}
