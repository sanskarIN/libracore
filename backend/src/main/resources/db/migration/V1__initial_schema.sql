CREATE TABLE branch (
    id UUID PRIMARY KEY,
    code VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(160) NOT NULL,
    timezone VARCHAR(64) NOT NULL DEFAULT 'UTC',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE publisher (
    id UUID PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    normalized_name VARCHAR(200) NOT NULL UNIQUE,
    website VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE author (
    id UUID PRIMARY KEY,
    display_name VARCHAR(200) NOT NULL,
    normalized_name VARCHAR(200) NOT NULL,
    sort_name VARCHAR(200),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_author_identity UNIQUE (normalized_name, sort_name)
);

CREATE TABLE category (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    normalized_name VARCHAR(120) NOT NULL UNIQUE,
    description VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE shelf (
    id UUID PRIMARY KEY,
    branch_id UUID NOT NULL REFERENCES branch(id),
    code VARCHAR(64) NOT NULL,
    label VARCHAR(160) NOT NULL,
    location_note VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_shelf_branch_code UNIQUE (branch_id, code)
);

CREATE TABLE book (
    id UUID PRIMARY KEY,
    isbn13 VARCHAR(13),
    title VARCHAR(400) NOT NULL,
    subtitle VARCHAR(400),
    description TEXT,
    language_code VARCHAR(16) NOT NULL DEFAULT 'en',
    publication_year INTEGER,
    edition_label VARCHAR(120),
    publisher_id UUID REFERENCES publisher(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_book_publication_year CHECK (publication_year IS NULL OR publication_year BETWEEN 1000 AND 9999),
    CONSTRAINT ck_book_isbn13 CHECK (isbn13 IS NULL OR CHAR_LENGTH(isbn13) = 13)
);

CREATE UNIQUE INDEX uq_book_isbn13 ON book(isbn13) WHERE isbn13 IS NOT NULL;
CREATE INDEX idx_book_title_lower ON book(LOWER(title));
CREATE INDEX idx_book_publisher ON book(publisher_id);

CREATE TABLE book_author (
    book_id UUID NOT NULL REFERENCES book(id) ON DELETE CASCADE,
    author_id UUID NOT NULL REFERENCES author(id),
    contribution_order INTEGER NOT NULL DEFAULT 0,
    contribution_role VARCHAR(40) NOT NULL DEFAULT 'AUTHOR',
    PRIMARY KEY (book_id, author_id, contribution_role),
    CONSTRAINT ck_book_author_order CHECK (contribution_order >= 0)
);

CREATE INDEX idx_book_author_author ON book_author(author_id);

CREATE TABLE book_category (
    book_id UUID NOT NULL REFERENCES book(id) ON DELETE CASCADE,
    category_id UUID NOT NULL REFERENCES category(id),
    PRIMARY KEY (book_id, category_id)
);

CREATE INDEX idx_book_category_category ON book_category(category_id);

CREATE TABLE book_copy (
    id UUID PRIMARY KEY,
    book_id UUID NOT NULL REFERENCES book(id),
    branch_id UUID NOT NULL REFERENCES branch(id),
    shelf_id UUID REFERENCES shelf(id),
    accession_code VARCHAR(80) NOT NULL,
    barcode_value VARCHAR(160),
    qr_value VARCHAR(300),
    status VARCHAR(32) NOT NULL DEFAULT 'AVAILABLE',
    acquired_on DATE,
    purchase_price NUMERIC(12,2),
    currency_code VARCHAR(3),
    condition_note VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_copy_accession UNIQUE (branch_id, accession_code),
    CONSTRAINT uq_copy_barcode UNIQUE (barcode_value),
    CONSTRAINT ck_copy_status CHECK (status IN ('AVAILABLE','ON_LOAN','RESERVED','LOST','REPAIR','WITHDRAWN')),
    CONSTRAINT ck_copy_price CHECK (purchase_price IS NULL OR purchase_price >= 0)
);

CREATE INDEX idx_copy_book_status ON book_copy(book_id, status);
CREATE INDEX idx_copy_branch_status ON book_copy(branch_id, status);
CREATE INDEX idx_copy_shelf ON book_copy(shelf_id);

CREATE TABLE member (
    id UUID PRIMARY KEY,
    home_branch_id UUID NOT NULL REFERENCES branch(id),
    library_card_number VARCHAR(80) NOT NULL UNIQUE,
    first_name VARCHAR(120) NOT NULL,
    last_name VARCHAR(120) NOT NULL,
    email VARCHAR(320) NOT NULL UNIQUE,
    phone VARCHAR(40),
    status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE',
    joined_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP WITH TIME ZONE,
    notes VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_member_status CHECK (status IN ('ACTIVE','SUSPENDED','CLOSED'))
);

CREATE INDEX idx_member_name ON member(last_name, first_name);
CREATE INDEX idx_member_branch_status ON member(home_branch_id, status);

CREATE TABLE app_user (
    id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(24) NOT NULL,
    member_id UUID UNIQUE REFERENCES member(id),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_user_role CHECK (role IN ('ADMIN','LIBRARIAN','MEMBER')),
    CONSTRAINT ck_member_role_link CHECK ((role = 'MEMBER' AND member_id IS NOT NULL) OR role <> 'MEMBER')
);

CREATE TABLE app_session (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    token_hash CHAR(64) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_seen_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked_at TIMESTAMP WITH TIME ZONE,
    user_agent_hash CHAR(64)
);

CREATE INDEX idx_session_user_active ON app_session(user_id, expires_at);
CREATE INDEX idx_session_expiry ON app_session(expires_at);

CREATE TABLE fine_rule (
    id UUID PRIMARY KEY,
    branch_id UUID NOT NULL REFERENCES branch(id),
    name VARCHAR(160) NOT NULL,
    daily_rate NUMERIC(12,2) NOT NULL DEFAULT 0,
    grace_days INTEGER NOT NULL DEFAULT 0,
    max_fine NUMERIC(12,2),
    currency_code VARCHAR(3) NOT NULL DEFAULT 'INR',
    max_renewals INTEGER NOT NULL DEFAULT 2,
    loan_period_days INTEGER NOT NULL DEFAULT 14,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    effective_from TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    effective_until TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_fine_daily_rate CHECK (daily_rate >= 0),
    CONSTRAINT ck_fine_grace_days CHECK (grace_days >= 0),
    CONSTRAINT ck_fine_max CHECK (max_fine IS NULL OR max_fine >= 0),
    CONSTRAINT ck_fine_renewals CHECK (max_renewals >= 0),
    CONSTRAINT ck_fine_loan_period CHECK (loan_period_days BETWEEN 1 AND 365)
);

CREATE INDEX idx_fine_rule_branch_active ON fine_rule(branch_id, active, effective_from);

CREATE TABLE loan (
    id UUID PRIMARY KEY,
    copy_id UUID NOT NULL REFERENCES book_copy(id),
    member_id UUID NOT NULL REFERENCES member(id),
    issued_by_user_id UUID NOT NULL REFERENCES app_user(id),
    returned_by_user_id UUID REFERENCES app_user(id),
    issued_at TIMESTAMP WITH TIME ZONE NOT NULL,
    due_at TIMESTAMP WITH TIME ZONE NOT NULL,
    returned_at TIMESTAMP WITH TIME ZONE,
    renewal_count INTEGER NOT NULL DEFAULT 0,
    last_renewed_at TIMESTAMP WITH TIME ZONE,
    status VARCHAR(24) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_loan_dates CHECK (due_at > issued_at),
    CONSTRAINT ck_loan_renewals CHECK (renewal_count >= 0),
    CONSTRAINT ck_loan_status CHECK (status IN ('OPEN','RETURNED','LOST'))
);

CREATE UNIQUE INDEX uq_open_loan_copy ON loan(copy_id) WHERE status = 'OPEN';
CREATE INDEX idx_loan_member_status ON loan(member_id, status);
CREATE INDEX idx_loan_due_open ON loan(due_at) WHERE status = 'OPEN';

CREATE TABLE reservation (
    id UUID PRIMARY KEY,
    book_id UUID NOT NULL REFERENCES book(id),
    member_id UUID NOT NULL REFERENCES member(id),
    pickup_branch_id UUID NOT NULL REFERENCES branch(id),
    status VARCHAR(24) NOT NULL DEFAULT 'WAITING',
    requested_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ready_at TIMESTAMP WITH TIME ZONE,
    expires_at TIMESTAMP WITH TIME ZONE,
    fulfilled_at TIMESTAMP WITH TIME ZONE,
    cancelled_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_reservation_status CHECK (status IN ('WAITING','READY','FULFILLED','CANCELLED','EXPIRED'))
);

CREATE UNIQUE INDEX uq_active_reservation_member_book
    ON reservation(book_id, member_id)
    WHERE status IN ('WAITING','READY');
CREATE INDEX idx_reservation_queue ON reservation(book_id, pickup_branch_id, status, requested_at);
CREATE INDEX idx_reservation_member ON reservation(member_id, status);

CREATE TABLE fine_charge (
    id UUID PRIMARY KEY,
    loan_id UUID NOT NULL REFERENCES loan(id),
    member_id UUID NOT NULL REFERENCES member(id),
    amount NUMERIC(12,2) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'OUTSTANDING',
    reason VARCHAR(300) NOT NULL,
    assessed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    settled_at TIMESTAMP WITH TIME ZONE,
    waived_by_user_id UUID REFERENCES app_user(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_fine_amount CHECK (amount >= 0),
    CONSTRAINT ck_fine_status CHECK (status IN ('OUTSTANDING','PAID','WAIVED'))
);

CREATE INDEX idx_fine_member_status ON fine_charge(member_id, status);

CREATE TABLE audit_event (
    id UUID PRIMARY KEY,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actor_user_id UUID REFERENCES app_user(id),
    action VARCHAR(120) NOT NULL,
    entity_type VARCHAR(80) NOT NULL,
    entity_id VARCHAR(120),
    outcome VARCHAR(24) NOT NULL DEFAULT 'SUCCESS',
    correlation_id VARCHAR(120),
    metadata_json TEXT,
    CONSTRAINT ck_audit_outcome CHECK (outcome IN ('SUCCESS','DENIED','FAILURE'))
);

CREATE INDEX idx_audit_time ON audit_event(occurred_at DESC);
CREATE INDEX idx_audit_entity ON audit_event(entity_type, entity_id);
CREATE INDEX idx_audit_actor ON audit_event(actor_user_id, occurred_at DESC);

CREATE TABLE notification_outbox (
    id UUID PRIMARY KEY,
    channel VARCHAR(24) NOT NULL,
    recipient VARCHAR(320) NOT NULL,
    template_key VARCHAR(120) NOT NULL,
    payload_json TEXT NOT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'PENDING',
    attempt_count INTEGER NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP WITH TIME ZONE,
    last_error_code VARCHAR(120),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_notification_channel CHECK (channel IN ('EMAIL','MOCK')),
    CONSTRAINT ck_notification_status CHECK (status IN ('PENDING','SENT','FAILED','CANCELLED')),
    CONSTRAINT ck_notification_attempts CHECK (attempt_count >= 0)
);

CREATE INDEX idx_notification_pending ON notification_outbox(status, next_attempt_at);

INSERT INTO branch (id, code, name, timezone)
VALUES ('00000000-0000-0000-0000-000000000001', 'MAIN', 'Main Library', 'Asia/Kolkata');

INSERT INTO fine_rule (
    id, branch_id, name, daily_rate, grace_days, max_fine, currency_code, max_renewals, loan_period_days
) VALUES (
    '00000000-0000-0000-0000-000000000101',
    '00000000-0000-0000-0000-000000000001',
    'Default circulation policy',
    2.00,
    0,
    500.00,
    'INR',
    2,
    14
);
