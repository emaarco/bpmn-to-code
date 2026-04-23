import { defineConfig } from 'vitepress'

export default defineConfig({
  title: 'bpmn-to-code',
  titleTemplate: false,
  description: 'Type-safe BPMN toolkit — generate APIs, validate models, surface process structure to your toolchain.',
  base: '/bpmn-to-code/',
  appearance: false,
  ignoreDeadLinks: [/\/\.claude\//],

  head: [
    ['link', { rel: 'icon', type: 'image/png', href: '/bpmn-to-code/favicon.png' }],
  ],

  themeConfig: {
    logo: '/favicon.png',

    nav: [
      { text: 'Generate', link: '/getting-started/gradle' },
      { text: 'Validate', link: '/validate/' },
      { text: 'Surface', link: '/surface/' },
      { text: 'Ship', link: '/skills/' },
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
        text: 'Overview',
        items: [
          { text: 'Why bpmn-to-code', link: '/overview/why' },
        ],
      },
      {
        text: 'Generate',
        items: [
          {
            text: 'Gradle',
            collapsed: true,
            items: [
              { text: 'Setup', link: '/getting-started/gradle' },
              { text: 'Advanced', link: '/getting-started/gradle-advanced' },
            ],
          },
          {
            text: 'Maven',
            collapsed: true,
            items: [
              { text: 'Setup', link: '/getting-started/maven' },
              { text: 'Advanced', link: '/getting-started/maven-advanced' },
            ],
          },
          { text: 'Generated API', link: '/guide/generated-api' },
          { text: 'Configuration', link: '/guide/configuration' },
        ],
      },
      {
        text: 'Validate',
        items: [
          { text: 'Build-time Validation', link: '/validate/' },
          { text: 'Testing Module', link: '/validate/testing' },
        ],
      },
      {
        text: 'Surface',
        items: [
          { text: 'Overview', link: '/surface/' },
          { text: 'JSON Export', link: '/surface/json' },
          { text: 'MCP Server', link: '/mcp/' },
        ],
      },
      {
        text: 'Ship',
        items: [
          { text: 'Agent Skills', link: '/skills/' },
        ],
      },
      {
        text: 'Reference',
        items: [
          {
            text: 'Engines',
            collapsed: true,
            items: [
              { text: 'Zeebe', link: '/engines/zeebe' },
              { text: 'Camunda 7', link: '/engines/camunda7' },
              { text: 'Operaton', link: '/engines/operaton' },
            ],
          },
          { text: 'Web App', link: '/web/' },
          { text: 'Examples', link: '/recipes/examples' },
          { text: 'Common Patterns', link: '/recipes/common-patterns' },
          { text: 'Changelog', link: '/changelog/v2' },
        ],
      },
      {
        text: 'Contributing',
        collapsed: true,
        items: [
          { text: 'Contributing Guide', link: '/contributing/' },
          { text: 'AI Skills Architecture', link: '/contributing/ai-skills' },
          { text: 'Docker Deployment', link: '/contributing/docker-hub-deployment' },
          { text: 'Best Practices', link: '/contributing/best-practices' },
          { text: 'Architecture Decisions', link: '/contributing/adr/' },
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
      pattern: 'https://github.com/emaarco/bpmn-to-code/edit/main/docs/:path',
    },

    footer: {
      message: 'Open source under the <a href="https://github.com/emaarco/bpmn-to-code?tab=MIT-1-ov-file#readme" target="_blank">MIT License</a>. Contributions welcome!',
      copyright: 'Created with ♥ by <a href="https://www.linkedin.com/in/schaeckm" target="_blank">Marco Schäck</a> · <a href="https://www.linkedin.com/in/schaeckm" target="_blank">LinkedIn</a> · <a href="https://medium.com/@emaarco" target="_blank">Medium</a>',
    },
  },
})
