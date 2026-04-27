# 🤖 MCP Server

Generate Process API code from BPMN XML directly inside your AI editor — no project setup needed. The [MCP (Model Context Protocol)](https://modelcontextprotocol.io/) server runs locally and exposes bpmn-to-code as a tool for AI assistants.

::: warning Experimental
This module is experimental. [Leave feedback](https://github.com/emaarco/bpmn-to-code/issues) if you're using it.
:::

## How it works

Your AI assistant reads a BPMN file from your project, calls the `generate_process_api` tool, and returns the generated code — all in one conversation. The MCP server runs as a local JAR and communicates via stdin/stdout.

## Tool: `generate_process_api`

| Parameter | Required | Default | Description |
|-----------|----------|---------|-------------|
| `bpmnXml` | yes | — | Raw BPMN XML content |
| `processName` | yes | — | Process identifier for naming generated classes |
| `outputLanguage` | no | `KOTLIN` | `KOTLIN` or `JAVA` |
| `processEngine` | no | `ZEEBE` | `ZEEBE`, `CAMUNDA_7`, or `OPERATON` |
| `packagePath` | no | `com.example.process` | Target package for generated code |

The tool description instructs the AI client to ask the user for `outputLanguage`, `processEngine`, and `packagePath` when they're not clear from context.

## Setup

### 1. Build the JAR

**Prerequisites:** JDK 21+

```bash
./gradlew :bpmn-to-code-mcp:shadowJar
```

The JAR is created at `bpmn-to-code-mcp/build/libs/bpmn-to-code-mcp-<version>-all.jar`.

### 2. Configure your MCP client

Add the server config to your client. Replace the JAR path with the absolute path on your machine.

::: code-group

```json [Claude Code]
// .claude/settings.json (project) or ~/.claude/settings.json (global)
{
  "mcpServers": {
    "bpmn-to-code": {
      "command": "java",
      "args": ["-jar", "/absolute/path/to/bpmn-to-code-mcp-2.0.1-all.jar"]
    }
  }
}
```

```json [Claude Desktop (macOS)]
// ~/Library/Application Support/Claude/claude_desktop_config.json
{
  "mcpServers": {
    "bpmn-to-code": {
      "command": "java",
      "args": ["-jar", "/absolute/path/to/bpmn-to-code-mcp-2.0.1-all.jar"]
    }
  }
}
```

```json [Cursor / Windsurf / Other]
// Refer to your client's MCP configuration docs
{
  "mcpServers": {
    "bpmn-to-code": {
      "command": "java",
      "args": ["-jar", "/absolute/path/to/bpmn-to-code-mcp-2.0.1-all.jar"]
    }
  }
}
```

:::
