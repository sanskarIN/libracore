import { describe, expect, it } from 'vitest'
import { formatCurrency, formatDate, initials } from './format'

describe('format helpers', () => {
  it('formats INR currency without hard-coded symbols', () => {
    expect(formatCurrency(149.5, 'INR', 'en-IN')).toContain('149.50')
  })

  it('uses a fallback marker for invalid dates', () => {
    expect(formatDate('not-a-date', 'en-IN')).toBe('—')
  })

  it('builds readable initials', () => {
    expect(initials('Ada', 'Lovelace')).toBe('AL')
  })
})
