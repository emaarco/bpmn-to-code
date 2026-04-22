---
name: create-unit-test
description: "Write or refactor unit tests following the canonical project pattern. Use when adding new unit tests or standardizing existing ones."
risk: safe
---

# Create Unit Test Skill

Write and refactor unit tests following the canonical pattern used in this project.

## When to Use

- Writing a new unit test class
- Refactoring an existing test to conform to project style
- Reviewing a test and deciding whether it needs updates

## Canonical Pattern

```kotlin
class FooServiceTest {

    // Mocks declared as private val at the top
    private val somePort = mockk<SomePort>(relaxed = true)

    // Subject under test is always named `underTest`
    private val underTest = FooService(somePort = somePort)

    @Test
    fun `does something when condition is met`() {

        // given: set up inputs and mock behaviour (omit section if single-line)
        val input = SomeInput(
            id = "order-42",
            value = "test",
        )
        every { somePort.fetch(any()) } returns SomeResult(ok = true)

        // when: the action is taken
        val actual = underTest.doSomething(input)

        // then: verify the result and all interactions
        assertThat(actual).isEqualTo(SomeResult(ok = true))
        verify { somePort.fetch(input) }
        confirmVerified(somePort)
    }
}
```

## Rules

### Structure
- `private val underTest` — always this name for the subject under test
- `private val <dep> = mockk<T>(relaxed = true)` — one line per mock, declared before `underTest`
- Static / reusable test data as `private val` fields at the bottom of the class
- Helper builder functions as `private fun` at the bottom of the class

### Given / When / Then Comments
- Add `// given:` / `// when:` / `// then:` comments when a section spans more than one line
- Omit them entirely for trivial single-assertion tests where the structure is obvious
- Put a blank line before each section comment

### Parameters
- Use **named parameters** everywhere — constructor calls, function calls, data class instantiation
- Multi-argument calls always use trailing commas and one arg per line

### Mocks
- Use `mockk<T>(relaxed = true)` — never strict mocks unless you have a specific reason
- Use `every { ... } returns ...` for stubbing
- Use `verify { ... }` to assert interactions
- Always call `confirmVerified(dep1, dep2, ...)` at the end to catch unexpected calls

### Assertions
- Use AssertJ: `assertThat(actual).isEqualTo(expected)` — never bare `assert()` or `assertEquals()`
- Prefer fluent assertion chains over multiple individual asserts

### Builders
- If a builder / helper already exists (e.g. `testBpmnModel()`, `testBpmnModelApi()`, `node()`), use it
- If none exists and the class is small, use its constructor directly with named parameters
- If none exists and the class is large/complex, use `AskUserQuestion` to decide whether to create a builder

### Function Bodies
- Always use block body with explicit return: `fun foo(): Bar { return ... }`
- Never use expression body: `fun foo(): Bar = ...`

### KDoc
- Always use multiline style: `/** \n * description\n */`
- Never use single-line: `/** description */`

## Implementation Checklist

- [ ] Class name ends in `Test`
- [ ] `private val underTest` is present and named correctly
- [ ] All dependencies are mocked with `mockk<T>(relaxed = true)`
- [ ] `// given:` / `// when:` / `// then:` comments present where sections are multi-line
- [ ] Named parameters used everywhere
- [ ] AssertJ assertions used (no bare `assert()`)
- [ ] `confirmVerified(...)` called after all `verify` blocks when mocks are used
- [ ] Test names written in backtick style describing behaviour
- [ ] No production code is changed
- [ ] All tests pass after refactoring
