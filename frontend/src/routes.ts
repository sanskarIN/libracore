import { useEffect, useState } from 'react'

export type Route =
  | 'dashboard'
  | 'catalog'
  | 'members'
  | 'circulation'
  | 'reservations'
  | 'reports'
  | 'staff-accounts'
  | 'settings'

const ROUTES = new Set<Route>([
  'dashboard',
  'catalog',
  'members',
  'circulation',
  'reservations',
  'reports',
  'staff-accounts',
  'settings',
])

export function readRoute(hash = window.location.hash): Route {
  const value = hash.replace(/^#\/?/, '').split(/[/?]/, 1)[0]
  return ROUTES.has(value as Route) ? (value as Route) : 'dashboard'
}

export function routeHref(route: Route): string {
  return `#/${route}`
}

export function useHashRoute(): Route {
  const [route, setRoute] = useState<Route>(() => readRoute())

  useEffect(() => {
    const onHashChange = () => setRoute(readRoute())
    window.addEventListener('hashchange', onHashChange)
    return () => window.removeEventListener('hashchange', onHashChange)
  }, [])

  return route
}
