import { defineConfig } from 'vitepress'

export default defineConfig({
  title: 'bpmn-to-code',
  titleTemplate: false,
  description: 'Type-safe BPMN toolkit — generate APIs, validate models, surface process structure to your toolchain.',
  base: '/bpmn-to-code/',
  appearance: false,
  ignoreDeadLinks: [/\/\.agent\//],

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
          {
            text: 'Architecture Decisions',
            collapsed: true,
            items: [
              { text: 'Overview', link: '/contributing/adr/' },
              { text: '001 – Hexagonal Architecture', link: '/contributing/adr/001-hexagonal-architecture' },
              { text: '002 – Model Merging', link: '/contributing/adr/002-model-merging' },
              { text: '003 – Generated API Structure', link: '/contributing/adr/003-generated-api-structure' },
              { text: '004 – Multi-Engine Strategy', link: '/contributing/adr/004-strategy-pattern-multi-engine' },
              { text: '005 – Code Generation Strategy', link: '/contributing/adr/005-strategy-pattern-code-generation' },
              { text: '006 – File-Based Versioning', link: '/contributing/adr/006-file-based-versioning' },
              { text: '007 – Variable Extraction Scope', link: '/contributing/adr/007-variable-extraction-scope' },
              { text: '008 – Web Module', link: '/contributing/adr/008-web-module-for-browser-access' },
              { text: '009 – Ktor Static Frontend', link: '/contributing/adr/009-ktor-static-frontend-single-module' },
              { text: '010 – Operaton Namespace Extractor', link: '/contributing/adr/010-operaton-namespace-only-extractor' },
              { text: '011 – Variable Name Collision', link: '/contributing/adr/011-variable-name-collision-detection' },
              { text: '012 – JSON Export', link: '/contributing/adr/012-json-export' },
              { text: '013 – Testing Module', link: '/contributing/adr/013-testing-module' },
              { text: '014 – Shared BPMN Types', link: '/contributing/adr/014-shared-bpmn-types' },
            ],
          },
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
