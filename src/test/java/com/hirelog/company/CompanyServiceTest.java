package com.hirelog.company;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hirelog.common.ConflictException;
import com.hirelog.common.NotFoundException;
import com.hirelog.company.CompanyDtos.CompanyRequest;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository companies;

    @InjectMocks
    private CompanyService service;

    @Test
    void rejectsDuplicateCompanyNames() {
        when(companies.existsByNameIgnoreCase("Acme")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new CompanyRequest("Acme", null, null, null, null)))
                .isInstanceOf(ConflictException.class);
        verify(companies, never()).save(any());
    }

    @Test
    void missingCompanyIsNotFound() {
        when(companies.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(99L)).isInstanceOf(NotFoundException.class);
    }
}
