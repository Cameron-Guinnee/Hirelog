-- Append-only audit log of every status an application has moved through.
CREATE TABLE application_status_changes (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    application_id BIGINT      NOT NULL REFERENCES applications (id) ON DELETE CASCADE,
    from_status    VARCHAR(20),
    to_status      VARCHAR(20) NOT NULL,
    changed_at     TIMESTAMPTZ NOT NULL,
    note           TEXT
);

CREATE INDEX ix_status_changes_application ON application_status_changes (application_id, changed_at);

-- Backfill an initial entry for applications created before history existed.
INSERT INTO application_status_changes (application_id, from_status, to_status, changed_at)
SELECT id, NULL, status, created_at FROM applications;
