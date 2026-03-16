# bpmn-to-code-mcp

An [MCP (Model Context Protocol)](https://modelcontextprotocol.io/) server that exposes bpmn-to-code's code generation as a tool for AI assistants.
Feed it BPMN XML and get type-safe process API code back — directly inside your AI-powered editor or chat.

## Why MCP?

- **No project setup needed** — generate process API code without adding a Gradle or Maven plugin
- **Works with any MCP client** — Claude Code, Claude Desktop, Cursor, Windsurf, and more
- **Same core engine** — 100% reuses `bpmn-to-code-core`, identical output to the build plugins

## Tool: `generate_process_api`

Generates type-safe process API code from BPMN XML.

| Parameter        | Required | Default               | Description                                     |
|------------------|----------|-----------------------|-------------------------------------------------|
| `bpmnXml`        | yes      | —                     | Raw BPMN XML content                            |
| `processName`    | yes      | —                     | Process identifier for naming generated classes |
| `outputLanguage` | no       | `KOTLIN`              | `KOTLIN` or `JAVA`                              |
| `processEngine`  | no       | `ZEEBE`               | `ZEEBE`, `CAMUNDA_7`, or `OPERATON`             |
| `packagePath`    | no       | `com.example.process` | Target package for generated code               |

## Option 1: Local (stdio)

Run the MCP server locally as a JAR. The MCP client launches and communicates with it via stdin/stdout.

**Prerequisites:** JDK 21+

**Build the fat JAR:**
```bash
./gradlew :bpmn-to-code-mcp:shadowJar
```

The JAR is created at `bpmn-to-code-mcp/build/libs/bpmn-to-code-mcp-<version>-all.jar`.

**Test it manually:**
```bash
java -jar bpmn-to-code-mcp/build/libs/bpmn-to-code-mcp-0.0.19-all.jar
```

### Configuring Your MCP Client (stdio)

#### Claude Code

Add to your `.claude/settings.json` (project-level) or `~/.claude/settings.json` (global):

```json
{
  "mcpServers": {
    "bpmn-to-code": {
      "command": "java",
      "args": ["-jar", "/absolute/path/to/bpmn-to-code-mcp-0.0.19-all.jar"]
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
      "args": ["-jar", "/absolute/path/to/bpmn-to-code-mcp-0.0.19-all.jar"]
    }
  }
}
```

#### Cursor / Windsurf / Other MCP Clients

Most MCP clients follow a similar pattern — point them at the JAR using stdio transport:

- **Command:** `java`
- **Args:** `["-jar", "/absolute/path/to/bpmn-to-code-mcp-0.0.19-all.jar"]`

Refer to your client's documentation for the exact configuration location.

## Option 2: Remote (HTTP)

Run the MCP server as a remote HTTP service. Useful for shared environments, CI pipelines, or hosting on a server.

**Start with HTTP transport:**
```bash
java -jar bpmn-to-code-mcp-0.0.19-all.jar --http
```

**Custom port (default: 8080):**
```bash
java -jar bpmn-to-code-mcp-0.0.19-all.jar --http --port=9090
```

You can also use environment variables:
```bash
MCP_TRANSPORT=http MCP_PORT=9090 java -jar bpmn-to-code-mcp-0.0.19-all.jar
```

### Docker

**Build the image:**
```bash
./gradlew :bpmn-to-code-mcp:dockerBuild
```

**Run it:**
```bash
docker run -p 8080:8080 --rm emaarco/bpmn-to-code-mcp:latest
```

### Configuring Your MCP Client (HTTP)

Point your MCP client at the server's URL:

```json
{
  "mcpServers": {
    "bpmn-to-code": {
      "url": "http://localhost:8080/mcp"
    }
  }
}
```

Replace `localhost:8080` with the actual host and port if deployed remotely.

## Remote Usage

If you don't want to build locally, you can download the JAR from [GitHub Releases](https://github.com/emaarco/bpmn-to-code/releases) (once published) and point your MCP client config at the downloaded file.
