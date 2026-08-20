import type { ApiErrorPayload } from './types'

const DEFAULT_BASE_URL = 'http://localhost:8080/api'
const DEFAULT_TIMEOUT_MS = 15_000

export class ApiRequestError extends Error {
  readonly status: number
  readonly code: string
  readonly fieldErrors: Record<string, string>
  readonly correlationId: string | undefined

  constructor(status: number, payload: ApiErrorPayload) {
    super(payload.message || `Request failed with status ${status}`)
    this.name = 'ApiRequestError'
    this.status = status
    this.code = payload.code || 'request_failed'
    this.fieldErrors = payload.fieldErrors || {}
    this.correlationId = payload.correlationId
  }
}

export class ApiClient {
  private readonly baseUrl: string
  private readonly getToken: () => string | null
  private readonly onUnauthorized: () => void

  constructor(options: {
    baseUrl?: string
    getToken: () => string | null
    onUnauthorized?: () => void
  }) {
    this.baseUrl = normalizeBaseUrl(options.baseUrl || import.meta.env.VITE_API_BASE_URL || DEFAULT_BASE_URL)
    this.getToken = options.getToken
    this.onUnauthorized = options.onUnauthorized || (() => undefined)
  }

  async get<T>(path: string): Promise<T> {
    return this.request<T>(path, { method: 'GET' })
  }

  async post<T>(path: string, body?: unknown): Promise<T> {
    const init: RequestInit = { method: 'POST' }
    if (body !== undefined) {
      init.body = JSON.stringify(body)
    }
    return this.request<T>(path, init)
  }

  async put<T>(path: string, body: unknown): Promise<T> {
    return this.request<T>(path, { method: 'PUT', body: JSON.stringify(body) })
  }

  async patch<T>(path: string, body: unknown): Promise<T> {
    return this.request<T>(path, { method: 'PATCH', body: JSON.stringify(body) })
  }

  async postCsv<T>(path: string, csv: string): Promise<T> {
    return this.request<T>(path, {
      method: 'POST',
      body: csv,
      headers: { 'Content-Type': 'text/csv; charset=utf-8' },
    })
  }

  async downloadCsv(path: string): Promise<Blob> {
    const response = await this.fetchWithTimeout(path, { method: 'POST' })
    if (!response.ok) {
      await this.throwResponseError(response)
    }
    return response.blob()
  }

  private async request<T>(path: string, init: RequestInit): Promise<T> {
    const response = await this.fetchWithTimeout(path, init)
    if (!response.ok) {
      await this.throwResponseError(response)
    }
    if (response.status === 204) {
      return undefined as T
    }
    const text = await response.text()
    if (!text) {
      return undefined as T
    }
    return JSON.parse(text) as T
  }

  private async fetchWithTimeout(path: string, init: RequestInit): Promise<Response> {
    const controller = new AbortController()
    const timeout = window.setTimeout(() => controller.abort(), DEFAULT_TIMEOUT_MS)
    try {
      const headers = new Headers(init.headers)
      if (init.body !== undefined && !headers.has('Content-Type')) {
        headers.set('Content-Type', 'application/json')
      }
      headers.set('Accept', 'application/json, text/csv;q=0.9')
      const token = this.getToken()
      if (token) {
        headers.set('Authorization', `Bearer ${token}`)
      }
      return await fetch(this.url(path), {
        ...init,
        headers,
        signal: controller.signal,
        credentials: 'omit',
        cache: 'no-store',
      })
    } catch (error) {
      if (error instanceof DOMException && error.name === 'AbortError') {
        throw new ApiRequestError(0, {
          code: 'request_timeout',
          message: 'The server did not respond in time.',
        })
      }
      if (error instanceof ApiRequestError) throw error
      throw new ApiRequestError(0, {
        code: 'network_error',
        message: 'LibraCore could not reach the server. Check your connection and try again.',
      })
    } finally {
      window.clearTimeout(timeout)
    }
  }

  private async throwResponseError(response: Response): Promise<never> {
    let payload: ApiErrorPayload = {}
    try {
      payload = (await response.json()) as ApiErrorPayload
    } catch {
      payload = { message: `Request failed with status ${response.status}` }
    }
    if (response.status === 401) {
      this.onUnauthorized()
    }
    throw new ApiRequestError(response.status, payload)
  }

  private url(path: string): string {
    const safePath = path.startsWith('/') ? path : `/${path}`
    return `${this.baseUrl}${safePath}`
  }
}

export function normalizeBaseUrl(value: string): string {
  return value.trim().replace(/\/+$/, '')
}

export function readableError(error: unknown): string {
  if (error instanceof ApiRequestError) return error.message
  if (error instanceof Error) return error.message
  return 'Something went wrong. Please try again.'
}
