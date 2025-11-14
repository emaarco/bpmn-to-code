# ADR 006: File-Based API Versioning

## Status
Accepted

## Context
BPMN processes evolve over time. When process structure changes, existing code using the old API may break. Need mechanism to support multiple API versions simultaneously during migration periods.

## Decision
Track API versions in `bpmn-to-code.properties` file:

```properties
newsletterSubscription=1
orderProcess=3
```

Version appended to generated class name when versioning enabled:
- `NewsletterSubscriptionProcessApiV1`
- `NewsletterSubscriptionProcessApiV2`

Users manually increment versions in properties file when making breaking changes.

## Consequences

### Positive
- Multiple API versions coexist (gradual migration)
- Explicit version control via checked-in properties file
- Simple implementation (no git analysis or hashing)
- Version visible in class name

### Negative
- **Manual management**: Users must remember to increment versions
- **No automatic detection**: Breaking changes not detected automatically
- **File management**: Properties file must be maintained separately
- **Cleanup burden**: Old versions accumulate unless manually deleted

## Alternatives Considered

**Git-based versioning** (Rejected)
- Would auto-detect changes via BPMN file hash
- Requires git; breaks for non-git workflows
- Version numbers unstable across branches

**Semantic versioning** (Rejected)
- Too complex for code generation use case
- Users unlikely to follow semver rules correctly

## Implementation
`VersionService` reads/writes properties file. Generated filename includes version suffix when `useVersioning=true`.
