package com.hirelog.company;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hirelog.common.ConflictException;
import com.hirelog.common.NotFoundException;
import com.hirelog.company.CompanyDtos.CompanyRequest;

@Service
@Transactional
public class CompanyService {

    private final CompanyRepository companies;

    public CompanyService(CompanyRepository companies) {
        this.companies = companies;
    }

    @Transactional(readOnly = true)
    public List<Company> list() {
        return companies.findAllByOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public Company get(Long id) {
        return companies.findById(id).orElseThrow(() -> new NotFoundException("Company", id));
    }

    public Company create(CompanyRequest request) {
        if (companies.existsByNameIgnoreCase(request.name())) {
            throw new ConflictException("Company '" + request.name() + "' already exists");
        }
        Company company = new Company(request.name());
        apply(company, request);
        return companies.save(company);
    }

    public Company update(Long id, CompanyRequest request) {
        Company company = get(id);
        if (companies.existsByNameIgnoreCaseAndIdNot(request.name(), id)) {
            throw new ConflictException("Company '" + request.name() + "' already exists");
        }
        apply(company, request);
        return company;
    }

    public void delete(Long id) {
        companies.delete(get(id));
    }

    private static void apply(Company company, CompanyRequest request) {
        company.setName(request.name());
        company.setWebsite(request.website());
        company.setIndustry(request.industry());
        company.setLocation(request.location());
        company.setNotes(request.notes());
    }
}
