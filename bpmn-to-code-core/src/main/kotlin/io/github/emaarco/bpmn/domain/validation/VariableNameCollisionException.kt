package io.github.emaarco.bpmn.domain.validation

/**
 * Exception thrown when variable name collisions are detected across one or more BPMN models.
 *
 * This exception aggregates all collisions found during validation, allowing users to see
 * and fix all issues in a single iteration rather than discovering them one at a time.
 */
class VariableNameCollisionException(
    val collisions: List<CollisionDetail>,
) : RuntimeException() {

    override val message = buildErrorMessage()

    private fun buildErrorMessage() = buildString {
        val collisionsByProcess = collisions.sortedBy { it.processId }
        val uniqueProcesses = collisions.map { it.processId }.distinct()
        val numberOfProcesses = uniqueProcesses.size

        appendLine("Cannot create Process-APIs. Detected collisions in configuration of $numberOfProcesses ${if (numberOfProcesses == 1) "process" else "processes"}")
        appendLine()

        // Group collisions by process
        uniqueProcesses.sorted().forEach { processId ->
            appendLine("Process: $processId")
            collisionsByProcess.filter { it.processId == processId }.forEach { collision ->
                val ids = collision.conflictingIds.joinToString(", ")
                appendLine("  [${collision.variableType}] ${collision.constantName}")
                appendLine("    Conflicting IDs: $ids")
            }
            appendLine()
        }

        append("Please update your BPMN files to use consistent naming.")
    }
}
