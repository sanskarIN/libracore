import { useState, type FormEvent } from 'react'
import { copy } from '../copy'

interface LoginPageProps {
  onLogin: (email: string, password: string) => Promise<void>
  error?: string | undefined
}

export function LoginPage({ onLogin, error }: LoginPageProps) {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (submitting) return
    setSubmitting(true)
    try {
      await onLogin(email, password)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <main className="auth-page">
      <section className="auth-intro" aria-labelledby="login-title">
        <div className="auth-brand">
          <img src="/logo.svg" width="56" height="56" alt="" />
          <div>
            <p className="eyebrow">Open-source library management</p>
            <h1 id="login-title">Welcome to {copy.appName}</h1>
          </div>
        </div>
        <p className="auth-lede">
          Keep catalog, circulation, members, reservations, reporting, and operational history connected in one focused workspace.
        </p>
        <ul className="auth-feature-list">
          <li>Branch-aware catalog and physical-copy tracking</li>
          <li>Issue, return, renewal, reservations, and fine policies</li>
          <li>Role-based access with auditable operational changes</li>
        </ul>
        <p className="auth-watermark">{copy.madeBy}</p>
      </section>

      <section className="auth-card" aria-label="Sign in form">
        <div>
          <p className="eyebrow">Secure access</p>
          <h2>Sign in</h2>
          <p>Use the account provided by your LibraCore administrator.</p>
        </div>
        {error ? <div className="inline-alert error" role="alert">{error}</div> : null}
        <form onSubmit={handleSubmit} className="form-stack">
          <label>
            <span>Email</span>
            <input
              type="email"
              name="email"
              autoComplete="username"
              inputMode="email"
              maxLength={320}
              required
              value={email}
              onChange={(event) => setEmail(event.target.value)}
            />
          </label>
          <label>
            <span>Password</span>
            <input
              type="password"
              name="password"
              autoComplete="current-password"
              maxLength={200}
              required
              value={password}
              onChange={(event) => setPassword(event.target.value)}
            />
          </label>
          <button className="button button-primary" type="submit" disabled={submitting}>
            {submitting ? 'Signing in…' : copy.actions.signIn}
          </button>
        </form>
        <p className="auth-help">
          If you cannot sign in, contact your library administrator or email{' '}
          <a href={`mailto:${copy.contacts.support}`}>{copy.contacts.support}</a>.
        </p>
      </section>
    </main>
  )
}
