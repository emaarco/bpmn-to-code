<script setup lang="ts">
defineProps<{
  beforePill: string
  beforeTitle: string
  beforeCode: string
  beforeVariant?: 'bad' | 'warn'
  afterPill: string
  afterTitle: string
  afterCode: string
  scrollBefore?: boolean
}>()
</script>

<template>
  <div class="compare">
    <div class="compare-card bad">
      <div class="compare-head">
        <span class="compare-pill" :class="beforeVariant ?? 'bad'">{{ beforePill }}</span>
        <span class="compare-title">{{ beforeTitle }}</span>
      </div>
      <div class="compare-code" :class="{ scroll: scrollBefore }" v-html="beforeCode"></div>
    </div>
    <div class="compare-arrow">→</div>
    <div class="compare-card good">
      <div class="compare-head">
        <span class="compare-pill good">{{ afterPill }}</span>
        <span class="compare-title">{{ afterTitle }}</span>
      </div>
      <div class="compare-code" v-html="afterCode"></div>
    </div>
  </div>
</template>
