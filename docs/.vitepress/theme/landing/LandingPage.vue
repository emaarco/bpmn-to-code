<script setup lang="ts">
import { withBase } from 'vitepress'
import IdeShowcase from './IdeShowcase.vue'
import CompareCards from './CompareCards.vue'
import RunnerPair from './RunnerPair.vue'
import SkillCard from './SkillCard.vue'
import CompatCard from './CompatCard.vue'
import LandingFooter from './LandingFooter.vue'
import { ideTabs, generateCompare, surfaceCompare, validateRules, skills } from './data'
</script>

<template>
  <div class="landing-root">
    <section class="hero">
      <h1>
        Your BPMN. <span class="grad">In&nbsp;Code.</span><br />
        <span class="grad-p">Validated.</span> <span class="grad-g">AI&#8209;ready.</span>
      </h1>
      <p class="subtitle">
        Generate type-safe APIs, validate architecture rules, and make your BPMN models AI-ready &mdash; straight from your build.
      </p>
      <div class="actions">
        <a class="btn btn-primary" href="https://bpmn-to-code.miragon.io/static/index.html" target="_blank" rel="noopener"><span>&rarr;</span> Try in browser</a>
        <a class="btn btn-secondary" :href="withBase('/overview/why')">Learn more</a>
      </div>

      <IdeShowcase :tabs="ideTabs" />

      <CompatCard />
    </section>

    <!-- P1 — Generate -->
    <section class="problem">
      <div class="problem-intro">
        <span class="pillar-badge generate"><span class="num">1</span>Generate <span class="eyebrow">&middot; compile-time safety</span></span>
        <h2>BPMN strings break silently. <span class="grad">Until runtime.</span></h2>
        <p>Your Zeebe and Camunda code is full of references to BPMN elements &mdash; process IDs, message names, task types. Rename one in the modeler and nothing warns you until it fails in production. bpmn-to-code generates a type-safe Process API from your <code>.bpmn</code> files via <strong>Gradle</strong>, <strong>Maven</strong>, or the <strong>in-browser</strong> tool.</p>
        <a class="pillar-link" :href="withBase('/guide/generated-api')">Learn how the generated API works <span class="arrow">&rarr;</span></a>
      </div>
      <CompareCards
        before-pill="✗ Before"
        before-title="hardcoded strings"
        :before-code="generateCompare.before"
        after-pill="✓ After"
        after-title="generated Process API"
        :after-code="generateCompare.after"
      />
    </section>

    <!-- P2 — Validate -->
    <section class="problem">
      <div class="problem-intro">
        <span class="pillar-badge validate"><span class="num">2</span>Validate <span class="eyebrow">&middot; architecture rules</span></span>
        <h2>Process rules <span class="grad-p">erode between deploys.</span></h2>
        <p>Missing implementations, undefined timers, inconsistent naming &mdash; without architectural tests, BPMN quality drifts silently. The testing module ships as a JUnit-friendly rule set you drop into any project. Catch violations in your test suite, not in staging.</p>
        <a class="pillar-link" :href="withBase('/validate/')">See the validation rules <span class="arrow">&rarr;</span></a>
      </div>
      <RunnerPair
        :command="validateRules.command"
        :fail="validateRules.fail"
        fail-summary="4 violations"
        fail-note="build blocked"
        :pass="validateRules.pass"
        pass-summary="4 rules validated"
        pass-note="ready to ship"
      />
    </section>

    <!-- P3 — Surface -->
    <section class="problem">
      <div class="problem-intro">
        <span class="pillar-badge context"><span class="num">3</span>Surface <span class="eyebrow">&middot; context for your AI</span></span>
        <h2>Agents can&rsquo;t read your <span class="grad-ctx">.bpmn file.</span></h2>
        <p>Raw BPMN is 80% rendering metadata &mdash; shapes, bounds, waypoints. Agents waste context on pixels instead of process logic. bpmn-to-code emits a semantic JSON model built for AI context, not for diagrams, and exposes it over an MCP server your agent can query directly.</p>
        <a class="pillar-link" :href="withBase('/surface/')">Read the JSON surface spec <span class="arrow">&rarr;</span></a>
      </div>
      <CompareCards
        before-pill="⚠ raw BPMN"
        before-title="rendering metadata noise"
        :before-code="surfaceCompare.before"
        before-variant="warn"
        :scroll-before="true"
        after-pill="✓ bpmn-to-code JSON"
        after-title="semantic model"
        :after-code="surfaceCompare.after"
      />
    </section>

    <!-- P4 — Agent Skills (offering) -->
    <section class="offering">
      <div class="offering-intro">
        <span class="pillar-badge skills"><span class="num">4</span>Ship <span class="eyebrow">&middot; with Agent Skills</span></span>
        <h2>Build processes <em>with</em> agents, not around them.</h2>
        <p>Drop-in agent skills, built on everything bpmn-to-code offers. Integrate the plugin into your project in one prompt, scaffold process services from a diagram, and get architectural tests written for you &mdash; while you focus on modeling the business logic. Works with Claude Code and any other agent supporting the <a href="https://agentskills.io" target="_blank" rel="noopener">agentskills.io</a> standard.</p>
        <a class="pillar-link" :href="withBase('/skills/')">See all skills <span class="arrow">&rarr;</span></a>
      </div>
      <div class="skills-grid">
        <SkillCard
          v-for="skill in skills"
          :key="skill.name"
          :icon="skill.icon"
          :name="skill.name"
          :description="skill.description"
        />
      </div>
    </section>

    <LandingFooter />
  </div>
</template>
