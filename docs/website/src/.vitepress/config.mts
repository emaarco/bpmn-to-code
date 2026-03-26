import { defineConfig } from 'vitepress'

export default defineConfig({
  title: 'bpmn-to-code',
  titleTemplate: false,
  description: 'Generate type-safe API definitions from BPMN process models',
  base: '/bpmn-to-code/',

  head: [
    ['link', { rel: 'icon', type: 'image/png', href: '/bpmn-to-code/favicon.png' }],
  ],

  themeConfig: {
    logo: '/favicon.png',

    nav: [
      { text: 'Build Plugins', link: '/getting-started/gradle' },
      { text: 'Standalone', link: '/web/' },
      { text: 'Engines', link: '/engines/zeebe' },
      {
        text: 'Links',
        items: [
          { text: 'GitHub', link: 'https://github.com/emaarco/bpmn-to-code' },
          { text: 'Maven Central', link: 'https://central.sonatype.com/artifact/io.github.emaarco/bpmn-to-code-maven' },
          { text: 'Gradle Plugin Portal', link: 'https://plugins.gradle.org/plugin/io.github.emaarco.bpmn-to-code-gradle' },
        ],
      },
    ],

    sidebar: [
      {
        text: 'Build Plugins',
        items: [
          {
            text: 'Gradle',
            collapsed: false,
            items: [
              { text: 'Setup', link: '/getting-started/gradle' },
              { text: 'Advanced', link: '/getting-started/gradle-advanced' },
            ],
          },
          {
            text: 'Maven',
            collapsed: false,
            items: [
              { text: 'Setup', link: '/getting-started/maven' },
              { text: 'Advanced', link: '/getting-started/maven-advanced' },
            ],
          },
        ],
      },
      {
        text: 'Standalone',
        items: [
          { text: 'Web App', link: '/web/' },
          { text: 'MCP Server', link: '/mcp/' },
        ],
      },
      {
        text: 'Guide',
        items: [
          { text: 'Configuration', link: '/guide/configuration' },
          { text: 'Generated API', link: '/guide/generated-api' },
          { text: 'Versioning', link: '/guide/versioning' },
        ],
      },
      {
        text: 'Engines',
        items: [
          { text: 'Zeebe', link: '/engines/zeebe' },
          { text: 'Camunda 7', link: '/engines/camunda7' },
          { text: 'Operaton', link: '/engines/operaton' },
        ],
      },
      {
        text: 'Extras',
        items: [
          { text: 'Examples', link: '/recipes/examples' },
          { text: 'AI Skills', link: '/skills/' },
          { text: 'Common Patterns', link: '/recipes/common-patterns' },
        ],
      },
    ],

    socialLinks: [
      { icon: 'github', link: 'https://github.com/emaarco/bpmn-to-code' },
    ],

    search: {
      provider: 'local',
    },

    editLink: {
      pattern: 'https://github.com/emaarco/bpmn-to-code/edit/main/docs/website/src/:path',
    },

    footer: {
      message: 'Open source under the <a href="https://github.com/emaarco/bpmn-to-code?tab=MIT-1-ov-file#readme" target="_blank">MIT License</a>. Contributions welcome!',
      copyright: 'Created with ♥ by <a href="https://www.linkedin.com/in/schaeckm" target="_blank">Marco Schäck</a> · <a href="https://www.linkedin.com/in/schaeckm" target="_blank">LinkedIn</a> · <a href="https://medium.com/@emaarco" target="_blank">Medium</a>',
    },
  },
})
