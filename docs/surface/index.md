# 🤖 Surface — AI-Ready by Design

The Surface pillar makes your process structure accessible to AI agents, code reviewers, and your build toolchain — deterministically, on every run.

Three capabilities, all derived from the same BPMN model:

## JSON Export <Badge type="warning" text="beta" />

Generates a structured JSON file alongside the Kotlin/Java API. Every flow node, sequence flow, message, signal, and error — sorted in process-flow order, stripped of layout noise.

Designed for AI agents (paste or stream into your assistant's context), code reviewers (readable diffs without opening Camunda Modeler), and CI pipelines (query or validate process structure programmatically).

[See JSON Export →](/surface/json)

## Agent Skills <Badge type="warning" text="beta" />

Reusable Claude Code skills that automate common workflows: setting up the plugin in a new project, migrating hardcoded strings to the generated API, and scaffolding worker stubs.

Install once, use across projects. Works with Claude Code out of the box.

[See Agent Skills →](/skills/)

## MCP Server

Exposes bpmn-to-code as a tool for AI assistants via the Model Context Protocol. Paste BPMN XML into your editor conversation and get a typed Kotlin or Java API back — no project setup, no build plugin required.

Runs locally as a JAR.

[See MCP Server →](/mcp/)
