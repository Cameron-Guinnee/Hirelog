package com.hirelog.followup;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowUpRepository extends JpaRepository<FollowUp, Long> {

    List<FollowUp> findAllByOrderByDueOnAsc();

    List<FollowUp> findByCompletedAtIsNullOrderByDueOnAsc();

    List<FollowUp> findByApplicationIdOrderByDueOnAsc(Long applicationId);

    /** Pending follow-ups due on or before {@code date} that the scheduler has not flagged yet. */
    List<FollowUp> findByCompletedAtIsNullAndFlaggedAtIsNullAndDueOnLessThanEqual(LocalDate date);

    List<FollowUp> findByCompletedAtIsNullAndFlaggedAtIsNotNullOrderByDueOnAsc();
}
