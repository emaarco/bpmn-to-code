---
title: Architecture Decision Records
description: Index of all architecture decisions made in the bpmn-to-code project.
---

Architecture Decision Records document significant architectural choices made in this project. Each ADR captures context, decision, consequences, and alternatives considered.

## Core Architecture
- [ADR 001: Hexagonal Architecture](001-hexagonal-architecture) — Clean architecture with ports and adapters
- [ADR 002: Model Merging](002-model-merging) — Combining multiple BPMN files into single API

## Code Generation
- [ADR 003: Generated API Structure](003-generated-api-structure) — Structure of generated Process APIs
- [ADR 005: Strategy Pattern for Code Generation](005-strategy-pattern-code-generation) — Language-specific builders (Java/Kotlin)

## Multi-Engine Support
- [ADR 004: Strategy Pattern for Multi-Engine](004-strategy-pattern-multi-engine) — Supporting Camunda 7 and Zeebe
- [ADR 010: Operaton Namespace-Only Extractor](010-operaton-namespace-only-extractor) — Dedicated extractor for Operaton engine

## Features
- [ADR 006: File-Based Versioning](006-file-based-versioning) — API versioning strategy
- [ADR 007: Variable Extraction Scope](007-variable-extraction-scope) — Explicit variable definitions only
- [ADR 011: Variable Name Collision Detection](011-variable-name-collision-detection) — Detecting and preventing ID normalization conflicts

## Web Module
- [ADR 008: Web Module for Browser-Based Access](008-web-module-for-browser-access) — Strategic decision for web application
- [ADR 009: Ktor with Static Frontend](009-ktor-static-frontend-single-module) — Technology choices for web module
