/*
 * myRC - Procurement Quote DTO
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-28
 * Version: 1.0.0
 *
 * Description:
 * Data Transfer Object for Procurement Quote.
 * Used for transferring quote data between layers.
 */
package com.myrc.dto;

import com.myrc.model.ProcurementQuote;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Data Transfer Object for Procurement Quote.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-28
 */
public class ProcurementQuoteDTO {

    private Long id;
    private String vendorName;
    private String vendorContact;
    private String quoteReference;
    private BigDecimal amount;
    private BigDecimal amountCap;
    private BigDecimal amountOm;
    private String currency;
    private BigDecimal exchangeRate;
    private BigDecimal amountCapCad;
    private BigDecimal amountOmCad;
    private LocalDate receivedDate;
    private LocalDate expiryDate;
    private String notes;
    private String status;
    private Boolean selected;
    private Long procurementItemId;
    private String procurementItemName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean active;
    private String createdBy;
    private String modifiedBy;
    private List<ProcurementQuoteFileDTO> files;
    private Integer fileCount;

    // Constructors
    public ProcurementQuoteDTO() {
        this.files = new ArrayList<>();
    }

    public ProcurementQuoteDTO(Long id, String vendorName, String vendorContact, String quoteReference,
                               BigDecimal amount, BigDecimal amountCap, BigDecimal amountOm, String currency,
                               BigDecimal exchangeRate, BigDecimal amountCapCad, BigDecimal amountOmCad,
                               LocalDate receivedDate, LocalDate expiryDate,
                               String notes, String status, Boolean selected, Long procurementItemId,
                               String procurementItemName, LocalDateTime createdAt, LocalDateTime updatedAt,
                               Boolean active, List<ProcurementQuoteFileDTO> files) {
        this.id = id;
        this.vendorName = vendorName;
        this.vendorContact = vendorContact;
        this.quoteReference = quoteReference;
        this.amount = amount;
        this.amountCap = amountCap;
        this.amountOm = amountOm;
        this.currency = currency;
        this.exchangeRate = exchangeRate;
        this.amountCapCad = amountCapCad;
        this.amountOmCad = amountOmCad;
        this.receivedDate = receivedDate;
        this.expiryDate = expiryDate;
        this.notes = notes;
        this.status = status;
        this.selected = selected;
        this.procurementItemId = procurementItemId;
        this.procurementItemName = procurementItemName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.active = active;
        this.files = files != null ? files : new ArrayList<>();
        this.fileCount = this.files.size();
    }

    /**
     * Creates a ProcurementQuoteDTO from a ProcurementQuote entity.
     *
     * @param quote the quote entity
     * @return the quote DTO
     */
    public static ProcurementQuoteDTO fromEntity(ProcurementQuote quote) {
        if (quote == null) {
            return null;
        }
        List<ProcurementQuoteFileDTO> fileDTOs = new ArrayList<>();
        if (quote.getFiles() != null) {
            fileDTOs = quote.getFiles().stream()
                    .filter(f -> f.getActive())
                    .map(ProcurementQuoteFileDTO::fromEntity)
                    .collect(Collectors.toList());
        }
        ProcurementQuoteDTO dto = new ProcurementQuoteDTO(
                quote.getId(),
                quote.getVendorName(),
                quote.getVendorContact(),
                quote.getQuoteReference(),
                quote.getAmount(),
                quote.getAmountCap(),
                quote.getAmountOm(),
                quote.getCurrency() != null ? quote.getCurrency().name() : null,
                quote.getExchangeRate(),
                quote.getAmountCapCad(),
                quote.getAmountOmCad(),
                quote.getReceivedDate(),
                quote.getExpiryDate(),
                quote.getNotes(),
                quote.getStatus() != null ? quote.getStatus().name() : null,
                quote.getSelected(),
                quote.getProcurementItem() != null ? quote.getProcurementItem().getId() : null,
                quote.getProcurementItem() != null ? quote.getProcurementItem().getName() : null,
                quote.getCreatedAt(),
                quote.getUpdatedAt(),
                quote.getActive(),
                fileDTOs
        );
        dto.setCreatedBy(quote.getCreatedBy());
        dto.setModifiedBy(quote.getModifiedBy());
        return dto;
    }

    /**
     * Creates a ProcurementQuoteDTO from a ProcurementQuote entity without files.
     *
     * @param quote the quote entity
     * @return the quote DTO without files
     */
    public static ProcurementQuoteDTO fromEntityWithoutFiles(ProcurementQuote quote) {
        if (quote == null) {
            return null;
        }
        ProcurementQuoteDTO dto = new ProcurementQuoteDTO();
        dto.setId(quote.getId());
        dto.setVendorName(quote.getVendorName());
        dto.setVendorContact(quote.getVendorContact());
        dto.setQuoteReference(quote.getQuoteReference());
        dto.setAmount(quote.getAmount());
        dto.setAmountCap(quote.getAmountCap());
        dto.setAmountOm(quote.getAmountOm());
        dto.setCurrency(quote.getCurrency() != null ? quote.getCurrency().name() : null);
        dto.setExchangeRate(quote.getExchangeRate());
        dto.setAmountCapCad(quote.getAmountCapCad());
        dto.setAmountOmCad(quote.getAmountOmCad());
        dto.setReceivedDate(quote.getReceivedDate());
        dto.setExpiryDate(quote.getExpiryDate());
        dto.setNotes(quote.getNotes());
        dto.setStatus(quote.getStatus() != null ? quote.getStatus().name() : null);
        dto.setSelected(quote.getSelected());
        dto.setProcurementItemId(quote.getProcurementItem() != null ? quote.getProcurementItem().getId() : null);
        dto.setProcurementItemName(quote.getProcurementItem() != null ? quote.getProcurementItem().getName() : null);
        dto.setCreatedAt(quote.getCreatedAt());
        dto.setUpdatedAt(quote.getUpdatedAt());
        dto.setActive(quote.getActive());
        dto.setCreatedBy(quote.getCreatedBy());
        dto.setModifiedBy(quote.getModifiedBy());
        dto.setFiles(new ArrayList<>());
        dto.setFileCount(quote.getFiles() != null ? (int) quote.getFiles().stream().filter(f -> f.getActive()).count() : 0);
        return dto;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getVendorContact() {
        return vendorContact;
    }

    public void setVendorContact(String vendorContact) {
        this.vendorContact = vendorContact;
    }

    public String getQuoteReference() {
        return quoteReference;
    }

    public void setQuoteReference(String quoteReference) {
        this.quoteReference = quoteReference;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmountCap() {
        return amountCap;
    }

    public void setAmountCap(BigDecimal amountCap) {
        this.amountCap = amountCap;
    }

    public BigDecimal getAmountOm() {
        return amountOm;
    }

    public void setAmountOm(BigDecimal amountOm) {
        this.amountOm = amountOm;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public BigDecimal getAmountCapCad() {
        return amountCapCad;
    }

    public void setAmountCapCad(BigDecimal amountCapCad) {
        this.amountCapCad = amountCapCad;
    }

    public BigDecimal getAmountOmCad() {
        return amountOmCad;
    }

    public void setAmountOmCad(BigDecimal amountOmCad) {
        this.amountOmCad = amountOmCad;
    }

    public LocalDate getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(LocalDate receivedDate) {
        this.receivedDate = receivedDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    public Long getProcurementItemId() {
        return procurementItemId;
    }

    public void setProcurementItemId(Long procurementItemId) {
        this.procurementItemId = procurementItemId;
    }

    public String getProcurementItemName() {
        return procurementItemName;
    }

    public void setProcurementItemName(String procurementItemName) {
        this.procurementItemName = procurementItemName;
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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public List<ProcurementQuoteFileDTO> getFiles() {
        return files;
    }

    public void setFiles(List<ProcurementQuoteFileDTO> files) {
        this.files = files;
        this.fileCount = files != null ? files.size() : 0;
    }

    public Integer getFileCount() {
        return fileCount;
    }

    public void setFileCount(Integer fileCount) {
        this.fileCount = fileCount;
    }
}
