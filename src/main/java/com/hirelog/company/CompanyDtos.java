package com.hirelog.company;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class CompanyDtos {

    private CompanyDtos() {
    }

    public record CompanyRequest(
            @NotBlank @Size(max = 200) String name,
            @Size(max = 500) String website,
            @Size(max = 100) String industry,
            @Size(max = 200) String location,
            String notes) {
    }

    public record CompanyResponse(
            Long id,
            String name,
            String website,
            String industry,
            String location,
            String notes,
            Instant createdAt,
            Instant updatedAt) {

        static CompanyResponse from(Company c) {
            return new CompanyResponse(c.getId(), c.getName(), c.getWebsite(), c.getIndustry(),
                    c.getLocation(), c.getNotes(), c.getCreatedAt(), c.getUpdatedAt());
        }
    }
}
