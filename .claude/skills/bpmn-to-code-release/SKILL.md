---
name: bpmn-to-code-release
description: Create a new release for bpmn-to-code and publish to Maven Central, Gradle Plugin Portal, and Docker Hub via GitHub Actions. Use when releasing a new version.
allowed-tools: Bash(git *), Bash(gh *), Bash(./gradlew *), AskUserQuestion
---

# Release – bpmn-to-code

## Step 0 – Enforce main branch

Run:

```bash
git branch --show-current
```

If the result is **not** `main`, stop immediately with:

> "This skill can only be run on the `main` branch. Please switch to `main` and try again."

Do not proceed with any further steps.

## Step 1 – Pre-flight checks

Verify the working directory is clean:

```bash
git status
```

If dirty, stop and ask the user to commit or stash their changes first.

Ensure local `main` is up to date with the remote:

```bash
git fetch origin main
git status -sb
```

If the branch is **behind** `origin/main`, run:

```bash
git pull --ff-only origin main
```

If the pull fails (e.g. due to diverged history), stop and ask the user to resolve it manually.

## Step 2 – Check whether a version bump is needed

Read the current version from `gradle.properties` (`projectVersion=...`).

Get the latest published release:

```bash
gh release list --limit 1
```

Compare:

- If `gradle.properties` already holds a version **greater than** the latest release tag → the bump was already done. Inform the user ("Version already bumped to `<CURRENT_VERSION>`, skipping bump step.") and set `<NEXT_VERSION>` to that value, then jump to **Step 4**.
- Otherwise → a bump is needed. Continue to **Step 3**.

## Step 3 – Determine bump type

Inspect commits since the last release tag:

```bash
git log <LAST_TAG>..HEAD --oneline
```

Use the following rules to derive a suggestion:

| Commits contain              | Suggested bump |
| ---------------------------- | -------------- |
| `feat!` or `BREAKING CHANGE` | **major**      |
| `feat:`                      | **minor**      |
| Only fixes / chores / deps   | **patch**      |

Compute all three candidate versions from `<LAST_RELEASE_VERSION>` (e.g. if last release was `1.2.3`: patch → `1.2.4`, minor → `1.3.0`, major → `2.0.0`).

Call `AskUserQuestion`:

> Based on the changes since `<LAST_TAG>`, here is what I found:
>
> `<one-line summary of notable commits>`
>
> **Suggestion: <suggested-type>**
>
> Which bump type should we use?
> - `patch` → v`<patch-version>`
> - `minor` → v`<minor-version>`
> - `major` → v`<major-version>`

Set `<NEXT_VERSION>` to the version the user selects.

## Step 4 – Bump version in gradle.properties, commit, and push

Update `projectVersion=<NEXT_VERSION>` in `gradle.properties`.

```bash
git add gradle.properties
git commit -m "chore(release): <NEXT_VERSION>"
git push origin main
```

## Step 5 – Build and test

Run the full build:

```bash
./gradlew build
```

If the build fails, **stop**. Do not tag or release until the build is green.

## Step 6 – Tag and push

```bash
git tag -a v<NEXT_VERSION> -m "Release version <NEXT_VERSION>"
git push origin v<NEXT_VERSION>
```

## Step 7 – Create a draft release

```bash
gh release create v<NEXT_VERSION> --title "v<NEXT_VERSION>" --generate-notes --draft
```

Use `git log <PREV_TAG>..v<NEXT_VERSION> --oneline` to understand all changes, then rewrite the release notes using this template:

```markdown
## 🧑🏽‍💻 Release – bpmn-to-code v<NEXT_VERSION>

### What's Changed

- **New Features**
  - <short summary> (<PR refs>)

- **Bug Fixes**
  - <short summary> (<PR refs>)

- **Refactoring**
  - <short summary> (<PR refs>)

- **Dependency Updates**
  - <short summary> (<PR refs>)

### Migration Notes

<Breaking changes or "No breaking changes. Upgrade to v<NEXT_VERSION> and regenerate your Process APIs to benefit from ...">

**Full Changelog**: https://github.com/emaarco/bpmn-to-code/compare/<PREV_TAG>...v<NEXT_VERSION>
```

Omit categories that have no entries. Update the draft:

```bash
gh release edit v<NEXT_VERSION> --notes "<rewritten notes>"
```

## Step 8 – Done

A maintainer will review and publish the draft.
Publishing the release triggers the GitHub Action to publish all artifacts automatically:

- **Maven Central** via `./gradlew publishAndReleaseToMavenCentral`
- **Gradle Plugin Portal** via `./gradlew publishPlugins`
- **Docker Hub** via `./gradlew :bpmn-to-code-web:dockerBuild` and `dockerPush`

---

## Manual Publishing (Fallback)

Use this only if automation fails or for an out-of-band publish.
Do **not** invoke this yourself — only if explicitly asked, and ask for confirmation first.

```bash
gh workflow run publish-all.yml
```

Or trigger individual workflows:

```bash
gh workflow run publish-to-maven.yml
gh workflow run publish-to-gradle.yml
gh workflow run publish-to-docker.yml
```

Verify after the workflow completes:

```bash
gh run list --workflow=publish-all.yml --limit 1
```
