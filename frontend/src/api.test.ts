import { describe, expect, it } from 'vitest'
import { ApiRequestError, normalizeBaseUrl, readableError } from './api'

describe('API helpers', () => {
  it('normalizes trailing slashes without changing the origin', () => {
    expect(normalizeBaseUrl(' https://example.test/api/// ')).toBe('https://example.test/api')
  })

  it('exposes safe API error messages', () => {
    const error = new ApiRequestError(400, { code: 'validation_failed', message: 'Invalid request.' })
    expect(readableError(error)).toBe('Invalid request.')
    expect(error.code).toBe('validation_failed')
  })
})
