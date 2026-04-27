# bpmn-to-code-mcp

An [MCP (Model Context Protocol)](https://modelcontextprotocol.io/) server that exposes bpmn-to-code's code generation as a tool for AI assistants.
Feed it BPMN XML and get type-safe process API code back — directly inside your AI-powered editor or chat.

> **Experimental:** This module is an experiment to explore MCP in practice. It is not officially supported for production use alongside the Gradle, Maven, and Web modules. If you're interested in using it, please [leave feedback](https://github.com/emaarco/bpmn-to-code/issues) so it can be promoted to officially supported.

## Why MCP?

- **No project setup needed** — generate process API code without adding a Gradle or Maven plugin
- **Works with any MCP client** — Claude Code, Claude Desktop, Cursor, Windsurf, and more
- **Same core engine** — 100% reuses `bpmn-to-code-core`, identical output to the build plugins

## Tool: `generate_process_api`

Generates type-safe process API code from BPMN XML.

The tool description instructs the AI client to **ask the user** for `outputLanguage`, `processEngine`, and `packagePath` when they are not already clear from context, rather than silently applying defaults.

| Parameter        | Required | Default               | Description                                     |
|------------------|----------|-----------------------|-------------------------------------------------|
| `bpmnXml`        | yes      | —                     | Raw BPMN XML content                            |
| `processName`    | yes      | —                     | Process identifier for naming generated classes |
| `outputLanguage` | no       | `KOTLIN`              | `KOTLIN` or `JAVA`                              |
| `processEngine`  | no       | `ZEEBE`               | `ZEEBE`, `CAMUNDA_7`, or `OPERATON`             |
| `packagePath`    | no       | `com.example.process` | Target package for generated code               |

## Getting Started

Run the MCP server locally as a JAR. The MCP client launches and communicates with it via stdin/stdout.

**Prerequisites:** JDK 21+

**Build the fat JAR:**
```bash
./gradlew :bpmn-to-code-mcp:shadowJar
```

The JAR is created at `bpmn-to-code-mcp/build/libs/bpmn-to-code-mcp-<version>-all.jar`.

**Test it manually:**
```bash
java -jar bpmn-to-code-mcp/build/libs/bpmn-to-code-mcp-2.0.1-all.jar
```

### Configuring Your MCP Client

#### Claude Code

Add to your `.claude/settings.json` (project-level) or `~/.claude/settings.json` (global):

```json
{
  "mcpServers": {
    "bpmn-to-code": {
      "command": "java",
      "args": ["-jar", "/absolute/path/to/bpmn-to-code-mcp-2.0.1-all.jar"]
    }
  }
}
```

#### Claude Desktop

Add to your Claude Desktop config (`~/Library/Application Support/Claude/claude_desktop_config.json` on macOS):

```json
{
  "mcpServers": {
    "bpmn-to-code": {
      "command": "java",
      "args": ["-jar", "/absolute/path/to/bpmn-to-code-mcp-2.0.1-all.jar"]
    }
  }
}
```

#### Cursor / Windsurf / Other MCP Clients

Most MCP clients follow a similar pattern — point them at the JAR using stdio transport:

- **Command:** `java`
- **Args:** `["-jar", "/absolute/path/to/bpmn-to-code-mcp-2.0.1-all.jar"]`

Refer to your client's documentation for the exact configuration location.
