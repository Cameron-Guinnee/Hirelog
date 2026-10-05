package com.hirelog.followup;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowUpRepository extends JpaRepository<FollowUp, Long> {

    List<FollowUp> findAllByOrderByDueOnAsc();

    List<FollowUp> findByCompletedAtIsNullOrderByDueOnAsc();

    List<FollowUp> findByApplicationIdOrderByDueOnAsc(Long applicationId);
}
