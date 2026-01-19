package com.boxoffice.service;

import com.boxoffice.dto.FiscalYearDTO;
import com.boxoffice.model.FiscalYear;
import com.boxoffice.model.ResponsibilityCentre;
import com.boxoffice.repository.FiscalYearRepository;
import com.boxoffice.repository.ResponsibilityCentreRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FiscalYearServiceTest {

    @Mock
    private FiscalYearRepository repository;

    @Mock
    private ResponsibilityCentreRepository rcRepository;

    @InjectMocks
    private FiscalYearService service;

    @Test
    void testGetAllForRc() {
        Long rcId = 1L;
        ResponsibilityCentre rc = ResponsibilityCentre.builder().id(rcId).build();
        FiscalYear fy1 = FiscalYear.builder().id(1L).name("2024").rc(rc).build();
        FiscalYear fy2 = FiscalYear.builder().id(2L).name("2025").rc(rc).build();

        when(repository.findByRcId(rcId)).thenReturn(Arrays.asList(fy1, fy2));

        List<FiscalYearDTO> result = service.getAllForRc(rcId);

        assertEquals(2, result.size());
        assertEquals("2024", result.get(0).getName());
        assertEquals("2025", result.get(1).getName());
    }

    @Test
    void testCreateFiscalYear() {
        String name = "2024";
        Long rcId = 1L;
        String username = "admin";
        ResponsibilityCentre rc = ResponsibilityCentre.builder()
                .id(rcId)
                .ownerUsername(username)
                .build();

        when(repository.existsByNameAndRcId(name, rcId)).thenReturn(false);
        when(rcRepository.findById(rcId)).thenReturn(Optional.of(rc));
        when(repository.save(any(FiscalYear.class))).thenAnswer(invocation -> {
            FiscalYear fy = invocation.getArgument(0);
            fy.setId(1L);
            return fy;
        });

        FiscalYearDTO result = service.create(name, rcId, username);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(name, result.getName());
        assertEquals(rcId, result.getRcId());
    }

    @Test
    void testCreateDuplicateFiscalYearFails() {
        String name = "2024";
        Long rcId = 1L;
        String username = "admin";
        ResponsibilityCentre rc = ResponsibilityCentre.builder()
                .id(rcId)
                .ownerUsername(username)
                .build();

        when(rcRepository.findById(rcId)).thenReturn(Optional.of(rc));
        when(repository.existsByNameAndRcId(name, rcId)).thenReturn(true);

        assertThrows(RuntimeException.class, () -> service.create(name, rcId, username));
    }
}
