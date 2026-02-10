/*
 * myRC - Export/Import Controller Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-02-15
 * Version: 1.0.0
 */
package com.myrc.controller;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import com.myrc.dto.ExportDataDTO;
import com.myrc.dto.ExportDataDTO.ExportMetadata;
import com.myrc.dto.ExportDataDTO.FileExportDTO;
import com.myrc.dto.ExportDataDTO.ProcurementEventExportDTO;
import com.myrc.dto.ExportDataDTO.ProcurementItemExportDTO;
import com.myrc.dto.ExportDataDTO.ProcurementQuoteExportDTO;
import com.myrc.dto.ExportDataDTO.SpendingInvoiceExportDTO;
import com.myrc.dto.ExportDataDTO.SpendingItemExportDTO;
import com.myrc.dto.FundingItemDTO;
import com.myrc.dto.ProcurementEventDTO;
import com.myrc.dto.ProcurementItemDTO;
import com.myrc.dto.ProcurementQuoteDTO;
import com.myrc.dto.SpendingInvoiceDTO;
import com.myrc.dto.SpendingItemDTO;
import com.myrc.service.ExportImportService;

/**
 * Unit tests for ExportImportController.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-15
 */
@ExtendWith(MockitoExtension.class)
class ExportImportControllerTest {

    @Mock
    private ExportImportService exportImportService;

    private Authentication authentication;
    private ExportImportController controller;

    @BeforeEach
    void setUp() {
        controller = new ExportImportController(exportImportService);
        authentication = createAuthentication("testuser");
    }

    private Authentication createAuthentication(String username) {
        return new Authentication() {
            @Override
            public String getName() { return username; }
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() { return Collections.emptyList(); }
            @Override
            public Object getCredentials() { return null; }
            @Override
            public Object getDetails() { return null; }
            @Override
            public Object getPrincipal() { return username; }
            @Override
            public boolean isAuthenticated() { return true; }
            @Override
            public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException { }
        };
    }

    @Test
    @DisplayName("Should create controller successfully")
    void testControllerCreation() {
        assertNotNull(controller);
    }

    @Nested
    @DisplayName("exportData Tests")
    class ExportDataTests {

        @Test
        @DisplayName("Should export data successfully")
        void testExportDataSuccess() {
            ExportDataDTO exportData = createSampleExportData();
            when(exportImportService.exportData(eq(1L), eq(2L), eq("testuser")))
                    .thenReturn(exportData);

            ResponseEntity<?> response = controller.exportData(1L, 2L, authentication);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody() instanceof ExportDataDTO);
            ExportDataDTO body = (ExportDataDTO) response.getBody();
            assertEquals(1, body.getFundingItems().size());
            assertEquals(1, body.getSpendingItems().size());
            assertEquals(1, body.getProcurementItems().size());
        }

        @Test
        @DisplayName("Should return 404 when fiscal year not found")
        void testExportDataNotFound() {
            when(exportImportService.exportData(eq(1L), eq(999L), eq("testuser")))
                    .thenThrow(new IllegalArgumentException("Fiscal year not found or access denied: 999"));

            ResponseEntity<?> response = controller.exportData(1L, 999L, authentication);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        @DisplayName("Should return 403 when access denied")
        void testExportDataAccessDenied() {
            when(exportImportService.exportData(eq(1L), eq(2L), eq("testuser")))
                    .thenThrow(new IllegalArgumentException("Access denied"));

            ResponseEntity<?> response = controller.exportData(1L, 2L, authentication);

            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        @Test
        @DisplayName("Should return 500 on unexpected error")
        void testExportDataInternalError() {
            when(exportImportService.exportData(eq(1L), eq(2L), eq("testuser")))
                    .thenThrow(new RuntimeException("Database error"));

            ResponseEntity<?> response = controller.exportData(1L, 2L, authentication);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        }

        @Test
        @DisplayName("Should use default username when authentication is null")
        void testExportDataNullAuth() {
            ExportDataDTO exportData = createSampleExportData();
            when(exportImportService.exportData(eq(1L), eq(2L), eq("default-user")))
                    .thenReturn(exportData);

            ResponseEntity<?> response = controller.exportData(1L, 2L, null);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(exportImportService).exportData(1L, 2L, "default-user");
        }

        @Test
        @DisplayName("Should export empty data when no items exist")
        void testExportDataEmpty() {
            ExportDataDTO exportData = new ExportDataDTO();
            ExportMetadata metadata = new ExportMetadata();
            metadata.setExportVersion("1.0.0");
            metadata.setFundingItemCount(0);
            metadata.setSpendingItemCount(0);
            metadata.setProcurementItemCount(0);
            exportData.setMetadata(metadata);

            when(exportImportService.exportData(eq(1L), eq(2L), eq("testuser")))
                    .thenReturn(exportData);

            ResponseEntity<?> response = controller.exportData(1L, 2L, authentication);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            ExportDataDTO body = (ExportDataDTO) response.getBody();
            assertNotNull(body);
            assertEquals(0, body.getMetadata().getFundingItemCount());
            assertTrue(body.getFundingItems().isEmpty());
        }

        @Test
        @DisplayName("Should export data with file attachments")
        void testExportDataWithFiles() {
            ExportDataDTO exportData = createExportDataWithFiles();
            when(exportImportService.exportData(eq(1L), eq(2L), eq("testuser")))
                    .thenReturn(exportData);

            ResponseEntity<?> response = controller.exportData(1L, 2L, authentication);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            ExportDataDTO body = (ExportDataDTO) response.getBody();
            assertNotNull(body);
            // Verify spending item has invoice with files
            SpendingItemExportDTO spendingExport = body.getSpendingItems().get(0);
            assertFalse(spendingExport.getInvoices().isEmpty());
            assertFalse(spendingExport.getInvoices().get(0).getFiles().isEmpty());
            assertNotNull(spendingExport.getInvoices().get(0).getFiles().get(0).getBase64Content());
        }
    }

    @Nested
    @DisplayName("importData Tests")
    class ImportDataTests {

        @Test
        @DisplayName("Should import data successfully")
        void testImportDataSuccess() {
            ExportDataDTO importData = createSampleExportData();
            ExportDataDTO result = new ExportDataDTO();
            ExportMetadata meta = new ExportMetadata();
            meta.setFundingItemCount(1);
            meta.setSpendingItemCount(1);
            meta.setProcurementItemCount(1);
            result.setMetadata(meta);

            when(exportImportService.importData(eq(1L), eq(2L), any(ExportDataDTO.class), eq("testuser")))
                    .thenReturn(result);

            ResponseEntity<?> response = controller.importData(1L, 2L, authentication, importData);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
        }

        @Test
        @DisplayName("Should return 400 when import data is null")
        void testImportDataNull() {
            ResponseEntity<?> response = controller.importData(1L, 2L, authentication, null);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("Should return 404 when fiscal year not found during import")
        void testImportDataNotFound() {
            ExportDataDTO importData = createSampleExportData();
            when(exportImportService.importData(eq(1L), eq(999L), any(), eq("testuser")))
                    .thenThrow(new IllegalArgumentException("Fiscal year not found or access denied: 999"));

            ResponseEntity<?> response = controller.importData(1L, 999L, authentication, importData);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        @DisplayName("Should return 403 when access denied during import")
        void testImportDataAccessDenied() {
            ExportDataDTO importData = createSampleExportData();
            when(exportImportService.importData(eq(1L), eq(2L), any(), eq("testuser")))
                    .thenThrow(new IllegalArgumentException("User does not have write access"));

            ResponseEntity<?> response = controller.importData(1L, 2L, authentication, importData);

            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        @Test
        @DisplayName("Should return 500 on unexpected error during import")
        void testImportDataInternalError() {
            ExportDataDTO importData = createSampleExportData();
            when(exportImportService.importData(eq(1L), eq(2L), any(), eq("testuser")))
                    .thenThrow(new RuntimeException("Database connection error"));

            ResponseEntity<?> response = controller.importData(1L, 2L, authentication, importData);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        }

        @Test
        @DisplayName("Should use default username when authentication is null during import")
        void testImportDataNullAuth() {
            ExportDataDTO importData = createSampleExportData();
            ExportDataDTO result = new ExportDataDTO();
            result.setMetadata(new ExportMetadata());

            when(exportImportService.importData(eq(1L), eq(2L), any(), eq("default-user")))
                    .thenReturn(result);

            ResponseEntity<?> response = controller.importData(1L, 2L, null, importData);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(exportImportService).importData(eq(1L), eq(2L), any(), eq("default-user"));
        }

        @Test
        @DisplayName("Should import data with file attachments")
        void testImportDataWithFiles() {
            ExportDataDTO importData = createExportDataWithFiles();
            ExportDataDTO result = new ExportDataDTO();
            ExportMetadata meta = new ExportMetadata();
            meta.setFundingItemCount(1);
            meta.setSpendingItemCount(1);
            meta.setProcurementItemCount(1);
            result.setMetadata(meta);

            when(exportImportService.importData(eq(1L), eq(2L), any(), eq("testuser")))
                    .thenReturn(result);

            ResponseEntity<?> response = controller.importData(1L, 2L, authentication, importData);

            assertEquals(HttpStatus.OK, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("Metadata Tests")
    class MetadataTests {

        @Test
        @DisplayName("Should include correct metadata in export")
        void testExportMetadata() {
            ExportDataDTO exportData = createSampleExportData();
            when(exportImportService.exportData(eq(1L), eq(2L), eq("testuser")))
                    .thenReturn(exportData);

            ResponseEntity<?> response = controller.exportData(1L, 2L, authentication);

            ExportDataDTO body = (ExportDataDTO) response.getBody();
            assertNotNull(body);
            ExportMetadata metadata = body.getMetadata();
            assertNotNull(metadata);
            assertEquals("1.0.0", metadata.getExportVersion());
            assertEquals("testuser", metadata.getExportedBy());
            assertEquals(1L, metadata.getResponsibilityCentreId());
            assertEquals(2L, metadata.getFiscalYearId());
            assertEquals(1, metadata.getFundingItemCount());
            assertEquals(1, metadata.getSpendingItemCount());
            assertEquals(1, metadata.getProcurementItemCount());
        }
    }

    // ============================
    // Helper methods
    // ============================

    private ExportDataDTO createSampleExportData() {
        ExportDataDTO data = new ExportDataDTO();

        ExportMetadata metadata = new ExportMetadata();
        metadata.setExportVersion("1.0.0");
        metadata.setExportedAt(LocalDateTime.now());
        metadata.setExportedBy("testuser");
        metadata.setResponsibilityCentreId(1L);
        metadata.setResponsibilityCentreName("Test RC");
        metadata.setFiscalYearId(2L);
        metadata.setFiscalYearName("FY 2025-2026");
        metadata.setFundingItemCount(1);
        metadata.setSpendingItemCount(1);
        metadata.setProcurementItemCount(1);
        data.setMetadata(metadata);

        // Funding item
        FundingItemDTO fundingItem = new FundingItemDTO();
        fundingItem.setId(1L);
        fundingItem.setName("Test Funding");
        data.setFundingItems(List.of(fundingItem));

        // Spending item
        SpendingItemExportDTO spendingExport = new SpendingItemExportDTO();
        SpendingItemDTO spendingItem = new SpendingItemDTO();
        spendingItem.setId(1L);
        spendingItem.setName("Test Spending");
        spendingExport.setItem(spendingItem);
        data.setSpendingItems(List.of(spendingExport));

        // Procurement item
        ProcurementItemExportDTO procExport = new ProcurementItemExportDTO();
        ProcurementItemDTO procItem = new ProcurementItemDTO();
        procItem.setId(1L);
        procItem.setName("Test Procurement");
        procExport.setItem(procItem);
        data.setProcurementItems(List.of(procExport));

        return data;
    }

    private ExportDataDTO createExportDataWithFiles() {
        ExportDataDTO data = createSampleExportData();

        // Add invoice with file to spending item
        SpendingItemExportDTO spendingExport = data.getSpendingItems().get(0);
        SpendingInvoiceExportDTO invoiceExport = new SpendingInvoiceExportDTO();
        SpendingInvoiceDTO invoice = new SpendingInvoiceDTO();
        invoice.setId(1L);
        invoice.setComments("INV-001");
        invoiceExport.setInvoice(invoice);

        FileExportDTO file = new FileExportDTO();
        file.setId(1L);
        file.setFileName("receipt.pdf");
        file.setContentType("application/pdf");
        file.setFileSize(1024L);
        file.setBase64Content("dGVzdCBjb250ZW50"); // "test content" in base64
        invoiceExport.setFiles(List.of(file));
        spendingExport.setInvoices(List.of(invoiceExport));

        // Add event with file to procurement item
        ProcurementItemExportDTO procExport = data.getProcurementItems().get(0);
        ProcurementEventExportDTO eventExport = new ProcurementEventExportDTO();
        ProcurementEventDTO event = new ProcurementEventDTO();
        event.setId(1L);
        event.setEventType("QUOTE_REQUESTED");
        eventExport.setEvent(event);

        FileExportDTO eventFile = new FileExportDTO();
        eventFile.setId(2L);
        eventFile.setFileName("specification.docx");
        eventFile.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        eventFile.setFileSize(2048L);
        eventFile.setBase64Content("ZXZlbnQgZmlsZQ=="); // "event file" in base64
        eventExport.setFiles(List.of(eventFile));
        procExport.setEvents(List.of(eventExport));

        // Add quote with file to procurement item
        ProcurementQuoteExportDTO quoteExport = new ProcurementQuoteExportDTO();
        ProcurementQuoteDTO quote = new ProcurementQuoteDTO();
        quote.setId(1L);
        quote.setVendorName("Test Vendor");
        quoteExport.setQuote(quote);

        FileExportDTO quoteFile = new FileExportDTO();
        quoteFile.setId(3L);
        quoteFile.setFileName("quote.pdf");
        quoteFile.setContentType("application/pdf");
        quoteFile.setFileSize(512L);
        quoteFile.setBase64Content("cXVvdGUgZmlsZQ=="); // "quote file" in base64
        quoteExport.setFiles(List.of(quoteFile));
        procExport.setQuotes(List.of(quoteExport));

        return data;
    }
}
