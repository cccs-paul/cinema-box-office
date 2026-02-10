/*
 * myRC - Export Data DTO
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-02-15
 * Version: 1.0.0
 *
 * Description:
 * Data Transfer Object for full data export/import.
 * Contains all line items plus base64-encoded file attachments.
 */
package com.myrc.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper DTO for complete fiscal year data export/import.
 * Includes funding items, spending items (with invoices and invoice files),
 * procurement items (with events, event files, quotes, and quote files).
 * All binary file content is base64-encoded for JSON transport.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-15
 */
public class ExportDataDTO {

    private ExportMetadata metadata;
    private List<FundingItemDTO> fundingItems;
    private List<SpendingItemExportDTO> spendingItems;
    private List<ProcurementItemExportDTO> procurementItems;

    /**
     * Default constructor.
     */
    public ExportDataDTO() {
        this.fundingItems = new ArrayList<>();
        this.spendingItems = new ArrayList<>();
        this.procurementItems = new ArrayList<>();
    }

    // Getters and Setters
    public ExportMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(ExportMetadata metadata) {
        this.metadata = metadata;
    }

    public List<FundingItemDTO> getFundingItems() {
        return fundingItems;
    }

    public void setFundingItems(List<FundingItemDTO> fundingItems) {
        this.fundingItems = fundingItems;
    }

    public List<SpendingItemExportDTO> getSpendingItems() {
        return spendingItems;
    }

    public void setSpendingItems(List<SpendingItemExportDTO> spendingItems) {
        this.spendingItems = spendingItems;
    }

    public List<ProcurementItemExportDTO> getProcurementItems() {
        return procurementItems;
    }

    public void setProcurementItems(List<ProcurementItemExportDTO> procurementItems) {
        this.procurementItems = procurementItems;
    }

    // ============================
    // Nested DTO classes
    // ============================

    /**
     * Metadata about the export (version, timestamps, source info).
     */
    public static class ExportMetadata {
        private String exportVersion;
        private LocalDateTime exportedAt;
        private String exportedBy;
        private Long responsibilityCentreId;
        private String responsibilityCentreName;
        private Long fiscalYearId;
        private String fiscalYearName;
        private int fundingItemCount;
        private int spendingItemCount;
        private int procurementItemCount;

        /**
         * Default constructor.
         */
        public ExportMetadata() {
        }

        // Getters and Setters
        public String getExportVersion() {
            return exportVersion;
        }

        public void setExportVersion(String exportVersion) {
            this.exportVersion = exportVersion;
        }

        public LocalDateTime getExportedAt() {
            return exportedAt;
        }

        public void setExportedAt(LocalDateTime exportedAt) {
            this.exportedAt = exportedAt;
        }

        public String getExportedBy() {
            return exportedBy;
        }

        public void setExportedBy(String exportedBy) {
            this.exportedBy = exportedBy;
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

        public int getFundingItemCount() {
            return fundingItemCount;
        }

        public void setFundingItemCount(int fundingItemCount) {
            this.fundingItemCount = fundingItemCount;
        }

        public int getSpendingItemCount() {
            return spendingItemCount;
        }

        public void setSpendingItemCount(int spendingItemCount) {
            this.spendingItemCount = spendingItemCount;
        }

        public int getProcurementItemCount() {
            return procurementItemCount;
        }

        public void setProcurementItemCount(int procurementItemCount) {
            this.procurementItemCount = procurementItemCount;
        }
    }

    /**
     * Spending item with full invoice data including base64-encoded file content.
     */
    public static class SpendingItemExportDTO {
        private SpendingItemDTO item;
        private List<SpendingInvoiceExportDTO> invoices;

        /**
         * Default constructor.
         */
        public SpendingItemExportDTO() {
            this.invoices = new ArrayList<>();
        }

        // Getters and Setters
        public SpendingItemDTO getItem() {
            return item;
        }

        public void setItem(SpendingItemDTO item) {
            this.item = item;
        }

        public List<SpendingInvoiceExportDTO> getInvoices() {
            return invoices;
        }

        public void setInvoices(List<SpendingInvoiceExportDTO> invoices) {
            this.invoices = invoices;
        }
    }

    /**
     * Spending invoice with base64-encoded file content.
     */
    public static class SpendingInvoiceExportDTO {
        private SpendingInvoiceDTO invoice;
        private List<FileExportDTO> files;

        /**
         * Default constructor.
         */
        public SpendingInvoiceExportDTO() {
            this.files = new ArrayList<>();
        }

        // Getters and Setters
        public SpendingInvoiceDTO getInvoice() {
            return invoice;
        }

        public void setInvoice(SpendingInvoiceDTO invoice) {
            this.invoice = invoice;
        }

        public List<FileExportDTO> getFiles() {
            return files;
        }

        public void setFiles(List<FileExportDTO> files) {
            this.files = files;
        }
    }

    /**
     * Procurement item with full events, quotes, and file content.
     */
    public static class ProcurementItemExportDTO {
        private ProcurementItemDTO item;
        private List<ProcurementEventExportDTO> events;
        private List<ProcurementQuoteExportDTO> quotes;

        /**
         * Default constructor.
         */
        public ProcurementItemExportDTO() {
            this.events = new ArrayList<>();
            this.quotes = new ArrayList<>();
        }

        // Getters and Setters
        public ProcurementItemDTO getItem() {
            return item;
        }

        public void setItem(ProcurementItemDTO item) {
            this.item = item;
        }

        public List<ProcurementEventExportDTO> getEvents() {
            return events;
        }

        public void setEvents(List<ProcurementEventExportDTO> events) {
            this.events = events;
        }

        public List<ProcurementQuoteExportDTO> getQuotes() {
            return quotes;
        }

        public void setQuotes(List<ProcurementQuoteExportDTO> quotes) {
            this.quotes = quotes;
        }
    }

    /**
     * Procurement event with base64-encoded file content.
     */
    public static class ProcurementEventExportDTO {
        private ProcurementEventDTO event;
        private List<FileExportDTO> files;

        /**
         * Default constructor.
         */
        public ProcurementEventExportDTO() {
            this.files = new ArrayList<>();
        }

        // Getters and Setters
        public ProcurementEventDTO getEvent() {
            return event;
        }

        public void setEvent(ProcurementEventDTO event) {
            this.event = event;
        }

        public List<FileExportDTO> getFiles() {
            return files;
        }

        public void setFiles(List<FileExportDTO> files) {
            this.files = files;
        }
    }

    /**
     * Procurement quote with base64-encoded file content.
     */
    public static class ProcurementQuoteExportDTO {
        private ProcurementQuoteDTO quote;
        private List<FileExportDTO> files;

        /**
         * Default constructor.
         */
        public ProcurementQuoteExportDTO() {
            this.files = new ArrayList<>();
        }

        // Getters and Setters
        public ProcurementQuoteDTO getQuote() {
            return quote;
        }

        public void setQuote(ProcurementQuoteDTO quote) {
            this.quote = quote;
        }

        public List<FileExportDTO> getFiles() {
            return files;
        }

        public void setFiles(List<FileExportDTO> files) {
            this.files = files;
        }
    }

    /**
     * Generic file export DTO with base64-encoded content.
     * Used for all file types (invoice files, event files, quote files).
     */
    public static class FileExportDTO {
        private Long id;
        private String fileName;
        private String contentType;
        private long fileSize;
        private String description;
        private String base64Content;

        /**
         * Default constructor.
         */
        public FileExportDTO() {
        }

        // Getters and Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public long getFileSize() {
            return fileSize;
        }

        public void setFileSize(long fileSize) {
            this.fileSize = fileSize;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getBase64Content() {
            return base64Content;
        }

        public void setBase64Content(String base64Content) {
            this.base64Content = base64Content;
        }
    }
}
