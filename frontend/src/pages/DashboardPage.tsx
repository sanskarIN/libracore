import { useCallback, useEffect, useState } from 'react'
import { ApiClient, readableError } from '../api'
import { MetricCard } from '../components/MetricCard'
import { PageHeader } from '../components/PageHeader'
import { StatePanel } from '../components/StatePanel'
import { StatusBadge } from '../components/StatusBadge'
import { formatCurrency, formatDate, formatDateTime } from '../format'
import type {
  DashboardView,
  FineChargeView,
  LoanView,
  MemberView,
  OverdueLoanView,
  Page,
  ReservationView,
  UserIdentity,
} from '../types'

interface DashboardPageProps {
  api: ApiClient
  user: UserIdentity
}

export function DashboardPage({ api, user }: DashboardPageProps) {
  if (user.role === 'MEMBER') {
    return <MemberDashboard api={api} />
  }
  return <StaffDashboard api={api} />
}

function StaffDashboard({ api }: { api: ApiClient }) {
  const [dashboard, setDashboard] = useState<DashboardView | null>(null)
  const [overdue, setOverdue] = useState<OverdueLoanView[]>([])
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const [summary, overduePage] = await Promise.all([
        api.get<DashboardView>('/reports/dashboard'),
        api.get<Page<OverdueLoanView>>('/reports/overdue?limit=8&offset=0'),
      ])
      setDashboard(summary)
      setOverdue(overduePage.items)
    } catch (reason) {
      setError(readableError(reason))
    } finally {
      setLoading(false)
    }
  }, [api])

  useEffect(() => {
    void load()
  }, [load])

  return (
    <div className="page-stack">
      <PageHeader
        eyebrow="Operations"
        title="Library dashboard"
        description="A current view of catalog availability, circulation pressure, members, reservations, and fines."
        actions={<button className="button button-secondary" type="button" onClick={() => void load()}>Refresh</button>}
      />

      {loading && !dashboard ? <StatePanel kind="loading" title="Loading dashboard" message="Reading current library operations…" /> : null}
      {error ? <StatePanel kind="error" title="Dashboard unavailable" message={error} action={<button className="button button-secondary" onClick={() => void load()}>Try again</button>} /> : null}

      {dashboard ? (
        <>
          <section className="metric-grid" aria-label="Library metrics">
            <MetricCard label="Books" value={dashboard.books.toLocaleString()} hint={`${dashboard.copies.toLocaleString()} physical copies`} />
            <MetricCard label="Available copies" value={dashboard.availableCopies.toLocaleString()} hint="Ready for circulation" emphasis="positive" />
            <MetricCard label="Open loans" value={dashboard.openLoans.toLocaleString()} hint={`${dashboard.overdueLoans.toLocaleString()} overdue`} emphasis={dashboard.overdueLoans > 0 ? 'attention' : 'default'} />
            <MetricCard label="Active members" value={dashboard.activeMembers.toLocaleString()} hint="Eligible accounts" />
            <MetricCard label="Waitlist" value={dashboard.waitingReservations.toLocaleString()} hint={`${dashboard.readyReservations.toLocaleString()} ready for pickup`} />
            <MetricCard label="Outstanding fines" value={formatCurrency(dashboard.outstandingFines, dashboard.fineCurrency)} hint={`Updated ${formatDateTime(dashboard.generatedAt)}`} emphasis={dashboard.outstandingFines > 0 ? 'attention' : 'default'} />
          </section>

          <section className="panel">
            <div className="panel-heading">
              <div>
                <p className="eyebrow">Attention queue</p>
                <h2>Overdue loans</h2>
              </div>
              <a className="button button-quiet" href="#/reports">Open reports</a>
            </div>
            {overdue.length === 0 ? (
              <StatePanel kind="success" title="No overdue loans" message="There are no currently overdue open loans in this view." />
            ) : (
              <div className="table-scroll">
                <table>
                  <thead>
                    <tr>
                      <th scope="col">Member</th>
                      <th scope="col">Book</th>
                      <th scope="col">Due</th>
                      <th scope="col">Days overdue</th>
                      <th scope="col">Branch</th>
                    </tr>
                  </thead>
                  <tbody>
                    {overdue.map((loan) => (
                      <tr key={loan.loanId}>
                        <td><strong>{loan.memberName}</strong><small>{loan.libraryCardNumber}</small></td>
                        <td>{loan.bookTitle}<small>{loan.accessionCode}</small></td>
                        <td>{formatDate(loan.dueAt)}</td>
                        <td><StatusBadge status="OVERDUE" /> <span className="numeric">{loan.overdueDays}</span></td>
                        <td>{loan.branchName}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </section>
        </>
      ) : null}
    </div>
  )
}

function MemberDashboard({ api }: { api: ApiClient }) {
  const [member, setMember] = useState<MemberView | null>(null)
  const [loans, setLoans] = useState<LoanView[]>([])
  const [reservations, setReservations] = useState<ReservationView[]>([])
  const [fines, setFines] = useState<FineChargeView[]>([])
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const [profile, loanPage, reservationPage, finePage] = await Promise.all([
        api.get<MemberView>('/members/me'),
        api.get<Page<LoanView>>('/circulation/loans/me?status=OPEN&limit=10&offset=0'),
        api.get<Page<ReservationView>>('/circulation/reservations/me?limit=10&offset=0'),
        api.get<Page<FineChargeView>>('/fines/me?status=OUTSTANDING&limit=10&offset=0'),
      ])
      setMember(profile)
      setLoans(loanPage.items)
      setReservations(reservationPage.items.filter((item) => item.status === 'WAITING' || item.status === 'READY'))
      setFines(finePage.items)
    } catch (reason) {
      setError(readableError(reason))
    } finally {
      setLoading(false)
    }
  }, [api])

  useEffect(() => {
    void load()
  }, [load])

  if (loading && !member) {
    return <StatePanel kind="loading" title="Loading your library account" />
  }
  if (error && !member) {
    return <StatePanel kind="error" title="Account dashboard unavailable" message={error} action={<button className="button button-secondary" onClick={() => void load()}>Try again</button>} />
  }

  const outstanding = fines.reduce((sum, fine) => sum + fine.amount, 0)
  const currency = fines[0]?.currencyCode ?? 'INR'

  return (
    <div className="page-stack">
      <PageHeader
        eyebrow="My library"
        title={member ? `Welcome, ${member.firstName}` : 'My dashboard'}
        description="Review your loans, due dates, reservations, and account status."
        actions={<button className="button button-secondary" type="button" onClick={() => void load()}>Refresh</button>}
      />
      {error ? <div className="inline-alert error" role="alert">{error}</div> : null}
      {member ? (
        <section className="metric-grid" aria-label="Account metrics">
          <MetricCard label="Open loans" value={loans.length} hint={`${loans.filter((loan) => loan.overdue).length} overdue`} emphasis={loans.some((loan) => loan.overdue) ? 'attention' : 'default'} />
          <MetricCard label="Active reservations" value={reservations.length} hint={`${reservations.filter((item) => item.status === 'READY').length} ready`} />
          <MetricCard label="Outstanding fines" value={formatCurrency(outstanding, currency)} emphasis={outstanding > 0 ? 'attention' : 'default'} />
          <MetricCard label="Membership" value={<StatusBadge status={member.status} />} hint={member.expiresAt ? `Expires ${formatDate(member.expiresAt)}` : 'No expiry recorded'} />
        </section>
      ) : null}

      <section className="panel">
        <div className="panel-heading"><div><p className="eyebrow">Circulation</p><h2>Current loans</h2></div></div>
        {loans.length === 0 ? <StatePanel kind="empty" title="No open loans" message="Your currently issued books will appear here." /> : (
          <div className="table-scroll">
            <table>
              <thead><tr><th scope="col">Book</th><th scope="col">Due</th><th scope="col">Status</th><th scope="col">Renewals</th></tr></thead>
              <tbody>{loans.map((loan) => (
                <tr key={loan.id}>
                  <td><strong>{loan.bookTitle}</strong><small>{loan.accessionCode}</small></td>
                  <td>{formatDate(loan.dueAt)}</td>
                  <td><StatusBadge status={loan.overdue ? 'OVERDUE' : loan.status} /></td>
                  <td className="numeric">{loan.renewalCount}</td>
                </tr>
              ))}</tbody>
            </table>
          </div>
        )}
      </section>

      <section className="panel">
        <div className="panel-heading"><div><p className="eyebrow">Requests</p><h2>Reservations</h2></div></div>
        {reservations.length === 0 ? <StatePanel kind="empty" title="No active reservations" /> : (
          <div className="card-list">{reservations.map((reservation) => (
            <article className="list-card" key={reservation.id}>
              <div><strong>{reservation.bookTitle}</strong><p>{reservation.pickupBranchName}</p></div>
              <div className="list-card-meta"><StatusBadge status={reservation.status} />{reservation.queuePosition > 0 ? <span>Queue #{reservation.queuePosition}</span> : null}</div>
            </article>
          ))}</div>
        )}
      </section>
    </div>
  )
}
