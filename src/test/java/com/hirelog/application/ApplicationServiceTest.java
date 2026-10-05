package com.hirelog.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hirelog.application.ApplicationDtos.CreateApplicationRequest;
import com.hirelog.company.Company;
import com.hirelog.company.CompanyService;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-05T12:00:00Z");

    @Mock
    private JobApplicationRepository applications;
    @Mock
    private StatusChangeRepository statusChanges;
    @Mock
    private CompanyService companies;

    private ApplicationService service;
    private Company company;

    @BeforeEach
    void setUp() {
        service = new ApplicationService(applications, statusChanges, companies, Clock.fixed(NOW, ZoneOffset.UTC));
        company = new Company("Acme");
        company.setId(1L);
    }

    @Test
    void createDefaultsToAppliedTodayAndRecordsInitialHistory() {
        when(companies.get(1L)).thenReturn(company);
        when(applications.save(any())).thenAnswer(inv -> inv.getArgument(0));

        JobApplication created = service.create(
                new CreateApplicationRequest(1L, "Backend Engineer", null, "LinkedIn", null, null, null));

        assertThat(created.getStatus()).isEqualTo(ApplicationStatus.APPLIED);
        assertThat(created.getAppliedOn()).isEqualTo(LocalDate.of(2026, 10, 5));
        StatusChange change = captureStatusChange();
        assertThat(change.getFromStatus()).isNull();
        assertThat(change.getToStatus()).isEqualTo(ApplicationStatus.APPLIED);
        assertThat(change.getChangedAt()).isEqualTo(NOW);
    }

    @Test
    void createSavedApplicationLeavesAppliedOnEmpty() {
        when(companies.get(1L)).thenReturn(company);
        when(applications.save(any())).thenAnswer(inv -> inv.getArgument(0));

        JobApplication created = service.create(new CreateApplicationRequest(
                1L, "Backend Engineer", null, null, ApplicationStatus.SAVED, null, null));

        assertThat(created.getAppliedOn()).isNull();
    }

    @Test
    void changeStatusAppendsTransitionToHistory() {
        JobApplication application = existing(ApplicationStatus.APPLIED);

        service.changeStatus(10L, ApplicationStatus.SCREENING, "Recruiter call booked");

        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.SCREENING);
        StatusChange change = captureStatusChange();
        assertThat(change.getFromStatus()).isEqualTo(ApplicationStatus.APPLIED);
        assertThat(change.getToStatus()).isEqualTo(ApplicationStatus.SCREENING);
        assertThat(change.getNote()).isEqualTo("Recruiter call booked");
    }

    @Test
    void changeStatusToSameStatusRecordsNothing() {
        existing(ApplicationStatus.INTERVIEWING);

        service.changeStatus(10L, ApplicationStatus.INTERVIEWING, null);

        verify(statusChanges, never()).save(any());
    }

    @Test
    void submittingASavedApplicationStampsAppliedOn() {
        JobApplication application = existing(ApplicationStatus.SAVED);

        service.changeStatus(10L, ApplicationStatus.APPLIED, null);

        assertThat(application.getAppliedOn()).isEqualTo(LocalDate.of(2026, 10, 5));
    }

    private JobApplication existing(ApplicationStatus status) {
        JobApplication application = new JobApplication(company, "Backend Engineer", status);
        application.setId(10L);
        when(applications.findById(10L)).thenReturn(Optional.of(application));
        return application;
    }

    private StatusChange captureStatusChange() {
        ArgumentCaptor<StatusChange> captor = ArgumentCaptor.forClass(StatusChange.class);
        verify(statusChanges).save(captor.capture());
        return captor.getValue();
    }
}
