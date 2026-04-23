package io.github.emaarco.bpmn.adapter.outbound.codegen.builder

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import io.github.emaarco.bpmn.adapter.outbound.codegen.CodeGenerationAdapter
import io.github.emaarco.bpmn.domain.GeneratedApiFile
import io.github.emaarco.bpmn.domain.shared.OutputLanguage

/**
 * Generates shared BPMN types as standalone Kotlin files in the `{packagePath}.types` sub-package:
 * enum `BpmnEngine`, data classes (BpmnTimer, BpmnError, BpmnEscalation, BpmnFlow, BpmnRelations),
 * and `@JvmInline` value classes wrapping leaf identifiers (ProcessId, ElementId, MessageName,
 * VariableName, SignalName).
 * These are identical for every process in the same package — generated once and deduplicated upstream.
 */
class KotlinSharedTypesBuilder : CodeGenerationAdapter.AbstractSharedTypesBuilder() {

    override fun buildTypeFiles(packagePath: String, language: OutputLanguage): List<GeneratedApiFile> {
        val typesPackage = "$packagePath.types"
        return listOf(
            buildBpmnEngineFile(typesPackage, language),
            buildBpmnTimerFile(typesPackage, language),
            buildBpmnErrorFile(typesPackage, language),
            buildBpmnEscalationFile(typesPackage, language),
            buildBpmnFlowFile(typesPackage, language),
            buildBpmnRelationsFile(typesPackage, language),
            buildValueClassFile(typesPackage, "ProcessId", language),
            buildValueClassFile(typesPackage, "ElementId", language),
            buildValueClassFile(typesPackage, "MessageName", language),
            buildValueClassFile(typesPackage, "VariableName", language),
            buildValueClassFile(typesPackage, "SignalName", language),
        )
    }

    private fun buildBpmnEngineFile(typesPackage: String, language: OutputLanguage): GeneratedApiFile {
        val typeSpec = TypeSpec.enumBuilder("BpmnEngine")
            .addEnumConstant("ZEEBE")
            .addEnumConstant("CAMUNDA_7")
            .addEnumConstant("OPERATON")
            .build()
        return buildTypeFile(typesPackage, "BpmnEngine", typeSpec, language)
    }

    private fun buildValueClassFile(typesPackage: String, className: String, language: OutputLanguage): GeneratedApiFile {
        val jvmInline = ClassName("kotlin.jvm", "JvmInline")
        val typeSpec = TypeSpec.classBuilder(className)
            .addModifiers(KModifier.VALUE)
            .addAnnotation(AnnotationSpec.builder(jvmInline).build())
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("value", STRING)
                    .build()
            )
            .addProperty(PropertySpec.builder("value", STRING).initializer("value").build())
            .build()
        return buildTypeFile(typesPackage, className, typeSpec, language)
    }

    private fun buildBpmnTimerFile(typesPackage: String, language: OutputLanguage): GeneratedApiFile {
        val typeSpec = TypeSpec.classBuilder("BpmnTimer")
            .addModifiers(KModifier.DATA)
            .addKdoc("A BPMN timer definition.\n`type` is one of `Duration`, `Date`, or `Cycle`.")
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("type", STRING)
                    .addParameter("timerValue", STRING)
                    .build()
            )
            .addProperty(PropertySpec.builder("type", STRING).initializer("type").build())
            .addProperty(PropertySpec.builder("timerValue", STRING).initializer("timerValue").build())
            .build()
        return buildTypeFile(typesPackage, "BpmnTimer", typeSpec, language)
    }

    private fun buildBpmnErrorFile(typesPackage: String, language: OutputLanguage): GeneratedApiFile {
        val typeSpec = TypeSpec.classBuilder("BpmnError")
            .addModifiers(KModifier.DATA)
            .addKdoc("A BPMN error definition referenced by error catch events and error end events.")
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("name", STRING)
                    .addParameter("code", STRING)
                    .build()
            )
            .addProperty(PropertySpec.builder("name", STRING).initializer("name").build())
            .addProperty(PropertySpec.builder("code", STRING).initializer("code").build())
            .build()
        return buildTypeFile(typesPackage, "BpmnError", typeSpec, language)
    }

    private fun buildBpmnEscalationFile(typesPackage: String, language: OutputLanguage): GeneratedApiFile {
        val typeSpec = TypeSpec.classBuilder("BpmnEscalation")
            .addModifiers(KModifier.DATA)
            .addKdoc("A BPMN escalation definition referenced by escalation catch events and escalation end events.")
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("name", STRING)
                    .addParameter("code", STRING)
                    .build()
            )
            .addProperty(PropertySpec.builder("name", STRING).initializer("name").build())
            .addProperty(PropertySpec.builder("code", STRING).initializer("code").build())
            .build()
        return buildTypeFile(typesPackage, "BpmnEscalation", typeSpec, language)
    }

    private fun buildBpmnFlowFile(typesPackage: String, language: OutputLanguage): GeneratedApiFile {
        val nullableString = STRING.copy(nullable = true)
        val typeSpec = TypeSpec.classBuilder("BpmnFlow")
            .addModifiers(KModifier.DATA)
            .addKdoc(
                "A BPMN sequence flow connecting two elements in the process graph.\n\n" +
                    "@param name Label of the flow as shown in the BPMN diagram; null when unlabelled.\n" +
                    "@param condition Condition expression evaluated at runtime; null for unconditional flows.\n" +
                    "@param isDefault True when this is the default flow of an exclusive or inclusive gateway.\n"
            )
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("id", STRING)
                    .addParameter(ParameterSpec.builder("name", nullableString).defaultValue("null").build())
                    .addParameter("sourceRef", STRING)
                    .addParameter("targetRef", STRING)
                    .addParameter(ParameterSpec.builder("condition", nullableString).defaultValue("null").build())
                    .addParameter(ParameterSpec.builder("isDefault", BOOLEAN).defaultValue("false").build())
                    .build()
            )
            .addProperty(PropertySpec.builder("id", STRING).initializer("id").build())
            .addProperty(PropertySpec.builder("name", nullableString).initializer("name").build())
            .addProperty(PropertySpec.builder("sourceRef", STRING).initializer("sourceRef").build())
            .addProperty(PropertySpec.builder("targetRef", STRING).initializer("targetRef").build())
            .addProperty(PropertySpec.builder("condition", nullableString).initializer("condition").build())
            .addProperty(PropertySpec.builder("isDefault", BOOLEAN).initializer("isDefault").build())
            .build()
        return buildTypeFile(typesPackage, "BpmnFlow", typeSpec, language)
    }

    private fun buildBpmnRelationsFile(typesPackage: String, language: OutputLanguage): GeneratedApiFile {
        val listType = LIST.parameterizedBy(STRING)
        val nullableString = STRING.copy(nullable = true)
        val typeSpec = TypeSpec.classBuilder("BpmnRelations")
            .addModifiers(KModifier.DATA)
            .addKdoc(
                "Per-element graph metadata capturing an element's neighbours in the process flow.\n\n" +
                    "@param previousElements Element ids of preceding flow nodes — not sequence-flow ids (see `Flows`).\n" +
                    "@param followingElements Element ids of following flow nodes — not sequence-flow ids (see `Flows`).\n" +
                    "@param parentId Id of the containing subprocess, or null if top-level.\n" +
                    "@param attachedToRef For boundary events: id of the host element; null otherwise.\n"
            )
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(ParameterSpec.builder("name", nullableString).defaultValue("null").build())
                    .addParameter("previousElements", listType)
                    .addParameter("followingElements", listType)
                    .addParameter("parentId", nullableString)
                    .addParameter("attachedToRef", nullableString)
                    .addParameter("attachedElements", listType)
                    .build()
            )
            .addProperty(PropertySpec.builder("name", nullableString).initializer("name").build())
            .addProperty(PropertySpec.builder("previousElements", listType).initializer("previousElements").build())
            .addProperty(PropertySpec.builder("followingElements", listType).initializer("followingElements").build())
            .addProperty(PropertySpec.builder("parentId", nullableString).initializer("parentId").build())
            .addProperty(PropertySpec.builder("attachedToRef", nullableString).initializer("attachedToRef").build())
            .addProperty(PropertySpec.builder("attachedElements", listType).initializer("attachedElements").build())
            .build()
        return buildTypeFile(typesPackage, "BpmnRelations", typeSpec, language)
    }

    private fun buildTypeFile(typesPackage: String, className: String, typeSpec: TypeSpec, language: OutputLanguage): GeneratedApiFile {
        val fileSpec = FileSpec.builder(typesPackage, className)
            .addFileComment(autoGenComment)
            .addType(typeSpec)
            .build()
        val content = buildString { fileSpec.writeTo(this) }.replace("public ", "")
        return GeneratedApiFile(
            fileName = "$className.kt",
            packagePath = typesPackage,
            content = content,
            language = language,
        )
    }
}
