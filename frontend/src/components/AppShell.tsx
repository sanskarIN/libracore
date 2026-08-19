import type { ReactNode } from 'react'
import { copy } from '../copy'
import type { Route } from '../routes'
import { routeHref } from '../routes'
import type { UserIdentity } from '../types'

interface AppShellProps {
  user: UserIdentity
  route: Route
  children: ReactNode
  onSignOut: () => void
}

interface NavItem {
  route: Route
  label: string
  roles?: UserIdentity['role'][]
}

const NAVIGATION: NavItem[] = [
  { route: 'dashboard', label: copy.navigation.dashboard },
  { route: 'catalog', label: copy.navigation.catalog },
  { route: 'members', label: copy.navigation.members, roles: ['ADMIN', 'LIBRARIAN'] },
  { route: 'circulation', label: copy.navigation.circulation, roles: ['ADMIN', 'LIBRARIAN'] },
  { route: 'reservations', label: copy.navigation.reservations },
  { route: 'reports', label: copy.navigation.reports, roles: ['ADMIN', 'LIBRARIAN'] },
  { route: 'staff-accounts', label: copy.navigation.staffAccounts, roles: ['ADMIN'] },
  { route: 'settings', label: copy.navigation.settings },
]

export function AppShell({ user, route, children, onSignOut }: AppShellProps) {
  const items = NAVIGATION.filter((item) => !item.roles || item.roles.includes(user.role))

  return (
    <div className="app-shell">
      <a className="skip-link" href="#main-content">Skip to main content</a>
      <aside className="sidebar" aria-label="Primary navigation">
        <a className="brand" href={routeHref('dashboard')} aria-label="LibraCore dashboard">
          <img src="/logo.svg" width="40" height="40" alt="" />
          <span>
            <strong>{copy.appName}</strong>
            <small>{copy.appTagline}</small>
          </span>
        </a>
        <nav>
          <ul className="nav-list">
            {items.map((item) => (
              <li key={item.route}>
                <a
                  className={route === item.route ? 'nav-link active' : 'nav-link'}
                  href={routeHref(item.route)}
                  aria-current={route === item.route ? 'page' : undefined}
                >
                  <span className="nav-dot" aria-hidden="true" />
                  {item.label}
                </a>
              </li>
            ))}
          </ul>
        </nav>
        <div className="sidebar-footer">
          <p className="role-pill">{user.role.replace('_', ' ')}</p>
          <p className="sidebar-email" title={user.email}>{user.email}</p>
          <button className="button button-quiet full-width" type="button" onClick={onSignOut}>
            {copy.actions.signOut}
          </button>
        </div>
      </aside>

      <div className="content-column">
        <header className="mobile-header">
          <a className="mobile-brand" href={routeHref('dashboard')}>
            <img src="/logo.svg" width="32" height="32" alt="" />
            <strong>{copy.appName}</strong>
          </a>
          <span className="role-pill">{user.role}</span>
        </header>
        <main id="main-content" className="main-content" tabIndex={-1}>
          {children}
        </main>
        <footer className="app-footer">
          <span>{copy.madeBy}</span>
          <span aria-hidden="true">•</span>
          <span>Open-source library management</span>
        </footer>
      </div>

      <nav className="mobile-nav" aria-label="Mobile primary navigation">
        {items.slice(0, 5).map((item) => (
          <a
            key={item.route}
            href={routeHref(item.route)}
            className={route === item.route ? 'mobile-nav-link active' : 'mobile-nav-link'}
            aria-current={route === item.route ? 'page' : undefined}
          >
            {item.label}
          </a>
        ))}
      </nav>
    </div>
  )
}
