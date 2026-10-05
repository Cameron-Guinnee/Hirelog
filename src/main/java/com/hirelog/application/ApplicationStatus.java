package com.hirelog.application;

public enum ApplicationStatus {
    SAVED,
    APPLIED,
    SCREENING,
    INTERVIEWING,
    OFFER,
    ACCEPTED,
    REJECTED,
    WITHDRAWN,
    GHOSTED;

    /** True once the application has actually been sent to the employer. */
    public boolean isSubmitted() {
        return this != SAVED;
    }
}
