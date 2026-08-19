import { useCallback, useEffect, useState, type FormEvent } from 'react'
import { ApiClient, readableError } from '../api'
import { PageHeader } from '../components/PageHeader'
import { StatePanel } from '../components/StatePanel'
import { StatusBadge } from '../components/StatusBadge'
import { formatCurrency } from '../format'
import type { BookDetail, BookSummary, Branch, CopyView, Page, ReservationView, UserIdentity } from '../types'

interface CatalogPageProps {
  api: ApiClient
  user: UserIdentity
}

export function CatalogPage({ api, user }: CatalogPageProps) {
  const [query, setQuery] = useState('')
  const [branchId, setBranchId] = useState('')
  const [availableOnly, setAvailableOnly] = useState(false)
  const [branches, setBranches] = useState<Branch[]>([])
  const [books, setBooks] = useState<BookSummary[]>([])
  const [selected, setSelected] = useState<BookDetail | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [notice, setNotice] = useState<string | null>(null)
  const [showCreate, setShowCreate] = useState(false)
  const staff = user.role === 'ADMIN' || user.role === 'LIBRARIAN'

  const search = useCallback(async (q = query, branch = branchId, onlyAvailable = availableOnly) => {
    setLoading(true)
    setError(null)
    const params = new URLSearchParams({ q, limit: '50', offset: '0', availableOnly: String(onlyAvailable) })
    if (branch) params.set('branchId', branch)
    try {
      const result = await api.get<Page<BookSummary>>(`/catalog/books?${params.toString()}`)
      setBooks(result.items)
    } catch (reason) {
      setError(readableError(reason))
    } finally {
      setLoading(false)
    }
  }, [api, availableOnly, branchId, query])

  useEffect(() => {
    void api.get<Branch[]>('/catalog/branches')
      .then(setBranches)
      .catch((reason) => setError(readableError(reason)))
    void search('', '', false)
  }, [api, search])

  async function openBook(bookId: string) {
    setError(null)
    try {
      setSelected(await api.get<BookDetail>(`/catalog/books/${bookId}`))
    } catch (reason) {
      setError(readableError(reason))
    }
  }

  async function reserve(book: BookSummary) {
    if (!branchId) {
      setError('Choose a pickup branch before placing a reservation.')
      return
    }
    setError(null)
    setNotice(null)
    try {
      const reservation = await api.post<ReservationView>('/circulation/reservations', {
        bookId: book.id,
        pickupBranchId: branchId,
      })
      setNotice(
        reservation.status === 'READY'
          ? `Reservation ready at ${reservation.pickupBranchName}.`
          : `Reservation added to the waitlist at position ${reservation.queuePosition}.`,
      )
      await openBook(book.id)
      await search()
    } catch (reason) {
      setError(readableError(reason))
    }
  }

  async function createBook(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError(null)
    setNotice(null)
    const form = new FormData(event.currentTarget)
    const authors = String(form.get('authors') || '').split(',').map((part) => part.trim()).filter(Boolean)
    const categories = String(form.get('categories') || '').split(',').map((part) => part.trim()).filter(Boolean)
    const year = String(form.get('publicationYear') || '').trim()
    try {
      const created = await api.post<BookDetail>('/catalog/books', {
        title: String(form.get('title') || '').trim(),
        subtitle: emptyToNull(form.get('subtitle')),
        isbn13: emptyToNull(form.get('isbn13')),
        description: emptyToNull(form.get('description')),
        languageCode: String(form.get('languageCode') || 'en').trim(),
        publicationYear: year ? Number(year) : null,
        editionLabel: emptyToNull(form.get('editionLabel')),
        publisherName: emptyToNull(form.get('publisherName')),
        authors,
        categories,
      })
      event.currentTarget.reset()
      setShowCreate(false)
      setNotice(`Added “${created.summary.title}” to the catalog.`)
      setSelected(created)
      await search()
    } catch (reason) {
      setError(readableError(reason))
    }
  }

  async function addCopy(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!selected) return
    setError(null)
    setNotice(null)
    const form = new FormData(event.currentTarget)
    const copyBranchId = String(form.get('branchId') || '')
    try {
      const copy = await api.post<CopyView>(`/catalog/books/${selected.summary.id}/copies`, {
        branchId: copyBranchId,
        accessionCode: String(form.get('accessionCode') || '').trim(),
        barcodeValue: emptyToNull(form.get('barcodeValue')),
        qrValue: emptyToNull(form.get('qrValue')),
        conditionNote: emptyToNull(form.get('conditionNote')),
      })
      event.currentTarget.reset()
      setNotice(`Added copy ${copy.accessionCode}.`)
      await openBook(selected.summary.id)
      await search()
    } catch (reason) {
      setError(readableError(reason))
    }
  }

  function submitSearch(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSelected(null)
    void search()
  }

  return (
    <div className="page-stack">
      <PageHeader
        eyebrow="Catalog"
        title="Find books and copies"
        description="Search title, ISBN, author, category, or publisher and filter by branch availability."
        actions={staff ? (
          <button className="button button-primary" type="button" onClick={() => setShowCreate((value) => !value)}>
            {showCreate ? 'Close form' : 'Add book'}
          </button>
        ) : undefined}
      />

      {notice ? <div className="inline-alert success" role="status">{notice}</div> : null}
      {error ? <div className="inline-alert error" role="alert">{error}</div> : null}

      {showCreate && staff ? (
        <section className="panel">
          <div className="panel-heading"><div><p className="eyebrow">Cataloging</p><h2>Add a book</h2></div></div>
          <form className="form-grid" onSubmit={createBook}>
            <label className="span-2"><span>Title</span><input name="title" required maxLength={400} /></label>
            <label><span>Subtitle</span><input name="subtitle" maxLength={400} /></label>
            <label><span>ISBN-13</span><input name="isbn13" inputMode="numeric" maxLength={32} placeholder="978…" /></label>
            <label><span>Language code</span><input name="languageCode" required defaultValue="en" maxLength={16} /></label>
            <label><span>Publication year</span><input name="publicationYear" type="number" min="1000" max="9999" /></label>
            <label><span>Edition</span><input name="editionLabel" maxLength={120} /></label>
            <label><span>Publisher</span><input name="publisherName" maxLength={200} /></label>
            <label className="span-2"><span>Authors</span><input name="authors" maxLength={1000} placeholder="One author, Another author" /></label>
            <label className="span-2"><span>Categories</span><input name="categories" maxLength={1000} placeholder="History, Reference" /></label>
            <label className="span-2"><span>Description</span><textarea name="description" maxLength={5000} rows={4} /></label>
            <div className="form-actions span-2"><button className="button button-primary" type="submit">Add book</button></div>
          </form>
        </section>
      ) : null}

      <section className="panel search-panel">
        <form className="search-form" onSubmit={submitSearch} role="search">
          <label className="search-field"><span className="sr-only">Search catalog</span><input value={query} onChange={(event) => setQuery(event.target.value)} maxLength={200} placeholder="Search title, ISBN, author…" /></label>
          <label><span className="sr-only">Branch</span><select value={branchId} onChange={(event) => setBranchId(event.target.value)}><option value="">All branches</option>{branches.filter((branch) => branch.active).map((branch) => <option key={branch.id} value={branch.id}>{branch.name}</option>)}</select></label>
          <label className="checkbox-row"><input type="checkbox" checked={availableOnly} onChange={(event) => setAvailableOnly(event.target.checked)} /><span>Available only</span></label>
          <button className="button button-secondary" type="submit">Search</button>
        </form>
      </section>

      <div className="split-layout">
        <section className="panel min-width-zero" aria-labelledby="catalog-results-title">
          <div className="panel-heading"><div><p className="eyebrow">Results</p><h2 id="catalog-results-title">{books.length} books</h2></div></div>
          {loading ? <StatePanel kind="loading" title="Searching catalog" /> : null}
          {!loading && books.length === 0 ? <StatePanel kind="empty" title="No matching books" message="Try a broader search or another branch." /> : null}
          {!loading && books.length > 0 ? (
            <div className="catalog-list">
              {books.map((book) => (
                <article key={book.id} className={selected?.summary.id === book.id ? 'catalog-card selected' : 'catalog-card'}>
                  <button className="catalog-card-main" type="button" onClick={() => void openBook(book.id)} aria-label={`Open ${book.title}`}>
                    <span className="book-mark" aria-hidden="true">{book.title.charAt(0).toUpperCase()}</span>
                    <span className="catalog-card-copy">
                      <strong>{book.title}</strong>
                      {book.authors.length ? <span>{book.authors.join(', ')}</span> : <span>Unknown author</span>}
                      <small>{book.isbn13 || 'No ISBN'} · {book.publisherName || 'Publisher not set'}</small>
                    </span>
                    <span className="availability-count"><strong>{book.availableCopies}</strong><small>available / {book.totalCopies}</small></span>
                  </button>
                  {user.role === 'MEMBER' ? <button className="button button-quiet" type="button" onClick={() => void reserve(book)}>Reserve</button> : null}
                </article>
              ))}
            </div>
          ) : null}
        </section>

        <aside className="panel detail-panel" aria-label="Book details">
          {!selected ? <StatePanel kind="empty" title="Select a book" message="Choose a result to inspect metadata and physical copies." /> : (
            <div className="detail-stack">
              <div>
                <p className="eyebrow">Book detail</p>
                <h2>{selected.summary.title}</h2>
                {selected.summary.subtitle ? <p className="page-description">{selected.summary.subtitle}</p> : null}
              </div>
              <dl className="detail-list">
                <div><dt>Authors</dt><dd>{selected.summary.authors.join(', ') || '—'}</dd></div>
                <div><dt>ISBN-13</dt><dd>{selected.summary.isbn13 || '—'}</dd></div>
                <div><dt>Publisher</dt><dd>{selected.summary.publisherName || '—'}</dd></div>
                <div><dt>Edition</dt><dd>{selected.summary.editionLabel || '—'}</dd></div>
                <div><dt>Language</dt><dd>{selected.summary.languageCode}</dd></div>
                <div><dt>Categories</dt><dd>{selected.summary.categories.join(', ') || '—'}</dd></div>
              </dl>
              {selected.description ? <p>{selected.description}</p> : null}
              <div>
                <h3>Copies</h3>
                {selected.copies.length === 0 ? <p className="muted">No physical copies have been recorded.</p> : (
                  <div className="copy-list">{selected.copies.map((copy) => (
                    <article key={copy.id} className="copy-row">
                      <div><strong>{copy.accessionCode}</strong><small>{copy.branchName}{copy.shelfLabel ? ` · ${copy.shelfLabel}` : ''}</small></div>
                      <div className="copy-meta"><StatusBadge status={copy.status} />{copy.purchasePrice !== undefined && copy.currencyCode ? <span>{formatCurrency(copy.purchasePrice, copy.currencyCode)}</span> : null}</div>
                    </article>
                  ))}</div>
                )}
              </div>
              {staff ? (
                <form className="form-stack compact-form" onSubmit={addCopy}>
                  <h3>Add physical copy</h3>
                  <label><span>Branch</span><select name="branchId" required defaultValue=""><option value="" disabled>Select branch</option>{branches.filter((branch) => branch.active).map((branch) => <option key={branch.id} value={branch.id}>{branch.name}</option>)}</select></label>
                  <label><span>Accession code</span><input name="accessionCode" required maxLength={80} /></label>
                  <label><span>Barcode</span><input name="barcodeValue" maxLength={160} /></label>
                  <label><span>QR value</span><input name="qrValue" maxLength={300} /></label>
                  <label><span>Condition note</span><input name="conditionNote" maxLength={500} /></label>
                  <button className="button button-secondary" type="submit">Add copy</button>
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
