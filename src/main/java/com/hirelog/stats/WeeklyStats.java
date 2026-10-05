package com.hirelog.stats;

import java.time.LocalDate;
import java.util.List;

import com.hirelog.application.ApplicationStatus;

/**
 * Stats for the cohort of applications sent in one ISO week (Monday to Sunday).
 *
 * @param responseRate share of the cohort that heard back (moved past APPLIED, including rejections), 0..1
 * @param stages funnel from APPLIED to OFFER; each stage counts applications that reached it or went further
 */
public record WeeklyStats(
        LocalDate weekStart,
        LocalDate weekEnd,
        int applicationsSent,
        int responses,
        double responseRate,
        List<StageConversion> stages) {

    /**
     * @param conversionRate reached / reached-at-previous-stage, or null for the first stage or an empty previous stage
     */
    public record StageConversion(ApplicationStatus stage, int reached, Double conversionRate) {
    }
}
