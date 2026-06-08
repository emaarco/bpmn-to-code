package io.github.emaarco.bpmn.adapter.outbound.codegen.emitter

/**
 * Multiplatform replacement for KotlinPoet's / JavaPoet's formatter.
 *
 * A tiny indent-aware writer. Deliberately deterministic: fixed 2-space indent, NO automatic
 * column-wrapping and NO automatic import collection (the two things the poet libraries do that
 * are hard to reproduce byte-for-byte and that the migration plan re-baselines away).
 */
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

    /** Appends one indented line (or a blank line when [text] is empty). */
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
