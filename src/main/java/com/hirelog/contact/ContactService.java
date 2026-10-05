package com.hirelog.contact;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hirelog.application.ApplicationService;
import com.hirelog.application.JobApplication;
import com.hirelog.common.NotFoundException;
import com.hirelog.company.Company;
import com.hirelog.company.CompanyService;
import com.hirelog.contact.ContactDtos.ContactRequest;

@Service
@Transactional
public class ContactService {

    private final ContactRepository contacts;
    private final CompanyService companies;
    private final ApplicationService applications;

    public ContactService(ContactRepository contacts, CompanyService companies, ApplicationService applications) {
        this.contacts = contacts;
        this.companies = companies;
        this.applications = applications;
    }

    @Transactional(readOnly = true)
    public List<Contact> list(Long companyId) {
        return companyId == null
                ? contacts.findAllByOrderByNameAsc()
                : contacts.findByCompanyIdOrderByNameAsc(companyId);
    }

    @Transactional(readOnly = true)
    public Contact get(Long id) {
        return contacts.findById(id).orElseThrow(() -> new NotFoundException("Contact", id));
    }

    public Contact create(ContactRequest request) {
        Contact contact = new Contact(companies.get(request.companyId()), request.name());
        apply(contact, request);
        return contacts.save(contact);
    }

    public Contact update(Long id, ContactRequest request) {
        Contact contact = get(id);
        apply(contact, request);
        return contact;
    }

    public void delete(Long id) {
        contacts.delete(get(id));
    }

    private void apply(Contact contact, ContactRequest request) {
        Company company = contact.getCompany().getId().equals(request.companyId())
                ? contact.getCompany()
                : companies.get(request.companyId());
        JobApplication application = null;
        if (request.applicationId() != null) {
            application = applications.get(request.applicationId());
            if (!application.getCompany().getId().equals(company.getId())) {
                throw new IllegalArgumentException("Application " + application.getId()
                        + " belongs to a different company than the contact");
            }
        }
        contact.setCompany(company);
        contact.setApplication(application);
        contact.setName(request.name());
        contact.setTitle(request.title());
        contact.setEmail(request.email());
        contact.setPhone(request.phone());
        contact.setLinkedinUrl(request.linkedinUrl());
        contact.setNotes(request.notes());
    }
}
