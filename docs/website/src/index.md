---
layout: home

hero:
  name: bpmn-to-code
  text: Your BPMN model in your toolchain.
  tagline: "compiler-safe · validated · AI-ready"
  actions:
    - theme: brand
      text: ✨ Try in Browser
      link: https://bpmn-to-code.miragon.io/static/index.html

features:
  - title: 🔒 Generate — Compile-Time Safety
    details: Type-safe constants generated from your BPMN model. Rename an element and the compiler tells you every place that breaks. No more silent runtime failures from hardcoded strings.
    link: /getting-started/gradle
    linkText: Get started
  - title: ✅ Validate — Architecture Rules for BPMN
    details: Like ArchUnit, but for your process models. Catch missing implementations, undefined timers, and naming violations at build time — before they reach production. Early access — API may evolve.
    link: /validate/
    linkText: Explore validation
  - title: 🤖 Surface — AI-Ready by Design
    details: Exports structured JSON for AI agents and code reviewers. Ships with Agent Skills for setup and migration, and an MCP Server that generates typed APIs from BPMN inside your AI conversation.
    link: /surface/
    linkText: See AI integration
  - title: 🔧 Works With Your Stack
    details: Zeebe, Camunda 7, and Operaton. Java and Kotlin. Gradle and Maven. Fits into what you already use.
    link: /engines/zeebe
    linkText: See supported engines
---

## The problem it solves

BPMN-based process automation relies on string references scattered across your codebase. When someone renames an element in the BPMN model, nothing warns you until runtime.

::: code-group

```kotlin [❌ Before — hardcoded strings]
// Scattered across your codebase, no compiler help
client.newCreateInstanceCommand()
    .bpmnProcessId("newsletterSubscription")      // typo? runtime error.
    .send()

client.newPublishMessageCommand()
    .messageName("Message_FormSubmitted")          // renamed in BPMN? silent failure.
    .correlationKey(subscriptionId)
    .send()

@JobWorker(type = "newsletter.sendConfirmationMail")  // deleted task? no warning.
fun sendConfirmationMail() { /* ... */ }
```

```kotlin [✅ After — generated Process API]
// Generated from your BPMN model. Rename an element → compiler error.
client.newCreateInstanceCommand()
    .bpmnProcessId(NewsletterSubscriptionProcessApi.PROCESS_ID)
    .send()

client.newPublishMessageCommand()
    .messageName(NewsletterSubscriptionProcessApi.Messages.MESSAGE_FORM_SUBMITTED)
    .correlationKey(subscriptionId)
    .send()

@JobWorker(type = NewsletterSubscriptionProcessApi.TaskTypes.NEWSLETTER_SEND_CONFIRMATION_MAIL)
fun sendConfirmationMail() { /* ... */ }
```

:::
