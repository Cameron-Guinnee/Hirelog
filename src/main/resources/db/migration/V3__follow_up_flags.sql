-- Set by the scheduled job when a pending follow-up comes due.
ALTER TABLE follow_ups ADD COLUMN flagged_at TIMESTAMPTZ;
