/*
 * myRC - Spending Invoice DTO
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Description:
 * Data Transfer Object for Spending Invoice.
 * Used for transferring invoice data between layers.
 */
package com.myrc.dto;

import com.myrc.model.SpendingInvoice;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Data Transfer Object for Spending Invoice.
 */
public class SpendingInvoiceDTO {

    private Long id;
    private Long spendingItemId;
    private String spendingItemName;
    private LocalDate dateReceived;
    private LocalDate dateProcessed;
    private String comments;
    private BigDecimal amount;
    private String currency;
    private BigDecimal exchangeRate;
    private BigDecimal amountCad;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean active;
    private String createdBy;
    private String modifiedBy;
    private List<SpendingInvoiceFileDTO> files;
    private Integer fileCount;

    // Constructors
    public SpendingInvoiceDTO() {
        this.files = new ArrayList<>();
    }

    /**
     * Creates a SpendingInvoiceDTO from a SpendingInvoice entity, including files.
     */
    public static SpendingInvoiceDTO fromEntity(SpendingInvoice invoice) {
        if (invoice == null) {
            return null;
        }
        SpendingInvoiceDTO dto = new SpendingInvoiceDTO();
        dto.setId(invoice.getId());
        dto.setSpendingItemId(invoice.getSpendingItem() != null ? invoice.getSpendingItem().getId() : null);
        dto.setSpendingItemName(invoice.getSpendingItem() != null ? invoice.getSpendingItem().getName() : null);
        dto.setDateReceived(invoice.getDateReceived());
        dto.setDateProcessed(invoice.getDateProcessed());
        dto.setComments(invoice.getComments());
        dto.setAmount(invoice.getAmount());
        dto.setCurrency(invoice.getCurrency() != null ? invoice.getCurrency().getCode() : "CAD");
        dto.setExchangeRate(invoice.getExchangeRate());
        dto.setAmountCad(invoice.getAmountCad());
        dto.setCreatedAt(invoice.getCreatedAt());
        dto.setUpdatedAt(invoice.getUpdatedAt());
        dto.setActive(invoice.getActive());
        dto.setCreatedBy(invoice.getCreatedBy());
        dto.setModifiedBy(invoice.getModifiedBy());

        List<SpendingInvoiceFileDTO> fileDTOs = new ArrayList<>();
        if (invoice.getFiles() != null) {
            fileDTOs = invoice.getFiles().stream()
                    .filter(f -> f.getActive())
                    .map(SpendingInvoiceFileDTO::fromEntity)
                    .collect(Collectors.toList());
        }
        dto.setFiles(fileDTOs);
        dto.setFileCount(fileDTOs.size());
        return dto;
    }

    /**
     * Creates a SpendingInvoiceDTO from a SpendingInvoice entity without files.
     */
    public static SpendingInvoiceDTO fromEntityWithoutFiles(SpendingInvoice invoice) {
        if (invoice == null) {
            return null;
        }
        SpendingInvoiceDTO dto = new SpendingInvoiceDTO();
        dto.setId(invoice.getId());
        dto.setSpendingItemId(invoice.getSpendingItem() != null ? invoice.getSpendingItem().getId() : null);
        dto.setSpendingItemName(invoice.getSpendingItem() != null ? invoice.getSpendingItem().getName() : null);
        dto.setDateReceived(invoice.getDateReceived());
        dto.setDateProcessed(invoice.getDateProcessed());
        dto.setComments(invoice.getComments());
        dto.setAmount(invoice.getAmount());
        dto.setCurrency(invoice.getCurrency() != null ? invoice.getCurrency().getCode() : "CAD");
        dto.setExchangeRate(invoice.getExchangeRate());
        dto.setAmountCad(invoice.getAmountCad());
        dto.setCreatedAt(invoice.getCreatedAt());
        dto.setUpdatedAt(invoice.getUpdatedAt());
        dto.setActive(invoice.getActive());
        dto.setCreatedBy(invoice.getCreatedBy());
        dto.setModifiedBy(invoice.getModifiedBy());
        dto.setFiles(new ArrayList<>());
        dto.setFileCount(invoice.getFiles() != null ? (int) invoice.getFiles().stream().filter(f -> f.getActive()).count() : 0);
        return dto;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSpendingItemId() {
        return spendingItemId;
    }

    public void setSpendingItemId(Long spendingItemId) {
        this.spendingItemId = spendingItemId;
    }

    public String getSpendingItemName() {
        return spendingItemName;
    }

    public void setSpendingItemName(String spendingItemName) {
        this.spendingItemName = spendingItemName;
    }

    public LocalDate getDateReceived() {
        return dateReceived;
    }

    public void setDateReceived(LocalDate dateReceived) {
        this.dateReceived = dateReceived;
    }

    public LocalDate getDateProcessed() {
        return dateProcessed;
    }

    public void setDateProcessed(LocalDate dateProcessed) {
        this.dateProcessed = dateProcessed;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
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

    public BigDecimal getAmountCad() {
        return amountCad;
    }

    public void setAmountCad(BigDecimal amountCad) {
        this.amountCad = amountCad;
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

    public List<SpendingInvoiceFileDTO> getFiles() {
        return files;
    }

    public void setFiles(List<SpendingInvoiceFileDTO> files) {
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
