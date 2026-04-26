import io.gitlab.arturbosch.detekt.extensions.DetektExtension
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
        baseline = file("$projectDir/detekt-baseline.xml")
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
}
