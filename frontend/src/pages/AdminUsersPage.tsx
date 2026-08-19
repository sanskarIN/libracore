import { useCallback, useEffect, useState, type FormEvent } from 'react'
import { ApiClient, readableError } from '../api'
import { PageHeader } from '../components/PageHeader'
import { StatePanel } from '../components/StatePanel'
import { StatusBadge } from '../components/StatusBadge'
import { formatDateTime } from '../format'
import type { Page, StaffUserView, UserIdentity } from '../types'

export function AdminUsersPage({ api, user }: { api: ApiClient; user: UserIdentity }) {
  const [users, setUsers] = useState<StaffUserView[]>([])
  const [role, setRole] = useState<'ALL' | 'ADMIN' | 'LIBRARIAN'>('ALL')
  const [loading, setLoading] = useState(true)
  const [busyId, setBusyId] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [notice, setNotice] = useState<string | null>(null)
  const [resetTarget, setResetTarget] = useState<StaffUserView | null>(null)

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const params = new URLSearchParams({ limit: '100', offset: '0' })
      if (role !== 'ALL') params.set('role', role)
      const page = await api.get<Page<StaffUserView>>(`/admin/users?${params.toString()}`)
      setUsers(page.items)
    } catch (reason) {
      setError(readableError(reason))
    } finally {
      setLoading(false)
    }
  }, [api, role])

  useEffect(() => { void load() }, [load])

  async function createUser(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError(null)
    setNotice(null)
    const form = new FormData(event.currentTarget)
    const email = String(form.get('email') || '').trim()
    const password = String(form.get('password') || '')
    const selectedRole = String(form.get('role') || '')
    try {
      await api.post<StaffUserView>('/admin/users', { email, password, role: selectedRole })
      event.currentTarget.reset()
      setNotice(`Created ${selectedRole.toLowerCase()} account for ${email}.`)
      await load()
    } catch (reason) {
      setError(readableError(reason))
    }
  }

  async function setEnabled(staff: StaffUserView, enabled: boolean) {
    setBusyId(staff.id)
    setError(null)
    setNotice(null)
    try {
      const updated = await api.patch<StaffUserView>(`/admin/users/${staff.id}/enabled`, { enabled })
      setUsers((current) => current.map((item) => item.id === updated.id ? updated : item))
      setNotice(`${updated.email} is now ${updated.enabled ? 'enabled' : 'disabled'}.`)
    } catch (reason) {
      setError(readableError(reason))
    } finally {
      setBusyId(null)
    }
  }

  async function resetPassword(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!resetTarget) return
    const form = new FormData(event.currentTarget)
    const password = String(form.get('password') || '')
    setBusyId(resetTarget.id)
    setError(null)
    setNotice(null)
    try {
      const updated = await api.post<StaffUserView>(`/admin/users/${resetTarget.id}/password`, { password })
      setUsers((current) => current.map((item) => item.id === updated.id ? updated : item))
      setNotice(`Password reset completed for ${updated.email}. Existing sessions were revoked by the server policy.`)
      setResetTarget(null)
    } catch (reason) {
      setError(readableError(reason))
    } finally {
      setBusyId(null)
    }
  }

  return (
    <div className="page-stack">
      <PageHeader
        eyebrow="Administrator"
        title="Staff accounts"
        description="Create librarian/administrator accounts, control access, and reset staff credentials. Server-side role checks remain authoritative."
        actions={<button className="button button-secondary" type="button" onClick={() => void load()} disabled={loading}>Refresh</button>}
      />

      {notice ? <div className="inline-alert success" role="status">{notice}</div> : null}
      {error ? <div className="inline-alert error" role="alert">{error}</div> : null}

      <section className="panel">
        <div className="panel-heading"><div><p className="eyebrow">Provisioning</p><h2>Create staff account</h2><p>Passwords must contain at least 12 characters. Share credentials through an approved private channel.</p></div></div>
        <form className="form-grid" onSubmit={(event) => void createUser(event)}>
          <label>
            <span>Email</span>
            <input name="email" type="email" autoComplete="off" maxLength={320} required />
          </label>
          <label>
            <span>Role</span>
            <select name="role" defaultValue="LIBRARIAN" required>
              <option value="LIBRARIAN">Librarian</option>
              <option value="ADMIN">Administrator</option>
            </select>
          </label>
          <label>
            <span>Temporary password</span>
            <input name="password" type="password" autoComplete="new-password" minLength={12} maxLength={200} required />
          </label>
          <div className="form-actions"><button className="button button-primary" type="submit">Create account</button></div>
        </form>
      </section>

      <section className="panel">
        <div className="panel-heading">
          <div><p className="eyebrow">Access control</p><h2>Staff directory</h2></div>
          <label>
            <span className="sr-only">Filter by role</span>
            <select value={role} onChange={(event) => setRole(event.target.value as typeof role)}>
              <option value="ALL">All staff roles</option>
              <option value="ADMIN">Administrators</option>
              <option value="LIBRARIAN">Librarians</option>
            </select>
          </label>
        </div>

        {loading && users.length === 0 ? <StatePanel kind="loading" title="Loading staff accounts" /> : null}
        {!loading && users.length === 0 ? <StatePanel kind="empty" title="No staff accounts found" message="Create an account or change the role filter." /> : null}
        {users.length > 0 ? (
          <div className="table-scroll">
            <table>
              <thead><tr><th scope="col">Account</th><th scope="col">Role</th><th scope="col">Status</th><th scope="col">Sessions</th><th scope="col">Updated</th><th scope="col">Actions</th></tr></thead>
              <tbody>
                {users.map((staff) => (
                  <tr key={staff.id}>
                    <td><strong>{staff.email}</strong>{staff.id === user.userId ? <small>Current account</small> : null}</td>
                    <td><StatusBadge status={staff.role} /></td>
                    <td><StatusBadge status={staff.enabled ? 'ACTIVE' : 'DISABLED'} /></td>
                    <td className="numeric">{staff.activeSessionCount}</td>
                    <td>{formatDateTime(staff.updatedAt)}</td>
                    <td>
                      <div className="button-row">
                        <button className="button button-quiet" type="button" onClick={() => setResetTarget(staff)}>Reset password</button>
                        <button
                          className="button button-secondary"
                          type="button"
                          disabled={busyId === staff.id || staff.id === user.userId}
                          onClick={() => void setEnabled(staff, !staff.enabled)}
                          title={staff.id === user.userId ? 'You cannot disable the account currently in use.' : undefined}
                        >
                          {staff.enabled ? 'Disable' : 'Enable'}
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : null}
      </section>

      {resetTarget ? (
        <section className="panel" aria-labelledby="reset-password-title">
          <div className="panel-heading"><div><p className="eyebrow">Credential reset</p><h2 id="reset-password-title">Reset {resetTarget.email}</h2><p>Changing a staff password revokes active sessions for that account.</p></div></div>
          <form className="form-grid" onSubmit={(event) => void resetPassword(event)}>
            <label>
              <span>New password</span>
              <input name="password" type="password" autoComplete="new-password" minLength={12} maxLength={200} required autoFocus />
            </label>
            <div className="form-actions button-row">
              <button className="button button-primary" type="submit" disabled={busyId === resetTarget.id}>Reset password</button>
              <button className="button button-quiet" type="button" onClick={() => setResetTarget(null)}>Cancel</button>
            </div>
          </form>
        </section>
      ) : null}
    </div>
  )
}
