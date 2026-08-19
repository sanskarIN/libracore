import { describe, expect, it } from 'vitest'
import { loadTheme, saveTheme, themeStorageKey } from './theme'

class MemoryStorage {
  private readonly values = new Map<string, string>()

  getItem(key: string) { return this.values.get(key) ?? null }
  setItem(key: string, value: string) { this.values.set(key, value) }
}

describe('theme preferences', () => {
  it('defaults to system', () => {
    expect(loadTheme(new MemoryStorage())).toBe('system')
  })

  it('persists a supported theme', () => {
    const storage = new MemoryStorage()
    saveTheme('dark', storage)
    expect(storage.getItem(themeStorageKey)).toBe('dark')
    expect(loadTheme(storage)).toBe('dark')
  })

  it('ignores unsupported stored values', () => {
    const storage = new MemoryStorage()
    storage.setItem(themeStorageKey, 'neon')
    expect(loadTheme(storage)).toBe('system')
  })
})
