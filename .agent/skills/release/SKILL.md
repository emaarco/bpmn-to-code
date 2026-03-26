---
name: release
disable-model-invocation: true
argument-hint: <version>
description: Create a new release for bpmn-to-code and publish to Maven Central, Gradle Plugin Portal, and Docker Hub via GitHub Actions. Use when releasing a new version.
---

# Release – bpmn-to-code

Create a new release for `bpmn-to-code` $ARGUMENTS.

## Permission Required

**Before taking any action**, summarize the release steps you are about to execute and ask the user for explicit confirmation to proceed. Do not run any commands until the user approves.

## Pre-flight Checks

After receiving approval, verify:
- Working directory is clean: `git status`
- You are on the `main` branch
- All changes are tested and committed

## Standard Release Workflow (Automated via GitHub Actions)

When a GitHub release is published (draft=false), the GitHub Action automatically publishes artifacts to Maven Central, Gradle Plugin Portal, and Docker Hub.

### 1. Bump Version

Check the current version in `gradle.properties` (`projectVersion=...`) and compare it against the most recent releases:

```bash
gh release list --limit 5
```

If the version was already bumped (i.e., `gradle.properties` holds a version newer than the latest release), skip this step. Otherwise, update the `projectVersion` in `gradle.properties` and commit:

```bash
git add gradle.properties
git commit -m "release: bump version to <VERSION>"
```

### 2. Build and Test

Run the full build to make sure everything passes:

```bash
./gradlew build
```

If the build fails, stop and fix the issue before proceeding.

### 3. Tag and Push

Create an annotated tag and push it:

```bash
git tag -a v<VERSION> -m "Release version <VERSION>"
git push origin v<VERSION>
```

Only push `main` if there are unpushed commits (e.g., a version bump commit). Otherwise, just push the tag.

### 4. Create a Draft Release

First, get the auto-generated notes as a starting point:

```bash
gh release create v<VERSION> --title "v<VERSION>" --generate-notes --draft
```

Then rewrite the release notes to match the project's established format. Use `git log <PREV_TAG>..v<VERSION> --oneline` to understand all changes, and structure the notes using this template:

```markdown
## 🧑🏽‍💻 Release – bpmn-to-code v<VERSION>

### What's Changed

- **New Features**
  - <short summary> (<PR refs>)
  - <short summary> (<PR refs>)

- **Bug Fixes**
  - <short summary> (<PR refs>)

- **Refactoring**
  - <short summary> (<PR refs>)
  - <short summary> (<PR refs>)

- **Dependency Updates**
  - <short summary> (<PR refs>)

### Migration Notes

<Breaking changes or "No breaking changes. Upgrade to v<VERSION> and regenerate your Process APIs to benefit from ...">

**Full Changelog**: https://github.com/emaarco/bpmn-to-code/compare/<PREV_TAG>...v<VERSION>
```

Group related PRs into logical categories. Use **New Features**, **Bug Fixes**, **Refactoring**, **Dependency Updates**, or other fitting labels. Omit categories that have no entries.

Update the draft with the rewritten notes:

```bash
gh release edit v<VERSION> --notes "<rewritten notes>"
```

### 5. Done

A maintainer will review and publish the draft.
Publishing the release triggers the GitHub Action to publish all artifacts automatically:
- **Maven Central** via `./gradlew publishAndReleaseToMavenCentral`
- **Gradle Plugin Portal** via `./gradlew publishPlugins`
- **Docker Hub** via `./gradlew :bpmn-to-code-web:dockerBuild` and `dockerPush`

## Manual Publishing (Fallback)

Use this if automation fails or for an out-of-band publish.
Do never invoke this yourself. Only if you are asked to do so.
Even then ask for explicit confirmation first.

Trigger the publish workflow manually:

```bash
gh workflow run publish-all.yml
```

Or trigger individual workflows:

```bash
gh workflow run publish-to-maven.yml
gh workflow run publish-to-gradle.yml
gh workflow run publish-to-docker.yml
```

### Verify Publication

After the workflow completes, verify:

```bash
gh run list --workflow=publish-all.yml --limit 1
```
