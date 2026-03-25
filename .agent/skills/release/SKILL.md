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

Create an annotated tag and push everything:

```bash
git tag -a v<VERSION> -m "Release version <VERSION>"
git push origin main --follow-tags
```

### 4. Create a Draft Release

Check previous releases to guide the release notes format, then create the draft:

```bash
gh release view <PREV_TAG>
gh release create v<VERSION> --title "v<VERSION>" --generate-notes --draft
```

### 5. Done

A maintainer will review and publish the draft.
Publishing the release triggers the GitHub Action to publish all artifacts automatically:
- **Maven Central** via `./gradlew publishToMavenCentral`
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
