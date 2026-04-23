package io.github.emaarco.bpmn.adapter.outbound.codegen.builder

import com.palantir.javapoet.ClassName
import com.palantir.javapoet.FieldSpec
import com.palantir.javapoet.JavaFile
import com.palantir.javapoet.MethodSpec
import com.palantir.javapoet.ParameterizedTypeName
import com.palantir.javapoet.TypeSpec
import io.github.emaarco.bpmn.adapter.outbound.codegen.CodeGenerationAdapter
import io.github.emaarco.bpmn.domain.GeneratedApiFile
import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import javax.lang.model.element.Modifier.ABSTRACT
import javax.lang.model.element.Modifier.FINAL
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.Modifier.SEALED

private const val VARIABLE_NAME_JAVADOC: String =
    "Name of a process variable declared by a BPMN element.\n\n" +
        "Direction is encoded in the subtype so consumer APIs can enforce it at compile time\n" +
        "(for example, a method accepting {@code VariableName.Output} rejects inputs).\n" +
        "{@code toString()} returns the underlying string, so {@code .value()} is optional in string contexts.\n"

/**
 * Generates shared BPMN types as standalone Java files in the `{packagePath}.types` sub-package:
 * enum `BpmnEngine`, classes (BpmnTimer, BpmnError, BpmnEscalation, BpmnFlow, BpmnRelations),
 * and records wrapping leaf identifiers (ProcessId, ElementId, MessageName, VariableName, SignalName).
 * These are identical for every process in the same package — generated once and deduplicated upstream.
 */
class JavaSharedTypesBuilder : CodeGenerationAdapter.AbstractSharedTypesBuilder() {

    override fun buildTypeFiles(packagePath: String, language: OutputLanguage): List<GeneratedApiFile> {
        val typesPackage = "$packagePath.types"
        return listOf(
            buildBpmnEngineFile(typesPackage, language),
            buildBpmnTimerFile(typesPackage, language),
            buildBpmnErrorFile(typesPackage, language),
            buildBpmnEscalationFile(typesPackage, language),
            buildBpmnFlowFile(typesPackage, language),
            buildBpmnRelationsFile(typesPackage, language),
            buildValueRecordFile(typesPackage, "ProcessId", language),
            buildValueRecordFile(typesPackage, "ElementId", language),
            buildValueRecordFile(typesPackage, "MessageName", language),
            buildVariableNameFile(typesPackage, language),
            buildValueRecordFile(typesPackage, "SignalName", language),
        )
    }

    private fun buildBpmnEngineFile(typesPackage: String, language: OutputLanguage): GeneratedApiFile {
        val typeSpec = TypeSpec.enumBuilder("BpmnEngine").addModifiers(PUBLIC)
            .addEnumConstant("ZEEBE")
            .addEnumConstant("CAMUNDA_7")
            .addEnumConstant("OPERATON")
            .build()
        return buildTypeFile(typesPackage, "BpmnEngine", typeSpec, language)
    }

    private fun buildValueRecordFile(typesPackage: String, recordName: String, language: OutputLanguage): GeneratedApiFile {
        val typeSpec = buildValueRecordSpec(recordName, topLevel = true)
        return buildTypeFile(typesPackage, recordName, typeSpec, language)
    }

    private fun buildValueRecordSpec(recordName: String, topLevel: Boolean, superInterface: ClassName? = null, javadoc: String? = null): TypeSpec {
        val builder = TypeSpec.recordBuilder(recordName)
            .addModifiers(PUBLIC)
            .recordConstructor(
                MethodSpec.constructorBuilder()
                    .addParameter(String::class.java, "value")
                    .build()
            )
            .addMethod(toStringReturningValue())
        if (superInterface != null) builder.addSuperinterface(superInterface)
        if (javadoc != null) builder.addJavadoc(javadoc)
        if (!topLevel) builder.addModifiers(javax.lang.model.element.Modifier.STATIC)
        return builder.build()
    }

    private fun toStringReturningValue(): MethodSpec {
        return MethodSpec.methodBuilder("toString")
            .addAnnotation(Override::class.java)
            .addModifiers(PUBLIC)
            .returns(String::class.java)
            .addStatement("return value")
            .build()
    }

    private fun buildVariableNameFile(typesPackage: String, language: OutputLanguage): GeneratedApiFile {
        val variableNameClass = ClassName.get(typesPackage, "VariableName")
        val inputRecord = buildValueRecordSpec(
            recordName = "Input",
            topLevel = false,
            superInterface = variableNameClass,
            javadoc = "A variable read by the declaring element (BPMN input mapping).\n",
        )
        val outputRecord = buildValueRecordSpec(
            recordName = "Output",
            topLevel = false,
            superInterface = variableNameClass,
            javadoc = "A variable written by the declaring element (BPMN output mapping).\n",
        )
        val inOutRecord = buildValueRecordSpec(
            recordName = "InOut",
            topLevel = false,
            superInterface = variableNameClass,
            javadoc = "A variable read AND written by the declaring element.\n",
        )

        val valueAccessor = MethodSpec.methodBuilder("value")
            .addModifiers(PUBLIC, ABSTRACT)
            .returns(String::class.java)
            .build()

        val sealedInterface = TypeSpec.interfaceBuilder("VariableName")
            .addModifiers(PUBLIC, SEALED)
            .addJavadoc(VARIABLE_NAME_JAVADOC)
            .addMethod(valueAccessor)
            .addType(inputRecord)
            .addType(outputRecord)
            .addType(inOutRecord)
            .build()
        return buildTypeFile(typesPackage, "VariableName", sealedInterface, language)
    }

    private fun buildBpmnTimerFile(typesPackage: String, language: OutputLanguage): GeneratedApiFile {
        val typeSpec = TypeSpec.classBuilder("BpmnTimer").addModifiers(PUBLIC)
            .addField(FieldSpec.builder(String::class.java, "type").addModifiers(PUBLIC, FINAL).build())
            .addField(FieldSpec.builder(String::class.java, "timerValue").addModifiers(PUBLIC, FINAL).build())
            .addMethod(
                MethodSpec.constructorBuilder()
                    .addModifiers(PUBLIC)
                    .addParameter(String::class.java, "type")
                    .addParameter(String::class.java, "timerValue")
                    .addStatement("this.type = type")
                    .addStatement("this.timerValue = timerValue")
                    .build()
            )
            .build()
        return buildTypeFile(typesPackage, "BpmnTimer", typeSpec, language)
    }

    private fun buildBpmnErrorFile(typesPackage: String, language: OutputLanguage): GeneratedApiFile {
        val typeSpec = TypeSpec.classBuilder("BpmnError").addModifiers(PUBLIC)
            .addField(FieldSpec.builder(String::class.java, "name").addModifiers(PUBLIC, FINAL).build())
            .addField(FieldSpec.builder(String::class.java, "code").addModifiers(PUBLIC, FINAL).build())
            .addMethod(
                MethodSpec.constructorBuilder()
                    .addModifiers(PUBLIC)
                    .addParameter(String::class.java, "name")
                    .addParameter(String::class.java, "code")
                    .addStatement("this.name = name")
                    .addStatement("this.code = code")
                    .build()
            )
            .build()
        return buildTypeFile(typesPackage, "BpmnError", typeSpec, language)
    }

    private fun buildBpmnEscalationFile(typesPackage: String, language: OutputLanguage): GeneratedApiFile {
        val typeSpec = TypeSpec.classBuilder("BpmnEscalation").addModifiers(PUBLIC)
            .addField(FieldSpec.builder(String::class.java, "name").addModifiers(PUBLIC, FINAL).build())
            .addField(FieldSpec.builder(String::class.java, "code").addModifiers(PUBLIC, FINAL).build())
            .addMethod(
                MethodSpec.constructorBuilder()
                    .addModifiers(PUBLIC)
                    .addParameter(String::class.java, "name")
                    .addParameter(String::class.java, "code")
                    .addStatement("this.name = name")
                    .addStatement("this.code = code")
                    .build()
            )
            .build()
        return buildTypeFile(typesPackage, "BpmnEscalation", typeSpec, language)
    }

    private fun buildBpmnFlowFile(typesPackage: String, language: OutputLanguage): GeneratedApiFile {
        val typeSpec = TypeSpec.classBuilder("BpmnFlow").addModifiers(PUBLIC)
            .addField(FieldSpec.builder(String::class.java, "id").addModifiers(PUBLIC, FINAL).build())
            .addField(FieldSpec.builder(String::class.java, "name").addModifiers(PUBLIC, FINAL).build())
            .addField(FieldSpec.builder(String::class.java, "sourceRef").addModifiers(PUBLIC, FINAL).build())
            .addField(FieldSpec.builder(String::class.java, "targetRef").addModifiers(PUBLIC, FINAL).build())
            .addField(FieldSpec.builder(String::class.java, "condition").addModifiers(PUBLIC, FINAL).build())
            .addField(FieldSpec.builder(requireNotNull(Boolean::class.javaPrimitiveType), "isDefault").addModifiers(PUBLIC, FINAL).build())
            .addMethod(
                MethodSpec.constructorBuilder()
                    .addModifiers(PUBLIC)
                    .addParameter(String::class.java, "id")
                    .addParameter(String::class.java, "name")
                    .addParameter(String::class.java, "sourceRef")
                    .addParameter(String::class.java, "targetRef")
                    .addParameter(String::class.java, "condition")
                    .addParameter(requireNotNull(Boolean::class.javaPrimitiveType), "isDefault")
                    .addStatement("this.id = id")
                    .addStatement("this.name = name")
                    .addStatement("this.sourceRef = sourceRef")
                    .addStatement("this.targetRef = targetRef")
                    .addStatement("this.condition = condition")
                    .addStatement("this.isDefault = isDefault")
                    .build()
            )
            .build()
        return buildTypeFile(typesPackage, "BpmnFlow", typeSpec, language)
    }

    private fun buildBpmnRelationsFile(typesPackage: String, language: OutputLanguage): GeneratedApiFile {
        val listClass = ClassName.get("java.util", "List")
        val listType = ParameterizedTypeName.get(listClass, ClassName.get("java.lang", "String"))
        val typeSpec = TypeSpec.classBuilder("BpmnRelations").addModifiers(PUBLIC)
            .addField(FieldSpec.builder(String::class.java, "name").addModifiers(PUBLIC, FINAL).build())
            .addField(FieldSpec.builder(listType, "previousElements").addModifiers(PUBLIC, FINAL).build())
            .addField(FieldSpec.builder(listType, "followingElements").addModifiers(PUBLIC, FINAL).build())
            .addField(FieldSpec.builder(String::class.java, "parentId").addModifiers(PUBLIC, FINAL).build())
            .addField(FieldSpec.builder(String::class.java, "attachedToRef").addModifiers(PUBLIC, FINAL).build())
            .addField(FieldSpec.builder(listType, "attachedElements").addModifiers(PUBLIC, FINAL).build())
            .addMethod(
                MethodSpec.constructorBuilder()
                    .addModifiers(PUBLIC)
                    .addParameter(String::class.java, "name")
                    .addParameter(listType, "previousElements")
                    .addParameter(listType, "followingElements")
                    .addParameter(String::class.java, "parentId")
                    .addParameter(String::class.java, "attachedToRef")
                    .addParameter(listType, "attachedElements")
                    .addStatement("this.name = name")
                    .addStatement("this.previousElements = previousElements")
                    .addStatement("this.followingElements = followingElements")
                    .addStatement("this.parentId = parentId")
                    .addStatement("this.attachedToRef = attachedToRef")
                    .addStatement("this.attachedElements = attachedElements")
                    .build()
            )
            .build()
        return buildTypeFile(typesPackage, "BpmnRelations", typeSpec, language)
    }

    private fun buildTypeFile(typesPackage: String, className: String, typeSpec: TypeSpec, language: OutputLanguage): GeneratedApiFile {
        val javaFile = JavaFile.builder(typesPackage, typeSpec)
            .addFileComment(autoGenComment)
            .build()
        val content = buildString { javaFile.writeTo(this) }
        return GeneratedApiFile(
            fileName = "$className.java",
            packagePath = typesPackage,
            content = content,
            language = language,
        )
    }
}
