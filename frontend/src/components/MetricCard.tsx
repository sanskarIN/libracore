import type { ReactNode } from 'react'

interface MetricCardProps {
  label: string
  value: ReactNode
  hint?: string
  emphasis?: 'default' | 'attention' | 'positive'
}

export function MetricCard({ label, value, hint, emphasis = 'default' }: MetricCardProps) {
  return (
    <article className={`metric-card metric-${emphasis}`}>
      <p className="metric-label">{label}</p>
      <p className="metric-value">{value}</p>
      {hint ? <p className="metric-hint">{hint}</p> : null}
    </article>
  )
}
