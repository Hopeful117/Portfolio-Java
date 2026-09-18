import { Component } from '@angular/core';

@Component({
  standalone: true,
  template: `
    <section class="contact-page">
      <header class="contact-header"><h1>Contact</h1><p>Vous pouvez me retrouver sur ces plateformes.</p></header>
      <div class="contact-grid">
        <article class="contact-card"><h2>GitHub</h2><a href="https://github.com/Hopeful117/Hopeful117" target="_blank" rel="noreferrer">Hopeful117</a></article>
        <article class="contact-card"><h2>LinkedIn</h2><a href="https://www.linkedin.com/in/ludovicbrot/" target="_blank" rel="noreferrer">ludovicbrot</a></article>
        <article class="contact-card"><h2>Email</h2><a href="mailto:ludovic.brot@gmail.com">ludovic.brot</a></article>
      </div>
    </section>
  `,
  styles: `
    .contact-page { max-width: 850px; }
    .contact-header { margin-bottom: 3rem; text-align: center; }
    h1 { margin: 0 0 .75rem; font-family: var(--font-display); font-size: clamp(3rem, 7vw, 5.2rem); font-weight: 500; line-height: .95; letter-spacing: -.06em; }
    .contact-header p { color: var(--muted); font-size: 1.15rem; }
    .contact-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 1rem; }
    .contact-card { padding: 1.5rem; border: 1px solid var(--line); background: var(--color-slate); }
    h2 { margin: 0 0 .5rem; font-family: var(--font-display); font-size: 1.5rem; font-weight: 500; }
    a { color: var(--accent); font-weight: 700; text-decoration: none; }
  `,
})
export class ContactPage {}
