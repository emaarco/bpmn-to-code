---
name: bpmn-to-code-release
description: Reference for releasing bpmn-to-code. Releases are automated via release-please; this skill documents the human-in-the-loop steps and edge cases.
allowed-tools: Bash(gh *)
---

# Release – bpmn-to-code

Releases are automated by [release-please](https://github.com/googleapis/release-please). There is **no manual bump or tag step**. The workflow `.github/workflows/release-please.yml` keeps a permanent "Release PR" open and creates the GitHub Release after merge.

## Normal flow

1. **Conventional commits land on `main`.** `feat:` → minor, `fix:` → patch, `feat!:` / `BREAKING CHANGE` → major. Other types (`chore`, `docs`, `refactor`, `build`, `test`, …) do not bump.
2. **Release-please opens / updates a PR** titled `chore(main): release <next-version>`. It touches only `gradle.properties`, `CHANGELOG.md`, and `.release-please-manifest.json`.
3. **Merge the Release PR** when you want to ship.
4. **A draft GitHub Release is created** (tag `v<version>`) with notes generated from the commits.
5. **Review the draft**, edit notes if needed, then click **Publish release**.
6. Publishing fires `publish-all.yml` → Maven Central → Gradle Plugin Portal → Docker Hub.

## Force a specific bump (edge case)

If the commits would not produce the version you want (e.g. you need `3.0.0` without a `feat!:` commit), add a footer to any commit on `main`:

```
Release-As: 3.0.0
```

Release-please uses that version on its next run.

## Edit notes before publishing

The draft Release is editable in the GitHub UI. Adjust wording, regroup items, or add a migration note, then publish. The tag is already in place once the draft exists, so editing the body is safe.

## Sanity checks

```bash
gh release list --limit 5
gh workflow view "Release Please" --web
```

If the Release PR doesn't appear after a push to `main`, the workflow run page shows why (usually: no Conventional commits with releasable types since the last tag).

## Manual publishing (fallback)

Only if automation fails. Ask the user for confirmation first.

```bash
gh workflow run publish-all.yml
```

Or individual workflows:

```bash
gh workflow run publish-to-maven.yml
gh workflow run publish-to-gradle.yml
gh workflow run publish-to-docker.yml
```

Verify:

```bash
gh run list --workflow=publish-all.yml --limit 1
```
