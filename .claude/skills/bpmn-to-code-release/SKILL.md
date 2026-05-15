---
name: bpmn-to-code-release
description: Create a new release for bpmn-to-code and publish to Maven Central, Gradle Plugin Portal, and Docker Hub via GitHub Actions. Use when releasing a new version.
allowed-tools: Bash(git *), Bash(gh *), Bash(./gradlew *), AskUserQuestion
---

# Release – bpmn-to-code

`main` is a protected branch — direct pushes are rejected. A release happens in two phases:

- **Phase 1 – Prep PR**: from a release branch, bump the version and open a PR into `main`.
- **Phase 2 – Tag & release**: after the PR is merged, from `main`, build, tag, and create the draft GitHub release.

## Step 0 – Detect phase

Run:

```bash
git branch --show-current
git fetch origin
```

Then read `projectVersion` from `gradle.properties` and the latest release tag:

```bash
gh release list --limit 1
```

Decide which phase to run:

- **On `main`** AND `gradle.properties` version is **greater than** the latest release tag → **Phase 2** (jump to **Step 5**).
- **On `main`** AND versions match → nothing to do. Tell the user there is no pending release and stop.
- **On any other branch** → **Phase 1** (continue to **Step 1**).

## Step 1 – Pre-flight checks (Phase 1)

Verify the working directory is clean:

```bash
git status
```

If dirty, stop and ask the user to commit or stash their changes first.

Ensure the current branch is up to date with `origin/main`. Compare:

```bash
git rev-list --left-right --count origin/main...HEAD
```

- If the branch is **behind** `origin/main` (left count > 0), stop and ask the user to rebase or merge `origin/main` first. Do **not** rebase automatically — the user owns history changes.
- If only ahead (right count > 0) or in sync → proceed.

## Step 2 – Determine the next version

Read the current version from `gradle.properties`. Get the latest release tag (`<LAST_TAG>`, e.g. `v1.2.3`).

If `gradle.properties` already holds a version **greater than** `<LAST_TAG>` → the bump was already done on this branch. Inform the user ("Version already bumped to `<CURRENT_VERSION>` on this branch, skipping bump.") and set `<NEXT_VERSION>` to that value, then jump to **Step 4**.

Otherwise inspect commits since `<LAST_TAG>`:

```bash
git log <LAST_TAG>..HEAD --oneline
```

Suggest a bump type:

| Commits contain              | Suggested bump |
| ---------------------------- | -------------- |
| `feat!` or `BREAKING CHANGE` | **major**      |
| `feat:`                      | **minor**      |
| Only fixes / chores / deps   | **patch**      |

Compute all three candidates from `<LAST_RELEASE_VERSION>` (e.g. from `1.2.3`: patch → `1.2.4`, minor → `1.3.0`, major → `2.0.0`).

## Step 3 – Confirm bump type

Call `AskUserQuestion`:

> Based on the changes since `<LAST_TAG>`:
>
> `<one-line summary of notable commits>`
>
> **Suggestion: <suggested-type>**
>
> Which bump type should we use?
> - `patch` → v`<patch-version>`
> - `minor` → v`<minor-version>`
> - `major` → v`<major-version>`

Set `<NEXT_VERSION>` to the chosen version.

## Step 4 – Bump, commit, push, and open PR

Update `projectVersion=<NEXT_VERSION>` in `gradle.properties`.

```bash
git add gradle.properties
git commit -m "chore(release): <NEXT_VERSION>"
git push -u origin HEAD
```

Open a PR into `main`:

```bash
gh pr create --base main --title "chore(release): <NEXT_VERSION>" --body "$(cat <<'EOF'
## Summary
- Bump `projectVersion` to `<NEXT_VERSION>` in `gradle.properties`.

## Release notes preview
<bullet list of notable changes since <LAST_TAG>>

After merge, re-run the `bpmn-to-code-release` skill from `main` to tag and create the draft GitHub release.
EOF
)"
```

Stop here. Tell the user:

> PR opened for v`<NEXT_VERSION>`. Once it's merged into `main`, switch to `main`, pull, and re-run this skill to continue with Phase 2 (build, tag, draft release).

## Step 5 – Build and test (Phase 2)

Ensure `main` is up to date:

```bash
git pull --ff-only origin main
```

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
