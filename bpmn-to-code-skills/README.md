# bpmn-to-code Skills Plugin

Claude Code plugin with AI skills for setting up and using the [bpmn-to-code](https://github.com/emaarco/bpmn-to-code) Gradle/Maven plugin.

## Installation

### Via Marketplace

```bash
# Register the marketplace
/plugin marketplace add emaarco/bpmn-to-code

# Install the plugin
/plugin install bpmn-to-code
```

### Direct Install

```bash
/plugin install bpmn-to-code@emaarco/bpmn-to-code
```

## Included Skills

| Skill | Description |
|-------|-------------|
| `setup-bpmn-to-code-gradle` | Set up the Gradle plugin in an existing project. Detects project structure, BPMN files, and output language. |
| `setup-bpmn-to-code-maven` | Set up the Maven plugin in an existing project. Adds plugin configuration to `pom.xml`. |
| `migrate-to-bpmn-to-code-apis` | Replace hardcoded BPMN strings with references to the generated Process API. |

## Usage

Once installed, skills are available as slash commands:

```
/bpmn-to-code:setup-bpmn-to-code-gradle
/bpmn-to-code:setup-bpmn-to-code-maven
/bpmn-to-code:migrate-to-bpmn-to-code-apis
```

Or describe what you need and Claude will invoke the appropriate skill automatically.
