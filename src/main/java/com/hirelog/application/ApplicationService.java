package com.hirelog.application;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hirelog.application.ApplicationDtos.CreateApplicationRequest;
import com.hirelog.application.ApplicationDtos.UpdateApplicationRequest;
import com.hirelog.common.NotFoundException;
import com.hirelog.company.Company;
import com.hirelog.company.CompanyService;

@Service
@Transactional
public class ApplicationService {

    private final JobApplicationRepository applications;
    private final StatusChangeRepository statusChanges;
    private final CompanyService companies;
    private final Clock clock;

    public ApplicationService(JobApplicationRepository applications, StatusChangeRepository statusChanges,
            CompanyService companies, Clock clock) {
        this.applications = applications;
        this.statusChanges = statusChanges;
        this.companies = companies;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<JobApplication> list(ApplicationStatus status) {
        return status == null
                ? applications.findAllByOrderByCreatedAtDesc()
                : applications.findByStatusOrderByCreatedAtDesc(status);
    }

    @Transactional(readOnly = true)
    public JobApplication get(Long id) {
        return applications.findById(id).orElseThrow(() -> new NotFoundException("Application", id));
    }

    public JobApplication create(CreateApplicationRequest request) {
        Company company = companies.get(request.companyId());
        ApplicationStatus status = request.status() == null ? ApplicationStatus.APPLIED : request.status();
        JobApplication application = new JobApplication(company, request.roleTitle(), status);
        application.setJobUrl(request.jobUrl());
        application.setSource(request.source());
        application.setNotes(request.notes());
        if (request.appliedOn() != null) {
            application.setAppliedOn(request.appliedOn());
        } else if (status.isSubmitted()) {
            application.setAppliedOn(LocalDate.now(clock));
        }
        JobApplication saved = applications.save(application);
        statusChanges.save(new StatusChange(saved, null, status, Instant.now(clock), null));
        return saved;
    }

    /**
     * Moves an application to a new status and appends the transition to its history.
     * Setting the current status again is a no-op and records nothing.
     */
    public JobApplication changeStatus(Long id, ApplicationStatus newStatus, String note) {
        JobApplication application = get(id);
        ApplicationStatus previous = application.getStatus();
        if (previous == newStatus) {
            return application;
        }
        application.setStatus(newStatus);
        if (newStatus.isSubmitted() && application.getAppliedOn() == null) {
            application.setAppliedOn(LocalDate.now(clock));
        }
        statusChanges.save(new StatusChange(application, previous, newStatus, Instant.now(clock), note));
        return application;
    }

    @Transactional(readOnly = true)
    public List<StatusChange> history(Long id) {
        get(id);
        return statusChanges.findByApplicationIdOrderByChangedAtAscIdAsc(id);
    }

    public JobApplication update(Long id, UpdateApplicationRequest request) {
        JobApplication application = get(id);
        if (!application.getCompany().getId().equals(request.companyId())) {
            application.setCompany(companies.get(request.companyId()));
        }
        application.setRoleTitle(request.roleTitle());
        application.setJobUrl(request.jobUrl());
        application.setSource(request.source());
        application.setAppliedOn(request.appliedOn());
        application.setNotes(request.notes());
        return application;
    }

    public void delete(Long id) {
        applications.delete(get(id));
    }
}
