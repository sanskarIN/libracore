import type { LoginResponse, UserIdentity } from './types'

const SESSION_KEY = 'libracore.session.v1'

export interface StoredSession {
  accessToken: string
  expiresAt: string
  user: UserIdentity
}

interface StorageLike {
  getItem(key: string): string | null
  setItem(key: string, value: string): void
  removeItem(key: string): void
}

export function saveSession(login: LoginResponse, storage = browserSessionStorage()): StoredSession {
  const session: StoredSession = {
    accessToken: login.accessToken,
    expiresAt: login.expiresAt,
    user: login.user,
  }
  storage?.setItem(SESSION_KEY, JSON.stringify(session))
  return session
}

export function loadSession(storage = browserSessionStorage(), now = Date.now()): StoredSession | null {
  if (!storage) return null
  const raw = storage.getItem(SESSION_KEY)
  if (!raw) return null

  try {
    const parsed = JSON.parse(raw) as Partial<StoredSession>
    if (
      typeof parsed.accessToken !== 'string' ||
      typeof parsed.expiresAt !== 'string' ||
      !parsed.user ||
      typeof parsed.user.userId !== 'string' ||
      typeof parsed.user.email !== 'string' ||
      !['ADMIN', 'LIBRARIAN', 'MEMBER'].includes(parsed.user.role ?? '')
    ) {
      storage.removeItem(SESSION_KEY)
      return null
    }
    const expiresAt = Date.parse(parsed.expiresAt)
    if (!Number.isFinite(expiresAt) || expiresAt <= now) {
      storage.removeItem(SESSION_KEY)
      return null
    }
    return parsed as StoredSession
  } catch {
    storage.removeItem(SESSION_KEY)
    return null
  }
}

export function clearSession(storage = browserSessionStorage()): void {
  storage?.removeItem(SESSION_KEY)
}

function browserSessionStorage(): StorageLike | undefined {
  if (typeof window === 'undefined') return undefined
  return window.sessionStorage
}

export const sessionStorageKey = SESSION_KEY
