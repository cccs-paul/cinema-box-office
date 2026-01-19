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

import static org.junit.jupiter.api.Assertions.*;
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
        when(repository.findAll()).thenReturn(Arrays.asList(userRc, demoRc));

        List<ResponsibilityCentreDTO> result = service.getAllForUser(username);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(dto -> dto.getName().equals("User RC") && dto.getAccessLevel().equals("READ_WRITE")));
        assertTrue(result.stream().anyMatch(dto -> dto.getName().equals(DEMO_RC_NAME) && dto.getAccessLevel().equals("READ_ONLY")));
    }
}
