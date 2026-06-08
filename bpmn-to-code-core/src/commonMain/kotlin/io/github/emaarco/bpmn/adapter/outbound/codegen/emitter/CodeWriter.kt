package io.github.emaarco.bpmn.adapter.outbound.codegen.emitter

/** A tiny deterministic indent-aware writer (replaces KotlinPoet/JavaPoet formatting). */
class CodeWriter(private val indentUnit: String = "  ") {

    private val sb = StringBuilder()
    private var level = 0

    fun indent(): CodeWriter {
        level++
        return this
    }

    fun unindent(): CodeWriter {
        level--
        return this
    }

    fun line(text: String = ""): CodeWriter {
        if (text.isEmpty()) {
            sb.append('\n')
        } else {
            repeat(level) { sb.append(indentUnit) }
            sb.append(text).append('\n')
        }
        return this
    }

    override fun toString(): String {
        return sb.toString()
    }
}
