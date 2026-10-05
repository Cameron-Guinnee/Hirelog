package com.hirelog.application;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    List<JobApplication> findAllByOrderByCreatedAtDesc();

    List<JobApplication> findByStatusOrderByCreatedAtDesc(ApplicationStatus status);

    List<JobApplication> findByAppliedOnBetween(LocalDate from, LocalDate to);
}
