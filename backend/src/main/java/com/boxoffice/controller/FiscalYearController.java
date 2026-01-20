package com.boxoffice.controller;

import com.boxoffice.dto.FiscalYearDTO;
import com.boxoffice.dto.FiscalYearRequest;
import com.boxoffice.service.FiscalYearService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fiscal-years")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FiscalYearController {

    private final FiscalYearService service;

    @GetMapping("/rc/{rcId}")
    public ResponseEntity<List<FiscalYearDTO>> getAllForRc(@PathVariable Long rcId) {
        return ResponseEntity.ok(service.getAllForRc(rcId));
    }

    @PostMapping
    public ResponseEntity<FiscalYearDTO> create(@RequestBody FiscalYearRequest request, Principal principal) {
        String username = principal != null ? principal.getName() : "anonymous"; // Fallback for dev/testing
        return ResponseEntity.ok(service.create(request.getName(), request.getRcId(), username));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FiscalYearDTO> update(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            Principal principal) {
        String username = principal != null ? principal.getName() : "anonymous";
        String name = request.get("name");
        return ResponseEntity.ok(service.update(id, name, username));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            Principal principal) {
        String username = principal != null ? principal.getName() : "anonymous";
        service.delete(id, username);
        return ResponseEntity.noContent().build();
    }
}
