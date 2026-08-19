import { useCallback, useEffect, useState, type FormEvent } from 'react'
import { ApiClient, readableError } from '../api'
import { PageHeader } from '../components/PageHeader'
import { StatePanel } from '../components/StatePanel'
import { StatusBadge } from '../components/StatusBadge'
import { formatCurrency, formatDate, formatDateTime } from '../format'
import type {
  Branch,
  CopyView,
  FineChargeView,
  FineRuleView,
  LoanView,
  MemberView,
  Page,
  ReservationView,
  UserIdentity,
} from '../types'

interface ReturnResult {
  loan: LoanView
  fine: {
    overdueDays: number
    amount: number
    currencyCode: string
    fineChargeId?: string
  }
  promotedReservation?: ReservationView
}

export function CirculationPage({ api, user }: { api: ApiClient; user: UserIdentity }) {
  const [memberQuery, setMemberQuery] = useState('')
  const [memberResults, setMemberResults] = useState<MemberView[]>([])
  const [member, setMember] = useState<MemberView | null>(null)
  const [loans, setLoans] = useState<LoanView[]>([])
  const [fines, setFines] = useState<FineChargeView[]>([])
  const [copyCode, setCopyCode] = useState('')
  const [copy, setCopy] = useState<CopyView | null>(null)
  const [branches, setBranches] = useState<Branch[]>([])
  const [policies, setPolicies] = useState<FineRuleView[]>([])
  const [error, setError] = useState<string | null>(null)
  const [notice, setNotice] = useState<string | null>(null)
  const [loadingMember, setLoadingMember] = useState(false)

  const loadMemberContext = useCallback(async (memberId: string) => {
    const [profile, loanPage, finePage] = await Promise.all([
      api.get<MemberView>(`/members/${memberId}`),
      api.get<Page<LoanView>>(`/circulation/loans?memberId=${encodeURIComponent(memberId)}&status=OPEN&limit=50&offset=0`),
      api.get<Page<FineChargeView>>(`/fines?memberId=${encodeURIComponent(memberId)}&limit=50&offset=0`),
    ])
    setMember(profile)
    setLoans(loanPage.items)
    setFines(finePage.items)
  }, [api])

  const loadPolicies = useCallback(async () => {
    const [branchList, policyList] = await Promise.all([
      api.get<Branch[]>('/catalog/branches'),
      api.get<FineRuleView[]>('/circulation/policies'),
    ])
    setBranches(branchList)
    setPolicies(policyList)
  }, [api])

  useEffect(() => {
    void loadPolicies().catch((reason) => setError(readableError(reason)))
  }, [loadPolicies])

  async function searchMembers(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError(null)
    setLoadingMember(true)
    try {
      const params = new URLSearchParams({ q: memberQuery, status: 'ACTIVE', limit: '20', offset: '0' })
      const page = await api.get<Page<MemberView>>(`/members?${params.toString()}`)
      setMemberResults(page.items)
    } catch (reason) {
      setError(readableError(reason))
    } finally {
      setLoadingMember(false)
    }
  }

  async function selectMember(next: MemberView) {
    setError(null)
    setNotice(null)
    try {
      await loadMemberContext(next.id)
    } catch (reason) {
      setError(readableError(reason))
    }
  }

  async function lookupCopy(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError(null)
    setNotice(null)
    try {
      const found = await api.get<CopyView>(`/catalog/copies/lookup?code=${encodeURIComponent(copyCode)}`)
      setCopy(found)
    } catch (reason) {
      setCopy(null)
      setError(readableError(reason))
    }
  }

  async function issueCopy() {
    if (!member || !copy) {
      setError('Select an active member and look up a copy first.')
      return
    }
    setError(null)
    setNotice(null)
    try {
      const loan = await api.post<LoanView>('/circulation/loans', { copyId: copy.id, memberId: member.id })
      setNotice(`Issued ${loan.bookTitle} to ${loan.memberName}; due ${formatDate(loan.dueAt)}.`)
      setCopy(null)
      setCopyCode('')
      await loadMemberContext(member.id)
    } catch (reason) {
      setError(readableError(reason))
    }
  }

  async function returnLoan(loan: LoanView) {
    if (!member) return
    setError(null)
    setNotice(null)
    try {
      const result = await api.post<ReturnResult>(`/circulation/loans/${loan.id}/return`)
      const fineMessage = result.fine.amount > 0
        ? ` Fine assessed: ${formatCurrency(result.fine.amount, result.fine.currencyCode)}.`
        : ''
      const holdMessage = result.promotedReservation
        ? ` A waiting reservation is now ready for ${result.promotedReservation.libraryCardNumber}.`
        : ''
      setNotice(`Returned ${result.loan.bookTitle}.${fineMessage}${holdMessage}`)
      await loadMemberContext(member.id)
    } catch (reason) {
      setError(readableError(reason))
    }
  }

  async function renewLoan(loan: LoanView) {
    if (!member) return
    setError(null)
    setNotice(null)
    try {
      const renewed = await api.post<LoanView>(`/circulation/loans/${loan.id}/renew`)
      setNotice(`Renewed ${renewed.bookTitle}; new due date ${formatDate(renewed.dueAt)}.`)
      await loadMemberContext(member.id)
    } catch (reason) {
      setError(readableError(reason))
    }
  }

  async function settleFine(fine: FineChargeView, status: 'PAID' | 'WAIVED') {
    if (!member) return
    setError(null)
    setNotice(null)
    try {
      await api.post<FineChargeView>(`/fines/${fine.id}/settle`, {
        status,
        note: status === 'WAIVED' ? 'Waived by staff from circulation desk.' : 'Payment recorded by staff.',
      })
      setNotice(status === 'PAID' ? 'Fine payment recorded.' : 'Fine waived.')
      await loadMemberContext(member.id)
    } catch (reason) {
      setError(readableError(reason))
    }
  }

  async function createPolicy(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    setError(null)
    setNotice(null)
    try {
      const policy = await api.post<FineRuleView>('/circulation/policies', {
        branchId: String(form.get('branchId') || ''),
        name: String(form.get('name') || '').trim(),
        dailyRate: Number(form.get('dailyRate')),
        graceDays: Number(form.get('graceDays')),
        maxFine: String(form.get('maxFine') || '').trim() ? Number(form.get('maxFine')) : null,
        currencyCode: String(form.get('currencyCode') || 'INR').trim().toUpperCase(),
        maxRenewals: Number(form.get('maxRenewals')),
        loanPeriodDays: Number(form.get('loanPeriodDays')),
        reservationHoldDays: Number(form.get('reservationHoldDays')),
      })
      event.currentTarget.reset()
      setNotice(`Created circulation policy “${policy.name}”.`)
      await loadPolicies()
    } catch (reason) {
      setError(readableError(reason))
    }
  }

  return (
    <div className="page-stack">
      <PageHeader
        eyebrow="Circulation"
        title="Issue and return desk"
        description="Select a member, scan or enter a copy code, and keep each circulation change transactional and auditable."
      />

      {notice ? <div className="inline-alert success" role="status">{notice}</div> : null}
      {error ? <div className="inline-alert error" role="alert">{error}</div> : null}

      <section className="workspace-grid">
        <div className="panel">
          <div className="panel-heading"><div><p className="eyebrow">Step 1</p><h2>Select member</h2></div></div>
          <form className="search-form" onSubmit={searchMembers} role="search">
            <label className="search-field"><span className="sr-only">Search member</span><input value={memberQuery} onChange={(event) => setMemberQuery(event.target.value)} placeholder="Card, name, or email" maxLength={200} /></label>
            <button className="button button-secondary" type="submit">Search</button>
          </form>
          {loadingMember ? <p className="muted">Searching…</p> : null}
          <div className="selection-list">
            {memberResults.map((result) => (
              <button type="button" key={result.id} className={member?.id === result.id ? 'selection-row selected' : 'selection-row'} onClick={() => void selectMember(result)}>
                <span><strong>{result.firstName} {result.lastName}</strong><small>{result.libraryCardNumber} · {result.email}</small></span>
                <StatusBadge status={result.status} />
              </button>
            ))}
          </div>
          {member ? (
            <div className="selected-summary">
              <div><p className="eyebrow">Selected member</p><h3>{member.firstName} {member.lastName}</h3><p>{member.libraryCardNumber} · {member.homeBranchName}</p></div>
              <dl className="mini-stats"><div><dt>Loans</dt><dd>{member.openLoanCount}</dd></div><div><dt>Fines</dt><dd>{formatCurrency(member.outstandingFine, 'INR')}</dd></div></dl>
            </div>
          ) : null}
        </div>

        <div className="panel">
          <div className="panel-heading"><div><p className="eyebrow">Step 2</p><h2>Scan or enter copy</h2></div></div>
          <form className="search-form" onSubmit={lookupCopy}>
            <label className="search-field"><span className="sr-only">Copy code</span><input value={copyCode} onChange={(event) => setCopyCode(event.target.value)} placeholder="Barcode, accession, or QR value" maxLength={300} /></label>
            <button className="button button-secondary" type="submit">Look up</button>
          </form>
          {copy ? (
            <div className="selected-summary">
              <div><p className="eyebrow">Copy found</p><h3>{copy.accessionCode}</h3><p>{copy.branchName}{copy.shelfLabel ? ` · ${copy.shelfLabel}` : ''}</p></div>
              <StatusBadge status={copy.status} />
              <button className="button button-primary" type="button" disabled={!member || !['AVAILABLE', 'RESERVED'].includes(copy.status)} onClick={() => void issueCopy()}>Issue copy</button>
            </div>
          ) : <StatePanel kind="empty" title="No copy selected" message="Scan a barcode/QR value or enter an accession code." />}
        </div>
      </section>

      <section className="panel">
        <div className="panel-heading"><div><p className="eyebrow">Selected member</p><h2>Open loans</h2></div></div>
        {!member ? <StatePanel kind="empty" title="Select a member first" /> : loans.length === 0 ? <StatePanel kind="empty" title="No open loans" /> : (
          <div className="table-scroll"><table>
            <thead><tr><th scope="col">Book</th><th scope="col">Issued</th><th scope="col">Due</th><th scope="col">Status</th><th scope="col">Actions</th></tr></thead>
            <tbody>{loans.map((loan) => (
              <tr key={loan.id}>
                <td><strong>{loan.bookTitle}</strong><small>{loan.accessionCode}</small></td>
                <td>{formatDate(loan.issuedAt)}</td>
                <td>{formatDate(loan.dueAt)}</td>
                <td><StatusBadge status={loan.overdue ? 'OVERDUE' : loan.status} /></td>
                <td><div className="button-row"><button className="button button-quiet" type="button" onClick={() => void renewLoan(loan)}>Renew</button><button className="button button-secondary" type="button" onClick={() => void returnLoan(loan)}>Return</button></div></td>
              </tr>
            ))}</tbody>
          </table></div>
        )}
      </section>

      <section className="panel">
        <div className="panel-heading"><div><p className="eyebrow">Selected member</p><h2>Fine ledger</h2></div></div>
        {!member ? <StatePanel kind="empty" title="Select a member first" /> : fines.length === 0 ? <StatePanel kind="empty" title="No fine charges" /> : (
          <div className="table-scroll"><table>
            <thead><tr><th scope="col">Book</th><th scope="col">Amount</th><th scope="col">Assessed</th><th scope="col">Status</th><th scope="col">Actions</th></tr></thead>
            <tbody>{fines.map((fine) => (
              <tr key={fine.id}>
                <td><strong>{fine.bookTitle}</strong><small>{fine.reason}</small></td>
                <td>{formatCurrency(fine.amount, fine.currencyCode)}</td>
                <td>{formatDateTime(fine.assessedAt)}</td>
                <td><StatusBadge status={fine.status} /></td>
                <td>{fine.status === 'OUTSTANDING' ? <div className="button-row"><button className="button button-secondary" type="button" onClick={() => void settleFine(fine, 'PAID')}>Record paid</button><button className="button button-quiet" type="button" onClick={() => void settleFine(fine, 'WAIVED')}>Waive</button></div> : '—'}</td>
              </tr>
            ))}</tbody>
          </table></div>
        )}
      </section>

      <section className="panel">
        <div className="panel-heading"><div><p className="eyebrow">Policy</p><h2>Circulation rules</h2></div></div>
        <div className="card-list">
          {policies.map((policy) => (
            <article className="list-card" key={policy.id}>
              <div><strong>{policy.name}</strong><p>{policy.branchName} · {policy.loanPeriodDays} day loan · {policy.maxRenewals} renewals</p><small>Fine {formatCurrency(policy.dailyRate, policy.currencyCode)}/day after {policy.graceDays} grace day(s) · hold {policy.reservationHoldDays} day(s)</small></div>
              <div className="list-card-meta"><StatusBadge status={policy.active ? 'ACTIVE' : 'CLOSED'} /><span>From {formatDate(policy.effectiveFrom)}</span></div>
            </article>
          ))}
        </div>
        {user.role === 'ADMIN' ? (
          <form className="form-grid policy-form" onSubmit={createPolicy}>
            <h3 className="span-2">Schedule a new policy</h3>
            <label><span>Branch</span><select name="branchId" required defaultValue=""><option value="" disabled>Select branch</option>{branches.filter((branch) => branch.active).map((branch) => <option key={branch.id} value={branch.id}>{branch.name}</option>)}</select></label>
            <label><span>Name</span><input name="name" required maxLength={160} /></label>
            <label><span>Daily fine</span><input name="dailyRate" type="number" min="0" step="0.01" defaultValue="2.00" required /></label>
            <label><span>Grace days</span><input name="graceDays" type="number" min="0" max="365" defaultValue="0" required /></label>
            <label><span>Maximum fine</span><input name="maxFine" type="number" min="0" step="0.01" defaultValue="500.00" /></label>
            <label><span>Currency</span><input name="currencyCode" pattern="[A-Z]{3}" defaultValue="INR" maxLength={3} required /></label>
            <label><span>Max renewals</span><input name="maxRenewals" type="number" min="0" max="20" defaultValue="2" required /></label>
            <label><span>Loan period days</span><input name="loanPeriodDays" type="number" min="1" max="365" defaultValue="14" required /></label>
            <label><span>Reservation hold days</span><input name="reservationHoldDays" type="number" min="1" max="30" defaultValue="3" required /></label>
            <div className="form-actions"><button className="button button-primary" type="submit">Create policy</button></div>
          </form>
        ) : null}
      </section>
    </div>
  )
}
