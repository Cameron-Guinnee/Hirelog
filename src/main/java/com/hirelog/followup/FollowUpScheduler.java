package com.hirelog.followup;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class FollowUpScheduler {

    private static final Logger log = LoggerFactory.getLogger(FollowUpScheduler.class);

    private final FollowUpService service;

    public FollowUpScheduler(FollowUpService service) {
        this.service = service;
    }

    @Scheduled(cron = "${hirelog.follow-ups.flag-cron}")
    public void flagDueFollowUps() {
        List<FollowUp> flagged = service.flagDue();
        if (!flagged.isEmpty()) {
            log.info("Flagged {} follow-up(s) as due: {}", flagged.size(),
                    flagged.stream().map(FollowUp::getId).toList());
        }
    }
}
