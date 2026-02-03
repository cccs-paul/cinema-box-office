/*
 * myRC - Procurement Item Entity
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-28
 * Version: 1.1.0
 *
 * Description:
 * Entity representing a Procurement Item associated with a Fiscal Year.
 * Each fiscal year can have 0..n procurement items.
 */
package com.myrc.model;

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
 * Entity representing a Procurement Item associated with a Fiscal Year.
 * Procurement items track purchase requisitions and purchase orders,
 * along with associated quotes and documentation.
 * 
 * Note: Status is tracked via ProcurementEvent records - use getMostRecentStatus()
 * to get the current status from the most recent tracking event.
 *
 * @author myRC Team
 * @version 1.1.0
 * @since 2026-01-28
 */
@Entity
@Table(name = "procurement_items")
public class ProcurementItem {

    /**
     * Enumeration of procurement item status values.
     * Status is stored in ProcurementEvent.newStatus for tracking history.
     */
    public enum Status {
        DRAFT,
        PENDING_QUOTES,
        QUOTES_RECEIVED,
        UNDER_REVIEW,
        APPROVED,
        PO_ISSUED,
        COMPLETED,
        CANCELLED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Purchase Requisition number (PR).
     * Optional - procurement items may not have a PR number.
     */
    @Column(name = "purchase_requisition", length = 100)
    private String purchaseRequisition;

    /**
     * Purchase Order number (PO).
     * Optional - may be assigned after quotes are approved.
     */
    @Column(name = "purchase_order", length = 100)
    private String purchaseOrder;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 2000)
    private String description;

    /**
     * Vendor name for this procurement.
     * Typically set after a quote is selected.
     */
    @Column(name = "preferred_vendor", length = 200)
    private String vendor;

    /**
     * Contract number associated with this procurement.
     */
    @Column(name = "contract_number", length = 100)
    private String contractNumber;

    /**
     * Contract start date.
     */
    @Column(name = "contract_start_date")
    private LocalDate contractStartDate;

    /**
     * Contract end date.
     */
    @Column(name = "contract_end_date")
    private LocalDate contractEndDate;

    /**
     * Final price for this procurement in the specified currency.
     */
    @Column(name = "final_price", precision = 15, scale = 2)
    private java.math.BigDecimal finalPrice;

    /**
     * Currency code for the final price. Defaults to CAD.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "final_price_currency", length = 3)
    private Currency finalPriceCurrency = Currency.CAD;

    /**
     * Exchange rate to convert final price to CAD.
     * Required when finalPriceCurrency is not CAD.
     */
    @Column(name = "final_price_exchange_rate", precision = 10, scale = 6)
    private java.math.BigDecimal finalPriceExchangeRate;

    /**
     * Final price converted to CAD.
     * Required when finalPriceCurrency is not CAD.
     */
    @Column(name = "final_price_cad", precision = 15, scale = 2)
    private java.math.BigDecimal finalPriceCad;

    /**
     * Quoted or estimated price for this procurement in the specified currency.
     */
    @Column(name = "quoted_price", precision = 15, scale = 2)
    private java.math.BigDecimal quotedPrice;

    /**
     * Currency code for the quoted price. Defaults to CAD.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "quoted_price_currency", length = 3)
    private Currency quotedPriceCurrency = Currency.CAD;

    /**
     * Exchange rate to convert quoted price to CAD.
     * Required when quotedPriceCurrency is not CAD.
     */
    @Column(name = "quoted_price_exchange_rate", precision = 10, scale = 6)
    private java.math.BigDecimal quotedPriceExchangeRate;

    /**
     * Quoted price converted to CAD.
     * Required when quotedPriceCurrency is not CAD.
     */
    @Column(name = "quoted_price_cad", precision = 15, scale = 2)
    private java.math.BigDecimal quotedPriceCad;

    /**
     * Whether the procurement has been completed.
     */
    @Column(name = "procurement_completed")
    private Boolean procurementCompleted = false;

    /**
     * Date when the procurement was completed.
     */
    @Column(name = "procurement_completed_date")
    private LocalDate procurementCompletedDate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fiscal_year_id", nullable = false)
    private FiscalYear fiscalYear;

    /**
     * The category this procurement item belongs to.
     * Optional - procurement items may or may not have a category.
     */
    @ManyToOne(optional = true)
    @JoinColumn(name = "category_id", nullable = true)
    private Category category;

    /**
     * Quotes associated with this procurement item.
     * Each procurement item can have 0..n quotes.
     */
    @OneToMany(mappedBy = "procurementItem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProcurementQuote> quotes = new ArrayList<>();

    /**
     * Spending items linked to this procurement item.
     * A procurement item can be linked to multiple spending items.
     */
    @OneToMany(mappedBy = "procurementItem", fetch = FetchType.LAZY)
    private List<SpendingItem> spendingItems = new ArrayList<>();

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
    public ProcurementItem() {
    }

    public ProcurementItem(String purchaseRequisition, String name, String description,
                           FiscalYear fiscalYear) {
        this.purchaseRequisition = purchaseRequisition;
        this.name = name;
        this.description = description;
        this.fiscalYear = fiscalYear;
    }

    public ProcurementItem(String purchaseRequisition, String name, FiscalYear fiscalYear) {
        this(purchaseRequisition, name, null, fiscalYear);
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

    public Currency getFinalPriceCurrency() {
        return finalPriceCurrency;
    }

    public void setFinalPriceCurrency(Currency finalPriceCurrency) {
        this.finalPriceCurrency = finalPriceCurrency != null ? finalPriceCurrency : Currency.CAD;
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

    public Currency getQuotedPriceCurrency() {
        return quotedPriceCurrency;
    }

    public void setQuotedPriceCurrency(Currency quotedPriceCurrency) {
        this.quotedPriceCurrency = quotedPriceCurrency != null ? quotedPriceCurrency : Currency.CAD;
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

    public FiscalYear getFiscalYear() {
        return fiscalYear;
    }

    public void setFiscalYear(FiscalYear fiscalYear) {
        this.fiscalYear = fiscalYear;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public List<ProcurementQuote> getQuotes() {
        return quotes;
    }

    public void setQuotes(List<ProcurementQuote> quotes) {
        this.quotes = quotes;
    }

    /**
     * Add a quote to this procurement item.
     *
     * @param quote the quote to add
     */
    public void addQuote(ProcurementQuote quote) {
        quotes.add(quote);
        quote.setProcurementItem(this);
    }

    /**
     * Remove a quote from this procurement item.
     *
     * @param quote the quote to remove
     */
    public void removeQuote(ProcurementQuote quote) {
        quotes.remove(quote);
        quote.setProcurementItem(null);
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

    /**
     * Gets the spending items linked to this procurement item.
     *
     * @return the list of linked spending items
     */
    public List<SpendingItem> getSpendingItems() {
        return spendingItems;
    }

    /**
     * Sets the spending items linked to this procurement item.
     *
     * @param spendingItems the list of linked spending items
     */
    public void setSpendingItems(List<SpendingItem> spendingItems) {
        this.spendingItems = spendingItems;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProcurementItem that = (ProcurementItem) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "ProcurementItem{" +
                "id=" + id +
                ", purchaseRequisition='" + purchaseRequisition + '\'' +
                ", purchaseOrder='" + purchaseOrder + '\'' +
                ", name='" + name + '\'' +
                ", vendor='" + vendor + '\'' +
                ", finalPriceCurrency=" + finalPriceCurrency +
                '}';
    }
}
