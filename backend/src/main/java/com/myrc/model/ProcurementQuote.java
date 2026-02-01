/*
 * myRC - Procurement Quote Entity
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-28
 * Version: 1.0.0
 *
 * Description:
 * Entity representing a Quote associated with a Procurement Item.
 * Each procurement item can have 0..n quotes, and each quote can have 0..n files.
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
 * Entity representing a Quote associated with a Procurement Item.
 * Quotes contain vendor information, pricing, and supporting documents.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-28
 */
@Entity
@Table(name = "procurement_quotes")
public class ProcurementQuote {

    /**
     * Enumeration of quote status values.
     */
    public enum Status {
        PENDING,
        UNDER_REVIEW,
        SELECTED,
        REJECTED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Vendor or supplier name for this quote.
     */
    @Column(nullable = false, length = 200)
    private String vendorName;

    /**
     * Vendor contact information.
     */
    @Column(length = 500)
    private String vendorContact;

    /**
     * Quote reference number from the vendor.
     */
    @Column(length = 100)
    private String quoteReference;

    /**
     * Quoted amount in the specified currency.
     */
    @Column(precision = 15, scale = 2)
    private BigDecimal amount;

    /**
     * The currency for this quote. Defaults to CAD.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Currency currency = Currency.CAD;

    /**
     * Date when the quote was received.
     */
    @Column
    private LocalDate receivedDate;

    /**
     * Date when the quote expires.
     */
    @Column
    private LocalDate expiryDate;

    /**
     * Notes about this quote.
     */
    @Column(length = 2000)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.PENDING;

    /**
     * Whether this quote was selected for the procurement.
     */
    @Column(nullable = false)
    private Boolean selected = false;

    @ManyToOne(optional = false)
    @JoinColumn(name = "procurement_item_id", nullable = false)
    private ProcurementItem procurementItem;

    /**
     * Files attached to this quote.
     * Each quote can have 0..n file attachments.
     */
    @OneToMany(mappedBy = "quote", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProcurementQuoteFile> files = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Version for optimistic locking.
     * Prevents lost updates when multiple users edit the same record.
     */
    @Version
    @Column(nullable = false)
    private Long version = 0L;

    @Column(nullable = false)
    private Boolean active = true;

    // Constructors
    public ProcurementQuote() {
    }

    public ProcurementQuote(String vendorName, BigDecimal amount, Currency currency,
                            ProcurementItem procurementItem) {
        this.vendorName = vendorName;
        this.amount = amount;
        this.currency = currency != null ? currency : Currency.CAD;
        this.procurementItem = procurementItem;
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

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    public ProcurementItem getProcurementItem() {
        return procurementItem;
    }

    public void setProcurementItem(ProcurementItem procurementItem) {
        this.procurementItem = procurementItem;
    }

    public List<ProcurementQuoteFile> getFiles() {
        return files;
    }

    public void setFiles(List<ProcurementQuoteFile> files) {
        this.files = files;
    }

    /**
     * Add a file to this quote.
     *
     * @param file the file to add
     */
    public void addFile(ProcurementQuoteFile file) {
        files.add(file);
        file.setQuote(this);
    }

    /**
     * Remove a file from this quote.
     *
     * @param file the file to remove
     */
    public void removeFile(ProcurementQuoteFile file) {
        files.remove(file);
        file.setQuote(null);
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

    /**
     * Get the version for optimistic locking.
     *
     * @return the version number
     */
    public Long getVersion() {
        return version;
    }

    /**
     * Set the version for optimistic locking.
     *
     * @param version the version number
     */
    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProcurementQuote that = (ProcurementQuote) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "ProcurementQuote{" +
                "id=" + id +
                ", vendorName='" + vendorName + '\'' +
                ", amount=" + amount +
                ", currency=" + currency +
                ", status=" + status +
                '}';
    }
}
