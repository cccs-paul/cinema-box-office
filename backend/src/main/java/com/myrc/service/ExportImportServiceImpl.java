/*
 * myRC - Export/Import Service Implementation
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-02-15
 * Version: 1.0.0
 *
 * Description:
 * Service implementation for data export and import operations.
 * Orchestrates retrieval of all line items and file content for export,
 * and creation of all data for import.
 */
package com.myrc.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.myrc.dto.ExportDataDTO;
import com.myrc.dto.ExportDataDTO.ExportMetadata;
import com.myrc.dto.ExportDataDTO.FileExportDTO;
import com.myrc.dto.ExportDataDTO.ProcurementEventExportDTO;
import com.myrc.dto.ExportDataDTO.ProcurementItemExportDTO;
import com.myrc.dto.ExportDataDTO.ProcurementQuoteExportDTO;
import com.myrc.dto.ExportDataDTO.SpendingInvoiceExportDTO;
import com.myrc.dto.ExportDataDTO.SpendingItemExportDTO;
import com.myrc.dto.FiscalYearDTO;
import com.myrc.dto.FundingItemDTO;
import com.myrc.dto.ProcurementEventDTO;
import com.myrc.dto.ProcurementEventFileDTO;
import com.myrc.dto.ProcurementItemDTO;
import com.myrc.dto.ProcurementQuoteDTO;
import com.myrc.dto.ProcurementQuoteFileDTO;
import com.myrc.dto.SpendingInvoiceDTO;
import com.myrc.dto.SpendingInvoiceFileDTO;
import com.myrc.dto.SpendingItemDTO;
import com.myrc.model.ProcurementEventFile;

/**
 * Implementation of the export/import service.
 * Orchestrates reading all fiscal year data and encoding file content as base64.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-15
 */
@Service
public class ExportImportServiceImpl implements ExportImportService {

    private static final Logger logger = Logger.getLogger(ExportImportServiceImpl.class.getName());
    private static final String EXPORT_VERSION = "1.0.0";

    private final FundingItemService fundingItemService;
    private final SpendingItemService spendingItemService;
    private final SpendingInvoiceService spendingInvoiceService;
    private final ProcurementItemService procurementItemService;
    private final ProcurementEventService procurementEventService;
    private final FiscalYearService fiscalYearService;
    private final ResponsibilityCentreService responsibilityCentreService;

    /**
     * Constructor with all required service dependencies.
     *
     * @param fundingItemService funding item service
     * @param spendingItemService spending item service
     * @param spendingInvoiceService spending invoice service
     * @param procurementItemService procurement item service
     * @param procurementEventService procurement event service
     * @param fiscalYearService fiscal year service
     * @param responsibilityCentreService responsibility centre service
     */
    public ExportImportServiceImpl(
            FundingItemService fundingItemService,
            SpendingItemService spendingItemService,
            SpendingInvoiceService spendingInvoiceService,
            ProcurementItemService procurementItemService,
            ProcurementEventService procurementEventService,
            FiscalYearService fiscalYearService,
            ResponsibilityCentreService responsibilityCentreService) {
        this.fundingItemService = fundingItemService;
        this.spendingItemService = spendingItemService;
        this.spendingInvoiceService = spendingInvoiceService;
        this.procurementItemService = procurementItemService;
        this.procurementEventService = procurementEventService;
        this.fiscalYearService = fiscalYearService;
        this.responsibilityCentreService = responsibilityCentreService;
    }

    @Override
    @Transactional(readOnly = true)
    public ExportDataDTO exportData(Long rcId, Long fyId, String username) {
        logger.info("Exporting data for RC " + rcId + ", FY " + fyId + " by user: " + username);

        // Validate access and get context info
        Optional<FiscalYearDTO> fyOpt = fiscalYearService.getFiscalYearById(fyId, username);
        if (fyOpt.isEmpty()) {
            throw new IllegalArgumentException("Fiscal year not found or access denied: " + fyId);
        }
        FiscalYearDTO fiscalYear = fyOpt.get();

        ExportDataDTO exportData = new ExportDataDTO();

        // Build metadata
        ExportMetadata metadata = new ExportMetadata();
        metadata.setExportVersion(EXPORT_VERSION);
        metadata.setExportedAt(LocalDateTime.now());
        metadata.setExportedBy(username);
        metadata.setResponsibilityCentreId(rcId);
        metadata.setResponsibilityCentreName(fiscalYear.getResponsibilityCentreName());
        metadata.setFiscalYearId(fyId);
        metadata.setFiscalYearName(fiscalYear.getName());

        // Export funding items
        List<FundingItemDTO> fundingItems = fundingItemService.getFundingItemsByFiscalYearId(fyId, username);
        exportData.setFundingItems(fundingItems);
        metadata.setFundingItemCount(fundingItems.size());
        logger.info("Exported " + fundingItems.size() + " funding items");

        // Export spending items with invoices and files
        List<SpendingItemDTO> spendingItems = spendingItemService.getSpendingItemsByFiscalYearId(fyId, username);
        List<SpendingItemExportDTO> spendingExports = new ArrayList<>();
        for (SpendingItemDTO spendingItem : spendingItems) {
            spendingExports.add(exportSpendingItem(spendingItem, username));
        }
        exportData.setSpendingItems(spendingExports);
        metadata.setSpendingItemCount(spendingItems.size());
        logger.info("Exported " + spendingItems.size() + " spending items");

        // Export procurement items with events, quotes, and files
        List<ProcurementItemDTO> procurementItems =
                procurementItemService.getProcurementItemsByFiscalYearId(fyId, username);
        List<ProcurementItemExportDTO> procurementExports = new ArrayList<>();
        for (ProcurementItemDTO procurementItem : procurementItems) {
            procurementExports.add(exportProcurementItem(procurementItem, username));
        }
        exportData.setProcurementItems(procurementExports);
        metadata.setProcurementItemCount(procurementItems.size());
        logger.info("Exported " + procurementItems.size() + " procurement items");

        exportData.setMetadata(metadata);
        return exportData;
    }

    @Override
    @Transactional
    public ExportDataDTO importData(Long rcId, Long fyId, ExportDataDTO exportData, String username) {
        logger.info("Importing data for RC " + rcId + ", FY " + fyId + " by user: " + username);

        // Validate write access
        Optional<FiscalYearDTO> fyOpt = fiscalYearService.getFiscalYearById(fyId, username);
        if (fyOpt.isEmpty()) {
            throw new IllegalArgumentException("Fiscal year not found or access denied: " + fyId);
        }

        ExportDataDTO result = new ExportDataDTO();
        ExportMetadata resultMetadata = new ExportMetadata();
        resultMetadata.setExportVersion(EXPORT_VERSION);
        resultMetadata.setExportedAt(LocalDateTime.now());
        resultMetadata.setExportedBy(username);
        resultMetadata.setResponsibilityCentreId(rcId);
        resultMetadata.setFiscalYearId(fyId);

        // Import funding items
        int fundingCount = 0;
        if (exportData.getFundingItems() != null) {
            for (FundingItemDTO fundingItem : exportData.getFundingItems()) {
                try {
                    fundingItemService.createFundingItem(
                            fyId, username,
                            fundingItem.getName(),
                            fundingItem.getDescription() != null ? fundingItem.getDescription() : "",
                            fundingItem.getSource(),
                            fundingItem.getComments(),
                            fundingItem.getCurrency(),
                            fundingItem.getExchangeRate(),
                            fundingItem.getCategoryId(),
                            fundingItem.getMoneyAllocations());
                    fundingCount++;
                } catch (Exception e) {
                    logger.warning("Failed to import funding item '" + fundingItem.getName() + "': " + e.getMessage());
                }
            }
        }
        resultMetadata.setFundingItemCount(fundingCount);
        logger.info("Imported " + fundingCount + " funding items");

        // Import spending items (without invoices for now — invoices need spending item ID)
        int spendingCount = 0;
        if (exportData.getSpendingItems() != null) {
            for (SpendingItemExportDTO spendingExport : exportData.getSpendingItems()) {
                try {
                    SpendingItemDTO item = spendingExport.getItem();
                    if (item != null) {
                        item.setFiscalYearId(fyId);
                        SpendingItemDTO created = spendingItemService.createSpendingItem(item, username);
                        // Import invoices for this spending item
                        importSpendingInvoices(created.getId(), spendingExport.getInvoices(), username);
                        spendingCount++;
                    }
                } catch (Exception e) {
                    logger.warning("Failed to import spending item: " + e.getMessage());
                }
            }
        }
        resultMetadata.setSpendingItemCount(spendingCount);
        logger.info("Imported " + spendingCount + " spending items");

        // Import procurement items
        int procurementCount = 0;
        if (exportData.getProcurementItems() != null) {
            for (ProcurementItemExportDTO procExport : exportData.getProcurementItems()) {
                try {
                    ProcurementItemDTO item = procExport.getItem();
                    if (item != null) {
                        item.setFiscalYearId(fyId);
                        ProcurementItemDTO created =
                                procurementItemService.createProcurementItem(item, username);
                        // Import events for this procurement item
                        importProcurementEvents(created.getId(), procExport.getEvents(), username);
                        // Import quotes for this procurement item
                        importProcurementQuotes(created.getId(), procExport.getQuotes(), username);
                        procurementCount++;
                    }
                } catch (Exception e) {
                    logger.warning("Failed to import procurement item: " + e.getMessage());
                }
            }
        }
        resultMetadata.setProcurementItemCount(procurementCount);
        logger.info("Imported " + procurementCount + " procurement items");

        result.setMetadata(resultMetadata);
        return result;
    }

    // ============================
    // Private export helpers
    // ============================

    /**
     * Export a spending item with all its invoices and invoice files.
     */
    private SpendingItemExportDTO exportSpendingItem(SpendingItemDTO spendingItem, String username) {
        SpendingItemExportDTO export = new SpendingItemExportDTO();
        export.setItem(spendingItem);

        // Get invoices for this spending item
        List<SpendingInvoiceDTO> invoices =
                spendingInvoiceService.getInvoicesBySpendingItemId(spendingItem.getId(), username);

        List<SpendingInvoiceExportDTO> invoiceExports = new ArrayList<>();
        for (SpendingInvoiceDTO invoice : invoices) {
            SpendingInvoiceExportDTO invoiceExport = new SpendingInvoiceExportDTO();
            invoiceExport.setInvoice(invoice);

            // Get files for this invoice
            List<SpendingInvoiceFileDTO> fileMetas =
                    spendingInvoiceService.getFiles(invoice.getId(), username);
            List<FileExportDTO> fileExports = new ArrayList<>();
            for (SpendingInvoiceFileDTO fileMeta : fileMetas) {
                fileExports.add(exportInvoiceFile(fileMeta, username));
            }
            invoiceExport.setFiles(fileExports);
            invoiceExports.add(invoiceExport);
        }
        export.setInvoices(invoiceExports);
        return export;
    }

    /**
     * Export a single invoice file with base64-encoded content.
     */
    private FileExportDTO exportInvoiceFile(SpendingInvoiceFileDTO fileMeta, String username) {
        FileExportDTO fileExport = new FileExportDTO();
        fileExport.setId(fileMeta.getId());
        fileExport.setFileName(fileMeta.getFileName());
        fileExport.setContentType(fileMeta.getContentType());
        fileExport.setFileSize(fileMeta.getFileSize() != null ? fileMeta.getFileSize() : 0L);
        fileExport.setDescription(fileMeta.getDescription());

        try {
            byte[] content = spendingInvoiceService.getFileContent(fileMeta.getId(), username);
            fileExport.setBase64Content(Base64.getEncoder().encodeToString(content));
        } catch (Exception e) {
            logger.warning("Failed to export invoice file " + fileMeta.getId() + ": " + e.getMessage());
            fileExport.setBase64Content(null);
        }
        return fileExport;
    }

    /**
     * Export a procurement item with all its events, quotes, and files.
     */
    private ProcurementItemExportDTO exportProcurementItem(ProcurementItemDTO procurementItem,
                                                            String username) {
        ProcurementItemExportDTO export = new ProcurementItemExportDTO();

        // Get the full item with quotes
        Optional<ProcurementItemDTO> fullItemOpt =
                procurementItemService.getProcurementItemWithQuotes(procurementItem.getId(), username);
        ProcurementItemDTO fullItem = fullItemOpt.orElse(procurementItem);
        export.setItem(fullItem);

        // Export events
        List<ProcurementEventDTO> events =
                procurementEventService.getEventsForProcurementItem(procurementItem.getId(), username);
        List<ProcurementEventExportDTO> eventExports = new ArrayList<>();
        for (ProcurementEventDTO event : events) {
            eventExports.add(exportProcurementEvent(event, username));
        }
        export.setEvents(eventExports);

        // Export quotes (from the full item)
        List<ProcurementQuoteExportDTO> quoteExports = new ArrayList<>();
        if (fullItem.getQuotes() != null) {
            for (ProcurementQuoteDTO quote : fullItem.getQuotes()) {
                quoteExports.add(exportProcurementQuote(quote, username));
            }
        }
        export.setQuotes(quoteExports);

        return export;
    }

    /**
     * Export a procurement event with base64-encoded file content.
     */
    private ProcurementEventExportDTO exportProcurementEvent(ProcurementEventDTO event, String username) {
        ProcurementEventExportDTO export = new ProcurementEventExportDTO();
        export.setEvent(event);

        // Get event files
        List<ProcurementEventFileDTO> fileMetas =
                procurementEventService.getEventFiles(event.getId(), username);
        List<FileExportDTO> fileExports = new ArrayList<>();
        for (ProcurementEventFileDTO fileMeta : fileMetas) {
            fileExports.add(exportEventFile(fileMeta, username));
        }
        export.setFiles(fileExports);
        return export;
    }

    /**
     * Export a single event file with base64-encoded content.
     */
    private FileExportDTO exportEventFile(ProcurementEventFileDTO fileMeta, String username) {
        FileExportDTO fileExport = new FileExportDTO();
        fileExport.setId(fileMeta.getId());
        fileExport.setFileName(fileMeta.getFileName());
        fileExport.setContentType(fileMeta.getContentType());
        fileExport.setFileSize(fileMeta.getFileSize() != null ? fileMeta.getFileSize() : 0L);
        fileExport.setDescription(fileMeta.getDescription());

        try {
            ProcurementEventFile eventFile =
                    procurementEventService.getEventFile(fileMeta.getId(), username);
            if (eventFile.getContent() != null) {
                fileExport.setBase64Content(Base64.getEncoder().encodeToString(eventFile.getContent()));
            }
        } catch (Exception e) {
            logger.warning("Failed to export event file " + fileMeta.getId() + ": " + e.getMessage());
            fileExport.setBase64Content(null);
        }
        return fileExport;
    }

    /**
     * Export a procurement quote with base64-encoded file content.
     */
    private ProcurementQuoteExportDTO exportProcurementQuote(ProcurementQuoteDTO quote, String username) {
        ProcurementQuoteExportDTO export = new ProcurementQuoteExportDTO();
        export.setQuote(quote);

        // Get quote files
        List<ProcurementQuoteFileDTO> fileMetas =
                procurementItemService.getFilesByQuoteId(quote.getId(), username);
        List<FileExportDTO> fileExports = new ArrayList<>();
        for (ProcurementQuoteFileDTO fileMeta : fileMetas) {
            fileExports.add(exportQuoteFile(fileMeta, username));
        }
        export.setFiles(fileExports);
        return export;
    }

    /**
     * Export a single quote file with base64-encoded content.
     */
    private FileExportDTO exportQuoteFile(ProcurementQuoteFileDTO fileMeta, String username) {
        FileExportDTO fileExport = new FileExportDTO();
        fileExport.setId(fileMeta.getId());
        fileExport.setFileName(fileMeta.getFileName());
        fileExport.setContentType(fileMeta.getContentType());
        fileExport.setFileSize(fileMeta.getFileSize() != null ? fileMeta.getFileSize() : 0L);
        fileExport.setDescription(fileMeta.getDescription());

        try {
            byte[] content = procurementItemService.getFileContent(fileMeta.getId(), username);
            fileExport.setBase64Content(Base64.getEncoder().encodeToString(content));
        } catch (Exception e) {
            logger.warning("Failed to export quote file " + fileMeta.getId() + ": " + e.getMessage());
            fileExport.setBase64Content(null);
        }
        return fileExport;
    }

    // ============================
    // Private import helpers
    // ============================

    /**
     * Import invoices for a spending item.
     */
    private void importSpendingInvoices(Long spendingItemId,
                                         List<SpendingInvoiceExportDTO> invoiceExports,
                                         String username) {
        if (invoiceExports == null) {
            return;
        }
        for (SpendingInvoiceExportDTO invoiceExport : invoiceExports) {
            try {
                SpendingInvoiceDTO invoice = invoiceExport.getInvoice();
                if (invoice != null) {
                    SpendingInvoiceDTO created =
                            spendingInvoiceService.createInvoice(spendingItemId, invoice, username);
                    // Import files for this invoice
                    importInvoiceFiles(created.getId(), invoiceExport.getFiles(), username);
                }
            } catch (Exception e) {
                logger.warning("Failed to import invoice: " + e.getMessage());
            }
        }
    }

    /**
     * Import files for a spending invoice from base64 content.
     */
    private void importInvoiceFiles(Long invoiceId, List<FileExportDTO> fileExports, String username) {
        if (fileExports == null) {
            return;
        }
        for (FileExportDTO fileExport : fileExports) {
            try {
                if (fileExport.getBase64Content() != null && !fileExport.getBase64Content().isEmpty()) {
                    byte[] content = Base64.getDecoder().decode(fileExport.getBase64Content());
                    var multipartFile = new com.myrc.util.InMemoryMultipartFile(
                                    "file",
                                    fileExport.getFileName(),
                                    fileExport.getContentType(),
                                    content);
                    spendingInvoiceService.uploadFile(invoiceId, multipartFile,
                            fileExport.getDescription(), username);
                }
            } catch (Exception e) {
                logger.warning("Failed to import invoice file '" + fileExport.getFileName() + "': "
                        + e.getMessage());
            }
        }
    }

    /**
     * Import events for a procurement item.
     */
    private void importProcurementEvents(Long procurementItemId,
                                          List<ProcurementEventExportDTO> eventExports,
                                          String username) {
        if (eventExports == null) {
            return;
        }
        for (ProcurementEventExportDTO eventExport : eventExports) {
            try {
                ProcurementEventDTO event = eventExport.getEvent();
                if (event != null) {
                    ProcurementEventDTO created =
                            procurementEventService.createEvent(procurementItemId, event, username);
                    // Import files for this event
                    importEventFiles(created.getId(), eventExport.getFiles(), username);
                }
            } catch (Exception e) {
                logger.warning("Failed to import procurement event: " + e.getMessage());
            }
        }
    }

    /**
     * Import files for a procurement event from base64 content.
     */
    private void importEventFiles(Long eventId, List<FileExportDTO> fileExports, String username) {
        if (fileExports == null) {
            return;
        }
        for (FileExportDTO fileExport : fileExports) {
            try {
                if (fileExport.getBase64Content() != null && !fileExport.getBase64Content().isEmpty()) {
                    byte[] content = Base64.getDecoder().decode(fileExport.getBase64Content());
                    var multipartFile = new com.myrc.util.InMemoryMultipartFile(
                                    "file",
                                    fileExport.getFileName(),
                                    fileExport.getContentType(),
                                    content);
                    procurementEventService.uploadEventFile(eventId, multipartFile,
                            fileExport.getDescription(), username);
                }
            } catch (Exception e) {
                logger.warning("Failed to import event file '" + fileExport.getFileName() + "': "
                        + e.getMessage());
            }
        }
    }

    /**
     * Import quotes for a procurement item.
     */
    private void importProcurementQuotes(Long procurementItemId,
                                          List<ProcurementQuoteExportDTO> quoteExports,
                                          String username) {
        if (quoteExports == null) {
            return;
        }
        for (ProcurementQuoteExportDTO quoteExport : quoteExports) {
            try {
                ProcurementQuoteDTO quote = quoteExport.getQuote();
                if (quote != null) {
                    ProcurementQuoteDTO created =
                            procurementItemService.createQuote(procurementItemId, quote, username);
                    // Import files for this quote
                    importQuoteFiles(created.getId(), quoteExport.getFiles(), username);
                }
            } catch (Exception e) {
                logger.warning("Failed to import procurement quote: " + e.getMessage());
            }
        }
    }

    /**
     * Import files for a procurement quote from base64 content.
     */
    private void importQuoteFiles(Long quoteId, List<FileExportDTO> fileExports, String username) {
        if (fileExports == null) {
            return;
        }
        for (FileExportDTO fileExport : fileExports) {
            try {
                if (fileExport.getBase64Content() != null && !fileExport.getBase64Content().isEmpty()) {
                    byte[] content = Base64.getDecoder().decode(fileExport.getBase64Content());
                    var multipartFile = new com.myrc.util.InMemoryMultipartFile(
                                    "file",
                                    fileExport.getFileName(),
                                    fileExport.getContentType(),
                                    content);
                    procurementItemService.uploadFile(quoteId, multipartFile,
                            fileExport.getDescription(), username);
                }
            } catch (Exception e) {
                logger.warning("Failed to import quote file '" + fileExport.getFileName() + "': "
                        + e.getMessage());
            }
        }
    }
}
