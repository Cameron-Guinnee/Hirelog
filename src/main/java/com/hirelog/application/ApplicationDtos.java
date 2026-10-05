package com.hirelog.application;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class ApplicationDtos {

    private ApplicationDtos() {
    }

    public record CreateApplicationRequest(
            @NotNull Long companyId,
            @NotBlank @Size(max = 200) String roleTitle,
            @Size(max = 1000) String jobUrl,
            @Size(max = 100) String source,
            ApplicationStatus status,
            LocalDate appliedOn,
            String notes) {
    }

    public record UpdateApplicationRequest(
            @NotNull Long companyId,
            @NotBlank @Size(max = 200) String roleTitle,
            @Size(max = 1000) String jobUrl,
            @Size(max = 100) String source,
            LocalDate appliedOn,
            String notes) {
    }

    public record ApplicationResponse(
            Long id,
            Long companyId,
            String companyName,
            String roleTitle,
            String jobUrl,
            String source,
            ApplicationStatus status,
            LocalDate appliedOn,
            String notes,
            Instant createdAt,
            Instant updatedAt) {

        static ApplicationResponse from(JobApplication a) {
            return new ApplicationResponse(a.getId(), a.getCompany().getId(), a.getCompany().getName(),
                    a.getRoleTitle(), a.getJobUrl(), a.getSource(), a.getStatus(), a.getAppliedOn(),
                    a.getNotes(), a.getCreatedAt(), a.getUpdatedAt());
        }
    }

    public record StatusChangeRequest(@NotNull ApplicationStatus status, String note) {
    }

    public record StatusChangeResponse(
            Long id,
            ApplicationStatus fromStatus,
            ApplicationStatus toStatus,
            Instant changedAt,
            String note) {

        static StatusChangeResponse from(StatusChange c) {
            return new StatusChangeResponse(c.getId(), c.getFromStatus(), c.getToStatus(), c.getChangedAt(),
                    c.getNote());
        }
    }
}
