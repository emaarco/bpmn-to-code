---
layout: home

hero:
  name: bpmn-to-code
  text: Your BPMN model changes. Your code doesn't break.
  tagline: Stop hardcoding element IDs, messages, and worker types. Generate a type-safe Process API that stays in sync with your BPMN models.
  actions:
    - theme: brand
      text: ✨ Try in Browser
      link: https://bpmn-to-code.miragon.io/static/index.html
    - theme: alt
      text: Why bpmn-to-code
      link: /overview/why
    - theme: alt
      text: Get Started (Gradle)
      link: /getting-started/gradle
    - theme: alt
      text: Get Started (Maven)
      link: /getting-started/maven

features:
  - title: 🛡️ Compile-Time Safety
    details: Rename a BPMN element and the compiler tells you every spot that needs updating. No more silent runtime failures.
  - title: 🔧 Works With Your Stack
    details: Zeebe, Camunda 7, and Operaton. Java and Kotlin. Gradle and Maven. Fits into what you already use.
  - title: 🌐 Try It Instantly
    details: Paste your BPMN into the web app and see the generated code in seconds. No installation required.
  - title: ⚡ Minimal Setup Effort
    details: Available as a Gradle and Maven plugin. Also serves AI skills that auto-configure the plugin and migrate your existing code.
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

