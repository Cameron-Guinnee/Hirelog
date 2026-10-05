package com.hirelog.contact;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactRepository extends JpaRepository<Contact, Long> {

    List<Contact> findAllByOrderByNameAsc();

    List<Contact> findByCompanyIdOrderByNameAsc(Long companyId);
}
