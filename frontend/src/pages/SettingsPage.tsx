import { copy } from '../copy'
import { PageHeader } from '../components/PageHeader'
import type { ThemePreference } from '../theme'
import type { UserIdentity } from '../types'

interface SettingsPageProps {
  user: UserIdentity
  theme: ThemePreference
  onThemeChange: (theme: ThemePreference) => void
}

export function SettingsPage({ user, theme, onThemeChange }: SettingsPageProps) {
  return (
    <div className="page-stack">
      <PageHeader eyebrow="Settings" title="Preferences and project information" description="Adjust local appearance and review account, privacy, support, and open-source information." />

      <section className="settings-grid">
        <article className="panel">
          <div className="panel-heading"><div><p className="eyebrow">Appearance</p><h2>Theme</h2></div></div>
          <fieldset className="segmented-control">
            <legend className="sr-only">Theme preference</legend>
            {(['system', 'light', 'dark'] as const).map((option) => (
              <label key={option} className={theme === option ? 'segment selected' : 'segment'}>
                <input type="radio" name="theme" value={option} checked={theme === option} onChange={() => onThemeChange(option)} />
                <span>{option.charAt(0).toUpperCase() + option.slice(1)}</span>
              </label>
            ))}
          </fieldset>
          <p className="field-help">System follows your device preference. The selected appearance is stored only in this browser.</p>
        </article>

        <article className="panel">
          <div className="panel-heading"><div><p className="eyebrow">Account</p><h2>Signed-in identity</h2></div></div>
          <dl className="detail-list">
            <div><dt>Email</dt><dd>{user.email}</dd></div>
            <div><dt>Role</dt><dd>{user.role}</dd></div>
            <div><dt>User ID</dt><dd className="wrap-anywhere"><code>{user.userId}</code></dd></div>
          </dl>
          <p className="field-help">The access token is kept in session-scoped browser storage and is cleared when you sign out or the session expires.</p>
        </article>

        <article className="panel">
          <div className="panel-heading"><div><p className="eyebrow">Accessibility</p><h2>Built-in behavior</h2></div></div>
          <ul className="plain-list">
            <li>Keyboard-operable navigation and actions</li>
            <li>Visible focus indicators and skip navigation</li>
            <li>Status symbols that do not depend on color alone</li>
            <li>Reduced-motion support through operating-system preferences</li>
            <li>Responsive layouts for zoom, narrow screens, and touch input</li>
          </ul>
        </article>

        <article className="panel">
          <div className="panel-heading"><div><p className="eyebrow">Privacy</p><h2>Data handling</h2></div></div>
          <p>LibraCore stores library data in the operator-configured PostgreSQL database. Operators control retention, backups, access, exports, and lawful handling of member records.</p>
          <p>The default interface does not load third-party analytics, advertising scripts, or remote web fonts.</p>
          <a className="text-link" href="https://github.com/sanskarIN/libracore/blob/main/PRIVACY.md" target="_blank" rel="noreferrer">Read the privacy documentation</a>
        </article>

        <article className="panel">
          <div className="panel-heading"><div><p className="eyebrow">Updates</p><h2>Version and release channel</h2></div></div>
          <dl className="detail-list"><div><dt>Web version</dt><dd>{__APP_VERSION__}</dd></div><div><dt>Channel</dt><dd>2.0.x release candidate</dd></div></dl>
          <p>Production operators should deploy tagged releases after reviewing migrations, release notes, and backup/rollback guidance.</p>
          <a className="text-link" href="https://github.com/sanskarIN/libracore/releases" target="_blank" rel="noreferrer">Open GitHub releases</a>
        </article>

        <article className="panel about-card">
          <div className="about-brand"><img src="/logo.svg" width="64" height="64" alt="" /><div><p className="eyebrow">About</p><h2>{copy.appName}</h2><p>{copy.appTagline}</p></div></div>
          <p>LibraCore is an open-source library management system focused on explicit circulation rules, reliable data, accessible workflows, auditability, and practical operations.</p>
          <p className="watermark-callout">{copy.madeBy}</p>
          <div className="contact-list">
            <a href={copy.contacts.github} target="_blank" rel="noreferrer">GitHub profile</a>
            <a href={`mailto:${copy.contacts.businessPrimary}`}>{copy.contacts.businessPrimary}</a>
            <a href={`mailto:${copy.contacts.businessSecondary}`}>{copy.contacts.businessSecondary}</a>
            <a href={`mailto:${copy.contacts.support}`}>Support: {copy.contacts.support}</a>
            <a href={copy.contacts.funding} target="_blank" rel="noreferrer">Buy Me a Coffee</a>
          </div>
        </article>
      </section>
    </div>
  )
}
