# ADR 005: Strategy Pattern for Multi-Language Code Generation

## Status
Accepted

## Context
Plugin must generate type-safe API code in multiple languages (Kotlin, Java) with language-specific syntax, type systems, and conventions. Each language requires different code generation libraries (KotlinPoet, JavaPoet).

## Decision
Use Strategy pattern with language-specific builders:

```kotlin
Map<OutputLanguage, AbstractApiBuilder>
```

Each builder implements code generation for its language using appropriate code generation libraries. Builders share common structure through abstract base class.

## Consequences

### Positive
- **Language isolation**: Kotlin/Java generation logic completely separate
- **Library flexibility**: Each builder uses appropriate code generation tool
- **Extensibility**: New languages (Scala, TypeScript) added via new builders
- **Parallel development**: Language implementations can evolve independently

### Negative
- **Code duplication**: Similar object structures generated differently per language
- **Consistency burden**: Must manually ensure output equivalence across languages
- **Testing overhead**: Each builder requires separate test suite

## Alternatives Considered

**Template-based generation** (Rejected)
- Would reduce duplication via shared templates
- Loses type safety of code generation libraries
- Harder to handle language-specific features

## Implementation
```kotlin
val builder = mapOf(
    OutputLanguage.KOTLIN to KotlinApiBuilder(),
    OutputLanguage.JAVA to JavaApiBuilder()
)
```

Builders generate identical API structures with language-specific syntax.
