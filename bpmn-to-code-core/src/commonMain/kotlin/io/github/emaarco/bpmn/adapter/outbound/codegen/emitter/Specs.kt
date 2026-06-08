package io.github.emaarco.bpmn.adapter.outbound.codegen.emitter

/** A reference to a (possibly nested) type; [packageName] drives import collection. */
data class ClassRef(
    val packageName: String,
    val simpleName: String,
    val nested: String? = null,
) {
    fun referencedName(): String {
        return if (nested != null) "$simpleName.$nested" else simpleName
    }
}

/** The initializer of a property/field: either a single line or several lines. */
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

/** A constant/property declaration; [refs] are the types it references (for import collection). */
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
