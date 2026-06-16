// Publishes relocation POMs for the old io.github.emaarco coordinates, redirecting consumers to the
// new io.miragon artifacts. This lives in its own module so it can be published to Maven Central with
// the personal io.github.emaarco credentials, separately from the org-owned io.miragon artifacts.
// Removed in 4.0.
plugins {
    alias(libs.plugins.mavenPublish)
}

group = "io.github.emaarco"
version = property("projectVersion").toString()

fun org.gradle.api.publish.maven.MavenPom.relocateTo(newArtifactId: String) {
    withXml {
        val relocation = asNode()
            .appendNode("distributionManagement")
            .appendNode("relocation")
        relocation.appendNode("groupId", "io.miragon")
        relocation.appendNode("artifactId", newArtifactId)
        relocation.appendNode(
            "message",
            "The bpmn-to-code artifacts moved from the io.github.emaarco group to io.miragon as part " +
                "of the namespace migration. Update your dependency to io.miragon:$newArtifactId.",
        )
    }
}

// Shared POM metadata (required by Maven Central) applied to every publication in this module.
mavenPublishing {
    publishToMavenCentral()
    if (project.hasProperty("signArtifacts")) signAllPublications()
    coordinates("io.github.emaarco", "bpmn-to-code-runtime", version.toString())
    pom {
        name.set("bpmn-to-code (relocated)")
        description.set("Relocation POMs redirecting io.github.emaarco coordinates to io.miragon")
        inceptionYear.set("2026")
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

// One POM-only relocation publication per old coordinate. vanniktech applies the shared pom {}
// metadata and signing above to each of these.
publishing {
    publications {
        register<MavenPublication>("relocateRuntime") {
            groupId = "io.github.emaarco"
            artifactId = "bpmn-to-code-runtime"
            version = project.version.toString()
            pom {
                name.set("bpmn-to-code-runtime (relocated)")
                description.set("Relocated to io.miragon:bpmn-to-code-runtime")
                relocateTo("bpmn-to-code-runtime")
            }
        }
        register<MavenPublication>("relocateMaven") {
            groupId = "io.github.emaarco"
            artifactId = "bpmn-to-code-maven"
            version = project.version.toString()
            pom {
                name.set("bpmn-to-code-maven (relocated)")
                description.set("Relocated to io.miragon:bpmn-to-code-maven")
                relocateTo("bpmn-to-code-maven")
            }
        }
        register<MavenPublication>("relocateTesting") {
            groupId = "io.github.emaarco"
            artifactId = "bpmn-to-code-testing"
            version = project.version.toString()
            pom {
                name.set("bpmn-to-code-testing (relocated)")
                description.set("Relocated to io.miragon:bpmn-to-code-testing")
                relocateTo("bpmn-to-code-testing")
            }
        }
    }
}
