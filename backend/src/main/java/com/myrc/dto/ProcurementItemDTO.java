/*
 * myRC - Procurement Item DTO
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-28
 * Version: 1.0.0
 *
 * Description:
 * Data Transfer Object for Procurement Item.
 * Used for transferring procurement item data between layers.
 */
package com.myrc.dto;

import com.myrc.model.ProcurementItem;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Data Transfer Object for Procurement Item.
 *
 * @author myRC Team
 * @version 1.1.0
 * @since 2026-01-28
 */
public class ProcurementItemDTO {

    private Long id;
    private String purchaseRequisition;
    private String purchaseOrder;
    private String name;
    private String description;
    /**
     * Current status derived from the most recent procurement event.
     * This is populated by the service layer from ProcurementEvent.newStatus.
     */
    private String currentStatus;
    private String vendor;
    private java.math.BigDecimal finalPrice;
    private String finalPriceCurrency;
    private java.math.BigDecimal finalPriceExchangeRate;
    private java.math.BigDecimal finalPriceCad;
    private java.math.BigDecimal quotedPrice;
    private String quotedPriceCurrency;
    private java.math.BigDecimal quotedPriceExchangeRate;
    private java.math.BigDecimal quotedPriceCad;
    private String contractNumber;
    private LocalDate contractStartDate;
    private LocalDate contractEndDate;
    private Boolean procurementCompleted;
    private LocalDate procurementCompletedDate;
    private Long fiscalYearId;
    private String fiscalYearName;
    private Long responsibilityCentreId;
    private String responsibilityCentreName;
    private Long categoryId;
    private String categoryName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean active;
    private List<ProcurementQuoteDTO> quotes;
    private Integer quoteCount;
    private Integer eventCount;
    
    /**
     * IDs of linked spending items.
     * A procurement item can be linked to multiple spending items.
     */
    private List<Long> linkedSpendingItemIds;
    
    /**
     * Names of linked spending items for display purposes.
     */
    private List<String> linkedSpendingItemNames;

    /**
     * Tracking status indicating the overall health/risk of the procurement.
     * Values: ON_TRACK, AT_RISK, CANCELLED
     */
    private String trackingStatus;

    /**
     * Procurement type indicating whether the procurement was RC-initiated or centrally managed.
     * Values: RC_INITIATED, CENTRALLY_MANAGED
     */
    private String procurementType;

    // Constructors
    public ProcurementItemDTO() {
        this.quotes = new ArrayList<>();
    }

    public ProcurementItemDTO(Long id, String purchaseRequisition, String purchaseOrder, String name,
                              String description, Long fiscalYearId, String fiscalYearName,
                              Long responsibilityCentreId, String responsibilityCentreName,
                              LocalDateTime createdAt, LocalDateTime updatedAt, Boolean active,
                              List<ProcurementQuoteDTO> quotes) {
        this.id = id;
        this.purchaseRequisition = purchaseRequisition;
        this.purchaseOrder = purchaseOrder;
        this.name = name;
        this.description = description;
        this.fiscalYearId = fiscalYearId;
        this.fiscalYearName = fiscalYearName;
        this.responsibilityCentreId = responsibilityCentreId;
        this.responsibilityCentreName = responsibilityCentreName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.active = active;
        this.quotes = quotes != null ? quotes : new ArrayList<>();
        this.quoteCount = this.quotes.size();
    }

    /**
     * Creates a ProcurementItemDTO from a ProcurementItem entity.
     *
     * @param item the procurement item entity
     * @return the procurement item DTO
     */
    public static ProcurementItemDTO fromEntity(ProcurementItem item) {
        if (item == null) {
            return null;
        }
        List<ProcurementQuoteDTO> quoteDTOs = new ArrayList<>();
        if (item.getQuotes() != null) {
            quoteDTOs = item.getQuotes().stream()
                    .filter(q -> q.getActive())
                    .map(ProcurementQuoteDTO::fromEntity)
                    .collect(Collectors.toList());
        }
        ProcurementItemDTO dto = new ProcurementItemDTO(
                item.getId(),
                item.getPurchaseRequisition(),
                item.getPurchaseOrder(),
                item.getName(),
                item.getDescription(),
                item.getFiscalYear() != null ? item.getFiscalYear().getId() : null,
                item.getFiscalYear() != null ? item.getFiscalYear().getName() : null,
                item.getFiscalYear() != null && item.getFiscalYear().getResponsibilityCentre() != null
                        ? item.getFiscalYear().getResponsibilityCentre().getId() : null,
                item.getFiscalYear() != null && item.getFiscalYear().getResponsibilityCentre() != null
                        ? item.getFiscalYear().getResponsibilityCentre().getName() : null,
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getActive(),
                quoteDTOs
        );
        dto.setVendor(item.getVendor());
        dto.setFinalPrice(item.getFinalPrice());
        dto.setFinalPriceCurrency(item.getFinalPriceCurrency() != null ? item.getFinalPriceCurrency().name() : "CAD");
        dto.setFinalPriceExchangeRate(item.getFinalPriceExchangeRate());
        dto.setFinalPriceCad(item.getFinalPriceCad());
        dto.setQuotedPrice(item.getQuotedPrice());
        dto.setQuotedPriceCurrency(item.getQuotedPriceCurrency() != null ? item.getQuotedPriceCurrency().name() : "CAD");
        dto.setQuotedPriceExchangeRate(item.getQuotedPriceExchangeRate());
        dto.setQuotedPriceCad(item.getQuotedPriceCad());
        dto.setContractNumber(item.getContractNumber());
        dto.setContractStartDate(item.getContractStartDate());
        dto.setContractEndDate(item.getContractEndDate());
        dto.setProcurementCompleted(item.getProcurementCompleted());
        dto.setProcurementCompletedDate(item.getProcurementCompletedDate());
        dto.setCategoryId(item.getCategory() != null ? item.getCategory().getId() : null);
        dto.setCategoryName(item.getCategory() != null ? item.getCategory().getName() : null);
        
        // Populate linked spending items
        if (item.getSpendingItems() != null && !item.getSpendingItems().isEmpty()) {
            dto.setLinkedSpendingItemIds(item.getSpendingItems().stream()
                    .filter(si -> si.getActive())
                    .map(si -> si.getId())
                    .collect(Collectors.toList()));
            dto.setLinkedSpendingItemNames(item.getSpendingItems().stream()
                    .filter(si -> si.getActive())
                    .map(si -> si.getName())
                    .collect(Collectors.toList()));
        } else {
            dto.setLinkedSpendingItemIds(new ArrayList<>());
            dto.setLinkedSpendingItemNames(new ArrayList<>());
        }
        
        // Populate tracking status
        dto.setTrackingStatus(item.getTrackingStatus() != null ? item.getTrackingStatus().name() : "PLANNING");
        
        // Populate procurement type
        dto.setProcurementType(item.getProcurementType() != null ? item.getProcurementType().name() : "RC_INITIATED");
        
        return dto;
    }

    /**
     * Creates a ProcurementItemDTO from a ProcurementItem entity without quotes.
     *
     * @param item the procurement item entity
     * @return the procurement item DTO without quotes
     */
    public static ProcurementItemDTO fromEntityWithoutQuotes(ProcurementItem item) {
        if (item == null) {
            return null;
        }
        ProcurementItemDTO dto = new ProcurementItemDTO();
        dto.setId(item.getId());
        dto.setPurchaseRequisition(item.getPurchaseRequisition());
        dto.setPurchaseOrder(item.getPurchaseOrder());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setFiscalYearId(item.getFiscalYear() != null ? item.getFiscalYear().getId() : null);
        dto.setFiscalYearName(item.getFiscalYear() != null ? item.getFiscalYear().getName() : null);
        dto.setResponsibilityCentreId(item.getFiscalYear() != null && item.getFiscalYear().getResponsibilityCentre() != null
                ? item.getFiscalYear().getResponsibilityCentre().getId() : null);
        dto.setResponsibilityCentreName(item.getFiscalYear() != null && item.getFiscalYear().getResponsibilityCentre() != null
                ? item.getFiscalYear().getResponsibilityCentre().getName() : null);
        dto.setCategoryId(item.getCategory() != null ? item.getCategory().getId() : null);
        dto.setCategoryName(item.getCategory() != null ? item.getCategory().getName() : null);
        dto.setVendor(item.getVendor());
        dto.setFinalPrice(item.getFinalPrice());
        dto.setFinalPriceCurrency(item.getFinalPriceCurrency() != null ? item.getFinalPriceCurrency().name() : "CAD");
        dto.setFinalPriceExchangeRate(item.getFinalPriceExchangeRate());
        dto.setFinalPriceCad(item.getFinalPriceCad());
        dto.setQuotedPrice(item.getQuotedPrice());
        dto.setQuotedPriceCurrency(item.getQuotedPriceCurrency() != null ? item.getQuotedPriceCurrency().name() : "CAD");
        dto.setQuotedPriceExchangeRate(item.getQuotedPriceExchangeRate());
        dto.setQuotedPriceCad(item.getQuotedPriceCad());
        dto.setContractNumber(item.getContractNumber());
        dto.setContractStartDate(item.getContractStartDate());
        dto.setContractEndDate(item.getContractEndDate());
        dto.setProcurementCompleted(item.getProcurementCompleted());
        dto.setProcurementCompletedDate(item.getProcurementCompletedDate());
        dto.setCreatedAt(item.getCreatedAt());
        dto.setUpdatedAt(item.getUpdatedAt());
        dto.setActive(item.getActive());
        dto.setQuotes(new ArrayList<>());
        dto.setQuoteCount(item.getQuotes() != null ? (int) item.getQuotes().stream().filter(q -> q.getActive()).count() : 0);
        
        // Populate linked spending items
        if (item.getSpendingItems() != null && !item.getSpendingItems().isEmpty()) {
            dto.setLinkedSpendingItemIds(item.getSpendingItems().stream()
                    .filter(si -> si.getActive())
                    .map(si -> si.getId())
                    .collect(Collectors.toList()));
            dto.setLinkedSpendingItemNames(item.getSpendingItems().stream()
                    .filter(si -> si.getActive())
                    .map(si -> si.getName())
                    .collect(Collectors.toList()));
        } else {
            dto.setLinkedSpendingItemIds(new ArrayList<>());
            dto.setLinkedSpendingItemNames(new ArrayList<>());
        }
        
        // Populate tracking status
        dto.setTrackingStatus(item.getTrackingStatus() != null ? item.getTrackingStatus().name() : "PLANNING");
        
        // Populate procurement type
        dto.setProcurementType(item.getProcurementType() != null ? item.getProcurementType().name() : "RC_INITIATED");
        
        return dto;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPurchaseRequisition() {
        return purchaseRequisition;
    }

    public void setPurchaseRequisition(String purchaseRequisition) {
        this.purchaseRequisition = purchaseRequisition;
    }

    public String getPurchaseOrder() {
        return purchaseOrder;
    }

    public void setPurchaseOrder(String purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public java.math.BigDecimal getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(java.math.BigDecimal finalPrice) {
        this.finalPrice = finalPrice;
    }

    public String getFinalPriceCurrency() {
        return finalPriceCurrency;
    }

    public void setFinalPriceCurrency(String finalPriceCurrency) {
        this.finalPriceCurrency = finalPriceCurrency != null ? finalPriceCurrency : "CAD";
    }

    public java.math.BigDecimal getFinalPriceExchangeRate() {
        return finalPriceExchangeRate;
    }

    public void setFinalPriceExchangeRate(java.math.BigDecimal finalPriceExchangeRate) {
        this.finalPriceExchangeRate = finalPriceExchangeRate;
    }

    public java.math.BigDecimal getFinalPriceCad() {
        return finalPriceCad;
    }

    public void setFinalPriceCad(java.math.BigDecimal finalPriceCad) {
        this.finalPriceCad = finalPriceCad;
    }

    public java.math.BigDecimal getQuotedPrice() {
        return quotedPrice;
    }

    public void setQuotedPrice(java.math.BigDecimal quotedPrice) {
        this.quotedPrice = quotedPrice;
    }

    public String getQuotedPriceCurrency() {
        return quotedPriceCurrency;
    }

    public void setQuotedPriceCurrency(String quotedPriceCurrency) {
        this.quotedPriceCurrency = quotedPriceCurrency != null ? quotedPriceCurrency : "CAD";
    }

    public java.math.BigDecimal getQuotedPriceExchangeRate() {
        return quotedPriceExchangeRate;
    }

    public void setQuotedPriceExchangeRate(java.math.BigDecimal quotedPriceExchangeRate) {
        this.quotedPriceExchangeRate = quotedPriceExchangeRate;
    }

    public java.math.BigDecimal getQuotedPriceCad() {
        return quotedPriceCad;
    }

    public void setQuotedPriceCad(java.math.BigDecimal quotedPriceCad) {
        this.quotedPriceCad = quotedPriceCad;
    }

    public String getContractNumber() {
        return contractNumber;
    }

    public void setContractNumber(String contractNumber) {
        this.contractNumber = contractNumber;
    }

    public LocalDate getContractStartDate() {
        return contractStartDate;
    }

    public void setContractStartDate(LocalDate contractStartDate) {
        this.contractStartDate = contractStartDate;
    }

    public LocalDate getContractEndDate() {
        return contractEndDate;
    }

    public void setContractEndDate(LocalDate contractEndDate) {
        this.contractEndDate = contractEndDate;
    }

    public Boolean getProcurementCompleted() {
        return procurementCompleted;
    }

    public void setProcurementCompleted(Boolean procurementCompleted) {
        this.procurementCompleted = procurementCompleted;
    }

    public LocalDate getProcurementCompletedDate() {
        return procurementCompletedDate;
    }

    public void setProcurementCompletedDate(LocalDate procurementCompletedDate) {
        this.procurementCompletedDate = procurementCompletedDate;
    }

    public Long getFiscalYearId() {
        return fiscalYearId;
    }

    public void setFiscalYearId(Long fiscalYearId) {
        this.fiscalYearId = fiscalYearId;
    }

    public String getFiscalYearName() {
        return fiscalYearName;
    }

    public void setFiscalYearName(String fiscalYearName) {
        this.fiscalYearName = fiscalYearName;
    }

    public Long getResponsibilityCentreId() {
        return responsibilityCentreId;
    }

    public void setResponsibilityCentreId(Long responsibilityCentreId) {
        this.responsibilityCentreId = responsibilityCentreId;
    }

    public String getResponsibilityCentreName() {
        return responsibilityCentreName;
    }

    public void setResponsibilityCentreName(String responsibilityCentreName) {
        this.responsibilityCentreName = responsibilityCentreName;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public List<ProcurementQuoteDTO> getQuotes() {
        return quotes;
    }

    public void setQuotes(List<ProcurementQuoteDTO> quotes) {
        this.quotes = quotes;
        this.quoteCount = quotes != null ? quotes.size() : 0;
    }

    public Integer getQuoteCount() {
        return quoteCount;
    }

    public void setQuoteCount(Integer quoteCount) {
        this.quoteCount = quoteCount;
    }

    public Integer getEventCount() {
        return eventCount;
    }

    public void setEventCount(Integer eventCount) {
        this.eventCount = eventCount;
    }

    public List<Long> getLinkedSpendingItemIds() {
        return linkedSpendingItemIds;
    }

    public void setLinkedSpendingItemIds(List<Long> linkedSpendingItemIds) {
        this.linkedSpendingItemIds = linkedSpendingItemIds;
    }

    public List<String> getLinkedSpendingItemNames() {
        return linkedSpendingItemNames;
    }

    public void setLinkedSpendingItemNames(List<String> linkedSpendingItemNames) {
        this.linkedSpendingItemNames = linkedSpendingItemNames;
    }

    public String getTrackingStatus() {
        return trackingStatus;
    }

    public void setTrackingStatus(String trackingStatus) {
        this.trackingStatus = trackingStatus;
    }

    public String getProcurementType() {
        return procurementType;
    }

    public void setProcurementType(String procurementType) {
        this.procurementType = procurementType;
    }
}
