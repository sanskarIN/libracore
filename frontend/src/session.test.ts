import { describe, expect, it } from 'vitest'
import { loadSession, saveSession, sessionStorageKey } from './session'
import type { LoginResponse } from './types'

class MemoryStorage {
  private readonly values = new Map<string, string>()

  getItem(key: string) { return this.values.get(key) ?? null }
  setItem(key: string, value: string) { this.values.set(key, value) }
  removeItem(key: string) { this.values.delete(key) }
}

const login: LoginResponse = {
  accessToken: 'token-value',
  tokenType: 'Bearer',
  expiresAt: '2026-08-20T00:00:00Z',
  user: { userId: 'user-1', email: 'reader@example.test', role: 'MEMBER', memberId: 'member-1' },
}

describe('session storage', () => {
  it('stores and loads an unexpired session', () => {
    const storage = new MemoryStorage()
    saveSession(login, storage)

    expect(loadSession(storage, Date.parse('2026-08-19T00:00:00Z'))).toEqual({
      accessToken: login.accessToken,
      expiresAt: login.expiresAt,
      user: login.user,
    })
  })

  it('removes an expired session', () => {
    const storage = new MemoryStorage()
    saveSession(login, storage)

    expect(loadSession(storage, Date.parse('2026-08-21T00:00:00Z'))).toBeNull()
    expect(storage.getItem(sessionStorageKey)).toBeNull()
  })

  it('removes malformed session data', () => {
    const storage = new MemoryStorage()
    storage.setItem(sessionStorageKey, '{broken')

    expect(loadSession(storage)).toBeNull()
    expect(storage.getItem(sessionStorageKey)).toBeNull()
  })
})
