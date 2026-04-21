<script setup lang="ts">
defineProps<{
  icon: string
  name: string
  description: string
}>()
</script>

<template>
  <div class="skill-card">
    <div class="skill-ico">{{ icon }}</div>
    <div class="skill-name">{{ name }}</div>
    <p class="skill-desc">{{ description }}</p>
  </div>
</template>
