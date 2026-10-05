package com.hirelog.contact;

import java.time.Instant;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class ContactDtos {

    private ContactDtos() {
    }

    public record ContactRequest(
            @NotNull Long companyId,
            Long applicationId,
            @NotBlank @Size(max = 200) String name,
            @Size(max = 200) String title,
            @Email @Size(max = 320) String email,
            @Size(max = 50) String phone,
            @Size(max = 500) String linkedinUrl,
            String notes) {
    }

    public record ContactResponse(
            Long id,
            Long companyId,
            Long applicationId,
            String name,
            String title,
            String email,
            String phone,
            String linkedinUrl,
            String notes,
            Instant createdAt,
            Instant updatedAt) {

        static ContactResponse from(Contact c) {
            return new ContactResponse(c.getId(), c.getCompany().getId(),
                    c.getApplication() == null ? null : c.getApplication().getId(),
                    c.getName(), c.getTitle(), c.getEmail(), c.getPhone(), c.getLinkedinUrl(), c.getNotes(),
                    c.getCreatedAt(), c.getUpdatedAt());
        }
    }
}
