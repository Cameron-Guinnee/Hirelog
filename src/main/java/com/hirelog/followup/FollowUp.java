package com.hirelog.followup;

import java.time.Instant;
import java.time.LocalDate;

import com.hirelog.application.JobApplication;
import com.hirelog.common.AuditedEntity;
import com.hirelog.contact.Contact;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "follow_ups")
public class FollowUp extends AuditedEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private JobApplication application;

    @ManyToOne
    @JoinColumn(name = "contact_id")
    private Contact contact;

    @Column(name = "due_on", nullable = false)
    private LocalDate dueOn;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(name = "completed_at")
    private Instant completedAt;

    protected FollowUp() {
    }

    public FollowUp(JobApplication application, LocalDate dueOn, String description) {
        this.application = application;
        this.dueOn = dueOn;
        this.description = description;
    }

    public boolean isCompleted() {
        return completedAt != null;
    }

    public JobApplication getApplication() {
        return application;
    }

    public void setApplication(JobApplication application) {
        this.application = application;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public LocalDate getDueOn() {
        return dueOn;
    }

    public void setDueOn(LocalDate dueOn) {
        this.dueOn = dueOn;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}
