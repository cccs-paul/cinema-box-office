package com.boxoffice.service;

import com.boxoffice.dto.ResponsibilityCentreDTO;
import com.boxoffice.model.ResponsibilityCentre;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResponsibilityCentreServiceTest {

    @Mock
    private ResponsibilityCentreRepository repository;

    @InjectMocks
    private ResponsibilityCentreService service;

    private static final String DEMO_RC_NAME = "Demo Responsibility Centre";

    @Test
    void testGetAllForUserIncludesDemoRC() {
        String username = "testuser";
        ResponsibilityCentre userRc = ResponsibilityCentre.builder()
                .id(1L)
                .name("User RC")
                .ownerUsername(username)
                .build();

        ResponsibilityCentre demoRc = ResponsibilityCentre.builder()
                .id(2L)
                .name(DEMO_RC_NAME)
                .ownerUsername("system")
                .build();

        when(repository.findByOwnerUsername(username)).thenReturn(Arrays.asList(userRc));
        when(repository.findByName(DEMO_RC_NAME)).thenReturn(Optional.of(demoRc));

        List<ResponsibilityCentreDTO> result = service.getAllForUser(username);

        assertEquals(2, result.size());
    }

    @Test
    void testCreateRCSuccess() {
        String username = "testuser";
        String name = "New RC";
        when(repository.existsByName(name)).thenReturn(false);
        when(repository.save(any(ResponsibilityCentre.class))).thenAnswer(i -> i.getArgument(0));

        ResponsibilityCentreDTO result = service.create(name, "Desc", username);

        assertNotNull(result);
        assertEquals(name, result.getName());
        verify(repository).save(any(ResponsibilityCentre.class));
    }

    @Test
    void testCreateRCDuplicateFails() {
        String name = "Existing RC";
        when(repository.existsByName(name)).thenReturn(true);

        assertThrows(RuntimeException.class, () -> service.create(name, "Desc", "user"));
    }

    @Test
    void testUpdateRCSuccess() {
        String username = "testuser";
        ResponsibilityCentre rc = ResponsibilityCentre.builder()
                .id(1L)
                .name("Old Name")
                .ownerUsername(username)
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(rc));
        when(repository.existsByName("New Name")).thenReturn(false);
        when(repository.save(any(ResponsibilityCentre.class))).thenAnswer(i -> i.getArgument(0));

        ResponsibilityCentreDTO result = service.update(1L, "New Name", "New Desc", username);

        assertEquals("New Name", result.getName());
    }

    @Test
    void testUpdateRCDuplicateFails() {
        String username = "testuser";
        ResponsibilityCentre rc = ResponsibilityCentre.builder()
                .id(1L)
                .name("Old Name")
                .ownerUsername(username)
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(rc));
        when(repository.existsByName("Taken Name")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> service.update(1L, "Taken Name", "Desc", username));
    }

    @Test
    void testUpdateRCSameNameSuccess() {
        String username = "testuser";
        ResponsibilityCentre rc = ResponsibilityCentre.builder()
                .id(1L)
                .name("Same Name")
                .ownerUsername(username)
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(rc));
        // existsByName might be true if it finds itself, but the service should allow same name for same RC
        when(repository.existsByName("Same Name")).thenReturn(true);
        when(repository.save(any(ResponsibilityCentre.class))).thenAnswer(i -> i.getArgument(0));

        ResponsibilityCentreDTO result = service.update(1L, "Same Name", "New Desc", username);

        assertEquals("Same Name", result.getName());
    }

    @Test
    void testDeleteRCSuccess() {
        String username = "testuser";
        ResponsibilityCentre rc = ResponsibilityCentre.builder()
                .id(1L)
                .name("To Delete")
                .ownerUsername(username)
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(rc));

        service.delete(1L, username);

        verify(repository).delete(rc);
    }
}
