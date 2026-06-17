# Changelog

All notable changes to this project are documented here.
This file was bootstrapped from git history with [git-cliff](https://git-cliff.org);
new entries are appended automatically by [release-please](https://github.com/googleapis/release-please).

## [2.4.0](https://github.com/emaarco/bpmn-to-code/compare/v2.3.0...v2.4.0) (2026-06-16)


### Features

* deprecate io.github.emaarco — bpmn-to-code moves to io.miragon ([#377](https://github.com/emaarco/bpmn-to-code/issues/377)) ([c0aae01](https://github.com/emaarco/bpmn-to-code/commit/c0aae0166ec35ba921b8d8766e303c826b9c964e))

## [2.3.0](https://github.com/emaarco/bpmn-to-code/compare/v2.2.0...v2.3.0) (2026-06-12)


### Features

* expose call-activity variable mappings for testing assertions ([#370](https://github.com/emaarco/bpmn-to-code/issues/370)) ([b0c8a19](https://github.com/emaarco/bpmn-to-code/commit/b0c8a1961b573b83e43876d41c1f80cde00446c0))

## [2.2.0](https://github.com/emaarco/bpmn-to-code/compare/v2.1.2...v2.2.0) (2026-06-09)


### Features

* detect engine mismatch and add engine-matched demo samples ([#365](https://github.com/emaarco/bpmn-to-code/issues/365)) ([3018bdb](https://github.com/emaarco/bpmn-to-code/commit/3018bdbfa3ff029cd52966d53c881dfc18663b4f))


### Bug Fixes

* tolerate engine-mismatched models in Zeebe extractor instead of crashing ([#367](https://github.com/emaarco/bpmn-to-code/issues/367)) ([475d109](https://github.com/emaarco/bpmn-to-code/commit/475d1096374404f197cd30517c10f2526dec0c0a))

## [2.1.2](https://github.com/emaarco/bpmn-to-code/compare/v2.1.1...v2.1.2) (2026-05-29)


### Bug Fixes

* **web:** correct broken doc links on landing page ([#355](https://github.com/emaarco/bpmn-to-code/issues/355)) ([8fdf49c](https://github.com/emaarco/bpmn-to-code/commit/8fdf49ccfc5b6dfcabc15b3c99e2a59119419a9e))

## [2.1.1](https://github.com/emaarco/bpmn-to-code/compare/v2.1.0...v2.1.1) (2026-05-29)


### Bug Fixes

* **testing:** bundle BPMN model + logging deps transitively ([#352](https://github.com/emaarco/bpmn-to-code/issues/352)) ([fb4a246](https://github.com/emaarco/bpmn-to-code/commit/fb4a2469b1d34fd97bea7742a5df035f4debc0f2))

## [2.1.0](https://github.com/emaarco/bpmn-to-code/compare/v2.0.4...v2.1.0) (2026-05-29)


### Features

* **core:** expose variable mapping expressions in the domain model ([#349](https://github.com/emaarco/bpmn-to-code/issues/349)) ([2cb7aeb](https://github.com/emaarco/bpmn-to-code/commit/2cb7aebd44799b5071c1572e51230703e848f9c1))


### Bug Fixes

* **release-please:** disable component prefix in tag ([#343](https://github.com/emaarco/bpmn-to-code/issues/343)) ([11483e1](https://github.com/emaarco/bpmn-to-code/commit/11483e1243c726b4e89f0a20cbf2bd37dc5a1880))
* **release-please:** use block markers for gradle.properties ([#345](https://github.com/emaarco/bpmn-to-code/issues/345)) ([aff1aad](https://github.com/emaarco/bpmn-to-code/commit/aff1aad218501aa35c99c720db83074390b0fc3f))

## [2.0.4] - 2026-05-15

### Bug Fixes

- **web:** Prevent `<none>:<none>` dangling images on Docker rebuild (#330)
- **web:** Make Docker Exec tasks configuration-cache compatible (#339)

### Dependencies

- **deps:** Bump the backend-dependencies group with 16 updates (#329)
- **deps:** Bump the backend-dependencies group with 3 updates (#331)
- **deps-dev:** Bump vue from 3.5.33 to 3.5.34 in /docs in the docs-dependencies group (#335)
- **deps:** Bump the backend-dependencies group with 2 updates (#334)

### Documentation

- Add module x language support matrix (#336)

### Features

- Add dry-run support to publish pipelines (#328)

### Refactor

- **core:** Use multi-dollar raw strings for Kotlin delegate expressions (#338)

## [2.0.3] - 2026-04-28

### Bug Fixes

- **gradle:** Inject JaCoCo agent into TestKit daemon to fix zero coverage (#322)
- **kotlin:** Escape double quotes in generated conditions (2.0.3) (#327)

## [2.0.2] - 2026-04-27

### Bug Fixes

- Preserve additionalInputVariables across variants (#324)

### Features

- Verbose model output for Gradle and Maven plugins (#325)

### Tests

- Cover additionalInputVariables on non-interrupting message start event in event subprocess (#321)

## [2.0.1] - 2026-04-27

### Bug Fixes

- Escape quotes in generated Kotlin BpmnFlow condition (#318)

### Features

- Use $$"..." syntax for generated BpmnFlow conditions (#320)

## [2.0.0] - 2026-04-26

### Bug Fixes

- One parameter per line in ModelInstanceUtils constructors
- Include escalations in model merge and sort (#242)
- Resolve discrepancies between BPMN models and generated API outputs (#256)
- Remove duplicate flat variables from generated Variables API (#265)
- Consistent indentation for Flows/Relations in generated Kotlin output (#282)
- Resolve maven publish build service classloader conflict (#284)
- Add maven publishing config to bpmn-to-code-core (#285)
- Normalize whitespace in BPMN element names (#297)
- PublishToMavenLocal and generateBpmnModelJson failures (#300)
- Hide include-sources toggle in JSON export tab (#305)
- Address high-severity findings from repo analysis (#306)
- Address findings from repo analysis (#307)
- Close file descriptors by storing BPMN content as ByteArray (#311)
- **ci:** Post coverage comment even when coverage check fails (#312)
- Eliminate double ByteArrayInputStream and document runCatching guard (#314)
- **ci:** Enable artifact signing and fix Docker config cache for release (#316)

### CI

- Fix path filters for backend and docs build workflows (#203)
- Use job-level conditional for path filtering (#204)

### Dependencies

- **deps:** Bump the backend-dependencies group with 3 updates (#201)
- **deps-dev:** Bump vue from 3.5.31 to 3.5.32 in /docs/website in the docs-dependencies group (#202)
- **deps:** Bump the backend-dependencies group with 2 updates (#258)
- **deps:** Bump org.apache.maven:maven-plugin-api from 3.9.14 to 3.9.15 in the backend-dependencies group (#273)
- **deps:** Bump actions/upload-pages-artifact from 4 to 5 in the github-actions group (#276)
- **deps-dev:** Bump vue from 3.5.32 to 3.5.33 in /docs in the docs-dependencies group (#275)

### Documentation

- Rework documentation for 2.0 three-pillar framing (#267)
- **website:** New three-pillar landing with compatibility matrix (#271)
- Consolidate all docs into single VitePress site (#274)
- Add preview gif to README (#301)
- Mark CIB7 as supported via Camunda 7 namespace (#302)
- Replace README preview GIF with higher quality version (#303)
- Why-focused KDoc + Dokka for published modules (#308)

### Features

- Standalone BPMN validation (#200)
- Add bpmn-to-code-testing module (#208)
- Enrich FlowNodeDefinition with properties and attachedToRef (#216)
- Add parentId to FlowNodeDefinition (#218)
- Extract sequence flows and incoming/outgoing references (#214)
- Generate JSON representation of BpmnModel (#223)
- Sort JSON flow nodes in process-flow order using DFS (#225)
- Typed element groups and per-element variables in generated API (#226)
- Add attachedElements to FlowNodeDefinition for bidirectional boundary event references (#228)
- Expose Flows and Relations in generated API (#219) (#232)
- Add isDefault flag to sequence flows (#234)
- Add EVENT_SUB_PROCESS type and escalation extraction (#237)
- Extract asyncBefore/asyncAfter/exclusive markers (#246)
- Add compensation event support (#245)
- Extract shared BPMN types into standalone files (#248)
- Extract variantName from process-level extension properties (#249)
- Variant-aware model merging for multi-file BPMN processes (#252)
- Enforce per-class test coverage (≥ 75%) via JaCoCo (#259)
- Extract displayName from BPMN flow nodes (#263)
- Include displayName of flow nodes in JSON output (#264)
- **web:** Merge JSON and Code API into single tabbed page (#269)
- **web:** Reskin web generator UI to match docs landing aesthetic (#272)
- AI-powered BPMN style guide skills (#262)
- Add scaffold-process-project skill (#185) (#266)
- Add v1-to-v2 migration skill and changelog (#283)
- Extend v1-to-v2 migration skill with useVersioning removal and LLM prompt (#286)
- Replace PROCESS_ENGINE string constant with typed BpmnEngine enum (#293)
- Add name fields and rename BpmnRelations edges (#289) (#295)
- Wrap leaf string constants in typed wrappers (#288) (#294)
- Split per-element Variables into Inputs/Outputs by direction (#296)
- Emit KDoc on generated ProcessApi nested objects and shared types (#298)
- Type variable direction via sealed VariableName; add toString to wrappers (#299)
- Publish shared types via bpmn-to-code-runtime artifact (#304)
- **quality:** Add Detekt static analysis (#313)
- **quality:** Extend JaCoCo coverage to all modules except MCP (#315)

### Refactor

- Remove API versioning feature (#199)
- Enrich domain model and validation context (#206)
- Merge ClasspathBpmnLoader and FilesystemBpmnLoader into BpmnResourceLoader (#210)
- Remove ValidateBpmnInMemoryPlugin (#211)
- Replace Ant DirectoryScanner with JDK PathMatcher and simplify BpmnResource (#212)
- Derive flat lists from enriched FlowNodes (#217)
- Remove element type groups from Elements in generated API (#229)
- Update docs for additionalVariables (#233)
- Adapt agents.md & multi-instance process (#235)
- Rename BPMN fixtures and sync event subprocess across engines (#236)
- Move Flows & Relations to end of generated API and JSON (#238)
- Rename customProperties to engineSpecificProperties (#251)
- Standardize unit test style across all modules (#254)
- Remove skills symlink from root (#278)
- Move skills from .agent to .claude (#279)
- Rename .claude/AGENT.md to CLAUDE.md (#280)
- Rename TaskTypes to ServiceTasks in generated API (#281)
- Improve CI quality gates (#310)

### Tests

- Validate syntax of generated Kotlin, Java, and JSON output (#260)

## [1.1.0] - 2026-04-04

### Bug Fixes

- **issue-177:** Eliminate all CI build warnings (#178)
- Docs build pipeline and link correction (#191)

### CI

- Add path filtering to build-backend workflow (#195)

### Dependencies

- **deps:** Bump gradle/actions from 4 to 6 in the github-actions group (#180)
- **deps:** Bump the backend-dependencies group with 14 updates (#179)

### Documentation

- Add 'Why a Plugin?' page with personal intro (#190)

### Features

- **skill:** Use AskUserQuestion for confirmation in create-ticket (#182)
- BPMN validation layer with built-in rules (#192)
- **skill:** Rewrite release skill with interactive version detection (#194)

## [1.0.0] - 2026-03-30

### Bug Fixes

- **docs:** Improve mobile responsiveness (#175)

### CI

- **issue-162:** Automate Maven Central release (#163)

### Features

- **issue-72:** Add context7.json for AI doc indexing (#166)
- **web:** Show version badge in web UI header (#168)
- **web:** Redesign UI with docs-aligned styling (#173)
- **issue-167:** Distribute user-facing skills as Claude Code plugin (#169)

### Refactor

- **issue-158:** Remove examples/ and add smoke tests (#165)

## [0.0.20] - 2026-03-26

### Bug Fixes

- **issue-159:** Add missing kotlin-logging and slf4j dependencies to plugin modules (#160)

### Dependencies

- **deps:** Bump the github-actions group with 4 updates (#155)

### Features

- **issue-153:** Add VitePress documentation site (#154)
- **issue-156:** Show latest version chip in docs navbar (#157)

## [0.0.19] - 2026-03-25

### Bug Fixes

- Resolve MCP server stdio issues and standardize fat JAR naming (#140)

### Build

- Init claude in gitHub (#109)

### Dependencies

- **deps:** Bump the backend-dependencies group with 2 updates (#106)
- **deps:** Bump the backend-dependencies group across 1 directory with 14 updates (#108)
- **deps:** Bump the backend-dependencies group with 4 updates (#110)
- **deps:** Bump the backend-dependencies group with 3 updates (#111)
- **deps:** Bump ch.qos.logback:logback-classic (#112)
- **deps:** Add github actions to dependa (#117)
- **deps:** Bump the github-actions group with 4 updates (#118)
- **deps:** Bump the backend-dependencies group with 13 updates
- **deps:** Bump the backend-dependencies group with 13 updates
- **deps:** Bump org.apache.maven:maven-plugin-api (#124)
- **deps:** Bump the backend-dependencies group with 6 updates (#138)
- **deps:** Bump the backend-dependencies group with 2 updates (#148)

### Documentation

- **issue-142:** Update README documentation for MCP module (#143)

### Features

- **issue-113:** Add call-activities to test-processes (#116)
- Update metadata for gradle-task
- **issue-113:** Export call activities to process-api (#120)
- **issue-114:** Add basic project-management skills (#122)
- **issue-130:** Local mcp for bpmn-to-code (#133)
- **issue-141:** Require clarification on MCP tool parameters (#144)
- **issue-125:** Add user-centric skills and skills index (#146)
- **issue-125:** Add migrate-to-bpmn-to-code-apis skill and rename skills (#147)
- **issue-115:** Extract variables from call activity in/out mappings (#150)
- **issue-149:** Extract additional variables from BPMN extension properties (#151)
- Add release skill to simplify publishing process (#152)

### Refactor

- **issue-114:** Add & test skills & prepare release (#126)
- **issue-121:** Improve bpmn-error handling (#129)
- **issue-132:** Migrate claude skills to agent directory (#134)
- **issue-131:** Replace println with proper logging (#136)
- **issue-137:** Centralize version management across modules (#139)

## [0.0.18] - 2026-01-15

### Build

- Prepare release for 0.0.18 (#105)

### Dependencies

- **deps:** Bump the backend-dependencies group with 4 updates (#102)
- **deps:** Bump the backend-dependencies group with 4 updates (#104)

### Features

- Extract variables from multi-instance tasks (#103)

## [0.0.17] - 2025-12-16

### Bug Fixes

- Prevent duplicates in processApi (#98)

### Features

- Modify release pipeline (#96)
- Release 0.0.17 (#99)

## [0.0.16] - 2025-12-15

### Dependencies

- **deps:** Bump the backend-dependencies group with 11 updates (#83)
- **deps:** Bump io.mockk:mockk in the backend-dependencies group (#85)
- **deps:** Bump ch.qos.logback:logback-classic (#89)

### Documentation

- Fix warnings for maven deployment (#82)

### Features

- Detect collisions in bpmn-model (#87)
- Sort process-api output (#92)
- Add engine to processApi (#93)
- Add issue templates #94
- Add issue templates (#95)

### Refactor

- Claim library in context7 (#84)

## [0.0.15] - 2025-11-30

### Bug Fixes

- Build docker image for amd64, add cors-config and revert logging
- File can be added again, after it got removed

### Dependencies

- **deps:** Bump the backend-dependencies group across 1 directory with 15 updates
- **deps:** Align version of bpmn-to-code web to others (#81)

### Documentation

- Add adr's for initial design decisions (#64)
- Describe variable extraction scope in adr
- Describe variable extraction scope in adr
- Update process api in main-readme to hold screaming-snake var-names
- Add best-practices
- Add best-practices
- Do not include api-docs in language-stats
- Adapt readme's for web-module

### Features

- Create process-api's without file-system
- Add module to use bpmn-to-code in the web

### Refactor

- Try to add logging per module
- Use version catalog for ktor-deps (#78)
- Use workerType as name for serviceTask variables (#79)
- Add extractor for operaton (#80)

## [0.0.14] - 2025-11-13

### Documentation

- Add ADR for process API property naming convention
- Create adr to describe naming decission

### Features

- Use UPPER_SNAKE_CASE for generated constants

## [0.0.13] - 2025-11-13

### Documentation

- Update README to reflect const val usage in generated API
- Add productivity and GitHub guidelines to CLAUDE.md

### Features

- Add Variables to generated API (#61)

## [0.0.12] - 2025-11-11

### Dependencies

- **deps:** Bump the backend-dependencies group with 2 updates
- **deps:** Bump the backend-dependencies group with 2 updates

### Documentation

- Update README

### Features

- Use const val for Kotlin API string constants (#60)

### Refactor

- Remove class-level supression

## [0.0.11] - 2025-10-22

### Bug Fixes

- Add ant to as api-dep to mvn dependencies

### Dependencies

- **deps:** Bump the backend-dependencies group with 3 updates
- **deps:** Bump org.camunda.bpm.model:camunda-bpmn-model

## [0.0.10] - 2025-10-02

### Bug Fixes

- Use should-write option to determine what objects are included in api

## [0.0.9] - 2025-10-02

### Features

- Dont show empty objects

## [0.0.8] - 2025-10-02

### Bug Fixes

- Load files outside of current root
- Update to 0.0.8

### Dependencies

- **deps:** Bump the backend-dependencies group across 1 directory with 2 updates

## [0.0.7] - 2025-08-18

### Bug Fixes

- Adapt maven build command

### Dependencies

- **deps:** Bump the backend-dependencies group with 4 updates
- **deps:** Bump the backend-dependencies group with 3 updates
- **deps:** Bump the backend-dependencies group with 2 updates
- **deps:** Bump the backend-dependencies group with 3 updates
- **deps:** Bump the backend-dependencies group with 2 updates
- **deps:** Bump the backend-dependencies group with 3 updates
- **deps:** Bump the backend-dependencies group with 2 updates
- **deps:** Bump org.jetbrains.kotlin.jvm

### Refactor

- Update interval of dependabot updates
- Remove maven task for dependabot
- Allow variable names in config
- Dependabot assignment rules
- Add claude.md

## [0.0.6] - 2025-06-12

### Bug Fixes

- Remove typo in docs
- Handle special-chars in process-config

### Refactor

- Change dependabot update interval

## [0.0.5-alpha] - 2025-05-14

### Dependencies

- **deps:** Bump org.jetbrains.kotlin.jvm
- **deps:** Bump io.mockk:mockk

### Features

- Allow to turn off versioning

## [0.0.4-alpha] - 2025-03-24

### Bug Fixes

- Change goal for maven plugin
- Docs for 0.0.4

### Features

- Add example-folder with gradle & maven examples

## [0.0.3-alpha] - 2025-03-18

### Features

- Rename process-api versioning-file

## [0.0.2-alpha] - 2025-03-18

### Features

- Add more elements to api

## [0.0.1-alpha] - 2025-03-17

### Dependencies

- **deps:** Bump the backend-dependencies group with 2 updates

### Features

- Create processApi files from bpmnModels
- Prepare for beta-publishing
