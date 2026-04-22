# Contributing

## Prerequisites

- Java 21 (see `.java-version`)
- [Lefthook](https://github.com/evilmartians/lefthook) for local git hooks

## One-time setup

Install Lefthook and register the git hooks:

```bash
# macOS
brew install lefthook

# Other platforms: https://github.com/evilmartians/lefthook#installation
```

```bash
lefthook install
```

This installs a `pre-push` hook that runs `:bpmn-to-code-core:jacocoTestCoverageVerification` — the same check enforced in CI.

## Common Commands

```bash
./gradlew build                                               # full build
./gradlew :bpmn-to-code-core:test                            # run core tests only
./gradlew :bpmn-to-code-core:jacocoTestCoverageVerification  # check coverage manually
```

## Skipping hooks

```bash
git push --no-verify  # bypasses lefthook; CI still enforces
```
