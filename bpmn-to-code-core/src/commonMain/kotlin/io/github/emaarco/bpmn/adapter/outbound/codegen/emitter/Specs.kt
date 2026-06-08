package io.github.emaarco.bpmn.adapter.outbound.codegen.emitter

/**
 * A reference to a (possibly nested) type. [packageName] drives import collection;
 * [simpleName] is the rendered name. [nested] models a member type such as `VariableName.Input`.
 */
data class ClassRef(
    val packageName: String,
    val simpleName: String,
    val nested: String? = null,
) {
    /** Name as referenced in source: `VariableName.Input` for a nested ref, else `VariableName`. */
    fun referencedName(): String {
        return if (nested != null) "$simpleName.$nested" else simpleName
    }
}

/**
 * The initializer of a property/field. Either fits on a single line ([single]) or spans several
 * lines ([multi]) — the latter is rendered with the constructor name on the first line and each
 * argument indented beneath it.
 */
class InitializerSpec private constructor(
    val single: String?,
    val multi: List<String>?,
) {
    companion object {
        fun ofSingle(line: String): InitializerSpec {
            return InitializerSpec(line, null)
        }

        fun ofMulti(lines: List<String>): InitializerSpec {
            return InitializerSpec(null, lines)
        }
    }
}

/**
 * A constant/property declaration. [refs] lists the types this property references so the file
 * emitter can collect imports.
 */
class PropertySpec(
    val name: String,
    val type: ClassRef,
    val initializer: InitializerSpec,
    val isConst: Boolean = false,
    val refs: List<ClassRef> = listOf(type),
)

/** A (possibly nested) object/class declaration. */
class TypeSpec(
    val name: String,
    val kdoc: List<String> = emptyList(),
    val properties: List<PropertySpec> = emptyList(),
    val nestedTypes: List<TypeSpec> = emptyList(),
) {
    class Builder(private val name: String) {
        private val kdoc = mutableListOf<String>()
        private val properties = mutableListOf<PropertySpec>()
        private val nestedTypes = mutableListOf<TypeSpec>()

        fun addKdoc(lines: List<String>): Builder {
            kdoc.addAll(lines)
            return this
        }

        fun addProperty(property: PropertySpec): Builder {
            properties.add(property)
            return this
        }

        fun addType(type: TypeSpec): Builder {
            nestedTypes.add(type)
            return this
        }

        fun build(): TypeSpec {
            return TypeSpec(name, kdoc.toList(), properties.toList(), nestedTypes.toList())
        }
    }
}

/** The root type plus its package, ready for a language-specific emitter. */
class FileSpec(
    val packageName: String,
    val fileComment: String,
    val rootType: TypeSpec,
)
