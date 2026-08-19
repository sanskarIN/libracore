export type ThemePreference = 'system' | 'light' | 'dark'

const THEME_KEY = 'libracore.theme.v1'

export function loadTheme(storage = browserLocalStorage()): ThemePreference {
  const saved = storage?.getItem(THEME_KEY)
  return saved === 'light' || saved === 'dark' || saved === 'system' ? saved : 'system'
}

export function saveTheme(theme: ThemePreference, storage = browserLocalStorage()): void {
  storage?.setItem(THEME_KEY, theme)
}

export function applyTheme(theme: ThemePreference, root = browserRoot()): void {
  if (!root) return
  if (theme === 'system') {
    root.removeAttribute('data-theme')
  } else {
    root.setAttribute('data-theme', theme)
  }
}

interface StorageLike {
  getItem(key: string): string | null
  setItem(key: string, value: string): void
}

interface RootLike {
  setAttribute(name: string, value: string): void
  removeAttribute(name: string): void
}

function browserLocalStorage(): StorageLike | undefined {
  return typeof window === 'undefined' ? undefined : window.localStorage
}

function browserRoot(): RootLike | undefined {
  return typeof document === 'undefined' ? undefined : document.documentElement
}

export const themeStorageKey = THEME_KEY
