import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.mavenPublish) apply false
    alias(libs.plugins.detekt) apply false
}

allprojects {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

subprojects {

    apply(plugin = "io.gitlab.arturbosch.detekt")

    configure<DetektExtension> {
        config.setFrom("$rootDir/config/detekt/detekt.yml")
        buildUponDefaultConfig = true
    }

    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_21
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }

    // Common JaCoCo configuration. Each module declares the jacoco plugin in its own
    // plugins {} block to ensure classDirectories is properly wired to compileKotlin task outputs.
    plugins.withId("jacoco") {
        tasks.withType<Test>().configureEach {
            finalizedBy(tasks.named("jacocoTestReport"))
        }

        tasks.withType<JacocoReport>().configureEach {
            dependsOn(tasks.withType<Test>())
            reports {
                xml.required.set(true)
                html.required.set(true)
            }
        }

        tasks.withType<JacocoCoverageVerification>().configureEach {
            // Explicit dependency on compilation so Gradle's implicit-dependency
            // validation passes when classDirectories is rebuilt from compiled output.
            dependsOn(tasks.withType<AbstractCompile>())
            violationRules {
                rule {
                    element = "CLASS"
                    // Deprecated io.github.emaarco.* compatibility shims (removed in 4.0) mirror the
                    // canonical io.miragon types and are exempt from the coverage bar.
                    excludes = listOf("io.github.emaarco.*")
                    limit {
                        counter = "LINE"
                        value = "COVEREDRATIO"
                        minimum = "0.75".toBigDecimal()
                    }
                }
            }
        }
    }
}
