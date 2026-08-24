\set ON_ERROR_STOP on

-- Deterministic, fictional benchmark data for a disposable LibraCore performance database.
-- Invoke only through scripts/load-performance-fixture.sh, which enforces database-name
-- and explicit-write safety gates before this SQL is allowed to run.

BEGIN;

-- Keep repeat runs deterministic by removing only the fixture UUID namespaces.
DELETE FROM fine_charge WHERE id::text LIKE 'fbfbfbfb-%';
DELETE FROM reservation WHERE id::text LIKE 'fafafafa-%';
DELETE FROM loan WHERE id::text LIKE 'f9f9f9f9-%';
DELETE FROM app_session WHERE id::text LIKE 'f8f8f8f8-%';
DELETE FROM app_user WHERE id::text LIKE 'f8f8f8f8-%';
DELETE FROM member WHERE id::text LIKE 'f7f7f7f7-%';
DELETE FROM book_author WHERE book_id::text LIKE 'f5f5f5f5-%';
DELETE FROM book_category WHERE book_id::text LIKE 'f5f5f5f5-%';
DELETE FROM book_copy WHERE id::text LIKE 'f6f6f6f6-%';
DELETE FROM book WHERE id::text LIKE 'f5f5f5f5-%';
DELETE FROM author WHERE id::text LIKE 'f3f3f3f3-%';
DELETE FROM category WHERE id::text LIKE 'f4f4f4f4-%';
DELETE FROM publisher WHERE id::text LIKE 'f2f2f2f2-%';
DELETE FROM shelf WHERE id::text LIKE 'f1f1f1f1-%';
DELETE FROM branch WHERE id = 'f0f0f0f0-0000-0000-0000-000000000001';

INSERT INTO branch (id, code, name, timezone, active)
VALUES (
    'f0f0f0f0-0000-0000-0000-000000000001',
    'PERF',
    'Performance Fixture Library',
    'Asia/Kolkata',
    TRUE
);

INSERT INTO shelf (id, branch_id, code, label, location_note, active)
VALUES (
    'f1f1f1f1-0000-0000-0000-000000000001',
    'f0f0f0f0-0000-0000-0000-000000000001',
    'PERF-A',
    'Performance Fixture Shelf',
    'Fictional benchmark data only',
    TRUE
);

WITH params AS (
    SELECT GREATEST(1, CEIL((:'books')::numeric / 100)::integer) AS publisher_count
)
INSERT INTO publisher (id, name, normalized_name, website)
SELECT
    ('f2f2f2f2-0000-0000-0000-' || lpad(g::text, 12, '0'))::uuid,
    'Performance Publisher ' || lpad(g::text, 6, '0'),
    'performance publisher ' || lpad(g::text, 6, '0'),
    'https://publisher-' || g || '.example.invalid'
FROM params, generate_series(1, publisher_count) AS g;

WITH params AS (
    SELECT GREATEST(1, CEIL((:'books')::numeric / 20)::integer) AS author_count
)
INSERT INTO author (id, display_name, normalized_name, sort_name)
SELECT
    ('f3f3f3f3-0000-0000-0000-' || lpad(g::text, 12, '0'))::uuid,
    'Performance Author ' || lpad(g::text, 6, '0'),
    'performance author ' || lpad(g::text, 6, '0'),
    'Author, Performance ' || lpad(g::text, 6, '0')
FROM params, generate_series(1, author_count) AS g;

WITH params AS (
    SELECT LEAST(50, GREATEST(10, CEIL((:'books')::numeric / 500)::integer)) AS category_count
)
INSERT INTO category (id, name, normalized_name, description)
SELECT
    ('f4f4f4f4-0000-0000-0000-' || lpad(g::text, 12, '0'))::uuid,
    'Performance Category ' || lpad(g::text, 3, '0'),
    'performance category ' || lpad(g::text, 3, '0'),
    'Fictional category generated for repeatable LibraCore performance tests.'
FROM params, generate_series(1, category_count) AS g;

WITH counts AS (
    SELECT GREATEST(1, CEIL((:'books')::numeric / 100)::integer) AS publisher_count
)
INSERT INTO book (
    id, title, subtitle, description, language_code, publication_year,
    edition_label, publisher_id
)
SELECT
    ('f5f5f5f5-0000-0000-0000-' || lpad(g::text, 12, '0'))::uuid,
    'Performance Book ' || lpad(g::text, 8, '0'),
    CASE WHEN g % 5 = 0 THEN 'Benchmark subtitle ' || g ELSE NULL END,
    'Fictional deterministic benchmark record ' || g || ' for catalog search and paging.',
    CASE WHEN g % 7 = 0 THEN 'hi' ELSE 'en' END,
    1980 + (g % 47),
    CASE WHEN g % 4 = 0 THEN ((g % 5) + 1) || 'th edition' ELSE NULL END,
    ('f2f2f2f2-0000-0000-0000-' || lpad((((g - 1) % publisher_count) + 1)::text, 12, '0'))::uuid
FROM counts, generate_series(1, (:'books')::integer) AS g;

WITH counts AS (
    SELECT GREATEST(1, CEIL((:'books')::numeric / 20)::integer) AS author_count
)
INSERT INTO book_author (book_id, author_id, contribution_order, contribution_role)
SELECT
    ('f5f5f5f5-0000-0000-0000-' || lpad(g::text, 12, '0'))::uuid,
    ('f3f3f3f3-0000-0000-0000-' || lpad((((g - 1) % author_count) + 1)::text, 12, '0'))::uuid,
    0,
    'AUTHOR'
FROM counts, generate_series(1, (:'books')::integer) AS g;

WITH counts AS (
    SELECT LEAST(50, GREATEST(10, CEIL((:'books')::numeric / 500)::integer)) AS category_count
)
INSERT INTO book_category (book_id, category_id)
SELECT
    ('f5f5f5f5-0000-0000-0000-' || lpad(g::text, 12, '0'))::uuid,
    ('f4f4f4f4-0000-0000-0000-' || lpad((((g - 1) % category_count) + 1)::text, 12, '0'))::uuid
FROM counts, generate_series(1, (:'books')::integer) AS g;

INSERT INTO book_copy (
    id, book_id, branch_id, shelf_id, accession_code, barcode_value,
    qr_value, status, acquired_on, purchase_price, currency_code, condition_note
)
SELECT
    ('f6f6f6f6-0000-0000-0000-' || lpad(serial_no::text, 12, '0'))::uuid,
    ('f5f5f5f5-0000-0000-0000-' || lpad(book_no::text, 12, '0'))::uuid,
    'f0f0f0f0-0000-0000-0000-000000000001',
    'f1f1f1f1-0000-0000-0000-000000000001',
    'PERF-' || lpad(serial_no::text, 10, '0'),
    'PERF-BARCODE-' || lpad(serial_no::text, 10, '0'),
    'PERF-QR-' || lpad(serial_no::text, 10, '0'),
    'AVAILABLE',
    CURRENT_DATE - ((serial_no % 3650)::integer),
    100.00 + (serial_no % 900),
    'INR',
    CASE WHEN serial_no % 11 = 0 THEN 'Fixture condition note ' || serial_no ELSE NULL END
FROM (
    SELECT
        book_no,
        copy_no,
        ((book_no - 1) * (:'copies_per_book')::integer + copy_no) AS serial_no
    FROM generate_series(1, (:'books')::integer) AS book_no
    CROSS JOIN generate_series(1, (:'copies_per_book')::integer) AS copy_no
) copies;

INSERT INTO member (
    id, home_branch_id, library_card_number, first_name, last_name,
    email, phone, status, joined_at, expires_at, notes
)
SELECT
    ('f7f7f7f7-0000-0000-0000-' || lpad(g::text, 12, '0'))::uuid,
    'f0f0f0f0-0000-0000-0000-000000000001',
    'PERF-CARD-' || lpad(g::text, 9, '0'),
    'Fixture' || lpad(g::text, 7, '0'),
    'Member' || lpad(((g - 1) % 1000 + 1)::text, 4, '0'),
    'perf.member.' || lpad(g::text, 9, '0') || '@example.invalid',
    NULL,
    CASE WHEN g % 40 = 0 THEN 'SUSPENDED' ELSE 'ACTIVE' END,
    CURRENT_TIMESTAMP - ((g % 1500)::integer * INTERVAL '1 day'),
    CURRENT_TIMESTAMP + (((g % 365) + 30)::integer * INTERVAL '1 day'),
    CASE WHEN g % 17 = 0 THEN 'Fictional benchmark member' ELSE NULL END
FROM generate_series(1, (:'members')::integer) AS g;

-- This disabled user exists only to satisfy loan audit foreign keys. It is not
-- a usable benchmark login account and deliberately has no valid password hash.
INSERT INTO app_user (id, email, password_hash, role, enabled)
VALUES (
    'f8f8f8f8-0000-0000-0000-000000000001',
    'perf.disabled.staff@example.invalid',
    'PERFORMANCE_FIXTURE_DISABLED_ACCOUNT',
    'LIBRARIAN',
    FALSE
);

INSERT INTO loan (
    id, copy_id, member_id, issued_by_user_id, issued_at, due_at,
    returned_at, renewal_count, status, fine_rule_id
)
SELECT
    ('f9f9f9f9-0000-0000-0000-' || lpad(g::text, 12, '0'))::uuid,
    ('f6f6f6f6-0000-0000-0000-' || lpad(g::text, 12, '0'))::uuid,
    ('f7f7f7f7-0000-0000-0000-' || lpad(g::text, 12, '0'))::uuid,
    'f8f8f8f8-0000-0000-0000-000000000001',
    CURRENT_TIMESTAMP - (((g % 30) + 1)::integer * INTERVAL '1 day'),
    CURRENT_TIMESTAMP - (((g % 30) + 1)::integer * INTERVAL '1 day') + INTERVAL '14 days',
    CASE
        WHEN g % 4 = 0
            THEN CURRENT_TIMESTAMP - (((g % 30) + 1)::integer * INTERVAL '1 day') + INTERVAL '7 days'
        ELSE NULL
    END,
    (g % 3)::integer,
    CASE WHEN g % 4 = 0 THEN 'RETURNED' ELSE 'OPEN' END,
    '00000000-0000-0000-0000-000000000101'
FROM generate_series(1, (:'loans')::integer) AS g;

UPDATE book_copy c
SET status = 'ON_LOAN', updated_at = CURRENT_TIMESTAMP
FROM loan l
WHERE l.id::text LIKE 'f9f9f9f9-%'
  AND l.status = 'OPEN'
  AND c.id = l.copy_id;

INSERT INTO reservation (
    id, book_id, member_id, pickup_branch_id, status, requested_at
)
SELECT
    ('fafafafa-0000-0000-0000-' || lpad(g::text, 12, '0'))::uuid,
    ('f5f5f5f5-0000-0000-0000-' || lpad(g::text, 12, '0'))::uuid,
    ('f7f7f7f7-0000-0000-0000-' || lpad(g::text, 12, '0'))::uuid,
    'f0f0f0f0-0000-0000-0000-000000000001',
    'WAITING',
    CURRENT_TIMESTAMP - ((g % 14)::integer * INTERVAL '1 day')
FROM generate_series(1, (:'reservations')::integer) AS g;

WITH overdue AS (
    SELECT id AS loan_id, member_id, row_number() OVER (ORDER BY id) AS seq
    FROM loan
    WHERE id::text LIKE 'f9f9f9f9-%'
      AND status = 'OPEN'
      AND due_at < CURRENT_TIMESTAMP
      AND (right(id::text, 1)::bit(4)::integer % 5 = 0)
)
INSERT INTO fine_charge (
    id, loan_id, member_id, amount, currency_code, status, reason, assessed_at
)
SELECT
    ('fbfbfbfb-0000-0000-0000-' || lpad(seq::text, 12, '0'))::uuid,
    loan_id,
    member_id,
    10.00 + (seq % 20),
    'INR',
    'OUTSTANDING',
    'Performance fixture overdue fine',
    CURRENT_TIMESTAMP
FROM overdue;

COMMIT;

ANALYZE branch;
ANALYZE publisher;
ANALYZE author;
ANALYZE category;
ANALYZE shelf;
ANALYZE book;
ANALYZE book_author;
ANALYZE book_category;
ANALYZE book_copy;
ANALYZE member;
ANALYZE app_user;
ANALYZE loan;
ANALYZE reservation;
ANALYZE fine_charge;

SELECT 'books' AS fixture_table, count(*) AS fixture_rows FROM book WHERE id::text LIKE 'f5f5f5f5-%'
UNION ALL
SELECT 'copies', count(*) FROM book_copy WHERE id::text LIKE 'f6f6f6f6-%'
UNION ALL
SELECT 'members', count(*) FROM member WHERE id::text LIKE 'f7f7f7f7-%'
UNION ALL
SELECT 'loans', count(*) FROM loan WHERE id::text LIKE 'f9f9f9f9-%'
UNION ALL
SELECT 'reservations', count(*) FROM reservation WHERE id::text LIKE 'fafafafa-%'
UNION ALL
SELECT 'fines', count(*) FROM fine_charge WHERE id::text LIKE 'fbfbfbfb-%'
ORDER BY fixture_table;
