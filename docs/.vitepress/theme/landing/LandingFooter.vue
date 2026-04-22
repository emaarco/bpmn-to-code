<script setup lang="ts">
import { withBase } from 'vitepress'
</script>

<template>
  <footer class="site-footer">
    <div class="footer-inner">
      <div class="footer-brand">
        <img class="mark" :src="withBase('favicon.png')" alt="bpmn-to-code" />
        <span class="footer-lines">
          <span class="footer-credit">Created with &hearts; by <a href="https://www.linkedin.com/in/schaeckm" target="_blank" rel="noopener">Marco Schäck</a></span>
          <span class="footer-legal">&copy; 2026 &middot; Open source under the <a href="https://github.com/emaarco/bpmn-to-code?tab=MIT-1-ov-file#readme" target="_blank" rel="noopener">MIT License</a></span>
        </span>
      </div>
      <div class="footer-links">
        <a href="https://medium.com/@emaarco" target="_blank" rel="noopener">Medium</a>
        <span class="footer-sep">&middot;</span>
        <a href="https://central.sonatype.com/artifact/io.github.emaarco/bpmn-to-code-maven" target="_blank" rel="noopener">Maven Central</a>
        <a class="footer-social" href="https://www.linkedin.com/in/schaeckm" target="_blank" rel="noopener" aria-label="LinkedIn">
          <svg fill="currentColor" viewBox="0 0 24 24"><path d="M20.447 20.452h-3.554v-5.569c0-1.328-.027-3.037-1.852-3.037-1.853 0-2.136 1.445-2.136 2.939v5.667H9.351V9h3.414v1.561h.046c.477-.9 1.637-1.852 3.37-1.852 3.601 0 4.267 2.37 4.267 5.455v6.288zM5.337 7.433a2.062 2.062 0 01-2.063-2.065 2.063 2.063 0 112.063 2.065zm1.782 13.019H3.555V9h3.564v11.452zM22.225 0H1.771C.792 0 0 .774 0 1.729v20.542C0 23.227.792 24 1.771 24h20.451C23.2 24 24 23.227 24 22.271V1.729C24 .774 23.2 0 22.222 0h.003z"/></svg>
        </a>
        <a class="footer-social" href="https://github.com/emaarco/bpmn-to-code" target="_blank" rel="noopener" aria-label="GitHub">
          <svg fill="currentColor" viewBox="0 0 24 24"><path d="M12 2C6.48 2 2 6.48 2 12c0 4.42 2.87 8.17 6.84 9.5.5.09.66-.22.66-.48v-1.69c-2.78.6-3.37-1.34-3.37-1.34-.45-1.16-1.11-1.47-1.11-1.47-.91-.62.07-.6.07-.6 1 .07 1.53 1.03 1.53 1.03.89 1.52 2.34 1.08 2.91.83.09-.65.35-1.08.63-1.34-2.22-.25-4.56-1.11-4.56-4.94 0-1.1.39-1.99 1.03-2.69-.1-.25-.45-1.27.1-2.65 0 0 .84-.27 2.75 1.02a9.5 9.5 0 0 1 5 0C18.14 4.18 19 4.45 19 4.45c.55 1.38.2 2.4.1 2.65.64.7 1.03 1.59 1.03 2.69 0 3.84-2.34 4.69-4.57 4.93.36.31.68.92.68 1.85v2.74c0 .27.16.58.67.48C19.14 20.16 22 16.42 22 12c0-5.52-4.48-10-10-10z"/></svg>
        </a>
      </div>
    </div>
  </footer>
</template>
