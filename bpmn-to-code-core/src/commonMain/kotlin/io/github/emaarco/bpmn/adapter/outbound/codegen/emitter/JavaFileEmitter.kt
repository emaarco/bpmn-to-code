package io.github.emaarco.bpmn.adapter.outbound.codegen.emitter

/** Renders a [FileSpec] as Java source (replaces JavaPoet). */
object JavaFileEmitter {

    fun emit(file: FileSpec): String {
        val writer = CodeWriter()
        writer.line("// ${file.fileComment}")
        writer.line("package ${file.packageName};")
        writer.line()
        emitImports(writer, file)
        emitRootClass(writer, file.rootType)
        return writer.toString()
    }

    private fun emitImports(writer: CodeWriter, file: FileSpec) {
        val imports = mutableSetOf<String>()
        collectImports(file.rootType, imports)
        imports.sorted().forEach { writer.line("import $it;") }
        writer.line()
    }

    private fun collectImports(type: TypeSpec, imports: MutableSet<String>) {
        type.properties.forEach { property ->
            property.refs.forEach { ref -> imports.add("${ref.packageName}.${ref.simpleName}") }
        }
        type.nestedTypes.forEach { nested -> collectImports(nested, imports) }
    }

    private fun emitRootClass(writer: CodeWriter, type: TypeSpec) {
        emitClass(writer, type, "public final class")
    }

    private fun emitClass(writer: CodeWriter, type: TypeSpec, declaration: String) {
        emitJavadoc(writer, type.kdoc)
        writer.line("$declaration ${type.name} {").indent()
        val members = type.properties.size + type.nestedTypes.size
        var emitted = 0
        type.properties.forEach { property ->
            emitField(writer, property)
            emitted++
            if (emitted < members) writer.line()
        }
        type.nestedTypes.forEach { nested ->
            emitClass(writer, nested, "public static final class")
            emitted++
            if (emitted < members) writer.line()
        }
        writer.unindent().line("}")
    }

    private fun emitField(writer: CodeWriter, property: PropertySpec) {
        val typeName = property.type.referencedName()
        val initializer = requireNotNull(property.initializer.single) {
            "Java fields are emitted as single-line initializers"
        }
        writer.line("public static final $typeName ${property.name} = $initializer;")
    }

    private fun emitJavadoc(writer: CodeWriter, kdoc: List<String>) {
        if (kdoc.isEmpty()) return
        writer.line("/**")
        kdoc.forEach { writer.line(" * $it") }
        writer.line(" */")
    }
}
