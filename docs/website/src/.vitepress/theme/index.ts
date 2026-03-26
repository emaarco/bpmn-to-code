import DefaultTheme from 'vitepress/theme'
import VersionBadge from './VersionBadge.vue'
import './style.css'
import { h } from 'vue'

export default {
  extends: DefaultTheme,
  Layout() {
    return h(DefaultTheme.Layout, null, {
      'nav-bar-content-after': () => h(VersionBadge),
    })
  },
}
