/*
 * myRC - Procurement Item Service Implementation
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-28
 * Version: 1.0.0
 *
 * Description:
 * Implementation of ProcurementItemService for managing procurement entities.
 */
package com.myrc.service;

import com.myrc.dto.ProcurementItemDTO;
import com.myrc.dto.ProcurementQuoteDTO;
import com.myrc.dto.ProcurementQuoteFileDTO;
import com.myrc.model.Category;
import com.myrc.model.Currency;
import com.myrc.model.FiscalYear;
import com.myrc.model.ProcurementItem;
import com.myrc.model.ProcurementQuote;
import com.myrc.model.ProcurementQuoteFile;
import com.myrc.model.RCAccess;
import com.myrc.model.ResponsibilityCentre;
import com.myrc.model.User;
import com.myrc.repository.CategoryRepository;
import com.myrc.repository.FiscalYearRepository;
import com.myrc.repository.ProcurementItemRepository;
import com.myrc.repository.ProcurementQuoteFileRepository;
import com.myrc.repository.ProcurementQuoteRepository;
import com.myrc.repository.RCAccessRepository;
import com.myrc.repository.ResponsibilityCentreRepository;
import com.myrc.repository.UserRepository;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Implementation of ProcurementItemService.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-28
 */
@Service
@Transactional
public class ProcurementItemServiceImpl implements ProcurementItemService {

    private static final Logger logger = Logger.getLogger(ProcurementItemServiceImpl.class.getName());

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "application/pdf",
            "image/jpeg",
            "image/png",
            "image/gif",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/plain",
            "text/csv"
    );

    private final ProcurementItemRepository procurementItemRepository;
    private final ProcurementQuoteRepository quoteRepository;
    private final ProcurementQuoteFileRepository fileRepository;
    private final FiscalYearRepository fiscalYearRepository;
    private final ResponsibilityCentreRepository rcRepository;
    private final RCAccessRepository accessRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public ProcurementItemServiceImpl(ProcurementItemRepository procurementItemRepository,
                                       ProcurementQuoteRepository quoteRepository,
                                       ProcurementQuoteFileRepository fileRepository,
                                       FiscalYearRepository fiscalYearRepository,
                                       ResponsibilityCentreRepository rcRepository,
                                       RCAccessRepository accessRepository,
                                       UserRepository userRepository,
                                       CategoryRepository categoryRepository) {
        this.procurementItemRepository = procurementItemRepository;
        this.quoteRepository = quoteRepository;
        this.fileRepository = fileRepository;
        this.fiscalYearRepository = fiscalYearRepository;
        this.rcRepository = rcRepository;
        this.accessRepository = accessRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    // ==========================
    // Procurement Item Operations
    // ==========================

    @Override
    @Transactional(readOnly = true)
    public List<ProcurementItemDTO> getProcurementItemsByFiscalYearId(Long fiscalYearId, String username) {
        Optional<FiscalYear> fyOpt = fiscalYearRepository.findById(fiscalYearId);
        if (fyOpt.isEmpty()) {
            throw new IllegalArgumentException("Fiscal Year not found");
        }

        FiscalYear fy = fyOpt.get();
        Long rcId = fy.getResponsibilityCentre().getId();

        if (!hasAccessToRC(rcId, username)) {
            throw new IllegalArgumentException("User does not have access to this Responsibility Centre");
        }

        List<ProcurementItem> items = procurementItemRepository.findByFiscalYearIdAndActiveTrueOrderByPurchaseRequisitionAsc(fiscalYearId);
        return items.stream()
                .map(ProcurementItemDTO::fromEntityWithoutQuotes)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProcurementItemDTO> getProcurementItemsByFiscalYearIdAndStatus(Long fiscalYearId, String status, String username) {
        Optional<FiscalYear> fyOpt = fiscalYearRepository.findById(fiscalYearId);
        if (fyOpt.isEmpty()) {
            throw new IllegalArgumentException("Fiscal Year not found");
        }

        FiscalYear fy = fyOpt.get();
        Long rcId = fy.getResponsibilityCentre().getId();

        if (!hasAccessToRC(rcId, username)) {
            throw new IllegalArgumentException("User does not have access to this Responsibility Centre");
        }

        ProcurementItem.Status itemStatus;
        try {
            itemStatus = ProcurementItem.Status.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }

        List<ProcurementItem> items = procurementItemRepository.findByFiscalYearIdAndStatusAndActiveTrueOrderByPurchaseRequisitionAsc(fiscalYearId, itemStatus);
        return items.stream()
                .map(ProcurementItemDTO::fromEntityWithoutQuotes)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProcurementItemDTO> getProcurementItemById(Long procurementItemId, String username) {
        Optional<ProcurementItem> itemOpt = procurementItemRepository.findById(procurementItemId);
        if (itemOpt.isEmpty() || !itemOpt.get().getActive()) {
            return Optional.empty();
        }

        ProcurementItem item = itemOpt.get();
        Long rcId = item.getFiscalYear().getResponsibilityCentre().getId();

        if (!hasAccessToRC(rcId, username)) {
            return Optional.empty();
        }

        return Optional.of(ProcurementItemDTO.fromEntityWithoutQuotes(item));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProcurementItemDTO> getProcurementItemWithQuotes(Long procurementItemId, String username) {
        Optional<ProcurementItem> itemOpt = procurementItemRepository.findByIdWithQuotes(procurementItemId);
        if (itemOpt.isEmpty()) {
            return Optional.empty();
        }

        ProcurementItem item = itemOpt.get();
        Long rcId = item.getFiscalYear().getResponsibilityCentre().getId();

        if (!hasAccessToRC(rcId, username)) {
            return Optional.empty();
        }

        return Optional.of(ProcurementItemDTO.fromEntity(item));
    }

    @Override
    public ProcurementItemDTO createProcurementItem(ProcurementItemDTO dto, String username) {
        // Validate required fields
        if (dto.getFiscalYearId() == null) {
            throw new IllegalArgumentException("Fiscal Year ID is required");
        }
        if (dto.getPurchaseRequisition() == null || dto.getPurchaseRequisition().trim().isEmpty()) {
            throw new IllegalArgumentException("Purchase Requisition (PR) is required");
        }
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }

        // Get fiscal year and verify write access
        Optional<FiscalYear> fyOpt = fiscalYearRepository.findById(dto.getFiscalYearId());
        if (fyOpt.isEmpty()) {
            throw new IllegalArgumentException("Fiscal Year not found");
        }

        FiscalYear fy = fyOpt.get();
        Long rcId = fy.getResponsibilityCentre().getId();

        if (!hasWriteAccessToRC(rcId, username)) {
            throw new IllegalArgumentException("User does not have write access to this Responsibility Centre");
        }

        // Check for duplicate PR
        if (procurementItemRepository.existsByPurchaseRequisitionAndFiscalYearAndActiveTrue(dto.getPurchaseRequisition().trim(), fy)) {
            throw new IllegalArgumentException("A procurement item with this PR already exists for this fiscal year");
        }

        // Parse status
        ProcurementItem.Status itemStatus = ProcurementItem.Status.NOT_STARTED;
        if (dto.getStatus() != null && !dto.getStatus().trim().isEmpty()) {
            try {
                itemStatus = ProcurementItem.Status.valueOf(dto.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid status: " + dto.getStatus());
            }
        }

        // Create procurement item
        ProcurementItem item = new ProcurementItem();
        item.setPurchaseRequisition(dto.getPurchaseRequisition().trim());
        item.setPurchaseOrder(dto.getPurchaseOrder() != null ? dto.getPurchaseOrder().trim() : null);
        item.setName(dto.getName().trim());
        item.setDescription(dto.getDescription());
        item.setStatus(itemStatus);
        item.setFiscalYear(fy);

        // Set currency (defaults to CAD if not provided)
        if (dto.getCurrency() != null && !dto.getCurrency().trim().isEmpty()) {
            try {
                item.setCurrency(com.myrc.model.Currency.valueOf(dto.getCurrency().toUpperCase()));
            } catch (IllegalArgumentException e) {
                item.setCurrency(com.myrc.model.Currency.CAD);
            }
        } else {
            item.setCurrency(com.myrc.model.Currency.CAD);
        }
        item.setExchangeRate(dto.getExchangeRate());
        
        // Set new procurement fields
        item.setPreferredVendor(dto.getPreferredVendor());
        item.setContractNumber(dto.getContractNumber());
        item.setContractStartDate(dto.getContractStartDate());
        item.setContractEndDate(dto.getContractEndDate());
        item.setProcurementCompleted(dto.getProcurementCompleted() != null ? dto.getProcurementCompleted() : false);
        item.setProcurementCompletedDate(dto.getProcurementCompletedDate());
        
        // Set category if provided
        if (dto.getCategoryId() != null) {
            categoryRepository.findById(dto.getCategoryId())
                .ifPresent(item::setCategory);
        }
        
        item.setActive(true);

        ProcurementItem saved = procurementItemRepository.save(item);
        logger.info("Created procurement item '" + dto.getName() + "' (PR: " + dto.getPurchaseRequisition() + ") for FY: " + fy.getName() + " by user " + username);

        return ProcurementItemDTO.fromEntityWithoutQuotes(saved);
    }

    @Override
    public ProcurementItemDTO updateProcurementItem(Long procurementItemId, ProcurementItemDTO dto, String username) {
        Optional<ProcurementItem> itemOpt = procurementItemRepository.findById(procurementItemId);
        if (itemOpt.isEmpty() || !itemOpt.get().getActive()) {
            throw new IllegalArgumentException("Procurement item not found");
        }

        ProcurementItem item = itemOpt.get();
        Long rcId = item.getFiscalYear().getResponsibilityCentre().getId();

        if (!hasWriteAccessToRC(rcId, username)) {
            throw new IllegalArgumentException("User does not have write access to this Responsibility Centre");
        }

        // Update fields
        if (dto.getPurchaseRequisition() != null && !dto.getPurchaseRequisition().trim().isEmpty()) {
            // Check for duplicate PR (excluding self)
            Optional<ProcurementItem> existingOpt = procurementItemRepository.findByPurchaseRequisitionAndFiscalYearAndActiveTrue(
                    dto.getPurchaseRequisition().trim(), item.getFiscalYear());
            if (existingOpt.isPresent() && !existingOpt.get().getId().equals(procurementItemId)) {
                throw new IllegalArgumentException("A procurement item with this PR already exists for this fiscal year");
            }
            item.setPurchaseRequisition(dto.getPurchaseRequisition().trim());
        }

        if (dto.getPurchaseOrder() != null) {
            item.setPurchaseOrder(dto.getPurchaseOrder().trim().isEmpty() ? null : dto.getPurchaseOrder().trim());
        }

        if (dto.getName() != null && !dto.getName().trim().isEmpty()) {
            item.setName(dto.getName().trim());
        }

        if (dto.getDescription() != null) {
            item.setDescription(dto.getDescription());
        }

        if (dto.getStatus() != null && !dto.getStatus().trim().isEmpty()) {
            try {
                item.setStatus(ProcurementItem.Status.valueOf(dto.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid status: " + dto.getStatus());
            }
        }

        // Update currency if provided
        if (dto.getCurrency() != null && !dto.getCurrency().trim().isEmpty()) {
            try {
                item.setCurrency(com.myrc.model.Currency.valueOf(dto.getCurrency().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Keep existing currency if invalid value provided
            }
        }
        if (dto.getExchangeRate() != null) {
            item.setExchangeRate(dto.getExchangeRate());
        }
        
        // Update new procurement fields
        if (dto.getPreferredVendor() != null) {
            item.setPreferredVendor(dto.getPreferredVendor().trim().isEmpty() ? null : dto.getPreferredVendor().trim());
        }
        if (dto.getContractNumber() != null) {
            item.setContractNumber(dto.getContractNumber().trim().isEmpty() ? null : dto.getContractNumber().trim());
        }
        if (dto.getContractStartDate() != null) {
            item.setContractStartDate(dto.getContractStartDate());
        }
        if (dto.getContractEndDate() != null) {
            item.setContractEndDate(dto.getContractEndDate());
        }
        if (dto.getProcurementCompleted() != null) {
            item.setProcurementCompleted(dto.getProcurementCompleted());
        }
        if (dto.getProcurementCompletedDate() != null) {
            item.setProcurementCompletedDate(dto.getProcurementCompletedDate());
        }
        
        // Update category (null removes the category)
        if (dto.getCategoryId() != null) {
            categoryRepository.findById(dto.getCategoryId())
                .ifPresentOrElse(
                    item::setCategory,
                    () -> item.setCategory(null)
                );
        } else if (dto.getCategoryId() == null && dto.getCategoryName() == null) {
            // Only clear category if both are null (explicit removal)
            // If neither field is in the request, don't change the category
        }

        ProcurementItem saved = procurementItemRepository.save(item);
        logger.info("Updated procurement item " + procurementItemId + " by user " + username);

        return ProcurementItemDTO.fromEntityWithoutQuotes(saved);
    }

    @Override
    public void deleteProcurementItem(Long procurementItemId, String username) {
        Optional<ProcurementItem> itemOpt = procurementItemRepository.findById(procurementItemId);
        if (itemOpt.isEmpty() || !itemOpt.get().getActive()) {
            throw new IllegalArgumentException("Procurement item not found");
        }

        ProcurementItem item = itemOpt.get();
        Long rcId = item.getFiscalYear().getResponsibilityCentre().getId();

        if (!hasWriteAccessToRC(rcId, username)) {
            throw new IllegalArgumentException("User does not have write access to this Responsibility Centre");
        }

        // Soft delete
        item.setActive(false);
        procurementItemRepository.save(item);
        logger.info("Deleted procurement item " + procurementItemId + " by user " + username);
    }

    @Override
    public ProcurementItemDTO updateProcurementItemStatus(Long procurementItemId, String status, String username) {
        Optional<ProcurementItem> itemOpt = procurementItemRepository.findById(procurementItemId);
        if (itemOpt.isEmpty() || !itemOpt.get().getActive()) {
            throw new IllegalArgumentException("Procurement item not found");
        }

        ProcurementItem item = itemOpt.get();
        Long rcId = item.getFiscalYear().getResponsibilityCentre().getId();

        if (!hasWriteAccessToRC(rcId, username)) {
            throw new IllegalArgumentException("User does not have write access to this Responsibility Centre");
        }

        ProcurementItem.Status newStatus;
        try {
            newStatus = ProcurementItem.Status.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }

        item.setStatus(newStatus);
        ProcurementItem saved = procurementItemRepository.save(item);
        logger.info("Updated status of procurement item " + procurementItemId + " to " + status + " by user " + username);

        return ProcurementItemDTO.fromEntityWithoutQuotes(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProcurementItemDTO> searchProcurementItems(Long fiscalYearId, String searchTerm, String username) {
        Optional<FiscalYear> fyOpt = fiscalYearRepository.findById(fiscalYearId);
        if (fyOpt.isEmpty()) {
            throw new IllegalArgumentException("Fiscal Year not found");
        }

        FiscalYear fy = fyOpt.get();
        Long rcId = fy.getResponsibilityCentre().getId();

        if (!hasAccessToRC(rcId, username)) {
            throw new IllegalArgumentException("User does not have access to this Responsibility Centre");
        }

        List<ProcurementItem> items = procurementItemRepository.searchByNameOrPrOrPo(fiscalYearId, searchTerm);
        return items.stream()
                .map(ProcurementItemDTO::fromEntityWithoutQuotes)
                .collect(Collectors.toList());
    }

    // ==========================
    // Quote Operations
    // ==========================

    @Override
    @Transactional(readOnly = true)
    public List<ProcurementQuoteDTO> getQuotesByProcurementItemId(Long procurementItemId, String username) {
        Optional<ProcurementItem> itemOpt = procurementItemRepository.findById(procurementItemId);
        if (itemOpt.isEmpty() || !itemOpt.get().getActive()) {
            throw new IllegalArgumentException("Procurement item not found");
        }

        ProcurementItem item = itemOpt.get();
        Long rcId = item.getFiscalYear().getResponsibilityCentre().getId();

        if (!hasAccessToRC(rcId, username)) {
            throw new IllegalArgumentException("User does not have access to this Responsibility Centre");
        }

        List<ProcurementQuote> quotes = quoteRepository.findByProcurementItemIdAndActiveTrueOrderByVendorNameAsc(procurementItemId);
        return quotes.stream()
                .map(ProcurementQuoteDTO::fromEntityWithoutFiles)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProcurementQuoteDTO> getQuoteById(Long quoteId, String username) {
        Optional<ProcurementQuote> quoteOpt = quoteRepository.findById(quoteId);
        if (quoteOpt.isEmpty() || !quoteOpt.get().getActive()) {
            return Optional.empty();
        }

        ProcurementQuote quote = quoteOpt.get();
        Long rcId = quote.getProcurementItem().getFiscalYear().getResponsibilityCentre().getId();

        if (!hasAccessToRC(rcId, username)) {
            return Optional.empty();
        }

        return Optional.of(ProcurementQuoteDTO.fromEntityWithoutFiles(quote));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProcurementQuoteDTO> getQuoteWithFiles(Long quoteId, String username) {
        Optional<ProcurementQuote> quoteOpt = quoteRepository.findByIdWithFiles(quoteId);
        if (quoteOpt.isEmpty()) {
            return Optional.empty();
        }

        ProcurementQuote quote = quoteOpt.get();
        Long rcId = quote.getProcurementItem().getFiscalYear().getResponsibilityCentre().getId();

        if (!hasAccessToRC(rcId, username)) {
            return Optional.empty();
        }

        return Optional.of(ProcurementQuoteDTO.fromEntity(quote));
    }

    @Override
    public ProcurementQuoteDTO createQuote(Long procurementItemId, ProcurementQuoteDTO dto, String username) {
        Optional<ProcurementItem> itemOpt = procurementItemRepository.findById(procurementItemId);
        if (itemOpt.isEmpty() || !itemOpt.get().getActive()) {
            throw new IllegalArgumentException("Procurement item not found");
        }

        ProcurementItem item = itemOpt.get();
        Long rcId = item.getFiscalYear().getResponsibilityCentre().getId();

        if (!hasWriteAccessToRC(rcId, username)) {
            throw new IllegalArgumentException("User does not have write access to this Responsibility Centre");
        }

        if (dto.getVendorName() == null || dto.getVendorName().trim().isEmpty()) {
            throw new IllegalArgumentException("Vendor name is required");
        }

        // Parse currency
        Currency quoteCurrency = Currency.CAD;
        if (dto.getCurrency() != null && !dto.getCurrency().trim().isEmpty()) {
            quoteCurrency = Currency.fromCode(dto.getCurrency());
            if (quoteCurrency == null) {
                throw new IllegalArgumentException("Invalid currency: " + dto.getCurrency());
            }
        }

        // Create quote
        ProcurementQuote quote = new ProcurementQuote();
        quote.setVendorName(dto.getVendorName().trim());
        quote.setVendorContact(dto.getVendorContact());
        quote.setQuoteReference(dto.getQuoteReference());
        quote.setAmount(dto.getAmount());
        quote.setCurrency(quoteCurrency);
        quote.setReceivedDate(dto.getReceivedDate());
        quote.setExpiryDate(dto.getExpiryDate());
        quote.setNotes(dto.getNotes());
        quote.setStatus(ProcurementQuote.Status.PENDING);
        quote.setSelected(false);
        quote.setProcurementItem(item);
        quote.setActive(true);

        ProcurementQuote saved = quoteRepository.save(quote);
        logger.info("Created quote from '" + dto.getVendorName() + "' for procurement item " + procurementItemId + " by user " + username);

        return ProcurementQuoteDTO.fromEntityWithoutFiles(saved);
    }

    @Override
    public ProcurementQuoteDTO updateQuote(Long quoteId, ProcurementQuoteDTO dto, String username) {
        Optional<ProcurementQuote> quoteOpt = quoteRepository.findById(quoteId);
        if (quoteOpt.isEmpty() || !quoteOpt.get().getActive()) {
            throw new IllegalArgumentException("Quote not found");
        }

        ProcurementQuote quote = quoteOpt.get();
        Long rcId = quote.getProcurementItem().getFiscalYear().getResponsibilityCentre().getId();

        if (!hasWriteAccessToRC(rcId, username)) {
            throw new IllegalArgumentException("User does not have write access to this Responsibility Centre");
        }

        // Update fields
        if (dto.getVendorName() != null && !dto.getVendorName().trim().isEmpty()) {
            quote.setVendorName(dto.getVendorName().trim());
        }
        if (dto.getVendorContact() != null) {
            quote.setVendorContact(dto.getVendorContact());
        }
        if (dto.getQuoteReference() != null) {
            quote.setQuoteReference(dto.getQuoteReference());
        }
        if (dto.getAmount() != null) {
            quote.setAmount(dto.getAmount());
        }
        if (dto.getCurrency() != null && !dto.getCurrency().trim().isEmpty()) {
            Currency quoteCurrency = Currency.fromCode(dto.getCurrency());
            if (quoteCurrency == null) {
                throw new IllegalArgumentException("Invalid currency: " + dto.getCurrency());
            }
            quote.setCurrency(quoteCurrency);
        }
        if (dto.getReceivedDate() != null) {
            quote.setReceivedDate(dto.getReceivedDate());
        }
        if (dto.getExpiryDate() != null) {
            quote.setExpiryDate(dto.getExpiryDate());
        }
        if (dto.getNotes() != null) {
            quote.setNotes(dto.getNotes());
        }
        if (dto.getStatus() != null && !dto.getStatus().trim().isEmpty()) {
            try {
                quote.setStatus(ProcurementQuote.Status.valueOf(dto.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid status: " + dto.getStatus());
            }
        }

        ProcurementQuote saved = quoteRepository.save(quote);
        logger.info("Updated quote " + quoteId + " by user " + username);

        return ProcurementQuoteDTO.fromEntityWithoutFiles(saved);
    }

    @Override
    public void deleteQuote(Long quoteId, String username) {
        Optional<ProcurementQuote> quoteOpt = quoteRepository.findById(quoteId);
        if (quoteOpt.isEmpty() || !quoteOpt.get().getActive()) {
            throw new IllegalArgumentException("Quote not found");
        }

        ProcurementQuote quote = quoteOpt.get();
        Long rcId = quote.getProcurementItem().getFiscalYear().getResponsibilityCentre().getId();

        if (!hasWriteAccessToRC(rcId, username)) {
            throw new IllegalArgumentException("User does not have write access to this Responsibility Centre");
        }

        // Soft delete
        quote.setActive(false);
        quoteRepository.save(quote);
        logger.info("Deleted quote " + quoteId + " by user " + username);
    }

    @Override
    public ProcurementQuoteDTO selectQuote(Long quoteId, String username) {
        Optional<ProcurementQuote> quoteOpt = quoteRepository.findById(quoteId);
        if (quoteOpt.isEmpty() || !quoteOpt.get().getActive()) {
            throw new IllegalArgumentException("Quote not found");
        }

        ProcurementQuote quote = quoteOpt.get();
        Long rcId = quote.getProcurementItem().getFiscalYear().getResponsibilityCentre().getId();

        if (!hasWriteAccessToRC(rcId, username)) {
            throw new IllegalArgumentException("User does not have write access to this Responsibility Centre");
        }

        // Deselect any previously selected quote
        Optional<ProcurementQuote> previousSelected = quoteRepository.findByProcurementItemIdAndSelectedTrueAndActiveTrue(
                quote.getProcurementItem().getId());
        if (previousSelected.isPresent()) {
            ProcurementQuote prev = previousSelected.get();
            prev.setSelected(false);
            prev.setStatus(ProcurementQuote.Status.REJECTED);
            quoteRepository.save(prev);
        }

        // Select this quote
        quote.setSelected(true);
        quote.setStatus(ProcurementQuote.Status.SELECTED);
        ProcurementQuote saved = quoteRepository.save(quote);
        logger.info("Selected quote " + quoteId + " for procurement item " + quote.getProcurementItem().getId() + " by user " + username);

        return ProcurementQuoteDTO.fromEntityWithoutFiles(saved);
    }

    // ==========================
    // File Operations
    // ==========================

    @Override
    @Transactional(readOnly = true)
    public List<ProcurementQuoteFileDTO> getFilesByQuoteId(Long quoteId, String username) {
        Optional<ProcurementQuote> quoteOpt = quoteRepository.findById(quoteId);
        if (quoteOpt.isEmpty() || !quoteOpt.get().getActive()) {
            throw new IllegalArgumentException("Quote not found");
        }

        ProcurementQuote quote = quoteOpt.get();
        Long rcId = quote.getProcurementItem().getFiscalYear().getResponsibilityCentre().getId();

        if (!hasAccessToRC(rcId, username)) {
            throw new IllegalArgumentException("User does not have access to this Responsibility Centre");
        }

        List<ProcurementQuoteFile> files = fileRepository.findByQuoteIdAndActiveTrueOrderByFileNameAsc(quoteId);
        return files.stream()
                .map(ProcurementQuoteFileDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProcurementQuoteFileDTO> getFileMetadataById(Long fileId, String username) {
        Optional<ProcurementQuoteFile> fileOpt = fileRepository.findById(fileId);
        if (fileOpt.isEmpty() || !fileOpt.get().getActive()) {
            return Optional.empty();
        }

        ProcurementQuoteFile file = fileOpt.get();
        Long rcId = file.getQuote().getProcurementItem().getFiscalYear().getResponsibilityCentre().getId();

        if (!hasAccessToRC(rcId, username)) {
            return Optional.empty();
        }

        return Optional.of(ProcurementQuoteFileDTO.fromEntity(file));
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getFileContent(Long fileId, String username) {
        Optional<ProcurementQuoteFile> fileOpt = fileRepository.findById(fileId);
        if (fileOpt.isEmpty() || !fileOpt.get().getActive()) {
            throw new IllegalArgumentException("File not found");
        }

        ProcurementQuoteFile file = fileOpt.get();
        Long rcId = file.getQuote().getProcurementItem().getFiscalYear().getResponsibilityCentre().getId();

        if (!hasAccessToRC(rcId, username)) {
            throw new IllegalArgumentException("User does not have access to this file");
        }

        return file.getContent();
    }

    @Override
    public ProcurementQuoteFileDTO uploadFile(Long quoteId, MultipartFile file, String description, String username) {
        Optional<ProcurementQuote> quoteOpt = quoteRepository.findById(quoteId);
        if (quoteOpt.isEmpty() || !quoteOpt.get().getActive()) {
            throw new IllegalArgumentException("Quote not found");
        }

        ProcurementQuote quote = quoteOpt.get();
        Long rcId = quote.getProcurementItem().getFiscalYear().getResponsibilityCentre().getId();

        if (!hasWriteAccessToRC(rcId, username)) {
            throw new IllegalArgumentException("User does not have write access to this Responsibility Centre");
        }

        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of 10MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("File type not allowed. Allowed types: PDF, images, Word, Excel, text, CSV");
        }

        try {
            ProcurementQuoteFile quoteFile = new ProcurementQuoteFile();
            quoteFile.setFileName(file.getOriginalFilename());
            quoteFile.setContentType(contentType);
            quoteFile.setFileSize(file.getSize());
            quoteFile.setContent(file.getBytes());
            quoteFile.setDescription(description);
            quoteFile.setQuote(quote);
            quoteFile.setActive(true);

            ProcurementQuoteFile saved = fileRepository.save(quoteFile);
            logger.info("Uploaded file '" + file.getOriginalFilename() + "' to quote " + quoteId + " by user " + username);

            return ProcurementQuoteFileDTO.fromEntity(saved);
        } catch (IOException e) {
            logger.severe("Failed to upload file: " + e.getMessage());
            throw new IllegalArgumentException("Failed to read file content");
        }
    }

    @Override
    public void deleteFile(Long fileId, String username) {
        Optional<ProcurementQuoteFile> fileOpt = fileRepository.findById(fileId);
        if (fileOpt.isEmpty() || !fileOpt.get().getActive()) {
            throw new IllegalArgumentException("File not found");
        }

        ProcurementQuoteFile file = fileOpt.get();
        Long rcId = file.getQuote().getProcurementItem().getFiscalYear().getResponsibilityCentre().getId();

        if (!hasWriteAccessToRC(rcId, username)) {
            throw new IllegalArgumentException("User does not have write access to this Responsibility Centre");
        }

        // Soft delete
        file.setActive(false);
        fileRepository.save(file);
        logger.info("Deleted file " + fileId + " by user " + username);
    }

    // ==========================
    // Access Control Helpers
    // ==========================

    /**
     * Check if a user has any access (read or write) to a Responsibility Centre.
     *
     * @param rcId the RC ID
     * @param username the username
     * @return true if user has access
     */
    private boolean hasAccessToRC(Long rcId, String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return false;
        }
        User user = userOpt.get();

        Optional<ResponsibilityCentre> rcOpt = rcRepository.findById(rcId);
        if (rcOpt.isEmpty()) {
            return false;
        }
        ResponsibilityCentre rc = rcOpt.get();

        // Demo RC is accessible to all users in read-only mode
        if ("Demo".equals(rc.getName())) {
            return true;
        }

        // Check if owner
        if (rc.getOwner() != null && rc.getOwner().getId().equals(user.getId())) {
            return true;
        }

        // Check for explicit access
        Optional<RCAccess> accessOpt = accessRepository.findByResponsibilityCentreAndUser(rc, user);
        return accessOpt.isPresent();
    }

    /**
     * Check if a user has write access to a Responsibility Centre.
     *
     * @param rcId the RC ID
     * @param username the username
     * @return true if user has write access
     */
    private boolean hasWriteAccessToRC(Long rcId, String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return false;
        }
        User user = userOpt.get();

        Optional<ResponsibilityCentre> rcOpt = rcRepository.findById(rcId);
        if (rcOpt.isEmpty()) {
            return false;
        }
        ResponsibilityCentre rc = rcOpt.get();

        // Check if owner
        if (rc.getOwner() != null && rc.getOwner().getId().equals(user.getId())) {
            return true;
        }

        // Check for explicit write access
        Optional<RCAccess> accessOpt = accessRepository.findByResponsibilityCentreAndUser(rc, user);
        if (accessOpt.isEmpty()) {
            return false;
        }
        return accessOpt.get().getAccessLevel() == RCAccess.AccessLevel.READ_WRITE;
    }
}
