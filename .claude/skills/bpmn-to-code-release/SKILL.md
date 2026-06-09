---
name: bpmn-to-code-release
description: Reference for releasing bpmn-to-code. Releases are automated via release-please; this skill documents the human-in-the-loop steps and edge cases.
allowed-tools: Bash(gh *)
---

# Release – bpmn-to-code

Releases are automated by [release-please](https://github.com/googleapis/release-please). There is **no manual bump or tag step**. The workflow `.github/workflows/release-please.yml` keeps a permanent "Release PR" open and publishes the GitHub Release (and its tag) immediately on merge.

## Normal flow

1. **Conventional commits land on `main`.** `feat:` → minor, `fix:` → patch, `feat!:` / `BREAKING CHANGE` → major. Other types (`chore`, `docs`, `refactor`, `build`, `test`, …) do not bump.
2. **Release-please opens / updates a PR** titled `chore(main): release <next-version>`. It touches only `gradle.properties`, `CHANGELOG.md`, and `.release-please-manifest.json`.
3. **Merge the Release PR** when you want to ship.
4. **A published GitHub Release is created** (tag `v<version>` created atomically) with notes generated from the commits. The release is *not* a draft — publishing immediately is what guarantees the tag exists, which release-please needs to anchor the next changelog.
5. **The same `Release` workflow run continues and pauses at the `release` approval gate.** Publishing is no longer a separate workflow reacting to the published release — the publish jobs run in the *same* run via `needs` + `release_created`. Open the workflow run and **approve the deployment** (or reject to abort). This is the human gate — it replaces the old "publish the draft" step.
6. After approval, the publish jobs run: Maven Central → Gradle Plugin Portal, Docker Hub, and the GitHub Pages docs deploy.

## Force a specific bump (edge case)

If the commits would not produce the version you want (e.g. you need `3.0.0` without a `feat!:` commit), add a footer to any commit on `main`:

```
Release-As: 3.0.0
```

Release-please uses that version on its next run.

## Edit release notes

The Release is published automatically on merge, so notes are edited **after** publishing in the GitHub UI (Releases → Edit). Adjust wording, regroup items, or add a migration note — the tag and artifacts are unaffected by body edits.

## Sanity checks

```bash
gh release list --limit 5
gh workflow view "Release" --web
```

If the Release PR doesn't appear after a push to `main`, the workflow run page shows why (usually: no Conventional commits with releasable types since the last tag).

## Manual publishing (fallback)

Only if automation fails. Ask the user for confirmation first. There is no single
"publish everything" workflow anymore — dispatch the individual reusable workflows
(each keeps its own `workflow_dispatch` + `dry_run`):

```bash
gh workflow run publish-to-maven.yml
gh workflow run publish-to-gradle.yml
gh workflow run publish-to-docker.yml
gh workflow run deploy-docs.yml
```

Verify:

```bash
gh run list --workflow=publish-to-maven.yml --limit 1
```
