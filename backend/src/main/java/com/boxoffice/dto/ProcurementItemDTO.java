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
package com.boxoffice.dto;

import com.boxoffice.model.ProcurementItem;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Data Transfer Object for Procurement Item.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-28
 */
public class ProcurementItemDTO {

    private Long id;
    private String purchaseRequisition;
    private String purchaseOrder;
    private String name;
    private String description;
    private String status;
    private String currency;
    private java.math.BigDecimal exchangeRate;
    private String preferredVendor;
    private String contractNumber;
    private LocalDate contractStartDate;
    private LocalDate contractEndDate;
    private Boolean procurementCompleted;
    private LocalDate procurementCompletedDate;
    private Long fiscalYearId;
    private String fiscalYearName;
    private Long responsibilityCentreId;
    private String responsibilityCentreName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean active;
    private List<ProcurementQuoteDTO> quotes;
    private Integer quoteCount;

    // Constructors
    public ProcurementItemDTO() {
        this.quotes = new ArrayList<>();
    }

    public ProcurementItemDTO(Long id, String purchaseRequisition, String purchaseOrder, String name,
                              String description, String status, Long fiscalYearId, String fiscalYearName,
                              Long responsibilityCentreId, String responsibilityCentreName,
                              LocalDateTime createdAt, LocalDateTime updatedAt, Boolean active,
                              List<ProcurementQuoteDTO> quotes) {
        this.id = id;
        this.purchaseRequisition = purchaseRequisition;
        this.purchaseOrder = purchaseOrder;
        this.name = name;
        this.description = description;
        this.status = status;
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
                item.getStatus() != null ? item.getStatus().name() : null,
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
        dto.setCurrency(item.getCurrency() != null ? item.getCurrency().name() : "CAD");
        dto.setExchangeRate(item.getExchangeRate());
        dto.setPreferredVendor(item.getPreferredVendor());
        dto.setContractNumber(item.getContractNumber());
        dto.setContractStartDate(item.getContractStartDate());
        dto.setContractEndDate(item.getContractEndDate());
        dto.setProcurementCompleted(item.getProcurementCompleted());
        dto.setProcurementCompletedDate(item.getProcurementCompletedDate());
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
        dto.setStatus(item.getStatus() != null ? item.getStatus().name() : null);
        dto.setFiscalYearId(item.getFiscalYear() != null ? item.getFiscalYear().getId() : null);
        dto.setFiscalYearName(item.getFiscalYear() != null ? item.getFiscalYear().getName() : null);
        dto.setResponsibilityCentreId(item.getFiscalYear() != null && item.getFiscalYear().getResponsibilityCentre() != null
                ? item.getFiscalYear().getResponsibilityCentre().getId() : null);
        dto.setResponsibilityCentreName(item.getFiscalYear() != null && item.getFiscalYear().getResponsibilityCentre() != null
                ? item.getFiscalYear().getResponsibilityCentre().getName() : null);
        dto.setCurrency(item.getCurrency() != null ? item.getCurrency().name() : "CAD");
        dto.setExchangeRate(item.getExchangeRate());
        dto.setPreferredVendor(item.getPreferredVendor());
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency != null ? currency : "CAD";
    }

    public java.math.BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(java.math.BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public String getPreferredVendor() {
        return preferredVendor;
    }

    public void setPreferredVendor(String preferredVendor) {
        this.preferredVendor = preferredVendor;
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
}
