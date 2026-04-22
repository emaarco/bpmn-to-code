<script setup lang="ts">
import type { RunnerRule } from './data'

defineProps<{
  command: string
  fail: RunnerRule[]
  failSummary: string
  failNote: string
  pass: RunnerRule[]
  passSummary: string
  passNote: string
}>()
</script>

<template>
  <div class="runner-wrap">
    <div class="runner fail">
      <div class="runner-titlebar">
        <span class="cmd">{{ command }}</span>
        <span class="status fail"><span class="dot"></span>FAILED</span>
      </div>
      <div class="runner-body">
        <template v-for="rule in fail" :key="rule.name">
          <span class="rline err">✗ {{ rule.name }}</span>
          <span class="rline hint" v-html="rule.hint"></span>
        </template>
      </div>
      <div class="runner-summary">
        <span class="metric"><span class="tag fail">{{ failSummary }}</span></span>
        <span class="metric">{{ failNote }}</span>
      </div>
    </div>

    <div class="runner-arrow">→</div>

    <div class="runner pass">
      <div class="runner-titlebar">
        <span class="cmd">{{ command }}</span>
        <span class="status pass"><span class="dot"></span>PASSED</span>
      </div>
      <div class="runner-body">
        <template v-for="rule in pass" :key="rule.name">
          <span class="rline ok">✓ {{ rule.name }}</span>
          <span class="rline hint" v-html="rule.hint"></span>
        </template>
      </div>
      <div class="runner-summary">
        <span class="metric"><span class="tag pass">{{ passSummary }}</span></span>
        <span class="metric">{{ passNote }}</span>
      </div>
    </div>
  </div>
</template>
