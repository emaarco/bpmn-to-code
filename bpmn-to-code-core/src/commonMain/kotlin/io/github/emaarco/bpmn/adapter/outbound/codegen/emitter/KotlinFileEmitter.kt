package io.github.emaarco.bpmn.adapter.outbound.codegen.emitter

/**
 * Renders a [FileSpec] as a Kotlin source file: file comment, `@file:Suppress`, package,
 * a sorted/deduplicated import block, and the nested object tree. Replaces KotlinPoet.
 */
object KotlinFileEmitter {

    private const val KOTLIN_PACKAGE = "kotlin"

    fun emit(file: FileSpec): String {
        val writer = CodeWriter()
        writer.line("// ${file.fileComment}")
        writer.line("@file:Suppress(\"unused\")")
        writer.line()
        writer.line("package ${file.packageName}")
        writer.line()
        emitImports(writer, file)
        emitType(writer, file.rootType)
        return writer.toString()
    }

    private fun emitImports(writer: CodeWriter, file: FileSpec) {
        val imports = sortedSetOf<String>()
        imports.add("$KOTLIN_PACKAGE.Suppress")
        collectImports(file.rootType, imports)
        imports.forEach { writer.line("import $it") }
        writer.line()
    }

    private fun collectImports(type: TypeSpec, imports: MutableSet<String>) {
        type.properties.forEach { property ->
            property.refs.forEach { ref -> imports.add("${ref.packageName}.${ref.simpleName}") }
        }
        type.nestedTypes.forEach { nested -> collectImports(nested, imports) }
    }

    private fun emitType(writer: CodeWriter, type: TypeSpec) {
        emitKdoc(writer, type.kdoc)
        writer.line("object ${type.name} {").indent()
        val members = type.properties.size + type.nestedTypes.size
        var emitted = 0
        type.properties.forEach { property ->
            emitProperty(writer, property)
            emitted++
            if (emitted < members) writer.line()
        }
        type.nestedTypes.forEach { nested ->
            emitType(writer, nested)
            emitted++
            if (emitted < members) writer.line()
        }
        writer.unindent().line("}")
    }

    private fun emitProperty(writer: CodeWriter, property: PropertySpec) {
        val keyword = if (property.isConst) "const val" else "val"
        val typeName = property.type.referencedName()
        val initializer = property.initializer
        if (initializer.single != null) {
            writer.line("$keyword ${property.name}: $typeName = ${initializer.single}")
        } else {
            val lines = requireNotNull(initializer.multi)
            writer.line("$keyword ${property.name}: $typeName = ${lines.first()}").indent()
            lines.drop(1).dropLast(1).forEach { writer.line(it) }
            writer.unindent().line(lines.last())
        }
    }

    private fun emitKdoc(writer: CodeWriter, kdoc: List<String>) {
        if (kdoc.isEmpty()) return
        writer.line("/**")
        kdoc.forEach { writer.line(" * $it") }
        writer.line(" */")
    }
}
