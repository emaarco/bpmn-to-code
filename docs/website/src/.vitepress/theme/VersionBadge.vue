<script setup>
import { ref, onMounted } from 'vue'

const version = ref('')

onMounted(async () => {
  try {
    const res = await fetch('https://api.github.com/repos/emaarco/bpmn-to-code/releases/latest')
    if (res.ok) {
      const data = await res.json()
      version.value = data.tag_name
    }
  } catch (e) {
    console.warn('Failed to fetch latest version:', e)
  }
})
</script>

<template>
  <a v-if="version" class="version-badge" href="https://github.com/emaarco/bpmn-to-code/releases" target="_blank">
    {{ version }}
  </a>
</template>
