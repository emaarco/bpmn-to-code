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

private const val VARIABLE_NAME_KDOC: String =
    "Name of a process variable declared by a BPMN element.\n\n" +
        "Direction is encoded in the subtype so consumer APIs can enforce it at compile time\n" +
        "(e.g. `fun setOutput(v: VariableName.Output, value: Any)`).\n" +
        "`toString()` returns the underlying string, so `.value` is optional in string contexts."

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
            buildVariableNameFile(typesPackage, language),
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
        val typeSpec = buildValueClassSpec(className)
        return buildTypeFile(typesPackage, className, typeSpec, language)
    }

    private fun buildValueClassSpec(className: String): TypeSpec {
        val jvmInline = ClassName("kotlin.jvm", "JvmInline")
        return TypeSpec.classBuilder(className)
            .addModifiers(KModifier.VALUE)
            .addAnnotation(AnnotationSpec.builder(jvmInline).build())
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("value", STRING)
                    .build()
            )
            .addProperty(PropertySpec.builder("value", STRING).initializer("value").build())
            .addFunction(toStringReturningValue())
            .build()
    }

    private fun toStringReturningValue(): FunSpec {
        return FunSpec.builder("toString")
            .addModifiers(KModifier.OVERRIDE)
            .returns(STRING)
            .addStatement("return value")
            .build()
    }

    private fun buildVariableNameFile(typesPackage: String, language: OutputLanguage): GeneratedApiFile {
        val jvmInline = ClassName("kotlin.jvm", "JvmInline")
        val variableName = ClassName(typesPackage, "VariableName")

        fun directionSubtype(subName: String, kdoc: String): TypeSpec {
            return TypeSpec.classBuilder(subName)
                .addModifiers(KModifier.VALUE)
                .addAnnotation(AnnotationSpec.builder(jvmInline).build())
                .addSuperinterface(variableName)
                .addKdoc(kdoc)
                .primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addParameter("value", STRING)
                        .build()
                )
                .addProperty(
                    PropertySpec.builder("value", STRING)
                        .addModifiers(KModifier.OVERRIDE)
                        .initializer("value")
                        .build()
                )
                .addFunction(toStringReturningValue())
                .build()
        }

        val sealedInterface = TypeSpec.interfaceBuilder("VariableName")
            .addModifiers(KModifier.SEALED)
            .addKdoc(VARIABLE_NAME_KDOC)
            .addProperty(PropertySpec.builder("value", STRING).build())
            .addType(
                directionSubtype(
                    "Input",
                    "A variable read by the declaring element (BPMN input mapping).",
                )
            )
            .addType(
                directionSubtype(
                    "Output",
                    "A variable written by the declaring element (BPMN output mapping).",
                )
            )
            .addType(
                directionSubtype(
                    "InOut",
                    "A variable read AND written by the declaring element.",
                )
            )
            .build()
        return buildTypeFile(typesPackage, "VariableName", sealedInterface, language)
    }

    private fun buildBpmnTimerFile(typesPackage: String, language: OutputLanguage): GeneratedApiFile {
        val typeSpec = TypeSpec.classBuilder("BpmnTimer")
            .addModifiers(KModifier.DATA)
            .addKdoc(
                "A BPMN timer definition.\n\n" +
                    "@param type One of `Duration`, `Date`, or `Cycle`.\n" +
                    "@param timerValue The timer expression (ISO 8601 duration, date, or cycle).\n"
            )
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
            .addKdoc(
                "A BPMN error definition referenced by error catch events and error end events.\n\n" +
                    "@param name The error name as declared in the BPMN model.\n" +
                    "@param code The error code used to match catch events at runtime.\n"
            )
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
            .addKdoc(
                "A BPMN escalation definition referenced by escalation catch events and escalation end events.\n\n" +
                    "@param name The escalation name as declared in the BPMN model.\n" +
                    "@param code The escalation code used to match catch events at runtime.\n"
            )
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
                    "@param id The sequence flow id as declared in the BPMN model.\n" +
                    "@param name Label of the flow as shown in the BPMN diagram; null when unlabelled.\n" +
                    "@param sourceRef Element id of the flow's source node.\n" +
                    "@param targetRef Element id of the flow's target node.\n" +
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
                    "@param name Display name of the element, if set in the BPMN model.\n" +
                    "@param previousElements Element ids of preceding flow nodes — not sequence-flow ids (see `Flows`).\n" +
                    "@param followingElements Element ids of following flow nodes — not sequence-flow ids (see `Flows`).\n" +
                    "@param parentId Id of the containing subprocess, or null if top-level.\n" +
                    "@param attachedToRef For boundary events: id of the host element; null otherwise.\n" +
                    "@param attachedElements Ids of boundary events attached to this element; empty when none.\n"
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
