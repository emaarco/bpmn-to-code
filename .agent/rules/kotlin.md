---
paths:
  - "**/*.kt"
---

# Kotlin Code Style

- When a collection literal (`setOf`, `listOf`, `mapOf`, etc.) spans multiple lines, put each element on its own line.
- Prefer function-body style (`{ return ... }`) over expression-body style (`= ...`) for multi-line function implementations.
- Do not declare top-level functions in a file that also contains a class. Move them inside the class (e.g. as a private member or companion object function).
