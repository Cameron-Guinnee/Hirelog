package com.hirelog.stats;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hirelog.application.ApplicationStatus;
import com.hirelog.application.JobApplication;
import com.hirelog.application.JobApplicationRepository;
import com.hirelog.application.StatusChange;
import com.hirelog.application.StatusChangeRepository;
import com.hirelog.company.Company;
import com.hirelog.stats.WeeklyStats.StageConversion;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    // Wednesday; the ISO week runs Monday 2026-10-05 to Sunday 2026-10-11.
    private static final LocalDate WEDNESDAY = LocalDate.of(2026, 10, 7);
    private static final LocalDate MONDAY = LocalDate.of(2026, 10, 5);
    private static final LocalDate SUNDAY = LocalDate.of(2026, 10, 11);

    @Mock
    private JobApplicationRepository applications;
    @Mock
    private StatusChangeRepository statusChanges;

    private StatsService service;
    private final Company company = new Company("Acme");
    private final List<JobApplication> cohort = new ArrayList<>();
    private final List<StatusChange> history = new ArrayList<>();
    private long nextId = 1;

    @BeforeEach
    void setUp() {
        service = new StatsService(applications, statusChanges,
                Clock.fixed(Instant.parse("2026-10-07T09:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    void emptyWeekReportsZeroes() {
        when(applications.findByAppliedOnBetween(MONDAY, SUNDAY)).thenReturn(List.of());

        WeeklyStats stats = service.weekly(null);

        assertThat(stats.weekStart()).isEqualTo(MONDAY);
        assertThat(stats.weekEnd()).isEqualTo(SUNDAY);
        assertThat(stats.applicationsSent()).isZero();
        assertThat(stats.responseRate()).isZero();
        assertThat(stats.stages()).extracting(StageConversion::reached).containsOnly(0);
        verify(statusChanges, never()).findByApplicationIdIn(anyCollection());
    }

    @Test
    void computesResponseRateAndFunnelFromHistory() {
        application(ApplicationStatus.APPLIED);                                        // no response
        application(ApplicationStatus.REJECTED, ApplicationStatus.APPLIED);             // rejected straight away
        application(ApplicationStatus.REJECTED, ApplicationStatus.SCREENING);           // rejected after screen
        application(ApplicationStatus.OFFER, ApplicationStatus.SCREENING, ApplicationStatus.INTERVIEWING);
        when(applications.findByAppliedOnBetween(MONDAY, SUNDAY)).thenReturn(cohort);
        when(statusChanges.findByApplicationIdIn(anyCollection())).thenReturn(history);

        WeeklyStats stats = service.weekly(WEDNESDAY);

        assertThat(stats.applicationsSent()).isEqualTo(4);
        assertThat(stats.responses()).isEqualTo(3);
        assertThat(stats.responseRate()).isEqualTo(0.75);
        assertThat(stats.stages()).containsExactly(
                new StageConversion(ApplicationStatus.APPLIED, 4, null),
                new StageConversion(ApplicationStatus.SCREENING, 2, 0.5),
                new StageConversion(ApplicationStatus.INTERVIEWING, 1, 0.5),
                new StageConversion(ApplicationStatus.OFFER, 1, 1.0));
    }

    @Test
    void skippedStagesStillCountTowardEarlierStages() {
        application(ApplicationStatus.INTERVIEWING);
        when(applications.findByAppliedOnBetween(MONDAY, SUNDAY)).thenReturn(cohort);
        when(statusChanges.findByApplicationIdIn(anyCollection())).thenReturn(history);

        WeeklyStats stats = service.weekly(WEDNESDAY);

        assertThat(stats.stages()).extracting(StageConversion::reached).containsExactly(1, 1, 1, 0);
    }

    /** Adds an application that walked through {@code path} (after APPLIED) and now sits at {@code current}. */
    private void application(ApplicationStatus current, ApplicationStatus... path) {
        JobApplication application = new JobApplication(company, "Engineer", current);
        application.setId(nextId++);
        application.setAppliedOn(MONDAY);
        cohort.add(application);
        ApplicationStatus from = null;
        List<ApplicationStatus> steps = new ArrayList<>(List.of(ApplicationStatus.APPLIED));
        steps.addAll(List.of(path));
        steps.add(current);
        for (ApplicationStatus to : steps) {
            if (to != from) {
                history.add(new StatusChange(application, from, to, Instant.EPOCH, null));
                from = to;
            }
        }
    }
}
