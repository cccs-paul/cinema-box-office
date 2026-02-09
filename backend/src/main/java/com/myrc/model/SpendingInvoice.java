/*
 * myRC - Spending Invoice Entity
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Description:
 * Entity representing an Invoice/Receipt attached to a Spending Item.
 * Each spending item can have 0..n invoices/receipts, each with optional files.
 */
package com.myrc.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * Entity representing an Invoice/Receipt attached to a Spending Item.
 * Contains pricing, dates, optional comments, and supporting file attachments.
 */
@Entity
@Table(name = "spending_invoices")
public class SpendingInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "spending_item_id", nullable = false)
    private SpendingItem spendingItem;

    /**
     * Date the invoice/receipt was received. Optional.
     */
    @Column
    private LocalDate dateReceived;

    /**
     * Date the invoice/receipt was processed. Optional.
     */
    @Column
    private LocalDate dateProcessed;

    /**
     * Optional comments about the invoice/receipt.
     */
    @Column(length = 2000)
    private String comments;

    /**
     * The amount of the invoice/receipt. Mandatory.
     */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    /**
     * The currency for this invoice. Defaults to CAD.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Currency currency = Currency.CAD;

    /**
     * The exchange rate to convert to CAD. Required when currency is not CAD.
     */
    @Column(precision = 15, scale = 6)
    private BigDecimal exchangeRate;

    /**
     * The amount converted to CAD. Calculated from amount * exchangeRate.
     */
    @Column(precision = 15, scale = 2)
    private BigDecimal amountCad;

    /**
     * File attachments for this invoice/receipt.
     */
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SpendingInvoiceFile> files = new ArrayList<>();

    @Column(nullable = false)
    private Boolean active = true;

    @Version
    @Column(nullable = false)
    private Long version = 0L;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(length = 100)
    private String createdBy;

    @Column(length = 100)
    private String modifiedBy;

    // Constructors
    public SpendingInvoice() {
    }

    public SpendingInvoice(SpendingItem spendingItem, BigDecimal amount, Currency currency) {
        this.spendingItem = spendingItem;
        this.amount = amount;
        this.currency = currency != null ? currency : Currency.CAD;
    }

    // Helper methods

    /**
     * Get the amount in CAD, applying exchange rate if necessary.
     */
    public BigDecimal getAmountInCAD() {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        if (currency == Currency.CAD || exchangeRate == null) {
            return amount;
        }
        return amount.multiply(exchangeRate);
    }

    /**
     * Add a file to this invoice.
     */
    public void addFile(SpendingInvoiceFile file) {
        files.add(file);
        file.setInvoice(this);
    }

    /**
     * Remove a file from this invoice.
     */
    public void removeFile(SpendingInvoiceFile file) {
        files.remove(file);
        file.setInvoice(null);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SpendingItem getSpendingItem() {
        return spendingItem;
    }

    public void setSpendingItem(SpendingItem spendingItem) {
        this.spendingItem = spendingItem;
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

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
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

    public List<SpendingInvoiceFile> getFiles() {
        return files;
    }

    public void setFiles(List<SpendingInvoiceFile> files) {
        this.files = files;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
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
}
