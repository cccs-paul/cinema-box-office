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
import com.myrc.model.ProcurementEvent;
import com.myrc.model.ProcurementItem;
import com.myrc.model.ProcurementQuote;
import com.myrc.model.ProcurementQuoteFile;
import com.myrc.model.RCAccess;
import com.myrc.model.ResponsibilityCentre;
import com.myrc.model.SpendingCategory;
import com.myrc.model.SpendingItem;
import com.myrc.model.User;
import com.myrc.repository.CategoryRepository;
import com.myrc.repository.FiscalYearRepository;
import com.myrc.repository.ProcurementEventRepository;
import com.myrc.repository.ProcurementItemRepository;
import com.myrc.repository.ProcurementQuoteFileRepository;
import com.myrc.repository.ProcurementQuoteRepository;
import com.myrc.repository.RCAccessRepository;
import com.myrc.repository.ResponsibilityCentreRepository;
import com.myrc.repository.SpendingCategoryRepository;
import com.myrc.repository.SpendingItemRepository;
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

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
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
    private final ProcurementEventRepository procurementEventRepository;
    private final FiscalYearRepository fiscalYearRepository;
    private final ResponsibilityCentreRepository rcRepository;
    private final RCAccessRepository accessRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final SpendingItemRepository spendingItemRepository;
    private final SpendingCategoryRepository spendingCategoryRepository;

    public ProcurementItemServiceImpl(ProcurementItemRepository procurementItemRepository,
                                       ProcurementQuoteRepository quoteRepository,
                                       ProcurementQuoteFileRepository fileRepository,
                                       ProcurementEventRepository procurementEventRepository,
                                       FiscalYearRepository fiscalYearRepository,
                                       ResponsibilityCentreRepository rcRepository,
                                       RCAccessRepository accessRepository,
                                       UserRepository userRepository,
                                       CategoryRepository categoryRepository,
                                       SpendingItemRepository spendingItemRepository,
                                       SpendingCategoryRepository spendingCategoryRepository) {
        this.procurementItemRepository = procurementItemRepository;
        this.quoteRepository = quoteRepository;
        this.fileRepository = fileRepository;
        this.procurementEventRepository = procurementEventRepository;
        this.fiscalYearRepository = fiscalYearRepository;
        this.rcRepository = rcRepository;
        this.accessRepository = accessRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.spendingItemRepository = spendingItemRepository;
        this.spendingCategoryRepository = spendingCategoryRepository;
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

        // Validate status value
        ProcurementItem.Status itemStatus;
        try {
            itemStatus = ProcurementItem.Status.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }

        // Since status is now tracked via events, we need to filter in memory
        // Get all items and filter by their current status (from events)
        List<ProcurementItem> items = procurementItemRepository.findByFiscalYearIdAndActiveTrueOrderByPurchaseRequisitionAsc(fiscalYearId);
        return items.stream()
                .filter(item -> {
                    String currentStatus = procurementEventRepository.findCurrentStatusByProcurementItemId(item.getId())
                            .orElse("DRAFT");
                    return currentStatus.equalsIgnoreCase(status);
                })
                .map(item -> {
                    ProcurementItemDTO dto = ProcurementItemDTO.fromEntityWithoutQuotes(item);
                    dto.setCurrentStatus(procurementEventRepository.findCurrentStatusByProcurementItemId(item.getId())
                            .orElse("DRAFT"));
                    return dto;
                })
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
        // Note: Purchase Requisition (PR) is optional
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

        // Check for duplicate PR (only if PR is provided)
        String prValue = dto.getPurchaseRequisition() != null ? dto.getPurchaseRequisition().trim() : null;
        if (prValue != null && !prValue.isEmpty() && 
            procurementItemRepository.existsByPurchaseRequisitionAndFiscalYearAndActiveTrue(prValue, fy)) {
            throw new IllegalArgumentException("A procurement item with this PR already exists for this fiscal year");
        }

        // Create procurement item
        ProcurementItem item = new ProcurementItem();
        item.setPurchaseRequisition(prValue != null && !prValue.isEmpty() ? prValue : null);
        item.setPurchaseOrder(dto.getPurchaseOrder() != null ? dto.getPurchaseOrder().trim() : null);
        item.setName(dto.getName().trim());
        item.setDescription(dto.getDescription());
        item.setFiscalYear(fy);
        
        // Set vendor and contract fields
        item.setVendor(dto.getVendor());
        item.setContractNumber(dto.getContractNumber());
        item.setContractStartDate(dto.getContractStartDate());
        item.setContractEndDate(dto.getContractEndDate());
        item.setProcurementCompleted(dto.getProcurementCompleted() != null ? dto.getProcurementCompleted() : false);
        item.setProcurementCompletedDate(dto.getProcurementCompletedDate());

        // Set final price fields
        item.setFinalPrice(dto.getFinalPrice());
        if (dto.getFinalPriceCurrency() != null && !dto.getFinalPriceCurrency().trim().isEmpty()) {
            try {
                item.setFinalPriceCurrency(com.myrc.model.Currency.valueOf(dto.getFinalPriceCurrency().toUpperCase()));
            } catch (IllegalArgumentException e) {
                item.setFinalPriceCurrency(com.myrc.model.Currency.CAD);
            }
        } else {
            item.setFinalPriceCurrency(com.myrc.model.Currency.CAD);
        }
        item.setFinalPriceExchangeRate(dto.getFinalPriceExchangeRate());
        item.setFinalPriceCad(dto.getFinalPriceCad());

        // Set quoted price fields
        item.setQuotedPrice(dto.getQuotedPrice());
        if (dto.getQuotedPriceCurrency() != null && !dto.getQuotedPriceCurrency().trim().isEmpty()) {
            try {
                item.setQuotedPriceCurrency(com.myrc.model.Currency.valueOf(dto.getQuotedPriceCurrency().toUpperCase()));
            } catch (IllegalArgumentException e) {
                item.setQuotedPriceCurrency(com.myrc.model.Currency.CAD);
            }
        } else {
            item.setQuotedPriceCurrency(com.myrc.model.Currency.CAD);
        }
        item.setQuotedPriceExchangeRate(dto.getQuotedPriceExchangeRate());
        item.setQuotedPriceCad(dto.getQuotedPriceCad());
        
        // Set category if provided
        if (dto.getCategoryId() != null) {
            categoryRepository.findById(dto.getCategoryId())
                .ifPresent(item::setCategory);
        }
        
        // Set tracking status if provided
        if (dto.getTrackingStatus() != null) {
            try {
                item.setTrackingStatus(ProcurementItem.TrackingStatus.valueOf(dto.getTrackingStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                item.setTrackingStatus(ProcurementItem.TrackingStatus.ON_TRACK);
            }
        } else {
            item.setTrackingStatus(ProcurementItem.TrackingStatus.ON_TRACK);
        }
        
        // Set procurement type if provided
        if (dto.getProcurementType() != null) {
            try {
                item.setProcurementType(ProcurementItem.ProcurementType.valueOf(dto.getProcurementType().toUpperCase()));
            } catch (IllegalArgumentException e) {
                item.setProcurementType(ProcurementItem.ProcurementType.RC_INITIATED);
            }
        } else {
            item.setProcurementType(ProcurementItem.ProcurementType.RC_INITIATED);
        }
        
        item.setActive(true);

        ProcurementItem saved = procurementItemRepository.save(item);
        String prLog = prValue != null && !prValue.isEmpty() ? " (PR: " + prValue + ")" : "";
        logger.info("Created procurement item '" + dto.getName() + "'" + prLog + " for FY: " + fy.getName() + " by user " + username);

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
        
        // Update new procurement fields
        if (dto.getVendor() != null) {
            item.setVendor(dto.getVendor().trim().isEmpty() ? null : dto.getVendor().trim());
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

        // Update final price fields
        if (dto.getFinalPrice() != null) {
            item.setFinalPrice(dto.getFinalPrice());
        }
        if (dto.getFinalPriceCurrency() != null && !dto.getFinalPriceCurrency().trim().isEmpty()) {
            try {
                item.setFinalPriceCurrency(com.myrc.model.Currency.valueOf(dto.getFinalPriceCurrency().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Keep existing currency if invalid value provided
            }
        }
        if (dto.getFinalPriceExchangeRate() != null) {
            item.setFinalPriceExchangeRate(dto.getFinalPriceExchangeRate());
        }
        if (dto.getFinalPriceCad() != null) {
            item.setFinalPriceCad(dto.getFinalPriceCad());
        }

        // Update quoted price fields
        if (dto.getQuotedPrice() != null) {
            item.setQuotedPrice(dto.getQuotedPrice());
        }
        if (dto.getQuotedPriceCurrency() != null && !dto.getQuotedPriceCurrency().trim().isEmpty()) {
            try {
                item.setQuotedPriceCurrency(com.myrc.model.Currency.valueOf(dto.getQuotedPriceCurrency().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Keep existing currency if invalid value provided
            }
        }
        if (dto.getQuotedPriceExchangeRate() != null) {
            item.setQuotedPriceExchangeRate(dto.getQuotedPriceExchangeRate());
        }
        if (dto.getQuotedPriceCad() != null) {
            item.setQuotedPriceCad(dto.getQuotedPriceCad());
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

        // Update tracking status if provided
        if (dto.getTrackingStatus() != null) {
            try {
                item.setTrackingStatus(ProcurementItem.TrackingStatus.valueOf(dto.getTrackingStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Keep existing tracking status if invalid value provided
            }
        }

        // Update procurement type if provided
        if (dto.getProcurementType() != null) {
            try {
                item.setProcurementType(ProcurementItem.ProcurementType.valueOf(dto.getProcurementType().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Keep existing procurement type if invalid value provided
            }
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

        // Soft delete all quotes (cascading will handle quote files via orphanRemoval)
        for (ProcurementQuote quote : item.getQuotes()) {
            quote.setActive(false);
        }
        logger.info("Soft deleted " + item.getQuotes().size() + " quotes for procurement item " + procurementItemId);

        // Soft delete all tracking events
        List<ProcurementEvent> events = procurementEventRepository.findByProcurementItemIdAndActiveTrue(procurementItemId);
        for (ProcurementEvent event : events) {
            event.setActive(false);
            procurementEventRepository.save(event);
        }
        logger.info("Soft deleted " + events.size() + " events for procurement item " + procurementItemId);

        // Soft delete all linked spending items
        List<SpendingItem> linkedSpendingItems = item.getSpendingItems();
        for (SpendingItem spendingItem : linkedSpendingItems) {
            if (spendingItem.getActive()) {
                spendingItem.setActive(false);
                spendingItemRepository.save(spendingItem);
                logger.info("Soft deleted linked spending item " + spendingItem.getId() + " for procurement item " + procurementItemId);
            }
        }
        logger.info("Soft deleted " + linkedSpendingItems.size() + " linked spending items for procurement item " + procurementItemId);

        // Soft delete the procurement item itself
        item.setActive(false);
        procurementItemRepository.save(item);
        logger.info("Deleted procurement item " + procurementItemId + " by user " + username);
    }

    @Override
    public ProcurementItemDTO updateProcurementItemStatus(Long procurementItemId, String status, String username) {
        // Status is now tracked via procurement events, not stored on the item itself.
        // This method is kept for backward compatibility but clients should use the
        // procurement event API to create a STATUS_CHANGE event instead.
        Optional<ProcurementItem> itemOpt = procurementItemRepository.findById(procurementItemId);
        if (itemOpt.isEmpty() || !itemOpt.get().getActive()) {
            throw new IllegalArgumentException("Procurement item not found");
        }

        ProcurementItem item = itemOpt.get();
        Long rcId = item.getFiscalYear().getResponsibilityCentre().getId();

        if (!hasWriteAccessToRC(rcId, username)) {
            throw new IllegalArgumentException("User does not have write access to this Responsibility Centre");
        }

        // Validate the status value
        try {
            ProcurementItem.Status.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }

        // Return the item without status change - clients should create a status change event
        logger.info("Status update requested for procurement item " + procurementItemId + " to " + status + " by user " + username + 
                   ". Status is now tracked via events. Please use the event API to create a STATUS_CHANGE event.");

        return ProcurementItemDTO.fromEntityWithoutQuotes(item);
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
        quote.setAmountCap(dto.getAmountCap());
        quote.setAmountOm(dto.getAmountOm());
        quote.setCurrency(quoteCurrency);
        quote.setExchangeRate(dto.getExchangeRate());
        quote.setAmountCapCad(dto.getAmountCapCad());
        quote.setAmountOmCad(dto.getAmountOmCad());
        quote.setReceivedDate(dto.getReceivedDate());
        quote.setExpiryDate(dto.getExpiryDate());
        quote.setNotes(dto.getNotes());
        quote.setStatus(ProcurementQuote.Status.PENDING);
        quote.setSelected(false);
        quote.setProcurementItem(item);
        quote.setActive(true);
        quote.setCreatedBy(username);
        quote.setModifiedBy(username);

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
        // Update CAP/OM amounts (allow null to clear values)
        quote.setAmountCap(dto.getAmountCap());
        quote.setAmountOm(dto.getAmountOm());
        quote.setExchangeRate(dto.getExchangeRate());
        quote.setAmountCapCad(dto.getAmountCapCad());
        quote.setAmountOmCad(dto.getAmountOmCad());
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

        quote.setModifiedBy(username);

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
            throw new IllegalArgumentException("File size exceeds maximum allowed size of 50MB");
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

    @Override
    public ProcurementQuoteFileDTO replaceFile(Long fileId, MultipartFile file, String description, String username) {
        Optional<ProcurementQuoteFile> existingFileOpt = fileRepository.findById(fileId);
        if (existingFileOpt.isEmpty() || !existingFileOpt.get().getActive()) {
            throw new IllegalArgumentException("File not found");
        }

        ProcurementQuoteFile existingFile = existingFileOpt.get();
        Long rcId = existingFile.getQuote().getProcurementItem().getFiscalYear().getResponsibilityCentre().getId();

        if (!hasWriteAccessToRC(rcId, username)) {
            throw new IllegalArgumentException("User does not have write access to this Responsibility Centre");
        }

        // Validate new file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of 50MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("File type not allowed. Allowed types: PDF, images, Word, Excel, text, CSV");
        }

        try {
            // Update file metadata
            existingFile.setFileName(file.getOriginalFilename());
            existingFile.setContentType(contentType);
            existingFile.setFileSize(file.getSize());
            existingFile.setContent(file.getBytes());
            
            // Update description if provided
            if (description != null) {
                existingFile.setDescription(description);
            }

            ProcurementQuoteFile savedFile = fileRepository.save(existingFile);
            logger.info("Replaced file " + fileId + " by user " + username);

            return ProcurementQuoteFileDTO.fromEntity(savedFile);
        } catch (java.io.IOException e) {
            throw new IllegalArgumentException("Failed to read file content");
        }
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

    @Override
    public ToggleSpendingLinkResult toggleSpendingLink(Long procurementItemId, String username, boolean forceUnlink) {
        // Validate user has write access
        Optional<ProcurementItem> itemOpt = procurementItemRepository.findById(procurementItemId);
        if (itemOpt.isEmpty()) {
            throw new IllegalArgumentException("Procurement item not found: " + procurementItemId);
        }

        ProcurementItem procurementItem = itemOpt.get();
        Long rcId = procurementItem.getFiscalYear().getResponsibilityCentre().getId();

        if (!hasWriteAccessToRC(rcId, username)) {
            throw new IllegalArgumentException("User does not have write access to this RC");
        }

        // Don't allow linking cancelled procurement items to spending
        if (procurementItem.getTrackingStatus() == ProcurementItem.TrackingStatus.CANCELLED) {
            throw new IllegalArgumentException("Cannot link a cancelled procurement item to spending");
        }

        List<SpendingItem> linkedSpendingItems = procurementItem.getSpendingItems().stream()
                .filter(SpendingItem::getActive)
                .collect(Collectors.toList());

        if (linkedSpendingItems.isEmpty()) {
            // Create a new spending item from the procurement item
            SpendingItem spendingItem = new SpendingItem();
            spendingItem.setName(procurementItem.getName());
            spendingItem.setDescription(procurementItem.getDescription());
            spendingItem.setVendor(procurementItem.getVendor());
            spendingItem.setReferenceNumber(procurementItem.getPurchaseOrder());
            spendingItem.setCategory(procurementItem.getCategory());
            spendingItem.setFiscalYear(procurementItem.getFiscalYear());
            spendingItem.setProcurementItem(procurementItem);
            spendingItem.setStatus(SpendingItem.Status.PLANNING);
            spendingItem.setCurrency(procurementItem.getFinalPriceCurrency() != null 
                    ? procurementItem.getFinalPriceCurrency() : Currency.CAD);
            spendingItem.setAmount(procurementItem.getFinalPrice() != null 
                    ? procurementItem.getFinalPrice() : procurementItem.getQuotedPrice());
            spendingItem.setActive(true);

            spendingItemRepository.save(spendingItem);
            logger.info("Created spending item from procurement item: " + procurementItemId);

            ProcurementItemDTO dto = ProcurementItemDTO.fromEntityWithoutQuotes(procurementItem);
            return ToggleSpendingLinkResult.success(dto, true);
        } else {
            // Unlink the spending item
            SpendingItem spendingItem = linkedSpendingItems.get(0);

            // Check if the spending item was modified after creation
            boolean wasModified = spendingItem.getVersion() > 0 
                    || (spendingItem.getUpdatedAt() != null 
                        && spendingItem.getCreatedAt() != null 
                        && !spendingItem.getUpdatedAt().equals(spendingItem.getCreatedAt()));

            if (wasModified && !forceUnlink) {
                // Return warning - the frontend should show a confirmation dialog
                ProcurementItemDTO dto = ProcurementItemDTO.fromEntityWithoutQuotes(procurementItem);
                return ToggleSpendingLinkResult.warning(dto, 
                        "The linked spending item has been modified. Are you sure you want to unlink it?");
            }

            // Soft delete the spending item
            spendingItem.setActive(false);
            spendingItemRepository.save(spendingItem);
            logger.info("Unlinked spending item from procurement item: " + procurementItemId);

            ProcurementItemDTO dto = ProcurementItemDTO.fromEntityWithoutQuotes(procurementItem);
            return ToggleSpendingLinkResult.success(dto, false);
        }
    }
}
