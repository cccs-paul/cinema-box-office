package com.boxoffice.service;

import com.boxoffice.dto.ResponsibilityCentreDTO;
import com.boxoffice.model.ResponsibilityCentre;
import com.boxoffice.repository.ResponsibilityCentreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResponsibilityCentreService {

    private final ResponsibilityCentreRepository repository;
    private static final String DEMO_RC_NAME = "Demo Responsibility Centre";
    private static final String DEMO_OWNER = "system";

    @Transactional(readOnly = true)
    public List<ResponsibilityCentreDTO> getAllForUser(String username) {
        List<ResponsibilityCentre> userRcs = repository.findByOwnerUsername(username);

        // Find demo RC
        ResponsibilityCentre demoRc = repository.findByName(DEMO_RC_NAME).orElse(null);

        List<ResponsibilityCentreDTO> dtos = userRcs.stream()
                .map(rc -> convertToDto(rc, username))
                .collect(Collectors.toList());

        // Add demo RC if not already present in user's list
        if (demoRc != null && userRcs.stream().noneMatch(rc -> rc.getId().equals(demoRc.getId()))) {
            dtos.add(convertToDto(demoRc, username));
        }

        return dtos;
    }

    @Transactional
    public ResponsibilityCentreDTO create(String name, String description, String username) {
        ResponsibilityCentre rc = ResponsibilityCentre.builder()
                .name(name)
                .description(description)
                .ownerUsername(username)
                .build();

        ResponsibilityCentre saved = repository.save(rc);
        return convertToDto(saved, username);
    }

    private ResponsibilityCentreDTO convertToDto(ResponsibilityCentre rc, String currentUsername) {
        boolean isOwner = rc.getOwnerUsername().equals(currentUsername);
        String accessLevel = isOwner ? "READ_WRITE" : "READ_ONLY";

        // Special case for Demo RC
        if (DEMO_RC_NAME.equals(rc.getName())) {
            accessLevel = "READ_ONLY";
        }

        return ResponsibilityCentreDTO.builder()
                .id(rc.getId())
                .name(rc.getName())
                .description(rc.getDescription())
                .ownerUsername(rc.getOwnerUsername())
                .accessLevel(accessLevel)
                .isOwner(isOwner)
                .createdAt(rc.getCreatedAt())
                .updatedAt(rc.getUpdatedAt())
                .build();
    }

    @Transactional
    public void initDemoRC() {
        if (!repository.existsByName(DEMO_RC_NAME)) {
            ResponsibilityCentre demo = ResponsibilityCentre.builder()
                    .name(DEMO_RC_NAME)
                    .description("A demo responsibility centre for everyone to explore.")
                    .ownerUsername(DEMO_OWNER)
                    .build();
            repository.save(demo);
        }
    }
}
