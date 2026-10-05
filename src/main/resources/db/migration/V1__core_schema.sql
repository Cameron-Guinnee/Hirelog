CREATE TABLE companies (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        VARCHAR(200) NOT NULL,
    website     VARCHAR(500),
    industry    VARCHAR(100),
    location    VARCHAR(200),
    notes       TEXT,
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL,
    CONSTRAINT uq_companies_name UNIQUE (name)
);

CREATE TABLE applications (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    company_id  BIGINT       NOT NULL REFERENCES companies (id) ON DELETE CASCADE,
    role_title  VARCHAR(200) NOT NULL,
    job_url     VARCHAR(1000),
    source      VARCHAR(100),
    status      VARCHAR(20)  NOT NULL,
    applied_on  DATE,
    notes       TEXT,
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL,
    CONSTRAINT ck_applications_status CHECK (status IN
        ('SAVED', 'APPLIED', 'SCREENING', 'INTERVIEWING', 'OFFER', 'ACCEPTED', 'REJECTED', 'WITHDRAWN', 'GHOSTED'))
);

CREATE INDEX ix_applications_company ON applications (company_id);
CREATE INDEX ix_applications_applied_on ON applications (applied_on);

CREATE TABLE contacts (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    company_id     BIGINT       NOT NULL REFERENCES companies (id) ON DELETE CASCADE,
    application_id BIGINT       REFERENCES applications (id) ON DELETE SET NULL,
    name           VARCHAR(200) NOT NULL,
    title          VARCHAR(200),
    email          VARCHAR(320),
    phone          VARCHAR(50),
    linkedin_url   VARCHAR(500),
    notes          TEXT,
    created_at     TIMESTAMPTZ  NOT NULL,
    updated_at     TIMESTAMPTZ  NOT NULL
);

CREATE INDEX ix_contacts_company ON contacts (company_id);

CREATE TABLE follow_ups (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    application_id BIGINT        NOT NULL REFERENCES applications (id) ON DELETE CASCADE,
    contact_id     BIGINT        REFERENCES contacts (id) ON DELETE SET NULL,
    due_on         DATE          NOT NULL,
    description    VARCHAR(1000) NOT NULL,
    completed_at   TIMESTAMPTZ,
    created_at     TIMESTAMPTZ   NOT NULL,
    updated_at     TIMESTAMPTZ   NOT NULL
);

CREATE INDEX ix_follow_ups_due_on ON follow_ups (due_on) WHERE completed_at IS NULL;
