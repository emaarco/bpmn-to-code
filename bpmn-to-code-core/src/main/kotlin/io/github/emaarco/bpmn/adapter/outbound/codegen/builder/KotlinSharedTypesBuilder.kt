package io.github.emaarco.bpmn.adapter.outbound.codegen.builder

import com.squareup.kotlinpoet.BOOLEAN
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
 * Generates the 5 shared BPMN data classes (BpmnTimer, BpmnError, BpmnEscalation, BpmnFlow, BpmnRelations)
 * as standalone Kotlin files in the `{packagePath}.types` sub-package.
 * These are identical for every process in the same package — generated once and deduplicated upstream.
 */
class KotlinSharedTypesBuilder : CodeGenerationAdapter.AbstractSharedTypesBuilder() {

    override fun buildTypeFiles(packagePath: String, language: OutputLanguage): List<GeneratedApiFile> {
        val typesPackage = "$packagePath.types"
        return listOf(
            buildBpmnTimerFile(typesPackage, language),
            buildBpmnErrorFile(typesPackage, language),
            buildBpmnEscalationFile(typesPackage, language),
            buildBpmnFlowFile(typesPackage, language),
            buildBpmnRelationsFile(typesPackage, language),
        )
    }

    private fun buildBpmnTimerFile(typesPackage: String, language: OutputLanguage): GeneratedApiFile {
        val typeSpec = TypeSpec.classBuilder("BpmnTimer")
            .addModifiers(KModifier.DATA)
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
        val previousElementsKdoc = "IDs of BPMN elements (not sequence flows) that directly precede this element. " +
            "Use `Flows` to traverse the connecting sequence flows."
        val followingElementsKdoc = "IDs of BPMN elements (not sequence flows) that directly follow this element. " +
            "Use `Flows` to traverse the connecting sequence flows."
        val typeSpec = TypeSpec.classBuilder("BpmnRelations")
            .addModifiers(KModifier.DATA)
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
            .addProperty(
                PropertySpec.builder("previousElements", listType)
                    .initializer("previousElements")
                    .addKdoc(previousElementsKdoc)
                    .build()
            )
            .addProperty(
                PropertySpec.builder("followingElements", listType)
                    .initializer("followingElements")
                    .addKdoc(followingElementsKdoc)
                    .build()
            )
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
