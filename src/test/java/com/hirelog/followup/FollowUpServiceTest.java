package com.hirelog.followup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hirelog.application.ApplicationService;
import com.hirelog.application.ApplicationStatus;
import com.hirelog.application.JobApplication;
import com.hirelog.company.Company;
import com.hirelog.contact.ContactService;
import com.hirelog.followup.FollowUpDtos.FollowUpRequest;

@ExtendWith(MockitoExtension.class)
class FollowUpServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-05T08:00:00Z");
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 5);

    @Mock
    private FollowUpRepository followUps;
    @Mock
    private ApplicationService applications;
    @Mock
    private ContactService contacts;

    private FollowUpService service;
    private JobApplication application;

    @BeforeEach
    void setUp() {
        service = new FollowUpService(followUps, applications, contacts, Clock.fixed(NOW, ZoneOffset.UTC));
        application = new JobApplication(new Company("Acme"), "Engineer", ApplicationStatus.APPLIED);
        application.setId(7L);
    }

    @Test
    void flagDueStampsEveryUnflaggedDueFollowUp() {
        FollowUp overdue = new FollowUp(application, TODAY.minusDays(2), "Chase recruiter");
        FollowUp dueToday = new FollowUp(application, TODAY, "Send thank-you note");
        when(followUps.findByCompletedAtIsNullAndFlaggedAtIsNullAndDueOnLessThanEqual(TODAY))
                .thenReturn(List.of(overdue, dueToday));

        List<FollowUp> flagged = service.flagDue();

        assertThat(flagged).containsExactly(overdue, dueToday);
        assertThat(flagged).allSatisfy(f -> assertThat(f.getFlaggedAt()).isEqualTo(NOW));
    }

    @Test
    void completeSetsCompletionTimeOnce() {
        FollowUp followUp = new FollowUp(application, TODAY, "Chase recruiter");
        Instant earlier = NOW.minusSeconds(3600);
        followUp.setCompletedAt(earlier);
        when(followUps.findById(1L)).thenReturn(Optional.of(followUp));

        service.complete(1L);

        assertThat(followUp.getCompletedAt()).isEqualTo(earlier);
    }

    @Test
    void movingDueDateIntoTheFutureClearsTheFlag() {
        FollowUp followUp = new FollowUp(application, TODAY, "Chase recruiter");
        followUp.setFlaggedAt(NOW);
        when(followUps.findById(1L)).thenReturn(Optional.of(followUp));

        service.update(1L, new FollowUpRequest(7L, null, TODAY.plusDays(3), "Chase recruiter again"));

        assertThat(followUp.isFlagged()).isFalse();
        assertThat(followUp.getDueOn()).isEqualTo(TODAY.plusDays(3));
    }
}
