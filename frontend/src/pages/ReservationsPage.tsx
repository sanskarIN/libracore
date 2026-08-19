import { useCallback, useEffect, useState, type FormEvent } from 'react'
import { ApiClient, readableError } from '../api'
import { PageHeader } from '../components/PageHeader'
import { StatePanel } from '../components/StatePanel'
import { StatusBadge } from '../components/StatusBadge'
import { formatDateTime } from '../format'
import type { BookSummary, Branch, MemberView, Page, ReservationView, UserIdentity } from '../types'

export function ReservationsPage({ api, user }: { api: ApiClient; user: UserIdentity }) {
  return user.role === 'MEMBER'
    ? <MemberReservations api={api} />
    : <StaffReservations api={api} />
}

function MemberReservations({ api }: { api: ApiClient }) {
  const [reservations, setReservations] = useState<ReservationView[]>([])
  const [error, setError] = useState<string | null>(null)
  const [notice, setNotice] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const page = await api.get<Page<ReservationView>>('/circulation/reservations/me?limit=100&offset=0')
      setReservations(page.items)
    } catch (reason) {
      setError(readableError(reason))
    } finally {
      setLoading(false)
    }
  }, [api])

  useEffect(() => { void load() }, [load])

  async function cancel(reservation: ReservationView) {
    setError(null)
    setNotice(null)
    try {
      await api.post<ReservationView>(`/circulation/reservations/${reservation.id}/cancel`)
      setNotice(`Cancelled reservation for “${reservation.bookTitle}”.`)
      await load()
    } catch (reason) {
      setError(readableError(reason))
    }
  }

  return (
    <div className="page-stack">
      <PageHeader eyebrow="My library" title="Reservations" description="Track waitlist position, pickup readiness, expiry, and reservation history." actions={<a className="button button-primary" href="#/catalog">Find a book</a>} />
      {notice ? <div className="inline-alert success" role="status">{notice}</div> : null}
      {error ? <div className="inline-alert error" role="alert">{error}</div> : null}
      <section className="panel">
        <div className="panel-heading"><div><p className="eyebrow">Requests</p><h2>Your reservations</h2></div></div>
        {loading ? <StatePanel kind="loading" title="Loading reservations" /> : null}
        {!loading && reservations.length === 0 ? <StatePanel kind="empty" title="No reservations yet" message="Find a title in the catalog and choose a pickup branch to reserve it." /> : null}
        {!loading && reservations.length > 0 ? (
          <div className="card-list">{reservations.map((reservation) => (
            <article className="list-card reservation-card" key={reservation.id}>
              <div>
                <strong>{reservation.bookTitle}</strong>
                <p>{reservation.pickupBranchName}</p>
                <small>Requested {formatDateTime(reservation.requestedAt)}</small>
              </div>
              <div className="list-card-meta">
                <StatusBadge status={reservation.status} />
                {reservation.status === 'WAITING' ? <span>Queue #{reservation.queuePosition}</span> : null}
                {reservation.status === 'READY' && reservation.expiresAt ? <span>Hold until {formatDateTime(reservation.expiresAt)}</span> : null}
                {['WAITING', 'READY'].includes(reservation.status) ? <button className="button button-quiet" type="button" onClick={() => void cancel(reservation)}>Cancel</button> : null}
              </div>
            </article>
          ))}</div>
        ) : null}
      </section>
    </div>
  )
}

function StaffReservations({ api }: { api: ApiClient }) {
  const [memberQuery, setMemberQuery] = useState('')
  const [memberResults, setMemberResults] = useState<MemberView[]>([])
  const [member, setMember] = useState<MemberView | null>(null)
  const [reservations, setReservations] = useState<ReservationView[]>([])
  const [bookQuery, setBookQuery] = useState('')
  const [books, setBooks] = useState<BookSummary[]>([])
  const [selectedBook, setSelectedBook] = useState<BookSummary | null>(null)
  const [branches, setBranches] = useState<Branch[]>([])
  const [branchId, setBranchId] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [notice, setNotice] = useState<string | null>(null)

  useEffect(() => {
    void api.get<Branch[]>('/catalog/branches').then(setBranches).catch((reason) => setError(readableError(reason)))
  }, [api])

  const loadReservations = useCallback(async (memberId: string) => {
    const page = await api.get<Page<ReservationView>>(`/circulation/reservations?memberId=${encodeURIComponent(memberId)}&limit=100&offset=0`)
    setReservations(page.items)
  }, [api])

  async function searchMembers(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError(null)
    try {
      const params = new URLSearchParams({ q: memberQuery, status: 'ACTIVE', limit: '20', offset: '0' })
      const page = await api.get<Page<MemberView>>(`/members?${params.toString()}`)
      setMemberResults(page.items)
    } catch (reason) {
      setError(readableError(reason))
    }
  }

  async function selectMember(next: MemberView) {
    setMember(next)
    setNotice(null)
    setError(null)
    try {
      await loadReservations(next.id)
    } catch (reason) {
      setError(readableError(reason))
    }
  }

  async function searchBooks(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError(null)
    const params = new URLSearchParams({ q: bookQuery, limit: '20', offset: '0', availableOnly: 'false' })
    if (branchId) params.set('branchId', branchId)
    try {
      const page = await api.get<Page<BookSummary>>(`/catalog/books?${params.toString()}`)
      setBooks(page.items)
    } catch (reason) {
      setError(readableError(reason))
    }
  }

  async function createReservation() {
    if (!member || !selectedBook || !branchId) {
      setError('Select a member, a book, and a pickup branch first.')
      return
    }
    setError(null)
    setNotice(null)
    try {
      const reservation = await api.post<ReservationView>('/circulation/reservations', {
        bookId: selectedBook.id,
        memberId: member.id,
        pickupBranchId: branchId,
      })
      setNotice(reservation.status === 'READY'
        ? `Reservation is ready at ${reservation.pickupBranchName}.`
        : `Reservation added to queue position ${reservation.queuePosition}.`)
      setSelectedBook(null)
      await loadReservations(member.id)
    } catch (reason) {
      setError(readableError(reason))
    }
  }

  async function cancel(reservation: ReservationView) {
    if (!member) return
    setError(null)
    setNotice(null)
    try {
      await api.post<ReservationView>(`/circulation/reservations/${reservation.id}/cancel`)
      setNotice(`Cancelled reservation for “${reservation.bookTitle}”.`)
      await loadReservations(member.id)
    } catch (reason) {
      setError(readableError(reason))
    }
  }

  return (
    <div className="page-stack">
      <PageHeader eyebrow="Reservations" title="Waitlist and pickup desk" description="Place assisted reservations, review queue state, and cancel active holds when requested." />
      {notice ? <div className="inline-alert success" role="status">{notice}</div> : null}
      {error ? <div className="inline-alert error" role="alert">{error}</div> : null}

      <section className="workspace-grid">
        <div className="panel">
          <div className="panel-heading"><div><p className="eyebrow">Member</p><h2>Select account</h2></div></div>
          <form className="search-form" onSubmit={searchMembers} role="search">
            <label className="search-field"><span className="sr-only">Member search</span><input value={memberQuery} onChange={(event) => setMemberQuery(event.target.value)} maxLength={200} placeholder="Card, name, or email" /></label>
            <button className="button button-secondary" type="submit">Search</button>
          </form>
          <div className="selection-list">{memberResults.map((result) => (
            <button key={result.id} className={member?.id === result.id ? 'selection-row selected' : 'selection-row'} type="button" onClick={() => void selectMember(result)}><span><strong>{result.firstName} {result.lastName}</strong><small>{result.libraryCardNumber}</small></span><StatusBadge status={result.status} /></button>
          ))}</div>
        </div>

        <div className="panel">
          <div className="panel-heading"><div><p className="eyebrow">Title</p><h2>Choose a book</h2></div></div>
          <form className="form-stack" onSubmit={searchBooks}>
            <label><span>Pickup branch</span><select value={branchId} onChange={(event) => setBranchId(event.target.value)} required><option value="">Select branch</option>{branches.filter((branch) => branch.active).map((branch) => <option key={branch.id} value={branch.id}>{branch.name}</option>)}</select></label>
            <div className="search-form"><label className="search-field"><span className="sr-only">Book search</span><input value={bookQuery} onChange={(event) => setBookQuery(event.target.value)} maxLength={200} placeholder="Title, ISBN, author…" /></label><button className="button button-secondary" type="submit">Search</button></div>
          </form>
          <div className="selection-list">{books.map((book) => (
            <button key={book.id} className={selectedBook?.id === book.id ? 'selection-row selected' : 'selection-row'} type="button" onClick={() => setSelectedBook(book)}><span><strong>{book.title}</strong><small>{book.authors.join(', ') || 'Unknown author'}</small></span><span>{book.availableCopies}/{book.totalCopies}</span></button>
          ))}</div>
          <button className="button button-primary full-width" type="button" onClick={() => void createReservation()} disabled={!member || !selectedBook || !branchId}>Place reservation</button>
        </div>
      </section>

      <section className="panel">
        <div className="panel-heading"><div><p className="eyebrow">History</p><h2>{member ? `${member.firstName} ${member.lastName}` : 'Member reservations'}</h2></div></div>
        {!member ? <StatePanel kind="empty" title="Select a member" /> : reservations.length === 0 ? <StatePanel kind="empty" title="No reservations for this member" /> : (
          <div className="card-list">{reservations.map((reservation) => (
            <article className="list-card" key={reservation.id}>
              <div><strong>{reservation.bookTitle}</strong><p>{reservation.pickupBranchName}</p><small>Requested {formatDateTime(reservation.requestedAt)}</small></div>
              <div className="list-card-meta"><StatusBadge status={reservation.status} />{reservation.status === 'WAITING' ? <span>Queue #{reservation.queuePosition}</span> : null}{['WAITING', 'READY'].includes(reservation.status) ? <button className="button button-quiet" type="button" onClick={() => void cancel(reservation)}>Cancel</button> : null}</div>
            </article>
          ))}</div>
        )}
      </section>
    </div>
  )
}
