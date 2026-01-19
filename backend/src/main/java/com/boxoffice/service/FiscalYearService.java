package com.boxoffice.service;

import com.boxoffice.dto.FiscalYearDTO;
import com.boxoffice.model.FiscalYear;
import com.boxoffice.model.ResponsibilityCentre;
import com.boxoffice.repository.FiscalYearRepository;
import com.boxoffice.repository.ResponsibilityCentreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FiscalYearService {

    private final FiscalYearRepository repository;
    private final ResponsibilityCentreRepository rcRepository;

    @Transactional(readOnly = true)
    public List<FiscalYearDTO> getAllForRc(Long rcId) {
        return repository.findByRcId(rcId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public FiscalYearDTO create(String name, Long rcId, String username) {
        ResponsibilityCentre rc = rcRepository.findById(rcId)
                .orElseThrow(() -> new RuntimeException("Responsibility Centre not found"));

        // Check if user is the owner
        if (!rc.getOwnerUsername().equals(username)) {
            throw new RuntimeException("You do not have permission to create Fiscal Years for this Responsibility Centre");
        }

        // Special check for Demo RC
        if ("Demo Responsibility Centre".equals(rc.getName())) {
            throw new RuntimeException("Cannot create Fiscal Years for the Demo Responsibility Centre");
        }

        if (repository.existsByNameAndRcId(name, rcId)) {
            throw new RuntimeException("Fiscal Year with name " + name + " already exists for this RC");
        }

        FiscalYear fy = FiscalYear.builder()
                .name(name)
                .rc(rc)
                .build();

        FiscalYear saved = repository.save(fy);
        return convertToDto(saved);
    }

    @Transactional
    public FiscalYearDTO update(Long id, String name, String username) {
        FiscalYear fy = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fiscal Year not found"));

        ResponsibilityCentre rc = fy.getRc();

        if (!rc.getOwnerUsername().equals(username)) {
            throw new RuntimeException("You do not have permission to update Fiscal Years for this Responsibility Centre");
        }

        if ("Demo Responsibility Centre".equals(rc.getName())) {
            throw new RuntimeException("Cannot update Fiscal Years for the Demo Responsibility Centre");
        }

        if (repository.existsByNameAndRcId(name, rc.getId()) && !fy.getName().equals(name)) {
            throw new RuntimeException("Fiscal Year with name " + name + " already exists for this RC");
        }

        fy.setName(name);
        FiscalYear saved = repository.save(fy);
        return convertToDto(saved);
    }

    @Transactional
    public void delete(Long id, String username) {
        FiscalYear fy = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fiscal Year not found"));

        ResponsibilityCentre rc = fy.getRc();

        if (!rc.getOwnerUsername().equals(username)) {
            throw new RuntimeException("You do not have permission to delete Fiscal Years for this Responsibility Centre");
        }

        if ("Demo Responsibility Centre".equals(rc.getName())) {
            throw new RuntimeException("Cannot delete Fiscal Years for the Demo Responsibility Centre");
        }

        repository.delete(fy);
    }

    private FiscalYearDTO convertToDto(FiscalYear fy) {
        return FiscalYearDTO.builder()
                .id(fy.getId())
                .name(fy.getName())
                .rcId(fy.getRc().getId())
                .createdAt(fy.getCreatedAt())
                .updatedAt(fy.getUpdatedAt())
                .build();
    }
}
