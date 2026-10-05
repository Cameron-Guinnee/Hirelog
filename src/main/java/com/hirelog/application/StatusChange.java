package com.hirelog.application;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** One immutable entry in an application's status history. */
@Entity
@Table(name = "application_status_changes")
public class StatusChange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "application_id", nullable = false, updatable = false)
    private JobApplication application;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 20, updatable = false)
    private ApplicationStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 20, updatable = false)
    private ApplicationStatus toStatus;

    @Column(name = "changed_at", nullable = false, updatable = false)
    private Instant changedAt;

    @Column(columnDefinition = "text", updatable = false)
    private String note;

    protected StatusChange() {
    }

    public StatusChange(JobApplication application, ApplicationStatus fromStatus, ApplicationStatus toStatus,
            Instant changedAt, String note) {
        this.application = application;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.changedAt = changedAt;
        this.note = note;
    }

    public Long getId() {
        return id;
    }

    public JobApplication getApplication() {
        return application;
    }

    public ApplicationStatus getFromStatus() {
        return fromStatus;
    }

    public ApplicationStatus getToStatus() {
        return toStatus;
    }

    public Instant getChangedAt() {
        return changedAt;
    }

    public String getNote() {
        return note;
    }
}
