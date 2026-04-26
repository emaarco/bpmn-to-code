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
    testImplementation(testFixtures(project(":bpmn-to-code-core")))
    testRuntimeOnly(libs.junitPlatformLauncher)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

mavenPlugin {
    dependencies = deps
}

// Configure the jar task to include output from the core module.
tasks.jar {
    from(project(":bpmn-to-code-core").sourceSets.main.get().output)
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