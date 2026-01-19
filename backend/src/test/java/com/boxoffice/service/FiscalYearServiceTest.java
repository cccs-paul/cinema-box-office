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
    }

    @Test
    void testCreateFiscalYearSuccess() {
        String name = "2024";
        Long rcId = 1L;
        String username = "admin";
        ResponsibilityCentre rc = ResponsibilityCentre.builder()
                .id(rcId)
                .ownerUsername(username)
                .name("Normal RC")
                .build();

        when(rcRepository.findById(rcId)).thenReturn(Optional.of(rc));
        when(repository.existsByNameAndRcId(name, rcId)).thenReturn(false);
        when(repository.save(any(FiscalYear.class))).thenAnswer(i -> {
            FiscalYear fy = i.getArgument(0);
            fy.setId(10L);
            return fy;
        });

        FiscalYearDTO result = service.create(name, rcId, username);

        assertNotNull(result);
        assertEquals(name, result.getName());
    }

    @Test
    void testCreateFiscalYearDuplicateFails() {
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

    @Test
    void testUpdateFiscalYearSuccess() {
        Long fyId = 10L;
        String username = "admin";
        ResponsibilityCentre rc = ResponsibilityCentre.builder()
                .id(1L)
                .ownerUsername(username)
                .name("Normal RC")
                .build();
        FiscalYear fy = FiscalYear.builder()
                .id(fyId)
                .name("Old Name")
                .rc(rc)
                .build();

        when(repository.findById(fyId)).thenReturn(Optional.of(fy));
        when(repository.existsByNameAndRcId("New Name", 1L)).thenReturn(false);
        when(repository.save(any(FiscalYear.class))).thenAnswer(i -> i.getArgument(0));

        FiscalYearDTO result = service.update(fyId, "New Name", username);

        assertEquals("New Name", result.getName());
    }

    @Test
    void testUpdateFiscalYearSameNameSuccess() {
        Long fyId = 10L;
        String username = "admin";
        ResponsibilityCentre rc = ResponsibilityCentre.builder()
                .id(1L)
                .ownerUsername(username)
                .name("Normal RC")
                .build();
        FiscalYear fy = FiscalYear.builder()
                .id(fyId)
                .name("Same Name")
                .rc(rc)
                .build();

        when(repository.findById(fyId)).thenReturn(Optional.of(fy));
        when(repository.existsByNameAndRcId("Same Name", 1L)).thenReturn(true);
        when(repository.save(any(FiscalYear.class))).thenAnswer(i -> i.getArgument(0));

        FiscalYearDTO result = service.update(fyId, "Same Name", username);

        assertEquals("Same Name", result.getName());
    }

    @Test
    void testDeleteFiscalYearSuccess() {
        Long fyId = 10L;
        String username = "admin";
        ResponsibilityCentre rc = ResponsibilityCentre.builder()
                .id(1L)
                .ownerUsername(username)
                .build();
        FiscalYear fy = FiscalYear.builder()
                .id(fyId)
                .rc(rc)
                .build();

        when(repository.findById(fyId)).thenReturn(Optional.of(fy));

        service.delete(fyId, username);

        verify(repository).delete(fy);
    }
}
