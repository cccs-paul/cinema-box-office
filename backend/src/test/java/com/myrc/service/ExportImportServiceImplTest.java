/*
 * myRC - Export/Import Service Implementation Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-02-15
 * Version: 1.0.0
 */
package com.myrc.service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

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
 * Unit tests for ExportImportServiceImpl.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-15
 */
@ExtendWith(MockitoExtension.class)
class ExportImportServiceImplTest {

    @Mock
    private FundingItemService fundingItemService;
    @Mock
    private SpendingItemService spendingItemService;
    @Mock
    private SpendingInvoiceService spendingInvoiceService;
    @Mock
    private ProcurementItemService procurementItemService;
    @Mock
    private ProcurementEventService procurementEventService;
    @Mock
    private FiscalYearService fiscalYearService;
    @Mock
    private ResponsibilityCentreService responsibilityCentreService;

    private ExportImportServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ExportImportServiceImpl(
                fundingItemService,
                spendingItemService,
                spendingInvoiceService,
                procurementItemService,
                procurementEventService,
                fiscalYearService,
                responsibilityCentreService
        );
    }

    @Test
    @DisplayName("Should create service successfully")
    void testServiceCreation() {
        assertNotNull(service);
    }

    @Nested
    @DisplayName("exportData Tests")
    class ExportDataTests {

        @Test
        @DisplayName("Should export all data types successfully")
        void testExportDataSuccess() {
            // Setup
            FiscalYearDTO fy = createFiscalYear();
            when(fiscalYearService.getFiscalYearById(eq(2L), eq("testuser")))
                    .thenReturn(Optional.of(fy));

            FundingItemDTO fundingItem = createFundingItem();
            when(fundingItemService.getFundingItemsByFiscalYearId(eq(2L), eq("testuser")))
                    .thenReturn(List.of(fundingItem));

            SpendingItemDTO spendingItem = createSpendingItem();
            when(spendingItemService.getSpendingItemsByFiscalYearId(eq(2L), eq("testuser")))
                    .thenReturn(List.of(spendingItem));
            when(spendingInvoiceService.getInvoicesBySpendingItemId(eq(1L), eq("testuser")))
                    .thenReturn(List.of());

            ProcurementItemDTO procItem = createProcurementItem();
            when(procurementItemService.getProcurementItemsByFiscalYearId(eq(2L), eq("testuser")))
                    .thenReturn(List.of(procItem));
            when(procurementItemService.getProcurementItemWithQuotes(eq(1L), eq("testuser")))
                    .thenReturn(Optional.of(procItem));
            when(procurementEventService.getEventsForProcurementItem(eq(1L), eq("testuser")))
                    .thenReturn(List.of());

            // Execute
            ExportDataDTO result = service.exportData(1L, 2L, "testuser");

            // Verify
            assertNotNull(result);
            assertNotNull(result.getMetadata());
            assertEquals("1.0.0", result.getMetadata().getExportVersion());
            assertEquals("testuser", result.getMetadata().getExportedBy());
            assertEquals(1L, result.getMetadata().getResponsibilityCentreId());
            assertEquals(2L, result.getMetadata().getFiscalYearId());
            assertEquals(1, result.getMetadata().getFundingItemCount());
            assertEquals(1, result.getMetadata().getSpendingItemCount());
            assertEquals(1, result.getMetadata().getProcurementItemCount());
            assertEquals(1, result.getFundingItems().size());
            assertEquals(1, result.getSpendingItems().size());
            assertEquals(1, result.getProcurementItems().size());
        }

        @Test
        @DisplayName("Should throw when fiscal year not found")
        void testExportDataFYNotFound() {
            when(fiscalYearService.getFiscalYearById(eq(999L), eq("testuser")))
                    .thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class,
                    () -> service.exportData(1L, 999L, "testuser"));
        }

        @Test
        @DisplayName("Should export empty lists when no data exists")
        void testExportDataEmpty() {
            FiscalYearDTO fy = createFiscalYear();
            when(fiscalYearService.getFiscalYearById(eq(2L), eq("testuser")))
                    .thenReturn(Optional.of(fy));
            when(fundingItemService.getFundingItemsByFiscalYearId(eq(2L), eq("testuser")))
                    .thenReturn(List.of());
            when(spendingItemService.getSpendingItemsByFiscalYearId(eq(2L), eq("testuser")))
                    .thenReturn(List.of());
            when(procurementItemService.getProcurementItemsByFiscalYearId(eq(2L), eq("testuser")))
                    .thenReturn(List.of());

            ExportDataDTO result = service.exportData(1L, 2L, "testuser");

            assertNotNull(result);
            assertTrue(result.getFundingItems().isEmpty());
            assertTrue(result.getSpendingItems().isEmpty());
            assertTrue(result.getProcurementItems().isEmpty());
            assertEquals(0, result.getMetadata().getFundingItemCount());
        }

        @Test
        @DisplayName("Should export spending items with invoices and files")
        void testExportSpendingWithInvoicesAndFiles() {
            FiscalYearDTO fy = createFiscalYear();
            when(fiscalYearService.getFiscalYearById(eq(2L), eq("testuser")))
                    .thenReturn(Optional.of(fy));
            when(fundingItemService.getFundingItemsByFiscalYearId(any(), any()))
                    .thenReturn(List.of());
            when(procurementItemService.getProcurementItemsByFiscalYearId(any(), any()))
                    .thenReturn(List.of());

            SpendingItemDTO spendingItem = createSpendingItem();
            when(spendingItemService.getSpendingItemsByFiscalYearId(eq(2L), eq("testuser")))
                    .thenReturn(List.of(spendingItem));

            SpendingInvoiceDTO invoice = new SpendingInvoiceDTO();
            invoice.setId(10L);
            invoice.setComments("INV-001");
            when(spendingInvoiceService.getInvoicesBySpendingItemId(eq(1L), eq("testuser")))
                    .thenReturn(List.of(invoice));

            SpendingInvoiceFileDTO fileMeta = new SpendingInvoiceFileDTO();
            fileMeta.setId(20L);
            fileMeta.setFileName("receipt.pdf");
            fileMeta.setContentType("application/pdf");
            fileMeta.setFileSize(1024L);
            when(spendingInvoiceService.getFiles(eq(10L), eq("testuser")))
                    .thenReturn(List.of(fileMeta));

            byte[] fileContent = "test content".getBytes(StandardCharsets.UTF_8);
            when(spendingInvoiceService.getFileContent(eq(20L), eq("testuser")))
                    .thenReturn(fileContent);

            ExportDataDTO result = service.exportData(1L, 2L, "testuser");

            assertEquals(1, result.getSpendingItems().size());
            SpendingItemExportDTO spendingExport = result.getSpendingItems().get(0);
            assertEquals(1, spendingExport.getInvoices().size());
            SpendingInvoiceExportDTO invoiceExport = spendingExport.getInvoices().get(0);
            assertEquals("INV-001", invoiceExport.getInvoice().getComments());
            assertEquals(1, invoiceExport.getFiles().size());
            FileExportDTO fileExport = invoiceExport.getFiles().get(0);
            assertEquals("receipt.pdf", fileExport.getFileName());
            assertEquals("application/pdf", fileExport.getContentType());
            assertNotNull(fileExport.getBase64Content());
            // Verify base64 decodes back to original content
            byte[] decoded = Base64.getDecoder().decode(fileExport.getBase64Content());
            assertArrayEquals(fileContent, decoded);
        }

        @Test
        @DisplayName("Should export procurement items with events and files")
        void testExportProcurementWithEventsAndFiles() {
            FiscalYearDTO fy = createFiscalYear();
            when(fiscalYearService.getFiscalYearById(eq(2L), eq("testuser")))
                    .thenReturn(Optional.of(fy));
            when(fundingItemService.getFundingItemsByFiscalYearId(any(), any()))
                    .thenReturn(List.of());
            when(spendingItemService.getSpendingItemsByFiscalYearId(any(), any()))
                    .thenReturn(List.of());

            ProcurementItemDTO procItem = createProcurementItem();
            when(procurementItemService.getProcurementItemsByFiscalYearId(eq(2L), eq("testuser")))
                    .thenReturn(List.of(procItem));
            when(procurementItemService.getProcurementItemWithQuotes(eq(1L), eq("testuser")))
                    .thenReturn(Optional.of(procItem));

            ProcurementEventDTO event = new ProcurementEventDTO();
            event.setId(30L);
            event.setEventType("QUOTE_REQUESTED");
            when(procurementEventService.getEventsForProcurementItem(eq(1L), eq("testuser")))
                    .thenReturn(List.of(event));

            ProcurementEventFileDTO eventFileMeta = new ProcurementEventFileDTO();
            eventFileMeta.setId(40L);
            eventFileMeta.setFileName("spec.docx");
            eventFileMeta.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            eventFileMeta.setFileSize(2048L);
            when(procurementEventService.getEventFiles(eq(30L), eq("testuser")))
                    .thenReturn(List.of(eventFileMeta));

            ProcurementEventFile eventFile = new ProcurementEventFile();
            eventFile.setContent("event content".getBytes(StandardCharsets.UTF_8));
            when(procurementEventService.getEventFile(eq(40L), eq("testuser")))
                    .thenReturn(eventFile);

            ExportDataDTO result = service.exportData(1L, 2L, "testuser");

            assertEquals(1, result.getProcurementItems().size());
            ProcurementItemExportDTO procExport = result.getProcurementItems().get(0);
            assertEquals(1, procExport.getEvents().size());
            ProcurementEventExportDTO eventExport = procExport.getEvents().get(0);
            assertEquals("QUOTE_REQUESTED", eventExport.getEvent().getEventType());
            assertEquals(1, eventExport.getFiles().size());
            assertNotNull(eventExport.getFiles().get(0).getBase64Content());
        }

        @Test
        @DisplayName("Should export procurement items with quotes and files")
        void testExportProcurementWithQuotesAndFiles() {
            FiscalYearDTO fy = createFiscalYear();
            when(fiscalYearService.getFiscalYearById(eq(2L), eq("testuser")))
                    .thenReturn(Optional.of(fy));
            when(fundingItemService.getFundingItemsByFiscalYearId(any(), any()))
                    .thenReturn(List.of());
            when(spendingItemService.getSpendingItemsByFiscalYearId(any(), any()))
                    .thenReturn(List.of());

            ProcurementItemDTO procItem = createProcurementItem();
            ProcurementQuoteDTO quote = new ProcurementQuoteDTO();
            quote.setId(50L);
            quote.setVendorName("Acme Corp");
            procItem.setQuotes(List.of(quote));
            when(procurementItemService.getProcurementItemsByFiscalYearId(eq(2L), eq("testuser")))
                    .thenReturn(List.of(procItem));
            when(procurementItemService.getProcurementItemWithQuotes(eq(1L), eq("testuser")))
                    .thenReturn(Optional.of(procItem));
            when(procurementEventService.getEventsForProcurementItem(eq(1L), eq("testuser")))
                    .thenReturn(List.of());

            ProcurementQuoteFileDTO quoteFileMeta = new ProcurementQuoteFileDTO();
            quoteFileMeta.setId(60L);
            quoteFileMeta.setFileName("quote.pdf");
            quoteFileMeta.setContentType("application/pdf");
            quoteFileMeta.setFileSize(512L);
            when(procurementItemService.getFilesByQuoteId(eq(50L), eq("testuser")))
                    .thenReturn(List.of(quoteFileMeta));

            byte[] quoteContent = "quote content".getBytes(StandardCharsets.UTF_8);
            when(procurementItemService.getFileContent(eq(60L), eq("testuser")))
                    .thenReturn(quoteContent);

            ExportDataDTO result = service.exportData(1L, 2L, "testuser");

            ProcurementItemExportDTO procExport = result.getProcurementItems().get(0);
            assertEquals(1, procExport.getQuotes().size());
            ProcurementQuoteExportDTO quoteExport = procExport.getQuotes().get(0);
            assertEquals("Acme Corp", quoteExport.getQuote().getVendorName());
            assertEquals(1, quoteExport.getFiles().size());
            assertNotNull(quoteExport.getFiles().get(0).getBase64Content());
        }

        @Test
        @DisplayName("Should handle file content retrieval errors gracefully")
        void testExportFileContentError() {
            FiscalYearDTO fy = createFiscalYear();
            when(fiscalYearService.getFiscalYearById(eq(2L), eq("testuser")))
                    .thenReturn(Optional.of(fy));
            when(fundingItemService.getFundingItemsByFiscalYearId(any(), any()))
                    .thenReturn(List.of());
            when(procurementItemService.getProcurementItemsByFiscalYearId(any(), any()))
                    .thenReturn(List.of());

            SpendingItemDTO spendingItem = createSpendingItem();
            when(spendingItemService.getSpendingItemsByFiscalYearId(eq(2L), eq("testuser")))
                    .thenReturn(List.of(spendingItem));

            SpendingInvoiceDTO invoice = new SpendingInvoiceDTO();
            invoice.setId(10L);
            when(spendingInvoiceService.getInvoicesBySpendingItemId(eq(1L), eq("testuser")))
                    .thenReturn(List.of(invoice));

            SpendingInvoiceFileDTO fileMeta = new SpendingInvoiceFileDTO();
            fileMeta.setId(20L);
            fileMeta.setFileName("corrupt.pdf");
            when(spendingInvoiceService.getFiles(eq(10L), eq("testuser")))
                    .thenReturn(List.of(fileMeta));
            when(spendingInvoiceService.getFileContent(eq(20L), eq("testuser")))
                    .thenThrow(new RuntimeException("File content corrupted"));

            ExportDataDTO result = service.exportData(1L, 2L, "testuser");

            // Should still export successfully, just with null file content
            assertNotNull(result);
            SpendingInvoiceExportDTO invoiceExport = result.getSpendingItems().get(0).getInvoices().get(0);
            assertNull(invoiceExport.getFiles().get(0).getBase64Content());
        }
    }

    @Nested
    @DisplayName("importData Tests")
    class ImportDataTests {

        @Test
        @DisplayName("Should import all data types successfully")
        void testImportDataSuccess() {
            FiscalYearDTO fy = createFiscalYear();
            when(fiscalYearService.getFiscalYearById(eq(2L), eq("testuser")))
                    .thenReturn(Optional.of(fy));

            FundingItemDTO fundingItem = createFundingItem();
            when(fundingItemService.createFundingItem(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(fundingItem);

            SpendingItemDTO spendingItem = createSpendingItem();
            when(spendingItemService.createSpendingItem(any(), any()))
                    .thenReturn(spendingItem);

            ProcurementItemDTO procItem = createProcurementItem();
            when(procurementItemService.createProcurementItem(any(), any()))
                    .thenReturn(procItem);

            ExportDataDTO importData = createImportData();
            ExportDataDTO result = service.importData(1L, 2L, importData, "testuser");

            assertNotNull(result);
            assertNotNull(result.getMetadata());
            assertEquals(1, result.getMetadata().getFundingItemCount());
            assertEquals(1, result.getMetadata().getSpendingItemCount());
            assertEquals(1, result.getMetadata().getProcurementItemCount());
        }

        @Test
        @DisplayName("Should throw when fiscal year not found during import")
        void testImportDataFYNotFound() {
            when(fiscalYearService.getFiscalYearById(eq(999L), eq("testuser")))
                    .thenReturn(Optional.empty());

            ExportDataDTO importData = createImportData();
            assertThrows(IllegalArgumentException.class,
                    () -> service.importData(1L, 999L, importData, "testuser"));
        }

        @Test
        @DisplayName("Should handle import errors gracefully per item")
        void testImportDataPartialFailure() {
            FiscalYearDTO fy = createFiscalYear();
            when(fiscalYearService.getFiscalYearById(eq(2L), eq("testuser")))
                    .thenReturn(Optional.of(fy));

            // First funding item succeeds, second fails
            when(fundingItemService.createFundingItem(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(createFundingItem())
                    .thenThrow(new RuntimeException("Duplicate name"));

            FundingItemDTO fi1 = createFundingItem();
            fi1.setName("Funding 1");
            FundingItemDTO fi2 = createFundingItem();
            fi2.setName("Funding 2");

            ExportDataDTO importData = new ExportDataDTO();
            importData.setFundingItems(List.of(fi1, fi2));

            ExportDataDTO result = service.importData(1L, 2L, importData, "testuser");

            // Only 1 should have succeeded
            assertEquals(1, result.getMetadata().getFundingItemCount());
        }

        @Test
        @DisplayName("Should import with null item lists without error")
        void testImportDataNullLists() {
            FiscalYearDTO fy = createFiscalYear();
            when(fiscalYearService.getFiscalYearById(eq(2L), eq("testuser")))
                    .thenReturn(Optional.of(fy));

            ExportDataDTO importData = new ExportDataDTO();
            importData.setFundingItems(null);
            importData.setSpendingItems(null);
            importData.setProcurementItems(null);

            ExportDataDTO result = service.importData(1L, 2L, importData, "testuser");

            assertNotNull(result);
            assertEquals(0, result.getMetadata().getFundingItemCount());
            assertEquals(0, result.getMetadata().getSpendingItemCount());
            assertEquals(0, result.getMetadata().getProcurementItemCount());
        }

        @Test
        @DisplayName("Should import spending with invoices and files")
        void testImportSpendingWithInvoicesAndFiles() {
            FiscalYearDTO fy = createFiscalYear();
            when(fiscalYearService.getFiscalYearById(eq(2L), eq("testuser")))
                    .thenReturn(Optional.of(fy));

            SpendingItemDTO createdSpending = createSpendingItem();
            createdSpending.setId(100L);
            when(spendingItemService.createSpendingItem(any(), eq("testuser")))
                    .thenReturn(createdSpending);

            SpendingInvoiceDTO createdInvoice = new SpendingInvoiceDTO();
            createdInvoice.setId(200L);
            when(spendingInvoiceService.createInvoice(eq(100L), any(), eq("testuser")))
                    .thenReturn(createdInvoice);

            SpendingInvoiceFileDTO uploadedFile = new SpendingInvoiceFileDTO();
            uploadedFile.setId(300L);
            when(spendingInvoiceService.uploadFile(eq(200L), any(), any(), eq("testuser")))
                    .thenReturn(uploadedFile);

            ExportDataDTO importData = createImportDataWithFiles();
            ExportDataDTO result = service.importData(1L, 2L, importData, "testuser");

            assertNotNull(result);
            assertEquals(1, result.getMetadata().getSpendingItemCount());
            verify(spendingInvoiceService).createInvoice(eq(100L), any(), eq("testuser"));
            verify(spendingInvoiceService).uploadFile(eq(200L), any(), any(), eq("testuser"));
        }

        @Test
        @DisplayName("Should import procurement with events and quotes")
        void testImportProcurementWithEventsAndQuotes() {
            FiscalYearDTO fy = createFiscalYear();
            when(fiscalYearService.getFiscalYearById(eq(2L), eq("testuser")))
                    .thenReturn(Optional.of(fy));

            ProcurementItemDTO createdProc = createProcurementItem();
            createdProc.setId(100L);
            when(procurementItemService.createProcurementItem(any(), eq("testuser")))
                    .thenReturn(createdProc);

            ProcurementEventDTO createdEvent = new ProcurementEventDTO();
            createdEvent.setId(200L);
            when(procurementEventService.createEvent(eq(100L), any(), eq("testuser")))
                    .thenReturn(createdEvent);

            ProcurementQuoteDTO createdQuote = new ProcurementQuoteDTO();
            createdQuote.setId(300L);
            when(procurementItemService.createQuote(eq(100L), any(), eq("testuser")))
                    .thenReturn(createdQuote);

            ExportDataDTO importData = createImportDataWithFiles();
            ExportDataDTO result = service.importData(1L, 2L, importData, "testuser");

            assertEquals(1, result.getMetadata().getProcurementItemCount());
            verify(procurementEventService).createEvent(eq(100L), any(), eq("testuser"));
            verify(procurementItemService).createQuote(eq(100L), any(), eq("testuser"));
        }
    }

    @Nested
    @DisplayName("ExportDataDTO Tests")
    class ExportDataDTOTests {

        @Test
        @DisplayName("Should create ExportDataDTO with defaults")
        void testExportDataDTODefaults() {
            ExportDataDTO dto = new ExportDataDTO();
            assertNotNull(dto.getFundingItems());
            assertNotNull(dto.getSpendingItems());
            assertNotNull(dto.getProcurementItems());
            assertTrue(dto.getFundingItems().isEmpty());
        }

        @Test
        @DisplayName("Should create ExportMetadata")
        void testExportMetadata() {
            ExportMetadata meta = new ExportMetadata();
            meta.setExportVersion("1.0.0");
            meta.setExportedAt(LocalDateTime.now());
            meta.setExportedBy("user");
            meta.setResponsibilityCentreId(1L);
            meta.setResponsibilityCentreName("RC");
            meta.setFiscalYearId(2L);
            meta.setFiscalYearName("FY");
            meta.setFundingItemCount(3);
            meta.setSpendingItemCount(4);
            meta.setProcurementItemCount(5);

            assertEquals("1.0.0", meta.getExportVersion());
            assertEquals("user", meta.getExportedBy());
            assertEquals(1L, meta.getResponsibilityCentreId());
            assertEquals("RC", meta.getResponsibilityCentreName());
            assertEquals(2L, meta.getFiscalYearId());
            assertEquals("FY", meta.getFiscalYearName());
            assertEquals(3, meta.getFundingItemCount());
            assertEquals(4, meta.getSpendingItemCount());
            assertEquals(5, meta.getProcurementItemCount());
        }

        @Test
        @DisplayName("Should create FileExportDTO")
        void testFileExportDTO() {
            FileExportDTO dto = new FileExportDTO();
            dto.setId(1L);
            dto.setFileName("test.pdf");
            dto.setContentType("application/pdf");
            dto.setFileSize(1024L);
            dto.setDescription("Test file");
            dto.setBase64Content("dGVzdA==");

            assertEquals(1L, dto.getId());
            assertEquals("test.pdf", dto.getFileName());
            assertEquals("application/pdf", dto.getContentType());
            assertEquals(1024L, dto.getFileSize());
            assertEquals("Test file", dto.getDescription());
            assertEquals("dGVzdA==", dto.getBase64Content());
        }

        @Test
        @DisplayName("Should create nested export DTOs")
        void testNestedExportDTOs() {
            SpendingItemExportDTO spendingExport = new SpendingItemExportDTO();
            assertNotNull(spendingExport.getInvoices());

            SpendingInvoiceExportDTO invoiceExport = new SpendingInvoiceExportDTO();
            assertNotNull(invoiceExport.getFiles());

            ProcurementItemExportDTO procExport = new ProcurementItemExportDTO();
            assertNotNull(procExport.getEvents());
            assertNotNull(procExport.getQuotes());

            ProcurementEventExportDTO eventExport = new ProcurementEventExportDTO();
            assertNotNull(eventExport.getFiles());

            ProcurementQuoteExportDTO quoteExport = new ProcurementQuoteExportDTO();
            assertNotNull(quoteExport.getFiles());
        }
    }

    @Nested
    @DisplayName("InMemoryMultipartFile Tests")
    class InMemoryMultipartFileTests {

        @Test
        @DisplayName("Should create InMemoryMultipartFile correctly")
        void testInMemoryMultipartFile() throws Exception {
            byte[] content = "test content".getBytes(StandardCharsets.UTF_8);
            var file = new com.myrc.util.InMemoryMultipartFile(
                    "file", "test.pdf", "application/pdf", content);

            assertEquals("file", file.getName());
            assertEquals("test.pdf", file.getOriginalFilename());
            assertEquals("application/pdf", file.getContentType());
            assertEquals(content.length, file.getSize());
            assertFalse(file.isEmpty());
            assertArrayEquals(content, file.getBytes());
            assertNotNull(file.getInputStream());
        }

        @Test
        @DisplayName("Should handle null content")
        void testInMemoryMultipartFileNullContent() throws Exception {
            var file = new com.myrc.util.InMemoryMultipartFile(
                    "file", "empty.txt", "text/plain", null);

            assertTrue(file.isEmpty());
            assertEquals(0, file.getSize());
        }

        @Test
        @DisplayName("Should throw on transferTo")
        void testInMemoryMultipartFileTransferTo() {
            var file = new com.myrc.util.InMemoryMultipartFile(
                    "file", "test.txt", "text/plain", new byte[0]);

            assertThrows(UnsupportedOperationException.class,
                    () -> file.transferTo(new java.io.File("/tmp/test")));
        }
    }

    // ============================
    // Helper methods
    // ============================

    private FiscalYearDTO createFiscalYear() {
        FiscalYearDTO fy = new FiscalYearDTO();
        fy.setId(2L);
        fy.setName("FY 2025-2026");
        fy.setResponsibilityCentreId(1L);
        fy.setResponsibilityCentreName("Test RC");
        return fy;
    }

    private FundingItemDTO createFundingItem() {
        FundingItemDTO item = new FundingItemDTO();
        item.setId(1L);
        item.setName("Test Funding");
        item.setDescription("Test Description");
        item.setSource("BUSINESS_PLAN");
        item.setCurrency("CAD");
        item.setExchangeRate(BigDecimal.ONE);
        item.setFiscalYearId(2L);
        return item;
    }

    private SpendingItemDTO createSpendingItem() {
        SpendingItemDTO item = new SpendingItemDTO();
        item.setId(1L);
        item.setName("Test Spending");
        item.setDescription("Test Description");
        item.setStatus("COMMITTED");
        item.setCurrency("CAD");
        item.setFiscalYearId(2L);
        return item;
    }

    private ProcurementItemDTO createProcurementItem() {
        ProcurementItemDTO item = new ProcurementItemDTO();
        item.setId(1L);
        item.setName("Test Procurement");
        item.setDescription("Test Description");
        item.setCurrentStatus("PLANNING");
        item.setFinalPriceCurrency("CAD");
        item.setFiscalYearId(2L);
        item.setQuotes(new ArrayList<>());
        return item;
    }

    private ExportDataDTO createImportData() {
        ExportDataDTO data = new ExportDataDTO();

        FundingItemDTO fundingItem = createFundingItem();
        data.setFundingItems(List.of(fundingItem));

        SpendingItemExportDTO spendingExport = new SpendingItemExportDTO();
        spendingExport.setItem(createSpendingItem());
        data.setSpendingItems(List.of(spendingExport));

        ProcurementItemExportDTO procExport = new ProcurementItemExportDTO();
        procExport.setItem(createProcurementItem());
        data.setProcurementItems(List.of(procExport));

        return data;
    }

    private ExportDataDTO createImportDataWithFiles() {
        ExportDataDTO data = new ExportDataDTO();

        // Spending with invoice and file
        SpendingItemExportDTO spendingExport = new SpendingItemExportDTO();
        spendingExport.setItem(createSpendingItem());
        SpendingInvoiceExportDTO invoiceExport = new SpendingInvoiceExportDTO();
        SpendingInvoiceDTO invoice = new SpendingInvoiceDTO();
        invoice.setComments("INV-001");
        invoiceExport.setInvoice(invoice);
        FileExportDTO invoiceFile = new FileExportDTO();
        invoiceFile.setFileName("receipt.pdf");
        invoiceFile.setContentType("application/pdf");
        invoiceFile.setBase64Content(Base64.getEncoder().encodeToString("receipt".getBytes()));
        invoiceExport.setFiles(List.of(invoiceFile));
        spendingExport.setInvoices(List.of(invoiceExport));
        data.setSpendingItems(List.of(spendingExport));

        // Procurement with event and quote
        ProcurementItemExportDTO procExport = new ProcurementItemExportDTO();
        procExport.setItem(createProcurementItem());

        ProcurementEventExportDTO eventExport = new ProcurementEventExportDTO();
        ProcurementEventDTO event = new ProcurementEventDTO();
        event.setEventType("QUOTE_REQUESTED");
        eventExport.setEvent(event);
        FileExportDTO eventFile = new FileExportDTO();
        eventFile.setFileName("spec.docx");
        eventFile.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        eventFile.setBase64Content(Base64.getEncoder().encodeToString("spec".getBytes()));
        eventExport.setFiles(List.of(eventFile));
        procExport.setEvents(List.of(eventExport));

        ProcurementQuoteExportDTO quoteExport = new ProcurementQuoteExportDTO();
        ProcurementQuoteDTO quote = new ProcurementQuoteDTO();
        quote.setVendorName("Acme Corp");
        quoteExport.setQuote(quote);
        FileExportDTO quoteFile = new FileExportDTO();
        quoteFile.setFileName("quote.pdf");
        quoteFile.setContentType("application/pdf");
        quoteFile.setBase64Content(Base64.getEncoder().encodeToString("quote".getBytes()));
        quoteExport.setFiles(List.of(quoteFile));
        procExport.setQuotes(List.of(quoteExport));

        data.setProcurementItems(List.of(procExport));
        return data;
    }
}
