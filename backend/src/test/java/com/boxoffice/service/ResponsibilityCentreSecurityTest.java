package com.boxoffice.service;

import com.boxoffice.model.ResponsibilityCentre;
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
public class ResponsibilityCentreSecurityTest {

    @Mock
    private ResponsibilityCentreRepository repository;

    @InjectMocks
    private ResponsibilityCentreService service;

    @Test
    public void update_shouldThrowException_whenUserIsNotOwner() {
        Long rcId = 1L;
        ResponsibilityCentre rc = ResponsibilityCentre.builder()
                .id(rcId)
                .ownerUsername("owner")
                .name("Some RC")
                .build();

        when(repository.findById(rcId)).thenReturn(Optional.of(rc));

        assertThrows(RuntimeException.class, () -> {
            service.update(rcId, "New Name", "Desc", "not-owner");
        });
    }

    @Test
    public void delete_shouldThrowException_whenUserIsNotOwner() {
        Long rcId = 1L;
        ResponsibilityCentre rc = ResponsibilityCentre.builder()
                .id(rcId)
                .ownerUsername("owner")
                .name("Some RC")
                .build();

        when(repository.findById(rcId)).thenReturn(Optional.of(rc));

        assertThrows(RuntimeException.class, () -> {
            service.delete(rcId, "not-owner");
        });
    }

    @Test
    public void update_shouldThrowException_whenRcIsDemo() {
        Long rcId = 1L;
        ResponsibilityCentre rc = ResponsibilityCentre.builder()
                .id(rcId)
                .ownerUsername("system")
                .name("Demo Responsibility Centre")
                .build();

        when(repository.findById(rcId)).thenReturn(Optional.of(rc));

        assertThrows(RuntimeException.class, () -> {
            service.update(rcId, "New Name", "Desc", "system");
        });
    }

    @Test
    public void delete_shouldThrowException_whenRcIsDemo() {
        Long rcId = 1L;
        ResponsibilityCentre rc = ResponsibilityCentre.builder()
                .id(rcId)
                .ownerUsername("system")
                .name("Demo Responsibility Centre")
                .build();

        when(repository.findById(rcId)).thenReturn(Optional.of(rc));

        assertThrows(RuntimeException.class, () -> {
            service.delete(rcId, "system");
        });
    }
}
