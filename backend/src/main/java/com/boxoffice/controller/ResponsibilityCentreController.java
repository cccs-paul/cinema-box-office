package com.boxoffice.controller;

import com.boxoffice.dto.ResponsibilityCentreDTO;
import com.boxoffice.service.ResponsibilityCentreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/responsibility-centres")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ResponsibilityCentreController {

    private final ResponsibilityCentreService service;

    @GetMapping
    public ResponseEntity<List<ResponsibilityCentreDTO>> getAll(Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "anonymous";
        return ResponseEntity.ok(service.getAllForUser(username));
    }

    @PostMapping
    public ResponseEntity<ResponsibilityCentreDTO> create(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "anonymous";
        String name = request.get("name");
        String description = request.get("description");
        return ResponseEntity.ok(service.create(name, description, username));
    }
}
