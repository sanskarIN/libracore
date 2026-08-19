interface StatusBadgeProps {
  status: string
}

const SYMBOLS: Record<string, string> = {
  AVAILABLE: '✓',
  ACTIVE: '✓',
  OPEN: '●',
  READY: '✓',
  SENT: '✓',
  SUCCESS: '✓',
  ON_LOAN: '↗',
  WAITING: '…',
  PENDING: '…',
  RESERVED: '◆',
  OVERDUE: '!',
  OUTSTANDING: '!',
  SUSPENDED: '!',
  FAILURE: '×',
  DENIED: '×',
  LOST: '×',
  CLOSED: '—',
  RETURNED: '✓',
  FULFILLED: '✓',
  PAID: '✓',
  WAIVED: '✓',
  CANCELLED: '—',
  EXPIRED: '—',
  REPAIR: '◆',
  WITHDRAWN: '—',
}

export function StatusBadge({ status }: StatusBadgeProps) {
  const normalized = status.toUpperCase()
  const symbol = SYMBOLS[normalized] ?? '•'
  const label = normalized.replaceAll('_', ' ')
  return (
    <span className={`status-badge status-${normalized.toLowerCase()}`}>
      <span aria-hidden="true">{symbol}</span>
      <span>{label}</span>
    </span>
  )
}
