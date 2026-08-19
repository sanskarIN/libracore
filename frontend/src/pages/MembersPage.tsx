import { useCallback, useEffect, useState, type FormEvent } from 'react'
import { ApiClient, readableError } from '../api'
import { PageHeader } from '../components/PageHeader'
import { StatePanel } from '../components/StatePanel'
import { StatusBadge } from '../components/StatusBadge'
import { formatCurrency, formatDate, initials } from '../format'
import type { Branch, MemberView, Page } from '../types'

export function MembersPage({ api }: { api: ApiClient }) {
  const [query, setQuery] = useState('')
  const [status, setStatus] = useState('')
  const [members, setMembers] = useState<MemberView[]>([])
  const [selected, setSelected] = useState<MemberView | null>(null)
  const [branches, setBranches] = useState<Branch[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [notice, setNotice] = useState<string | null>(null)
  const [showCreate, setShowCreate] = useState(false)

  const search = useCallback(async (searchQuery = query, memberStatus = status) => {
    setLoading(true)
    setError(null)
    const params = new URLSearchParams({ q: searchQuery, limit: '50', offset: '0' })
    if (memberStatus) params.set('status', memberStatus)
    try {
      const page = await api.get<Page<MemberView>>(`/members?${params.toString()}`)
      setMembers(page.items)
      if (selected) {
        const refreshed = page.items.find((member) => member.id === selected.id)
        if (refreshed) setSelected(refreshed)
      }
    } catch (reason) {
      setError(readableError(reason))
    } finally {
      setLoading(false)
    }
  }, [api, query, selected, status])

  useEffect(() => {
    void api.get<Branch[]>('/catalog/branches').then(setBranches).catch((reason) => setError(readableError(reason)))
    void search('', '')
    // Initial load intentionally uses empty filters; subsequent searches are explicit.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [api])

  async function openMember(memberId: string) {
    setError(null)
    try {
      setSelected(await api.get<MemberView>(`/members/${memberId}`))
    } catch (reason) {
      setError(readableError(reason))
    }
  }

  async function createMember(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError(null)
    setNotice(null)
    const form = new FormData(event.currentTarget)
    const expiry = String(form.get('expiresAt') || '')
    try {
      const member = await api.post<MemberView>('/members', {
        homeBranchId: String(form.get('homeBranchId') || ''),
        libraryCardNumber: String(form.get('libraryCardNumber') || '').trim(),
        firstName: String(form.get('firstName') || '').trim(),
        lastName: String(form.get('lastName') || '').trim(),
        email: String(form.get('email') || '').trim(),
        phone: emptyToNull(form.get('phone')),
        expiresAt: expiry ? `${expiry}T23:59:59Z` : null,
        notes: emptyToNull(form.get('notes')),
        accountPassword: emptyToNull(form.get('accountPassword')),
      })
      event.currentTarget.reset()
      setShowCreate(false)
      setSelected(member)
      setNotice(`Created member ${member.firstName} ${member.lastName}.`)
      await search()
    } catch (reason) {
      setError(readableError(reason))
    }
  }

  async function updateMember(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!selected) return
    setError(null)
    setNotice(null)
    const form = new FormData(event.currentTarget)
    const expiry = String(form.get('expiresAt') || '')
    try {
      const member = await api.put<MemberView>(`/members/${selected.id}`, {
        homeBranchId: String(form.get('homeBranchId') || ''),
        firstName: String(form.get('firstName') || '').trim(),
        lastName: String(form.get('lastName') || '').trim(),
        email: String(form.get('email') || '').trim(),
        phone: emptyToNull(form.get('phone')),
        expiresAt: expiry ? `${expiry}T23:59:59Z` : null,
        notes: emptyToNull(form.get('notes')),
      })
      setSelected(member)
      setNotice('Member profile updated.')
      await search()
    } catch (reason) {
      setError(readableError(reason))
    }
  }

  async function changeStatus(nextStatus: MemberView['status']) {
    if (!selected) return
    setError(null)
    setNotice(null)
    try {
      const member = await api.patch<MemberView>(`/members/${selected.id}/status`, { status: nextStatus })
      setSelected(member)
      setNotice(`Member status changed to ${nextStatus.toLowerCase()}.`)
      await search()
    } catch (reason) {
      setError(readableError(reason))
    }
  }

  async function createAccount(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!selected) return
    const form = new FormData(event.currentTarget)
    setError(null)
    setNotice(null)
    try {
      const member = await api.post<MemberView>(`/members/${selected.id}/account`, {
        password: String(form.get('password') || ''),
      })
      event.currentTarget.reset()
      setSelected(member)
      setNotice('Member login account created.')
      await search()
    } catch (reason) {
      setError(readableError(reason))
    }
  }

  function submitSearch(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    void search()
  }

  return (
    <div className="page-stack">
      <PageHeader
        eyebrow="Members"
        title="Member directory"
        description="Register members, manage status and account access, and review circulation context."
        actions={<button className="button button-primary" type="button" onClick={() => setShowCreate((value) => !value)}>{showCreate ? 'Close form' : 'Add member'}</button>}
      />

      {notice ? <div className="inline-alert success" role="status">{notice}</div> : null}
      {error ? <div className="inline-alert error" role="alert">{error}</div> : null}

      {showCreate ? (
        <section className="panel">
          <div className="panel-heading"><div><p className="eyebrow">Registration</p><h2>Add member</h2></div></div>
          <form className="form-grid" onSubmit={createMember}>
            <label><span>Home branch</span><select name="homeBranchId" required defaultValue=""><option value="" disabled>Select branch</option>{branches.filter((branch) => branch.active).map((branch) => <option key={branch.id} value={branch.id}>{branch.name}</option>)}</select></label>
            <label><span>Library card number</span><input name="libraryCardNumber" required maxLength={80} /></label>
            <label><span>First name</span><input name="firstName" required maxLength={120} /></label>
            <label><span>Last name</span><input name="lastName" required maxLength={120} /></label>
            <label><span>Email</span><input name="email" type="email" inputMode="email" required maxLength={320} /></label>
            <label><span>Phone</span><input name="phone" type="tel" maxLength={40} /></label>
            <label><span>Expiry date</span><input name="expiresAt" type="date" /></label>
            <label><span>Optional account password</span><input name="accountPassword" type="password" autoComplete="new-password" minLength={12} maxLength={200} /></label>
            <label className="span-2"><span>Notes</span><textarea name="notes" rows={3} maxLength={1000} /></label>
            <div className="form-actions span-2"><button className="button button-primary" type="submit">Create member</button></div>
          </form>
        </section>
      ) : null}

      <section className="panel search-panel">
        <form className="search-form" onSubmit={submitSearch} role="search">
          <label className="search-field"><span className="sr-only">Search members</span><input value={query} onChange={(event) => setQuery(event.target.value)} maxLength={200} placeholder="Name, card number, or email" /></label>
          <label><span className="sr-only">Member status</span><select value={status} onChange={(event) => setStatus(event.target.value)}><option value="">All statuses</option><option value="ACTIVE">Active</option><option value="SUSPENDED">Suspended</option><option value="CLOSED">Closed</option></select></label>
          <button className="button button-secondary" type="submit">Search</button>
        </form>
      </section>

      <div className="split-layout">
        <section className="panel min-width-zero">
          <div className="panel-heading"><div><p className="eyebrow">Directory</p><h2>{members.length} members</h2></div></div>
          {loading ? <StatePanel kind="loading" title="Loading members" /> : null}
          {!loading && members.length === 0 ? <StatePanel kind="empty" title="No members found" /> : null}
          {!loading && members.length > 0 ? (
            <div className="member-list">
              {members.map((member) => (
                <button key={member.id} className={selected?.id === member.id ? 'member-row selected' : 'member-row'} type="button" onClick={() => void openMember(member.id)}>
                  <span className="avatar" aria-hidden="true">{initials(member.firstName, member.lastName)}</span>
                  <span className="member-row-main"><strong>{member.firstName} {member.lastName}</strong><span>{member.libraryCardNumber}</span><small>{member.email}</small></span>
                  <StatusBadge status={member.status} />
                </button>
              ))}
            </div>
          ) : null}
        </section>

        <aside className="panel detail-panel" aria-label="Member details">
          {!selected ? <StatePanel kind="empty" title="Select a member" message="Choose a member to review or update their profile." /> : (
            <div className="detail-stack">
              <div className="member-detail-heading">
                <span className="avatar large" aria-hidden="true">{initials(selected.firstName, selected.lastName)}</span>
                <div><p className="eyebrow">{selected.libraryCardNumber}</p><h2>{selected.firstName} {selected.lastName}</h2><StatusBadge status={selected.status} /></div>
              </div>
              <dl className="detail-list">
                <div><dt>Open loans</dt><dd>{selected.openLoanCount}</dd></div>
                <div><dt>Reservations</dt><dd>{selected.activeReservationCount}</dd></div>
                <div><dt>Outstanding fine</dt><dd>{formatCurrency(selected.outstandingFine, 'INR')}</dd></div>
                <div><dt>Login account</dt><dd>{selected.accountEnabled ? 'Enabled' : 'Not enabled'}</dd></div>
                <div><dt>Joined</dt><dd>{formatDate(selected.joinedAt)}</dd></div>
                <div><dt>Expires</dt><dd>{formatDate(selected.expiresAt)}</dd></div>
              </dl>

              <form className="form-stack compact-form" onSubmit={updateMember}>
                <h3>Profile</h3>
                <label><span>Home branch</span><select name="homeBranchId" defaultValue={selected.homeBranchId} required>{branches.map((branch) => <option key={branch.id} value={branch.id}>{branch.name}</option>)}</select></label>
                <div className="inline-fields"><label><span>First name</span><input name="firstName" defaultValue={selected.firstName} required maxLength={120} /></label><label><span>Last name</span><input name="lastName" defaultValue={selected.lastName} required maxLength={120} /></label></div>
                <label><span>Email</span><input name="email" type="email" defaultValue={selected.email} required maxLength={320} /></label>
                <label><span>Phone</span><input name="phone" type="tel" defaultValue={selected.phone || ''} maxLength={40} /></label>
                <label><span>Expiry date</span><input name="expiresAt" type="date" defaultValue={selected.expiresAt?.slice(0, 10) || ''} /></label>
                <label><span>Notes</span><textarea name="notes" defaultValue={selected.notes || ''} rows={3} maxLength={1000} /></label>
                <button className="button button-secondary" type="submit">Save profile</button>
              </form>

              <div className="detail-section">
                <h3>Status</h3>
                <div className="button-row">
                  {(['ACTIVE', 'SUSPENDED', 'CLOSED'] as const).map((memberStatus) => (
                    <button key={memberStatus} type="button" className="button button-quiet" disabled={selected.status === memberStatus} onClick={() => void changeStatus(memberStatus)}>{memberStatus.toLowerCase()}</button>
                  ))}
                </div>
                <p className="field-help">Closing is blocked while open loans or active reservations exist.</p>
              </div>

              {!selected.accountEnabled && selected.status === 'ACTIVE' ? (
                <form className="form-stack compact-form" onSubmit={createAccount}>
                  <h3>Create login account</h3>
                  <label><span>Temporary password</span><input name="password" type="password" autoComplete="new-password" minLength={12} maxLength={200} required /></label>
                  <p className="field-help">Share credentials through an approved private channel, never through repository issues or logs.</p>
                  <button className="button button-secondary" type="submit">Create account</button>
                </form>
              ) : null}
            </div>
          )}
        </aside>
      </div>
    </div>
  )
}

function emptyToNull(value: FormDataEntryValue | null): string | null {
  const text = String(value || '').trim()
  return text ? text : null
}
