# Changelog

All notable changes to this project are documented here.
This file was bootstrapped from git history with [git-cliff](https://git-cliff.org);
new entries are appended automatically by [release-please](https://github.com/googleapis/release-please).

## [2.2.0](https://github.com/emaarco/bpmn-to-code/compare/v2.1.0...v2.2.0) (2026-05-29)


### Features

* add attachedElements to FlowNodeDefinition for bidirectional boundary event references ([#228](https://github.com/emaarco/bpmn-to-code/issues/228)) ([fb67b8a](https://github.com/emaarco/bpmn-to-code/commit/fb67b8a6ce38601de4416284f603e6a2a177017d))
* add bpmn-to-code-testing module ([#208](https://github.com/emaarco/bpmn-to-code/issues/208)) ([18b47d7](https://github.com/emaarco/bpmn-to-code/commit/18b47d79802a7e9b00ad3ca9bfe4c4aac34671a9))
* add compensation event support ([#245](https://github.com/emaarco/bpmn-to-code/issues/245)) ([f4e5a0f](https://github.com/emaarco/bpmn-to-code/commit/f4e5a0f14560ef1155db4ae6b51f4bd957043f8d))
* add dry-run support to publish pipelines ([#328](https://github.com/emaarco/bpmn-to-code/issues/328)) ([e50862d](https://github.com/emaarco/bpmn-to-code/commit/e50862d572c655e00fc19da431c0e86eacf2d5e4))
* add engine to processApi ([#93](https://github.com/emaarco/bpmn-to-code/issues/93)) ([b78a4e6](https://github.com/emaarco/bpmn-to-code/commit/b78a4e69a17faa043b1f067e346a917e7986eac7)), closes [#91](https://github.com/emaarco/bpmn-to-code/issues/91)
* add EVENT_SUB_PROCESS type and escalation extraction ([#237](https://github.com/emaarco/bpmn-to-code/issues/237)) ([281e496](https://github.com/emaarco/bpmn-to-code/commit/281e4968285d5598aa9852b51ab9971e43b2ca81))
* add example-folder with gradle & maven examples ([9aca1de](https://github.com/emaarco/bpmn-to-code/commit/9aca1de2e11c4ee740677357849d17462f3bd1e0))
* add isDefault flag to sequence flows ([#234](https://github.com/emaarco/bpmn-to-code/issues/234)) ([e09ccb5](https://github.com/emaarco/bpmn-to-code/commit/e09ccb5b1c9db2eb08b831a83683f6af41c7fc3a))
* add issue templates ([#95](https://github.com/emaarco/bpmn-to-code/issues/95)) ([98e0a6a](https://github.com/emaarco/bpmn-to-code/commit/98e0a6a110a03315eb6f92b2791a93a5407eb513))
* add issue templates [#94](https://github.com/emaarco/bpmn-to-code/issues/94) ([dd4351b](https://github.com/emaarco/bpmn-to-code/commit/dd4351b58486a3e936bb956131900316d7ca5a1a))
* add module to use bpmn-to-code in the web ([7a0ed69](https://github.com/emaarco/bpmn-to-code/commit/7a0ed69d4858d8bc352902aedf5b42619b720ac6))
* add more elements to api ([eeef51c](https://github.com/emaarco/bpmn-to-code/commit/eeef51c6ca9487dd98730cf77374c96124f4a73e))
* add name fields and rename BpmnRelations edges ([#289](https://github.com/emaarco/bpmn-to-code/issues/289)) ([#295](https://github.com/emaarco/bpmn-to-code/issues/295)) ([4c148e7](https://github.com/emaarco/bpmn-to-code/commit/4c148e7965774d4d209582f1925febfd2ca625bd))
* add parentId to FlowNodeDefinition ([#218](https://github.com/emaarco/bpmn-to-code/issues/218)) ([b123437](https://github.com/emaarco/bpmn-to-code/commit/b123437d7eea2e82a20da14567cacde0f7ab48f3))
* add release skill to simplify publishing process ([#152](https://github.com/emaarco/bpmn-to-code/issues/152)) ([8bf0b4e](https://github.com/emaarco/bpmn-to-code/commit/8bf0b4e1ab22a56c225bd6502ac39bd3eebc5748))
* add scaffold-process-project skill ([#185](https://github.com/emaarco/bpmn-to-code/issues/185)) ([#266](https://github.com/emaarco/bpmn-to-code/issues/266)) ([3da87aa](https://github.com/emaarco/bpmn-to-code/commit/3da87aa12dc31840b747c6048fe60bf85623bf5d))
* add v1-to-v2 migration skill and changelog ([#283](https://github.com/emaarco/bpmn-to-code/issues/283)) ([e9b92e2](https://github.com/emaarco/bpmn-to-code/commit/e9b92e2ee20207f5a2c3ed95b0a8bb032a851dbd))
* add Variables to generated API ([#61](https://github.com/emaarco/bpmn-to-code/issues/61)) ([cdad5dd](https://github.com/emaarco/bpmn-to-code/commit/cdad5ddc1ac6f7a7fc3835f16983d46d911d370e))
* AI-powered BPMN style guide skills ([#262](https://github.com/emaarco/bpmn-to-code/issues/262)) ([0cff8f0](https://github.com/emaarco/bpmn-to-code/commit/0cff8f0541560154adace4d9839a551f52668616))
* allow to turn off versioning ([0f1523d](https://github.com/emaarco/bpmn-to-code/commit/0f1523d805d87a48c0c35d30daa92720b4d74c70))
* BPMN validation layer with built-in rules ([#192](https://github.com/emaarco/bpmn-to-code/issues/192)) ([a207c44](https://github.com/emaarco/bpmn-to-code/commit/a207c446ac677bc25119095d72ce4f9e5c9bfd47))
* **core:** expose variable mapping expressions in the domain model ([#349](https://github.com/emaarco/bpmn-to-code/issues/349)) ([2cb7aeb](https://github.com/emaarco/bpmn-to-code/commit/2cb7aebd44799b5071c1572e51230703e848f9c1))
* create process-api's without file-system ([0167fae](https://github.com/emaarco/bpmn-to-code/commit/0167fae4ee8ffead83eb15b9f8acd0ebd41af813))
* create processApi files from bpmnModels ([a8434f6](https://github.com/emaarco/bpmn-to-code/commit/a8434f67c9fbf1a70b89a81e1274399633a63af0))
* detect collisions in bpmn-model ([#87](https://github.com/emaarco/bpmn-to-code/issues/87)) ([01c2dcf](https://github.com/emaarco/bpmn-to-code/commit/01c2dcf0b3b1487c60f199adca942b447d62150f))
* dont show empty objects ([12c27a1](https://github.com/emaarco/bpmn-to-code/commit/12c27a1d85c6bd8a68e7d0a685d25c470df2f809))
* emit KDoc on generated ProcessApi nested objects and shared types ([#298](https://github.com/emaarco/bpmn-to-code/issues/298)) ([c2b8e27](https://github.com/emaarco/bpmn-to-code/commit/c2b8e2768a550e272da5fa4802817897f0d690f9))
* enforce per-class test coverage (≥ 75%) via JaCoCo ([#259](https://github.com/emaarco/bpmn-to-code/issues/259)) ([2693e44](https://github.com/emaarco/bpmn-to-code/commit/2693e442fb6bf67b23cc6e6c1f9f0ca866be9bdb))
* enrich FlowNodeDefinition with properties and attachedToRef ([#216](https://github.com/emaarco/bpmn-to-code/issues/216)) ([59f8c8d](https://github.com/emaarco/bpmn-to-code/commit/59f8c8dd20617409d38071298f29b32bb675ccb0))
* expose Flows and Relations in generated API ([#219](https://github.com/emaarco/bpmn-to-code/issues/219)) ([#232](https://github.com/emaarco/bpmn-to-code/issues/232)) ([668596d](https://github.com/emaarco/bpmn-to-code/commit/668596dcea80a63b52a45a95368bf3b1392144ae))
* extend v1-to-v2 migration skill with useVersioning removal and LLM prompt ([#286](https://github.com/emaarco/bpmn-to-code/issues/286)) ([f76f438](https://github.com/emaarco/bpmn-to-code/commit/f76f43853e9f0607b919769a94f1e4a459db6485))
* extract asyncBefore/asyncAfter/exclusive markers ([#246](https://github.com/emaarco/bpmn-to-code/issues/246)) ([16c44f4](https://github.com/emaarco/bpmn-to-code/commit/16c44f46ac4d7bb3f228c75cbf8b79b2fba1d4f9))
* extract displayName from BPMN flow nodes ([#263](https://github.com/emaarco/bpmn-to-code/issues/263)) ([34e053e](https://github.com/emaarco/bpmn-to-code/commit/34e053e999d099aef7c4b3a15eb484d06e024bc7))
* extract sequence flows and incoming/outgoing references ([#214](https://github.com/emaarco/bpmn-to-code/issues/214)) ([1ada410](https://github.com/emaarco/bpmn-to-code/commit/1ada410f312dbe9e7b34df6194e7eee4ae2bfb07))
* extract shared BPMN types into standalone files ([#248](https://github.com/emaarco/bpmn-to-code/issues/248)) ([1dc4458](https://github.com/emaarco/bpmn-to-code/commit/1dc4458845321926be1964e101280658e5eddadc))
* extract variables from multi-instance tasks ([#103](https://github.com/emaarco/bpmn-to-code/issues/103)) ([02fe8b4](https://github.com/emaarco/bpmn-to-code/commit/02fe8b4c6508cd3fa1a0d8998c6bfa9c56b3f9c6))
* extract variantName from process-level extension properties ([#249](https://github.com/emaarco/bpmn-to-code/issues/249)) ([fea74a3](https://github.com/emaarco/bpmn-to-code/commit/fea74a33e3841ed6e635e684d4b755e6eb8f7fcd))
* generate JSON representation of BpmnModel ([#223](https://github.com/emaarco/bpmn-to-code/issues/223)) ([db56803](https://github.com/emaarco/bpmn-to-code/commit/db56803f9415d5d0fe417709620683d699a96066))
* include displayName of flow nodes in JSON output ([#264](https://github.com/emaarco/bpmn-to-code/issues/264)) ([b84a225](https://github.com/emaarco/bpmn-to-code/commit/b84a2250483f5040707e153c1c3442c5dcc6ea0d))
* **issue-113:** add call-activities to test-processes ([#116](https://github.com/emaarco/bpmn-to-code/issues/116)) ([d2b2364](https://github.com/emaarco/bpmn-to-code/commit/d2b2364ba81a40e1576d8a7c286faff143aa4503))
* **issue-113:** export call activities to process-api ([#120](https://github.com/emaarco/bpmn-to-code/issues/120)) ([b6e22d1](https://github.com/emaarco/bpmn-to-code/commit/b6e22d1ec70adacde3820cf2de65e330c9869e1c))
* **issue-114:** add basic project-management skills ([#122](https://github.com/emaarco/bpmn-to-code/issues/122)) ([eccaf94](https://github.com/emaarco/bpmn-to-code/commit/eccaf9472b286fee2ad493690220551cf26f6fa0))
* **issue-115:** extract variables from call activity in/out mappings ([#150](https://github.com/emaarco/bpmn-to-code/issues/150)) ([916a51d](https://github.com/emaarco/bpmn-to-code/commit/916a51dc6918c562183a02fa9f7f7df214b3e81f))
* **issue-125:** add migrate-to-bpmn-to-code-apis skill and rename skills ([#147](https://github.com/emaarco/bpmn-to-code/issues/147)) ([f13af91](https://github.com/emaarco/bpmn-to-code/commit/f13af916ac333247ac441156320338f4da8b097b))
* **issue-125:** add user-centric skills and skills index ([#146](https://github.com/emaarco/bpmn-to-code/issues/146)) ([01fbc46](https://github.com/emaarco/bpmn-to-code/commit/01fbc4666067eb2ec66d90c4b08192b3eeecef06))
* **issue-130:** local mcp for bpmn-to-code ([#133](https://github.com/emaarco/bpmn-to-code/issues/133)) ([df6efb0](https://github.com/emaarco/bpmn-to-code/commit/df6efb0ed42ca95490ca6b7c3f5193f8d9ebd21d))
* **issue-141:** require clarification on MCP tool parameters ([#144](https://github.com/emaarco/bpmn-to-code/issues/144)) ([43ee13f](https://github.com/emaarco/bpmn-to-code/commit/43ee13f375e7266c158b4e2e2432f7e76881de61))
* **issue-149:** extract additional variables from BPMN extension properties ([#151](https://github.com/emaarco/bpmn-to-code/issues/151)) ([0f50280](https://github.com/emaarco/bpmn-to-code/commit/0f50280c8f87ba6655da61fde46a55c406841b27))
* **issue-153:** add VitePress documentation site ([#154](https://github.com/emaarco/bpmn-to-code/issues/154)) ([d1bf8c0](https://github.com/emaarco/bpmn-to-code/commit/d1bf8c0fecae4441989f75a15e1c56fa1e9f3ba9))
* **issue-156:** show latest version chip in docs navbar ([#157](https://github.com/emaarco/bpmn-to-code/issues/157)) ([f1f5ce4](https://github.com/emaarco/bpmn-to-code/commit/f1f5ce4329af4991c19c01ea6b3599621695248a))
* **issue-167:** distribute user-facing skills as Claude Code plugin ([#169](https://github.com/emaarco/bpmn-to-code/issues/169)) ([6cda586](https://github.com/emaarco/bpmn-to-code/commit/6cda58661ff1b7630adacc5c32a4657530742949))
* **issue-72:** add context7.json for AI doc indexing ([#166](https://github.com/emaarco/bpmn-to-code/issues/166)) ([6d37106](https://github.com/emaarco/bpmn-to-code/commit/6d37106f05fad6da9528727f95f6299fbfff4461))
* modify release pipeline ([#96](https://github.com/emaarco/bpmn-to-code/issues/96)) ([478f483](https://github.com/emaarco/bpmn-to-code/commit/478f483b4e3a320df0a3a96404fb3659701e33e4))
* prepare for beta-publishing ([cf76201](https://github.com/emaarco/bpmn-to-code/commit/cf762014dc7e1d4301a1e56452704008adb7b448))
* publish shared types via bpmn-to-code-runtime artifact ([#304](https://github.com/emaarco/bpmn-to-code/issues/304)) ([7f0c455](https://github.com/emaarco/bpmn-to-code/commit/7f0c455cd18519a9efd930ad4a7da53af5bc0b57))
* **quality:** add Detekt static analysis ([#313](https://github.com/emaarco/bpmn-to-code/issues/313)) ([f93f8d3](https://github.com/emaarco/bpmn-to-code/commit/f93f8d3ecd5b33f7bfdd440bdc3a83a0e3dfb602))
* **quality:** extend JaCoCo coverage to all modules except MCP ([#315](https://github.com/emaarco/bpmn-to-code/issues/315)) ([b408cb9](https://github.com/emaarco/bpmn-to-code/commit/b408cb9b976b613eaf1d47e0cc4711acc50613fa))
* release 0.0.17 ([#99](https://github.com/emaarco/bpmn-to-code/issues/99)) ([0c8c91e](https://github.com/emaarco/bpmn-to-code/commit/0c8c91ef9e8afc47f8998caa787900e78e16848a))
* rename process-api versioning-file ([e6054c5](https://github.com/emaarco/bpmn-to-code/commit/e6054c5d0f98a9f643c3f7d94f5736689970784e))
* replace PROCESS_ENGINE string constant with typed BpmnEngine enum ([#293](https://github.com/emaarco/bpmn-to-code/issues/293)) ([acb80bf](https://github.com/emaarco/bpmn-to-code/commit/acb80bf7189ef47cc1235a04847e665f5508c61d))
* **skill:** rewrite release skill with interactive version detection ([#194](https://github.com/emaarco/bpmn-to-code/issues/194)) ([3ce9edf](https://github.com/emaarco/bpmn-to-code/commit/3ce9edff7fb70c69136224d10244c29539b1a9a1))
* **skill:** use AskUserQuestion for confirmation in create-ticket ([#182](https://github.com/emaarco/bpmn-to-code/issues/182)) ([a3b214f](https://github.com/emaarco/bpmn-to-code/commit/a3b214fc92f888fd39c33a3b6cf80d35d0310929))
* sort JSON flow nodes in process-flow order using DFS ([#225](https://github.com/emaarco/bpmn-to-code/issues/225)) ([e2e2ed2](https://github.com/emaarco/bpmn-to-code/commit/e2e2ed29ac21e90ab07e9f633fe9c7b4780b6f24))
* sort process-api output ([#92](https://github.com/emaarco/bpmn-to-code/issues/92)) ([a376ed9](https://github.com/emaarco/bpmn-to-code/commit/a376ed9fb900aa2fc9a13de344da9ac4299a8f77)), closes [#90](https://github.com/emaarco/bpmn-to-code/issues/90)
* split per-element Variables into Inputs/Outputs by direction ([#296](https://github.com/emaarco/bpmn-to-code/issues/296)) ([bc8ad46](https://github.com/emaarco/bpmn-to-code/commit/bc8ad4602c853dbc21d03df70b335e6cbb305806))
* standalone BPMN validation ([#200](https://github.com/emaarco/bpmn-to-code/issues/200)) ([23e56b3](https://github.com/emaarco/bpmn-to-code/commit/23e56b392d85cd76fcdf3137858e3b1130d19154))
* type variable direction via sealed VariableName; add toString to wrappers ([#299](https://github.com/emaarco/bpmn-to-code/issues/299)) ([565309c](https://github.com/emaarco/bpmn-to-code/commit/565309cb42dfac170e7e977690a6c0ddac9be262))
* typed element groups and per-element variables in generated API ([#226](https://github.com/emaarco/bpmn-to-code/issues/226)) ([98fd223](https://github.com/emaarco/bpmn-to-code/commit/98fd223ae045a0cbde4ebc14c8772a1c0a98669e))
* update metadata for gradle-task ([9e721dc](https://github.com/emaarco/bpmn-to-code/commit/9e721dc2c35951ac03ce68de7768f63b6b5cf7b0))
* use $$"..." syntax for generated BpmnFlow conditions ([#320](https://github.com/emaarco/bpmn-to-code/issues/320)) ([72449e0](https://github.com/emaarco/bpmn-to-code/commit/72449e01b461cc44a357944c49ca2d16dc66d08b))
* use const val for Kotlin API string constants ([#60](https://github.com/emaarco/bpmn-to-code/issues/60)) ([c9f9563](https://github.com/emaarco/bpmn-to-code/commit/c9f9563633707a12e5806c0cac170410e089ac2c))
* use UPPER_SNAKE_CASE for generated constants ([0a4de00](https://github.com/emaarco/bpmn-to-code/commit/0a4de006097fe5e84990bf1b10724561cbbfbe76))
* variant-aware model merging for multi-file BPMN processes ([#252](https://github.com/emaarco/bpmn-to-code/issues/252)) ([2700e7c](https://github.com/emaarco/bpmn-to-code/commit/2700e7c94439a66ebc7e5144936c03d64297ef1b))
* verbose model output for Gradle and Maven plugins ([#325](https://github.com/emaarco/bpmn-to-code/issues/325)) ([f1ddc1d](https://github.com/emaarco/bpmn-to-code/commit/f1ddc1d8f2cb8c73736a312cb2db2af2e7e2e535))
* **web:** merge JSON and Code API into single tabbed page ([#269](https://github.com/emaarco/bpmn-to-code/issues/269)) ([1a1015e](https://github.com/emaarco/bpmn-to-code/commit/1a1015eb573d80c5b6080fe7acaa7486c380e1d8))
* **web:** redesign UI with docs-aligned styling ([#173](https://github.com/emaarco/bpmn-to-code/issues/173)) ([11f69cf](https://github.com/emaarco/bpmn-to-code/commit/11f69cf1d300dfbe14f223cb72460c67b7ac92ce))
* **web:** reskin web generator UI to match docs landing aesthetic ([#272](https://github.com/emaarco/bpmn-to-code/issues/272)) ([c638fd6](https://github.com/emaarco/bpmn-to-code/commit/c638fd69b033fdd1a8900e82d5cc93cb144ae41d))
* **web:** show version badge in web UI header ([#168](https://github.com/emaarco/bpmn-to-code/issues/168)) ([b9128a7](https://github.com/emaarco/bpmn-to-code/commit/b9128a7fb1f5a902e56546d7c10917d55a9880e9))
* wrap leaf string constants in typed wrappers ([#288](https://github.com/emaarco/bpmn-to-code/issues/288)) ([#294](https://github.com/emaarco/bpmn-to-code/issues/294)) ([3fbeeb0](https://github.com/emaarco/bpmn-to-code/commit/3fbeeb05d7f9920e0f1b184ae263922218290269))


### Bug Fixes

* adapt maven build command ([f11fc72](https://github.com/emaarco/bpmn-to-code/commit/f11fc72554f9c00644c472287742d3773678757b))
* add ant to as api-dep to mvn dependencies ([8c81efe](https://github.com/emaarco/bpmn-to-code/commit/8c81efe7510cecf46b3283f21ec64cd0755e341b))
* add maven publishing config to bpmn-to-code-core ([#285](https://github.com/emaarco/bpmn-to-code/issues/285)) ([1cceaa7](https://github.com/emaarco/bpmn-to-code/commit/1cceaa758df298c4a90d3954d673e476d1fff267))
* address findings from repo analysis ([#307](https://github.com/emaarco/bpmn-to-code/issues/307)) ([e4db6bd](https://github.com/emaarco/bpmn-to-code/commit/e4db6bdfead8f123845e5c8f594100cb0783d584))
* address high-severity findings from repo analysis ([#306](https://github.com/emaarco/bpmn-to-code/issues/306)) ([f821ff7](https://github.com/emaarco/bpmn-to-code/commit/f821ff7ae0a35c0828f9960d56f403d3e93e4da6))
* build docker image for amd64, add cors-config and revert logging ([43eedeb](https://github.com/emaarco/bpmn-to-code/commit/43eedeb98740b7e55531610d655831a815487ed0))
* change goal for maven plugin ([f704074](https://github.com/emaarco/bpmn-to-code/commit/f70407468db296317978d369490ff1f00e66ef37))
* **ci:** enable artifact signing and fix Docker config cache for release ([#316](https://github.com/emaarco/bpmn-to-code/issues/316)) ([ff84ff6](https://github.com/emaarco/bpmn-to-code/commit/ff84ff686a71b27feccce1b7ca6a1bffa943a926))
* **ci:** post coverage comment even when coverage check fails ([#312](https://github.com/emaarco/bpmn-to-code/issues/312)) ([69ce331](https://github.com/emaarco/bpmn-to-code/commit/69ce331fdb7933625b4a24398f9bff0304fd578e))
* close file descriptors by storing BPMN content as ByteArray ([#311](https://github.com/emaarco/bpmn-to-code/issues/311)) ([81b19e5](https://github.com/emaarco/bpmn-to-code/commit/81b19e55df4487de59e542e5dc02ff7c02a71e6e))
* consistent indentation for Flows/Relations in generated Kotlin output ([#282](https://github.com/emaarco/bpmn-to-code/issues/282)) ([fcd34f0](https://github.com/emaarco/bpmn-to-code/commit/fcd34f05986af92b51dfd90271b1b0ef82c566d6))
* docs build pipeline and link correction ([#191](https://github.com/emaarco/bpmn-to-code/issues/191)) ([04477be](https://github.com/emaarco/bpmn-to-code/commit/04477be11af9f85fd83617d87350de45fd4a7a12))
* docs for 0.0.4 ([4576af5](https://github.com/emaarco/bpmn-to-code/commit/4576af5543c4d1a09bd05c491adcfc82d2a83925))
* **docs:** improve mobile responsiveness ([#175](https://github.com/emaarco/bpmn-to-code/issues/175)) ([b2d4f0c](https://github.com/emaarco/bpmn-to-code/commit/b2d4f0cb1cf407f992aa537b480647671214b42f)), closes [#174](https://github.com/emaarco/bpmn-to-code/issues/174)
* eliminate double ByteArrayInputStream and document runCatching guard ([#314](https://github.com/emaarco/bpmn-to-code/issues/314)) ([e8dab5f](https://github.com/emaarco/bpmn-to-code/commit/e8dab5fab8616316fd13aacff97863d20bee3f56))
* escape quotes in generated Kotlin BpmnFlow condition ([#318](https://github.com/emaarco/bpmn-to-code/issues/318)) ([22115b6](https://github.com/emaarco/bpmn-to-code/commit/22115b6ac78c1474a0f861287039ebaf82b5f4c1)), closes [#317](https://github.com/emaarco/bpmn-to-code/issues/317)
* file can be added again, after it got removed ([cbbf266](https://github.com/emaarco/bpmn-to-code/commit/cbbf266c653c8e1c8b59a22a749233be91e9b594))
* **gradle:** inject JaCoCo agent into TestKit daemon to fix zero coverage ([#322](https://github.com/emaarco/bpmn-to-code/issues/322)) ([5099290](https://github.com/emaarco/bpmn-to-code/commit/5099290d4633ae1cce7589ec2993ae921d6d5d88))
* handle special-chars in process-config ([01b61d0](https://github.com/emaarco/bpmn-to-code/commit/01b61d04ed47ed3c21020a12aebdafbb9e9edafe))
* hide include-sources toggle in JSON export tab ([#305](https://github.com/emaarco/bpmn-to-code/issues/305)) ([d46ff69](https://github.com/emaarco/bpmn-to-code/commit/d46ff69c65f566ead677910be0ec61ebb9d154b2))
* include escalations in model merge and sort ([#242](https://github.com/emaarco/bpmn-to-code/issues/242)) ([885af9a](https://github.com/emaarco/bpmn-to-code/commit/885af9a2711a6b19ed668fc025d215e45ee4eaca))
* **issue-159:** add missing kotlin-logging and slf4j dependencies to plugin modules ([#160](https://github.com/emaarco/bpmn-to-code/issues/160)) ([92108f6](https://github.com/emaarco/bpmn-to-code/commit/92108f69d13ae8fc6634b86c240faa3b5cccb290))
* **issue-177:** eliminate all CI build warnings ([#178](https://github.com/emaarco/bpmn-to-code/issues/178)) ([2e62765](https://github.com/emaarco/bpmn-to-code/commit/2e62765dfe4ce141f13fa4589a6e05d9eb18d491))
* **kotlin:** escape double quotes in generated conditions (2.0.3) ([#327](https://github.com/emaarco/bpmn-to-code/issues/327)) ([2636991](https://github.com/emaarco/bpmn-to-code/commit/2636991b94f0de074a4ff4c7dc571c8de6636f4a))
* load files outside of current root ([53fab1b](https://github.com/emaarco/bpmn-to-code/commit/53fab1b41222f155d8bb53014cbfa33f17e63ad7))
* normalize whitespace in BPMN element names ([#297](https://github.com/emaarco/bpmn-to-code/issues/297)) ([c5208f8](https://github.com/emaarco/bpmn-to-code/commit/c5208f82527cdf51add3c20f57250b63cf35901f))
* one parameter per line in ModelInstanceUtils constructors ([20719fc](https://github.com/emaarco/bpmn-to-code/commit/20719fca6e05473d236cdf86145e16ad54bc5c3e))
* preserve additionalInputVariables across variants ([#324](https://github.com/emaarco/bpmn-to-code/issues/324)) ([6eca584](https://github.com/emaarco/bpmn-to-code/commit/6eca5844d29f205179b494e86d61dec7bc5b77d0)), closes [#323](https://github.com/emaarco/bpmn-to-code/issues/323)
* prevent duplicates in processApi ([#98](https://github.com/emaarco/bpmn-to-code/issues/98)) ([8ec1f65](https://github.com/emaarco/bpmn-to-code/commit/8ec1f6559bb42f3ba10f10322bd4645735c1e007))
* publishToMavenLocal and generateBpmnModelJson failures ([#300](https://github.com/emaarco/bpmn-to-code/issues/300)) ([d3e2993](https://github.com/emaarco/bpmn-to-code/commit/d3e2993a64a3f228ba1c5bd580a064ca11abe6c6))
* **release-please:** disable component prefix in tag ([#343](https://github.com/emaarco/bpmn-to-code/issues/343)) ([11483e1](https://github.com/emaarco/bpmn-to-code/commit/11483e1243c726b4e89f0a20cbf2bd37dc5a1880))
* **release-please:** use block markers for gradle.properties ([#345](https://github.com/emaarco/bpmn-to-code/issues/345)) ([aff1aad](https://github.com/emaarco/bpmn-to-code/commit/aff1aad218501aa35c99c720db83074390b0fc3f))
* remove duplicate flat variables from generated Variables API ([#265](https://github.com/emaarco/bpmn-to-code/issues/265)) ([b56a3e2](https://github.com/emaarco/bpmn-to-code/commit/b56a3e2cdd919d06b86b616c89648304046385ca))
* remove typo in docs ([e4b9a3a](https://github.com/emaarco/bpmn-to-code/commit/e4b9a3a75502108714fdba78247e904d82bcdf73))
* resolve discrepancies between BPMN models and generated API outputs ([#256](https://github.com/emaarco/bpmn-to-code/issues/256)) ([4573eb1](https://github.com/emaarco/bpmn-to-code/commit/4573eb147de2b400d343a706f932fa22f653d7c7))
* resolve maven publish build service classloader conflict ([#284](https://github.com/emaarco/bpmn-to-code/issues/284)) ([f05100e](https://github.com/emaarco/bpmn-to-code/commit/f05100e566005565599481bd92b96803009444f6))
* resolve MCP server stdio issues and standardize fat JAR naming ([#140](https://github.com/emaarco/bpmn-to-code/issues/140)) ([9891726](https://github.com/emaarco/bpmn-to-code/commit/989172670e627cc6ada4d56c3f83e4c0b379e647))
* update to 0.0.8 ([8324d6a](https://github.com/emaarco/bpmn-to-code/commit/8324d6acc1f705ef9e2a403cecdd5f38c2ddfb04))
* use should-write option to determine what objects are included in api ([a601abf](https://github.com/emaarco/bpmn-to-code/commit/a601abf104600e69fb4dfe0ec73e9f635d6efd0c))
* **web:** make Docker Exec tasks configuration-cache compatible ([#339](https://github.com/emaarco/bpmn-to-code/issues/339)) ([684e39c](https://github.com/emaarco/bpmn-to-code/commit/684e39cbfa03085a790e0b745b6973fb93204470))
* **web:** prevent &lt;none&gt;:&lt;none&gt; dangling images on Docker rebuild ([#330](https://github.com/emaarco/bpmn-to-code/issues/330)) ([7406bdd](https://github.com/emaarco/bpmn-to-code/commit/7406bdd913517643f8d233d9a2e54bfa77e06655))

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
