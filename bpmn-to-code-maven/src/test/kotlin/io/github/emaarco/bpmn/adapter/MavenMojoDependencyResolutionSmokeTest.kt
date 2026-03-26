package io.github.emaarco.bpmn.adapter

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * Resolves the Maven plugin artifact from mavenLocal (published POM) and executes the Mojo
 * in an isolated classpath. This catches missing transitive dependencies that wouldn't surface
 * when the Mojo is instantiated directly in the test JVM.
 * See: https://github.com/emaarco/bpmn-to-code/issues/159
 */
class MavenMojoDependencyResolutionSmokeTest {

    private val pluginVersion: String = System.getProperty("pluginVersion")

    @Test
    fun `mojo resolves all dependencies from published artifact`(@TempDir projectDir: File) {
        // Copy BPMN file into the temp project
        val resourcesDir = File(projectDir, "src/main/resources").also { it.mkdirs() }
        val bpmnStream = javaClass.classLoader.getResourceAsStream("bpmn/c8-newsletter.bpmn")!!
        File(resourcesDir, "c8-newsletter.bpmn").writeBytes(bpmnStream.readBytes())

        // Write settings.gradle
        File(projectDir, "settings.gradle").writeText("")

        // Write build.gradle that resolves the Maven plugin artifact from mavenLocal
        // and executes the Mojo via a custom task with an isolated classpath
        File(projectDir, "build.gradle").writeText(
            """
            plugins {
                id 'java'
            }

            repositories {
                mavenLocal()
                mavenCentral()
            }

            configurations {
                mojoClasspath
            }

            dependencies {
                mojoClasspath 'io.github.emaarco:bpmn-to-code-maven:$pluginVersion'
            }

            tasks.register('executeMojo') {
                doLast {
                    def urls = configurations.mojoClasspath.files.collect { it.toURI().toURL() }
                    def cl = new URLClassLoader(urls as URL[], ClassLoader.systemClassLoader)

                    def mojoClass = cl.loadClass('io.github.emaarco.bpmn.adapter.BpmnModelMojo')
                    def mojo = mojoClass.getDeclaredConstructor().newInstance()

                    def outputPath = new File(projectDir, 'build/generated').absolutePath
                    ['baseDir': projectDir.absolutePath,
                     'filePattern': 'src/main/resources/*.bpmn',
                     'outputFolderPath': outputPath,
                     'packagePath': 'io.github.emaarco.smoketest',
                     'outputLanguage': 'KOTLIN',
                     'processEngine': 'ZEEBE',
                     'useVersioning': false].each { name, value ->
                        def f = mojoClass.getDeclaredField(name)
                        f.setAccessible(true)
                        f.set(mojo, value)
                    }

                    mojo.execute()
                }
            }
            """.trimIndent()
        )

        // Run WITHOUT withPluginClasspath() — Gradle resolves artifact from mavenLocal
        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("executeMojo")
            .build()

        assertThat(result.task(":executeMojo")?.outcome).isEqualTo(TaskOutcome.SUCCESS)

        val packageDir = File(projectDir, "build/generated/io/github/emaarco/smoketest")
        assertThat(packageDir).isDirectory()
        val generatedFiles = packageDir.listFiles()!!
        assertThat(generatedFiles).isNotEmpty()
        assertThat(generatedFiles).allSatisfy { file ->
            assertThat(file.name).endsWith(".kt")
        }
    }
}
