package com.hirelog.stats;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hirelog.application.ApplicationStatus;
import com.hirelog.application.JobApplication;
import com.hirelog.application.JobApplicationRepository;
import com.hirelog.application.StatusChange;
import com.hirelog.application.StatusChangeRepository;
import com.hirelog.stats.WeeklyStats.StageConversion;

@Service
@Transactional(readOnly = true)
public class StatsService {

    /** Funnel stages in order; an application counts toward every stage up to the furthest it reached. */
    static final List<ApplicationStatus> FUNNEL = List.of(
            ApplicationStatus.APPLIED,
            ApplicationStatus.SCREENING,
            ApplicationStatus.INTERVIEWING,
            ApplicationStatus.OFFER);

    private static final Set<ApplicationStatus> RESPONSES = EnumSet.of(
            ApplicationStatus.SCREENING,
            ApplicationStatus.INTERVIEWING,
            ApplicationStatus.OFFER,
            ApplicationStatus.ACCEPTED,
            ApplicationStatus.REJECTED);

    private final JobApplicationRepository applications;
    private final StatusChangeRepository statusChanges;
    private final Clock clock;

    public StatsService(JobApplicationRepository applications, StatusChangeRepository statusChanges, Clock clock) {
        this.applications = applications;
        this.statusChanges = statusChanges;
        this.clock = clock;
    }

    public WeeklyStats weekly(LocalDate weekOf) {
        LocalDate day = weekOf == null ? LocalDate.now(clock) : weekOf;
        LocalDate start = day.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate end = start.plusDays(6);

        List<JobApplication> cohort = applications.findByAppliedOnBetween(start, end).stream()
                .filter(a -> a.getStatus().isSubmitted())
                .toList();
        Map<Long, Set<ApplicationStatus>> reached = statusesReached(cohort);

        int responses = 0;
        int[] reachedPerStage = new int[FUNNEL.size()];
        for (Set<ApplicationStatus> statuses : reached.values()) {
            if (statuses.stream().anyMatch(RESPONSES::contains)) {
                responses++;
            }
            int furthest = furthestStage(statuses);
            for (int i = 0; i <= furthest; i++) {
                reachedPerStage[i]++;
            }
        }

        List<StageConversion> stages = new ArrayList<>();
        for (int i = 0; i < FUNNEL.size(); i++) {
            Double rate = i == 0 || reachedPerStage[i - 1] == 0
                    ? null
                    : ratio(reachedPerStage[i], reachedPerStage[i - 1]);
            stages.add(new StageConversion(FUNNEL.get(i), reachedPerStage[i], rate));
        }

        int sent = cohort.size();
        return new WeeklyStats(start, end, sent, responses, sent == 0 ? 0.0 : ratio(responses, sent), stages);
    }

    private Map<Long, Set<ApplicationStatus>> statusesReached(List<JobApplication> cohort) {
        Map<Long, Set<ApplicationStatus>> reached = new HashMap<>();
        for (JobApplication application : cohort) {
            Set<ApplicationStatus> statuses = EnumSet.of(ApplicationStatus.APPLIED, application.getStatus());
            reached.put(application.getId(), statuses);
        }
        if (!reached.isEmpty()) {
            for (StatusChange change : statusChanges.findByApplicationIdIn(reached.keySet())) {
                reached.get(change.getApplication().getId()).add(change.getToStatus());
            }
        }
        return reached;
    }

    private static int furthestStage(Set<ApplicationStatus> statuses) {
        if (statuses.contains(ApplicationStatus.ACCEPTED)) {
            return FUNNEL.indexOf(ApplicationStatus.OFFER);
        }
        int furthest = 0;
        for (int i = 0; i < FUNNEL.size(); i++) {
            if (statuses.contains(FUNNEL.get(i))) {
                furthest = i;
            }
        }
        return furthest;
    }

    private static double ratio(int numerator, int denominator) {
        return Math.round(1000.0 * numerator / denominator) / 1000.0;
    }
}
