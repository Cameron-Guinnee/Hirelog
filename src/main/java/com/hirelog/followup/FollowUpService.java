package com.hirelog.followup;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hirelog.application.ApplicationService;
import com.hirelog.application.JobApplication;
import com.hirelog.common.NotFoundException;
import com.hirelog.contact.Contact;
import com.hirelog.contact.ContactService;
import com.hirelog.followup.FollowUpDtos.FollowUpRequest;

@Service
@Transactional
public class FollowUpService {

    private final FollowUpRepository followUps;
    private final ApplicationService applications;
    private final ContactService contacts;
    private final Clock clock;

    public FollowUpService(FollowUpRepository followUps, ApplicationService applications,
            ContactService contacts, Clock clock) {
        this.followUps = followUps;
        this.applications = applications;
        this.contacts = contacts;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<FollowUp> list(Long applicationId, boolean pendingOnly) {
        if (applicationId != null) {
            return followUps.findByApplicationIdOrderByDueOnAsc(applicationId).stream()
                    .filter(f -> !pendingOnly || !f.isCompleted())
                    .toList();
        }
        return pendingOnly ? followUps.findByCompletedAtIsNullOrderByDueOnAsc() : followUps.findAllByOrderByDueOnAsc();
    }

    @Transactional(readOnly = true)
    public FollowUp get(Long id) {
        return followUps.findById(id).orElseThrow(() -> new NotFoundException("Follow-up", id));
    }

    public FollowUp create(FollowUpRequest request) {
        FollowUp followUp = new FollowUp(applications.get(request.applicationId()), request.dueOn(),
                request.description());
        followUp.setContact(resolveContact(request.contactId()));
        return followUps.save(followUp);
    }

    public FollowUp update(Long id, FollowUpRequest request) {
        FollowUp followUp = get(id);
        JobApplication application = followUp.getApplication().getId().equals(request.applicationId())
                ? followUp.getApplication()
                : applications.get(request.applicationId());
        followUp.setApplication(application);
        followUp.setContact(resolveContact(request.contactId()));
        followUp.setDueOn(request.dueOn());
        followUp.setDescription(request.description());
        return followUp;
    }

    public FollowUp complete(Long id) {
        FollowUp followUp = get(id);
        if (!followUp.isCompleted()) {
            followUp.setCompletedAt(Instant.now(clock));
        }
        return followUp;
    }

    public void delete(Long id) {
        followUps.delete(get(id));
    }

    private Contact resolveContact(Long contactId) {
        return contactId == null ? null : contacts.get(contactId);
    }
}
