<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, computed, useTemplateRef } from 'vue'
import { withBase } from 'vitepress'
import type { IdeTab } from './data'

const props = defineProps<{
  tabs: IdeTab[]
  rotateMs?: number
}>()

const rotateMs = computed(() => props.rotateMs ?? 3800)

const activeIdx = ref(0)
const paused = ref(false)
const sticky = ref(false)
const hintText = ref('auto-switching')

const progressEl = useTemplateRef<HTMLElement>('progressEl')
let timerId: ReturnType<typeof setInterval> | null = null

const activeTab = computed(() => props.tabs[activeIdx.value])
const breadcrumbFile = computed(() => activeTab.value?.filename ?? '')

function runProgress() {
  const el = progressEl.value
  if (!el) return
  el.style.transition = 'none'
  el.style.width = '0%'
  // Force reflow so the next transition applies.
  void el.offsetWidth
  el.style.transition = `width ${rotateMs.value}ms linear`
  el.style.width = '100%'
}

function stopTimer() {
  if (timerId !== null) {
    clearInterval(timerId)
    timerId = null
  }
  const el = progressEl.value
  if (el) {
    el.style.transition = 'none'
    el.style.width = '0%'
  }
}

function startTimer() {
  stopTimer()
  runProgress()
  timerId = setInterval(() => {
    activeIdx.value = (activeIdx.value + 1) % props.tabs.length
    runProgress()
  }, rotateMs.value)
}

function onMouseEnter() {
  if (sticky.value) return
  stopTimer()
  paused.value = true
  hintText.value = 'paused'
}

function onMouseLeave() {
  if (sticky.value) return
  paused.value = false
  hintText.value = 'auto-switching'
  startTimer()
}

function onTabClick(i: number) {
  activeIdx.value = i
  sticky.value = true
  stopTimer()
  paused.value = true
  hintText.value = 'paused'
}

onMounted(() => {
  startTimer()
})

onBeforeUnmount(() => {
  stopTimer()
})
</script>

<template>
  <div class="ide-wrap">
    <div class="ide" :class="{ paused }" @mouseenter="onMouseEnter" @mouseleave="onMouseLeave">
      <div class="ide-titlebar">
        <span class="dots"><i></i><i></i><i></i></span>
        <div class="breadcrumb">
          subscription-service<span class="sep">/</span>src<span class="sep">/</span>main<span class="sep">/</span>resources<span class="sep">/</span><span class="bc-file">{{ breadcrumbFile }}</span>
        </div>
        <span class="ide-hint">
          <span class="pulse"></span>
          <span>{{ hintText }}</span>
        </span>
      </div>
      <div class="ide-tabs">
        <button
          v-for="(tab, i) in tabs"
          :key="tab.id"
          class="ide-tab"
          :class="{ active: i === activeIdx }"
          @click="onTabClick(i)"
        >
          <span class="tab-ico" :class="tab.iconClass">{{ tab.iconText }}</span>{{ tab.label }}<span v-if="tab.beta" class="tab-beta">beta</span>
        </button>
      </div>
      <div class="ide-body">
        <div ref="progressEl" class="ide-progress"></div>
        <template v-for="(tab, i) in tabs" :key="tab.id">
          <div
            v-if="tab.paneType === 'image'"
            class="ide-pane image-pane"
            :class="{ active: i === activeIdx }"
          >
            <img :src="withBase(tab.src!)" :alt="`${tab.filename} diagram`" />
          </div>
          <div
            v-else
            class="ide-pane"
            :class="{ active: i === activeIdx }"
            v-html="tab.code"
          ></div>
        </template>
      </div>
    </div>
  </div>
</template>
