package io.github.emaarco.bpmn.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.architecture.KoArchitectureCreator.assertArchitecture
import com.lemonappdev.konsist.api.architecture.Layer
import com.lemonappdev.konsist.api.declaration.KoInterfaceDeclaration
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class HexagonalArchitectureTest {

    private val rootPackage = "io.github.emaarco.bpmn"

    @Test
    fun `hexagonal architecture layers are respected`() {
        Konsist
            .scopeFromProject()
            .assertArchitecture {
                val domainLayer = Layer("Domain", "$rootPackage.domain..")
                val inPortsLayer = Layer("In-Ports", "$rootPackage.application.port.inbound..")
                val outPortsLayer = Layer("Out-Ports", "$rootPackage.application.port.outbound..")
                val inAdaptersLayer = Layer("In-Adapters", "$rootPackage.adapter.inbound..")
                val outAdaptersLayer = Layer("Out-Adapters", "$rootPackage.adapter.outbound..")
                val applicationLayer = Layer("Application", "$rootPackage.application.service..")

                domainLayer.dependsOnNothing()
                inPortsLayer.dependsOn(domainLayer)
                outPortsLayer.dependsOn(domainLayer)
                outAdaptersLayer.dependsOn(domainLayer, outPortsLayer)
                // Services import concrete out-adapter classes as constructor default values (no DI framework).
                // The constructor parameter TYPES must still be out-port interfaces — enforced separately below.
                applicationLayer.dependsOn(domainLayer, inPortsLayer, outPortsLayer, outAdaptersLayer)
                // In-adapters act as composition root: they instantiate services (which wire their own adapters).
                inAdaptersLayer.dependsOn(domainLayer, inPortsLayer, applicationLayer)
            }
    }

    @Nested
    inner class PortTests {

        @Test
        fun `all ports are interfaces`() {
            Konsist
                .scopeFromPackage("$rootPackage.application.port..")
                .classesAndInterfacesAndObjects(includeNested = false, includeLocal = false)
                .assertTrue { it is KoInterfaceDeclaration }
        }
    }

    @Nested
    inner class ServiceTests {

        @Test
        fun `services are named with Service suffix`() {
            Konsist
                .scopeFromPackage("$rootPackage.application.service..")
                .classesAndInterfacesAndObjects(includeNested = false, includeLocal = false)
                .filter { it.path.contains("/src/main/") }
                .assertTrue { it.hasNameEndingWith("Service") }
        }

        @Test
        fun `each service imports exactly one inbound port`() {
            Konsist
                .scopeFromPackage("$rootPackage.application.service..")
                .files
                .filter { it.path.contains("/src/main/") }
                .assertTrue { file ->
                    val inboundPortImports = file.imports
                        .filter { it.name.contains(".application.port.inbound.") }
                    inboundPortImports.size == 1
                }
        }

        @Test
        fun `service constructor parameters are typed as out-port interfaces, not concrete adapter implementations`() {
            // Services may import concrete out-adapter classes as constructor default values (no DI framework),
            // but the constructor parameter TYPES must use out-port interfaces, not the concrete adapter classes.
            Konsist
                .scopeFromPackage("$rootPackage.application.service..")
                .files
                .filter { it.path.contains("/src/main/") }
                .assertTrue { file ->
                    val outAdapterImportNames = file.imports
                        .filter { it.name.startsWith("$rootPackage.adapter.outbound.") }
                        .map { it.name.substringAfterLast(".") }
                    val constructorParamTypeNames = file.classes()
                        .flatMap { it.primaryConstructor?.parameters ?: emptyList() }
                        .map { it.type.name }
                    outAdapterImportNames.none { it in constructorParamTypeNames }
                }
        }
    }

    @Nested
    inner class InAdapterTests {

        @Test
        fun `in-adapter constructor parameters are typed as inbound port interfaces, not concrete service implementations`() {
            // In-adapters may import concrete service classes as constructor default values (no DI framework),
            // but the constructor parameter TYPES must use inbound port interfaces, not the concrete service classes.
            Konsist
                .scopeFromPackage("$rootPackage.adapter.inbound..")
                .files
                .filter { it.path.contains("/src/main/") }
                .assertTrue { file ->
                    val serviceImportNames = file.imports
                        .filter { it.name.startsWith("$rootPackage.application.service.") }
                        .map { it.name.substringAfterLast(".") }
                    val constructorParamTypeNames = file.classes()
                        .flatMap { it.primaryConstructor?.parameters ?: emptyList() }
                        .map { it.type.name }
                    serviceImportNames.none { it in constructorParamTypeNames }
                }
        }
    }
}
