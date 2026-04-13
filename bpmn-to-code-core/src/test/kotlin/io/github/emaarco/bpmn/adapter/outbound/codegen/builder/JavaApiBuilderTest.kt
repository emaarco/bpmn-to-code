package io.github.emaarco.bpmn.adapter.outbound.codegen.builder

import io.github.emaarco.bpmn.domain.shared.EscalationDefinition
import io.github.emaarco.bpmn.domain.shared.ServiceTaskDefinition.Companion.IMPL_VALUE_KEY
import io.github.emaarco.bpmn.domain.shared.VariableDefinition
import io.github.emaarco.bpmn.domain.testBpmnModelApi
import io.github.emaarco.bpmn.domain.testNewsletterBpmnModel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class JavaApiBuilderTest {

    private val underTest = JavaApiBuilder()

    @Test
    fun `buildApiFile generates correct API file content`() {

        // given: a BPMN model with custom service task implementations
        val modelApi = testBpmnModelApi(
            packagePath = "de.emaarco.example",
            model = testNewsletterBpmnModel(
                flowNodes = buildNewsletterFlowNodes(
                    confirmationMailImpl = "#{newsletterSendConfirmationMail}",
                    welcomeMailImpl = "\${newsletterSendWelcomeMail}",
                    registrationCompletedImpl = "newsletter.registrationCompleted",
                    extraVariables = listOf(VariableDefinition("testVariable")),
                ),
                escalations = listOf(EscalationDefinition("EndEvent_RegistrationNotPossible", "Escalation_RegistrationFailed", "100"))
            )
        )

        // when: we build the API file
        val results = underTest.buildApiFile(modelApi)

        // then: 6 files returned — 1 model + 5 shared types
        assertThat(results).hasSize(6)
        val typeFiles = results.filter { it.packagePath == "de.emaarco.example.types" }
        assertThat(typeFiles.map { it.fileName }).containsExactlyInAnyOrder(
            "BpmnTimer.java", "BpmnError.java", "BpmnEscalation.java", "BpmnFlow.java", "BpmnRelations.java"
        )

        val modelFile = results.first { it.fileName == "${modelApi.fileName()}.java" }
        val expectedFile = File(javaClass.getResource("/api/NewsletterSubscriptionProcessApiJava.txt").toURI())
        assertThat(modelFile.content).isEqualToIgnoringWhitespace(expectedFile.readText())
        assertThat(modelFile.packagePath).isEqualTo("de.emaarco.example")

        // and: each type file matches its expected fixture
        typeFiles.forEach { typeFile ->
            val fixtureName = typeFile.fileName.replace(".java", "Java.txt")
            val fixtureResource = javaClass.getResource("/api/types/$fixtureName")
                ?: error("Missing fixture: /api/types/$fixtureName")
            assertThat(typeFile.content).isEqualToIgnoringWhitespace(File(fixtureResource.toURI()).readText())
        }
    }

    @Test
    fun `maps content of id to valid variable name format`() {

        // given: a model with flow nodes that have slashes in their names
        val defaultModel = testNewsletterBpmnModel()
        val modifiedNodes = defaultModel.flowNodes.map { it.copy(id = it.getName().replace("_", "-")) }
        val modelApi = testBpmnModelApi(
            model = testNewsletterBpmnModel(flowNodes = modifiedNodes),
            packagePath = "de.emaarco.example"
        )

        // when: we build the API file
        val results = underTest.buildApiFile(modelApi)

        // then: expect the generated code contains valid Java
        val modelFile = results.first { it.fileName == "${modelApi.fileName()}.java" }
        assertThat(modelFile.content).isNotEmpty()
    }

}
