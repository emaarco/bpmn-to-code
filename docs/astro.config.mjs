import { defineConfig } from 'astro/config';
import starlight from '@astrojs/starlight';

export default defineConfig({
  site: 'https://emaarco.github.io',
  base: '/bpmn-to-code',
  integrations: [
    starlight({
      title: 'bpmn-to-code',
      description: 'Generate type-safe API definitions from BPMN process models',
      social: [{ icon: 'github', label: 'GitHub', href: 'https://github.com/emaarco/bpmn-to-code' }],
      editLink: { baseUrl: 'https://github.com/emaarco/bpmn-to-code/edit/main/docs/' },
      sidebar: [
        { label: 'Overview', slug: 'index' },
        { label: 'Gradle Plugin', slug: 'gradle' },
        { label: 'Maven Plugin', slug: 'maven' },
        { label: 'Web Application', slug: 'web' },
        {
          label: 'Guides',
          items: [{ label: 'BPMN File Filtering', slug: 'guides/filtering' }],
        },
        { label: 'Architecture', slug: 'architecture' },
        { label: 'Best Practices', slug: 'best-practices-process-modeling' },
        { label: 'Contributing', slug: 'contributing' },
        { label: 'ADRs', collapsed: true, autogenerate: { directory: 'adr' } },
      ],
    }),
  ],
});
