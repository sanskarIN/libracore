import type { ReactNode } from 'react'

interface StatePanelProps {
  kind: 'loading' | 'empty' | 'error' | 'success' | 'warning'
  title: string
  message?: string
  action?: ReactNode
}

export function StatePanel({ kind, title, message, action }: StatePanelProps) {
  const symbol = {
    loading: '◌',
    empty: '○',
    error: '!',
    success: '✓',
    warning: '△',
  }[kind]

  return (
    <section className={`state-panel state-${kind}`} role={kind === 'error' ? 'alert' : 'status'}>
      <span className="state-symbol" aria-hidden="true">{symbol}</span>
      <div>
        <h2>{title}</h2>
        {message ? <p>{message}</p> : null}
        {action ? <div className="state-action">{action}</div> : null}
      </div>
    </section>
  )
}
