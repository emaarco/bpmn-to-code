import org.gradlex.maven.plugin.development.task.GenerateMavenPluginDescriptorTask

plugins {
    `maven-publish`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.mavenPluginDevelopment)
    alias(libs.plugins.mavenPublish)
    jacoco
}

group = "io.github.emaarco"
version = property("projectVersion").toString()

val deps: Configuration by configurations.creating

repositories {
    mavenCentral()
}

sourceSets {
    test {
        resources.srcDir(rootProject.file("shared"))
    }
}

dependencies {
    api(kotlin("stdlib"))
    api(libs.bpmnmodel)
    api(libs.bundles.codegen)
    api(libs.kotlinxSerializationJson)
    api(libs.slf4jApi)
    api(libs.kotlinLogging)
    compileOnly(project(":bpmn-to-code-core"))
    implementation(libs.mavenPluginApi)
    implementation(libs.mavenPluginAnnotations)
    testImplementation(project(":bpmn-to-code-core"))
    testImplementation(libs.bundles.testing)
    testImplementation(project(":bpmn-to-code-test-fixtures"))
    testRuntimeOnly(libs.junitPlatformLauncher)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

mavenPlugin {
    dependencies = deps
}

// The maven-plugin-development plugin detects core (on the compile classpath) as an
// "upstream project" and tries to read its conventional `main` source set. The KMP core
// has no such source set, and it contains no Mojos, so drop it from the upstream projects.
tasks.withType<GenerateMavenPluginDescriptorTask>().configureEach {
    upstreamProjects.set(emptyList())
}

// Shade the core's JVM compilation output (KMP `jvmJar` contents) into the plugin jar.
val coreJvmJar = project(":bpmn-to-code-core").tasks.named("jvmJar")

tasks.jar {
    dependsOn(coreJvmJar)
    from({ zipTree(coreJvmJar.get().outputs.files.singleFile) }) {
        exclude("META-INF/MANIFEST.MF")
    }
}


mavenPublishing {

    publishToMavenCentral()
    if (project.hasProperty("signArtifacts")) signAllPublications()
    coordinates("io.github.emaarco", "bpmn-to-code-maven", version.toString())

    // Configure the POM details.
    pom {
        name.set("bpmn-to-code-maven")
        description.set("Maven plugin that bridges gaps between BPMN and code - fostering the creation of clean process-automation solutions")
        inceptionYear.set("2025")
        url.set("https://github.com/emaarco/bpmn-to-code")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("https://opensource.org/licenses/MIT")
            }
        }
        developers {
            developer {
                id.set("emaarco")
                name.set("Marco Schaeck")
                url.set("https://github.com/emaarco")
            }
        }
        scm {
            url.set("https://github.com/emaarco/bpmn-to-code")
            connection.set("scm:git:git://github.com/emaarco/bpmn-to-code.git")
            developerConnection.set("scm:git:ssh://git@github.com/emaarco/bpmn-to-code.git")
        }
    }
}