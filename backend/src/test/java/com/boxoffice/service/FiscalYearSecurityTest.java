package com.boxoffice.service;

import com.boxoffice.model.FiscalYear;
import com.boxoffice.model.ResponsibilityCentre;
import com.boxoffice.repository.FiscalYearRepository;
import com.boxoffice.repository.ResponsibilityCentreRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FiscalYearSecurityTest {

    @Mock
    private FiscalYearRepository repository;

    @Mock
    private ResponsibilityCentreRepository rcRepository;

    @InjectMocks
    private FiscalYearService service;

    @Test
    public void create_shouldThrowException_whenUserIsNotOwner() {
        Long rcId = 1L;
        ResponsibilityCentre rc = ResponsibilityCentre.builder()
                .id(rcId)
                .ownerUsername("owner")
                .name("Some RC")
                .build();

        when(rcRepository.findById(rcId)).thenReturn(Optional.of(rc));

        assertThrows(RuntimeException.class, () -> {
            service.create("FY 2024", rcId, "not-owner");
        });
    }

    @Test
    public void create_shouldThrowException_whenRcIsDemo() {
        Long rcId = 1L;
        ResponsibilityCentre rc = ResponsibilityCentre.builder()
                .id(rcId)
                .ownerUsername("system")
                .name("Demo Responsibility Centre")
                .build();

        when(rcRepository.findById(rcId)).thenReturn(Optional.of(rc));

        assertThrows(RuntimeException.class, () -> {
            service.create("FY 2024", rcId, "system");
        });
    }

    @Test
    public void update_shouldThrowException_whenUserIsNotOwner() {
        Long fyId = 10L;
        ResponsibilityCentre rc = ResponsibilityCentre.builder()
                .id(1L)
                .ownerUsername("owner")
                .build();
        FiscalYear fy = FiscalYear.builder()
                .id(fyId)
                .rc(rc)
                .build();

        when(repository.findById(fyId)).thenReturn(Optional.of(fy));

        assertThrows(RuntimeException.class, () -> {
            service.update(fyId, "New Name", "not-owner");
        });
    }

    @Test
    public void delete_shouldThrowException_whenUserIsNotOwner() {
        Long fyId = 10L;
        ResponsibilityCentre rc = ResponsibilityCentre.builder()
                .id(1L)
                .ownerUsername("owner")
                .build();
        FiscalYear fy = FiscalYear.builder()
                .id(fyId)
                .rc(rc)
                .build();

        when(repository.findById(fyId)).thenReturn(Optional.of(fy));

        assertThrows(RuntimeException.class, () -> {
            service.delete(fyId, "not-owner");
        });
    }
}
