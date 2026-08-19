import { useCallback, useEffect, useState, type ChangeEvent } from 'react'
import { ApiClient, readableError } from '../api'
import { MetricCard } from '../components/MetricCard'
import { PageHeader } from '../components/PageHeader'
import { StatePanel } from '../components/StatePanel'
import { StatusBadge } from '../components/StatusBadge'
import { formatCurrency, formatDateTime } from '../format'
import type { AuditEventView, DashboardView, OverdueLoanView, Page, UserIdentity } from '../types'

interface ImportResult {
  resource: string
  importedRows: number
  warnings: string[]
}

export function ReportsPage({ api, user }: { api: ApiClient; user: UserIdentity }) {
  const [dashboard, setDashboard] = useState<DashboardView | null>(null)
  const [overdue, setOverdue] = useState<OverdueLoanView[]>([])
  const [audit, setAudit] = useState<AuditEventView[]>([])
  const [error, setError] = useState<string | null>(null)
  const [notice, setNotice] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)
  const [importing, setImporting] = useState(false)

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const [summary, overduePage] = await Promise.all([
        api.get<DashboardView>('/reports/dashboard'),
        api.get<Page<OverdueLoanView>>('/reports/overdue?limit=100&offset=0'),
      ])
      setDashboard(summary)
      setOverdue(overduePage.items)
      if (user.role === 'ADMIN') {
        const auditPage = await api.get<Page<AuditEventView>>('/reports/audit?limit=100&offset=0')
        setAudit(auditPage.items)
      }
    } catch (reason) {
      setError(readableError(reason))
    } finally {
      setLoading(false)
    }
  }, [api, user.role])

  useEffect(() => { void load() }, [load])

  async function exportCsv(resource: 'books' | 'members') {
    setError(null)
    setNotice(null)
    try {
      const blob = await api.downloadCsv(`/exchange/${resource}/export`)
      downloadBlob(blob, `libracore-${resource}.csv`)
      setNotice(`${resource === 'books' ? 'Catalog' : 'Member'} export prepared.`)
    } catch (reason) {
      setError(readableError(reason))
    }
  }

  async function importCsv(resource: 'books' | 'members', event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0]
    event.target.value = ''
    if (!file) return
    if (file.size > 2_000_000) {
      setError('CSV import must be 2 MB or smaller.')
      return
    }
    setImporting(true)
    setError(null)
    setNotice(null)
    try {
      const csv = await file.text()
      const result = await api.postCsv<ImportResult>(`/exchange/${resource}/import`, csv)
      const warningText = result.warnings.length ? ` ${result.warnings.length} warning(s) require review.` : ''
      setNotice(`Imported ${result.importedRows} ${result.resource} row(s).${warningText}`)
      await load()
    } catch (reason) {
      setError(readableError(reason))
    } finally {
      setImporting(false)
    }
  }

  return (
    <div className="page-stack">
      <PageHeader
        eyebrow="Reports"
        title="Operations and data"
        description="Review current circulation pressure, overdue work, audit history, and bounded CSV exchange."
        actions={<button className="button button-secondary" type="button" onClick={() => void load()}>Refresh</button>}
      />

      {notice ? <div className="inline-alert success" role="status">{notice}</div> : null}
      {error ? <div className="inline-alert error" role="alert">{error}</div> : null}
      {loading && !dashboard ? <StatePanel kind="loading" title="Loading reports" /> : null}

      {dashboard ? (
        <section className="metric-grid" aria-label="Report summary">
          <MetricCard label="Catalog copies" value={dashboard.copies.toLocaleString()} hint={`${dashboard.availableCopies.toLocaleString()} available`} />
          <MetricCard label="Open loans" value={dashboard.openLoans.toLocaleString()} hint={`${dashboard.overdueLoans.toLocaleString()} overdue`} emphasis={dashboard.overdueLoans ? 'attention' : 'default'} />
          <MetricCard label="Active members" value={dashboard.activeMembers.toLocaleString()} />
          <MetricCard label="Waitlisted" value={dashboard.waitingReservations.toLocaleString()} hint={`${dashboard.readyReservations.toLocaleString()} ready`} />
          <MetricCard label="Outstanding fines" value={formatCurrency(dashboard.outstandingFines, dashboard.fineCurrency)} emphasis={dashboard.outstandingFines ? 'attention' : 'default'} />
        </section>
      ) : null}

      <section className="panel">
        <div className="panel-heading"><div><p className="eyebrow">Action queue</p><h2>Overdue loans</h2></div><span className="count-pill">{overdue.length}</span></div>
        {overdue.length === 0 && !loading ? <StatePanel kind="success" title="No overdue loans" /> : (
          <div className="table-scroll"><table>
            <thead><tr><th scope="col">Member</th><th scope="col">Book / copy</th><th scope="col">Due</th><th scope="col">Overdue</th><th scope="col">Branch</th><th scope="col">Contact</th></tr></thead>
            <tbody>{overdue.map((loan) => (
              <tr key={loan.loanId}>
                <td><strong>{loan.memberName}</strong><small>{loan.libraryCardNumber}</small></td>
                <td><strong>{loan.bookTitle}</strong><small>{loan.accessionCode}</small></td>
                <td>{formatDateTime(loan.dueAt)}</td>
                <td><StatusBadge status="OVERDUE" /> <span className="numeric">{loan.overdueDays} day(s)</span></td>
                <td>{loan.branchName}</td>
                <td><a href={`mailto:${loan.memberEmail}`}>Email member</a></td>
              </tr>
            ))}</tbody>
          </table></div>
        )}
      </section>

      <section className="panel">
        <div className="panel-heading"><div><p className="eyebrow">Data exchange</p><h2>CSV import and export</h2></div></div>
        <p className="panel-copy">Operational CSV is bounded to 10,000 export rows and 2 MB imports. Use database backups for complete disaster recovery.</p>
        <div className="data-actions">
          <article className="data-action-card">
            <div><h3>Catalog books</h3><p>Export or import catalog metadata. Authors and categories use pipe-separated values inside their CSV cells.</p></div>
            <div className="button-row"><button className="button button-secondary" type="button" onClick={() => void exportCsv('books')}>Export CSV</button><label className="button button-quiet file-button">Import CSV<input type="file" accept="text/csv,.csv" disabled={importing} onChange={(event) => void importCsv('books', event)} /></label></div>
          </article>
          <article className="data-action-card">
            <div><h3>Members</h3><p>Exports contain personal member data. Handle files according to your organization’s privacy and retention policy.</p></div>
            <div className="button-row"><button className="button button-secondary" type="button" onClick={() => void exportCsv('members')}>Export CSV</button><label className="button button-quiet file-button">Import CSV<input type="file" accept="text/csv,.csv" disabled={importing} onChange={(event) => void importCsv('members', event)} /></label></div>
          </article>
        </div>
      </section>

      {user.role === 'ADMIN' ? (
        <section className="panel">
          <div className="panel-heading"><div><p className="eyebrow">Administrator</p><h2>Audit trail</h2></div><span className="count-pill">{audit.length}</span></div>
          {audit.length === 0 && !loading ? <StatePanel kind="empty" title="No audit events found" /> : (
            <div className="table-scroll"><table>
              <thead><tr><th scope="col">Time</th><th scope="col">Action</th><th scope="col">Actor</th><th scope="col">Entity</th><th scope="col">Outcome</th></tr></thead>
              <tbody>{audit.map((event) => (
                <tr key={event.id}>
                  <td>{formatDateTime(event.occurredAt)}</td>
                  <td><code>{event.action}</code></td>
                  <td>{event.actorEmail || 'System'}</td>
                  <td>{event.entityType}<small>{event.entityId || '—'}</small></td>
                  <td><StatusBadge status={event.outcome} /></td>
                </tr>
              ))}</tbody>
            </table></div>
          )}
        </section>
      ) : null}
    </div>
  )
}

function downloadBlob(blob: Blob, filename: string) {
  const url = URL.createObjectURL(blob)
  const anchor = document.createElement('a')
  anchor.href = url
  anchor.download = filename
  anchor.rel = 'noopener'
  document.body.append(anchor)
  anchor.click()
  anchor.remove()
  window.setTimeout(() => URL.revokeObjectURL(url), 0)
}
