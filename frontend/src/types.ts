export type Role = 'ADMIN' | 'LIBRARIAN' | 'MEMBER'

export interface UserIdentity {
  userId: string
  email: string
  role: Role
  memberId?: string
}

export interface LoginResponse {
  accessToken: string
  tokenType: 'Bearer'
  expiresAt: string
  user: UserIdentity
}

export interface ApiErrorPayload {
  timestamp?: string
  status?: number
  code?: string
  message?: string
  path?: string
  fieldErrors?: Record<string, string>
  correlationId?: string
}

export interface Page<T> {
  items: T[]
  limit: number
  offset: number
  hasMore: boolean
}

export interface Branch {
  id: string
  code: string
  name: string
  timezone: string
  active: boolean
  createdAt: string
}

export interface Shelf {
  id: string
  branchId: string
  code: string
  label: string
  locationNote?: string
  active: boolean
}

export interface BookSummary {
  id: string
  isbn13?: string
  title: string
  subtitle?: string
  languageCode: string
  publicationYear?: number
  editionLabel?: string
  publisherName?: string
  authors: string[]
  categories: string[]
  totalCopies: number
  availableCopies: number
}

export interface CopyView {
  id: string
  bookId: string
  branchId: string
  branchName: string
  shelfId?: string
  shelfLabel?: string
  accessionCode: string
  barcodeValue?: string
  qrValue?: string
  status: 'AVAILABLE' | 'ON_LOAN' | 'RESERVED' | 'LOST' | 'REPAIR' | 'WITHDRAWN'
  acquiredOn?: string
  purchasePrice?: number
  currencyCode?: string
  conditionNote?: string
}

export interface BookDetail {
  summary: BookSummary
  description?: string
  copies: CopyView[]
  createdAt: string
  updatedAt: string
}

export interface MemberView {
  id: string
  homeBranchId: string
  homeBranchName: string
  libraryCardNumber: string
  firstName: string
  lastName: string
  email: string
  phone?: string
  status: 'ACTIVE' | 'SUSPENDED' | 'CLOSED'
  joinedAt: string
  expiresAt?: string
  notes?: string
  openLoanCount: number
  activeReservationCount: number
  outstandingFine: number
  accountEnabled: boolean
}

export interface LoanView {
  id: string
  copyId: string
  accessionCode: string
  bookId: string
  bookTitle: string
  memberId: string
  libraryCardNumber: string
  memberName: string
  issuedAt: string
  dueAt: string
  returnedAt?: string
  renewalCount: number
  status: 'OPEN' | 'RETURNED' | 'LOST'
  overdue: boolean
}

export interface ReservationView {
  id: string
  bookId: string
  bookTitle: string
  memberId: string
  libraryCardNumber: string
  pickupBranchId: string
  pickupBranchName: string
  assignedCopyId?: string
  status: 'WAITING' | 'READY' | 'FULFILLED' | 'CANCELLED' | 'EXPIRED'
  requestedAt: string
  readyAt?: string
  expiresAt?: string
  queuePosition: number
}

export interface FineChargeView {
  id: string
  loanId: string
  memberId: string
  libraryCardNumber: string
  memberName: string
  bookId: string
  bookTitle: string
  amount: number
  currencyCode: string
  status: 'OUTSTANDING' | 'PAID' | 'WAIVED'
  reason: string
  assessedAt: string
  settledAt?: string
  settlementNote?: string
}

export interface DashboardView {
  books: number
  copies: number
  availableCopies: number
  openLoans: number
  overdueLoans: number
  activeMembers: number
  waitingReservations: number
  readyReservations: number
  outstandingFines: number
  fineCurrency: string
  generatedAt: string
}

export interface OverdueLoanView {
  loanId: string
  copyId: string
  accessionCode: string
  bookTitle: string
  memberId: string
  libraryCardNumber: string
  memberName: string
  memberEmail: string
  issuedAt: string
  dueAt: string
  overdueDays: number
  branchId: string
  branchName: string
}

export interface AuditEventView {
  id: string
  occurredAt: string
  actorUserId?: string
  actorEmail?: string
  action: string
  entityType: string
  entityId?: string
  outcome: 'SUCCESS' | 'DENIED' | 'FAILURE'
  correlationId?: string
}

export interface FineRuleView {
  id: string
  branchId: string
  branchName: string
  name: string
  dailyRate: number
  graceDays: number
  maxFine?: number
  currencyCode: string
  maxRenewals: number
  loanPeriodDays: number
  reservationHoldDays: number
  active: boolean
  effectiveFrom: string
  effectiveUntil?: string
}
