package com.hirelog.application;

import java.time.Clock;
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
    private final CompanyService companies;
    private final Clock clock;

    public ApplicationService(JobApplicationRepository applications, CompanyService companies, Clock clock) {
        this.applications = applications;
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
        return applications.save(application);
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
