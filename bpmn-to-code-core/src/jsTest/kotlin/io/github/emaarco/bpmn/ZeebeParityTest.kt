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
 * Cross-target parity gate: the Kotlin/JS bpmn-moddle parser, fed through the shared synchronous
 * [ProcessApiGeneration] core, must produce byte-for-byte the same generated Kotlin API file as the
 * JVM pipeline does for the same fixture (`shared/bpmn/c8-subscribe-newsletter.bpmn`).
 *
 * The JVM reference (`NewsletterSubscriptionProcessApiKotlin.jvm-reference.txt`) was produced by
 * running `ZeebeModelExtractor` + `ProcessApiGeneration` on the JVM for this exact fixture. (The
 * committed `jvmTest` golden `NewsletterSubscriptionProcessApiKotlin.txt` is a *synthetic* codegen
 * fixture with hand-edited names/impls, so it is intentionally NOT used as the parse-parity oracle.)
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

  /**
   * The node test runner's cwd is `<repoRoot>/build/js/packages/...`. Strip everything from
   * `/build/` onwards to recover the repo root, then read the file via Node `fs`.
   */
  private fun readRepoFile(repoRelativePath: String): String {
    val process = js("process")
    val cwd = process.cwd() as String
    val repoRoot = cwd.substringBefore("/build/")
    val fs = nodeRequire()("fs")
    return fs.readFileSync("$repoRoot/$repoRelativePath", "utf8") as String
  }
}
