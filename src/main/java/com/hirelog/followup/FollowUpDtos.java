package com.hirelog.followup;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class FollowUpDtos {

    private FollowUpDtos() {
    }

    public record FollowUpRequest(
            @NotNull Long applicationId,
            Long contactId,
            @NotNull LocalDate dueOn,
            @NotBlank @Size(max = 1000) String description) {
    }

    public record FollowUpResponse(
            Long id,
            Long applicationId,
            Long contactId,
            LocalDate dueOn,
            String description,
            boolean completed,
            Instant completedAt,
            Instant createdAt,
            Instant updatedAt) {

        static FollowUpResponse from(FollowUp f) {
            return new FollowUpResponse(f.getId(), f.getApplication().getId(),
                    f.getContact() == null ? null : f.getContact().getId(),
                    f.getDueOn(), f.getDescription(), f.isCompleted(), f.getCompletedAt(),
                    f.getCreatedAt(), f.getUpdatedAt());
        }
    }
}
