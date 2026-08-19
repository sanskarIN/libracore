import { useCallback, useEffect, useMemo, useState } from 'react'
import { ApiClient, readableError } from './api'
import { AppShell } from './components/AppShell'
import { CatalogPage } from './pages/CatalogPage'
import { CirculationPage } from './pages/CirculationPage'
import { DashboardPage } from './pages/DashboardPage'
import { LoginPage } from './pages/LoginPage'
import { MembersPage } from './pages/MembersPage'
import { ReportsPage } from './pages/ReportsPage'
import { ReservationsPage } from './pages/ReservationsPage'
import { SettingsPage } from './pages/SettingsPage'
import { useHashRoute, type Route } from './routes'
import { clearSession, loadSession, saveSession, type StoredSession } from './session'
import { applyTheme, loadTheme, saveTheme, type ThemePreference } from './theme'
import type { LoginResponse } from './types'

export default function App() {
  const route = useHashRoute()
  const [session, setSession] = useState<StoredSession | null>(() => loadSession())
  const [loginError, setLoginError] = useState<string | undefined>()
  const [theme, setTheme] = useState<ThemePreference>(() => loadTheme())

  const invalidateSession = useCallback(() => {
    clearSession()
    setSession(null)
  }, [])

  const api = useMemo(() => new ApiClient({
    getToken: () => session?.accessToken ?? null,
    onUnauthorized: invalidateSession,
  }), [invalidateSession, session?.accessToken])

  useEffect(() => {
    applyTheme(theme)
  }, [theme])

  async function login(email: string, password: string) {
    setLoginError(undefined)
    try {
      const response = await api.post<LoginResponse>('/auth/login', { email, password })
      const stored = saveSession(response)
      setSession(stored)
      if (!window.location.hash) window.location.hash = '#/dashboard'
    } catch (reason) {
      setLoginError(readableError(reason))
    }
  }

  async function signOut() {
    try {
      await api.post<void>('/auth/logout')
    } catch {
      // Local session cleanup must still happen if the server is unreachable.
    } finally {
      invalidateSession()
    }
  }

  function changeTheme(next: ThemePreference) {
    setTheme(next)
    saveTheme(next)
    applyTheme(next)
  }

  if (!session) {
    return <LoginPage onLogin={login} error={loginError} />
  }

  const safeRoute = authorizedRoute(route, session.user.role)

  return (
    <AppShell user={session.user} route={safeRoute} onSignOut={() => void signOut()}>
      {safeRoute === 'dashboard' ? <DashboardPage api={api} user={session.user} /> : null}
      {safeRoute === 'catalog' ? <CatalogPage api={api} user={session.user} /> : null}
      {safeRoute === 'members' ? <MembersPage api={api} /> : null}
      {safeRoute === 'circulation' ? <CirculationPage api={api} user={session.user} /> : null}
      {safeRoute === 'reservations' ? <ReservationsPage api={api} user={session.user} /> : null}
      {safeRoute === 'reports' ? <ReportsPage api={api} user={session.user} /> : null}
      {safeRoute === 'settings' ? <SettingsPage user={session.user} theme={theme} onThemeChange={changeTheme} /> : null}
    </AppShell>
  )
}

function authorizedRoute(route: Route, role: StoredSession['user']['role']): Route {
  if (role === 'MEMBER' && (route === 'members' || route === 'circulation' || route === 'reports')) {
    return 'dashboard'
  }
  return route
}
