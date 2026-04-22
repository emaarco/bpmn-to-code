import DefaultTheme from 'vitepress/theme'
import VersionBadge from './VersionBadge.vue'
import '@fontsource-variable/manrope'
import '@fontsource-variable/jetbrains-mono'
import './style.css'
import './landing/landing.css'
import { h } from 'vue'

export default {
  extends: DefaultTheme,
  Layout() {
    return h(DefaultTheme.Layout, null, {
      'nav-bar-content-after': () => h(VersionBadge),
    })
  },
}
