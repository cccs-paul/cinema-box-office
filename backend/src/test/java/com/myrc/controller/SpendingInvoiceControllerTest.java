/*
 * myRC - Spending Invoice Controller Tests
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-02-09
 * Version: 1.0.0
 *
 * Description:
 * Unit tests for SpendingInvoiceController.
 * Tests all invoice CRUD and file download/view endpoints.
 */
package com.myrc.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import com.myrc.dto.SpendingInvoiceDTO;
import com.myrc.dto.SpendingInvoiceFileDTO;
import com.myrc.service.SpendingInvoiceService;

/**
 * Unit tests for SpendingInvoiceController.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-09
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SpendingInvoiceController Tests")
class SpendingInvoiceControllerTest {

    @Mock
    private SpendingInvoiceService invoiceService;

    private SpendingInvoiceController controller;
    private SpendingInvoiceDTO invoiceDTO;
    private SpendingInvoiceFileDTO fileDTO;
    private Authentication authentication;

    private static final Long RC_ID = 1L;
    private static final Long FY_ID = 2L;
    private static final Long SPENDING_ITEM_ID = 3L;
    private static final Long INVOICE_ID = 10L;
    private static final Long FILE_ID = 20L;
    private static final String USERNAME = "testuser";

    /**
     * Simple test implementation of Authentication.
     */
    private static class TestAuthentication implements Authentication {
        private final String name;

        TestAuthentication(String name) {
            this.name = name;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return Collections.emptyList();
        }

        @Override
        public Object getCredentials() {
            return null;
        }

        @Override
        public Object getDetails() {
            return null;
        }

        @Override
        public Object getPrincipal() {
            return name;
        }

        @Override
        public boolean isAuthenticated() {
            return true;
        }

        @Override
        public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
            // No-op for testing
        }

        @Override
        public String getName() {
            return name;
        }
    }

    @BeforeEach
    void setUp() {
        controller = new SpendingInvoiceController(invoiceService);
        authentication = new TestAuthentication(USERNAME);

        invoiceDTO = new SpendingInvoiceDTO();
        invoiceDTO.setId(INVOICE_ID);
        invoiceDTO.setSpendingItemId(SPENDING_ITEM_ID);
        invoiceDTO.setSpendingItemName("GPU Purchase");
        invoiceDTO.setDateReceived(LocalDate.of(2026, 1, 15));
        invoiceDTO.setComments("Test invoice");
        invoiceDTO.setAmount(new BigDecimal("5000.00"));
        invoiceDTO.setCurrency("CAD");
        invoiceDTO.setAmountCad(new BigDecimal("5000.00"));
        invoiceDTO.setActive(true);

        fileDTO = new SpendingInvoiceFileDTO(
                FILE_ID, "receipt.pdf", "application/pdf", 12345L,
                "12 KB", "Receipt scan", INVOICE_ID,
                LocalDateTime.now(), LocalDateTime.now(), true
        );
    }

    @Test
    @DisplayName("Should create controller successfully")
    void testControllerCreation() {
        assertNotNull(controller);
    }

    // ==========================
    // Invoice CRUD Tests
    // ==========================

    @Nested
    @DisplayName("getInvoices Tests")
    class GetInvoicesTests {

        @Test
        @DisplayName("Returns all invoices for a spending item")
        void returnsAllInvoices() {
            List<SpendingInvoiceDTO> invoices = Arrays.asList(invoiceDTO);
            when(invoiceService.getInvoicesBySpendingItemId(eq(SPENDING_ITEM_ID), eq(USERNAME)))
                    .thenReturn(invoices);

            ResponseEntity<?> response = controller.getInvoices(RC_ID, FY_ID, SPENDING_ITEM_ID, authentication);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            @SuppressWarnings("unchecked")
            List<SpendingInvoiceDTO> body = (List<SpendingInvoiceDTO>) response.getBody();
            assertEquals(1, body.size());
            assertEquals(INVOICE_ID, body.get(0).getId());
        }

        @Test
        @DisplayName("Returns empty list when no invoices exist")
        void returnsEmptyList() {
            when(invoiceService.getInvoicesBySpendingItemId(eq(SPENDING_ITEM_ID), eq(USERNAME)))
                    .thenReturn(Collections.emptyList());

            ResponseEntity<?> response = controller.getInvoices(RC_ID, FY_ID, SPENDING_ITEM_ID, authentication);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            @SuppressWarnings("unchecked")
            List<SpendingInvoiceDTO> body = (List<SpendingInvoiceDTO>) response.getBody();
            assertNotNull(body);
            assertEquals(0, body.size());
        }

        @Test
        @DisplayName("Returns 403 when access denied")
        void returnsForbiddenOnAccessDenied() {
            when(invoiceService.getInvoicesBySpendingItemId(anyLong(), anyString()))
                    .thenThrow(new IllegalArgumentException("Access denied"));

            ResponseEntity<?> response = controller.getInvoices(RC_ID, FY_ID, SPENDING_ITEM_ID, authentication);

            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        @Test
        @DisplayName("Returns 500 on unexpected error")
        void returnsInternalServerError() {
            when(invoiceService.getInvoicesBySpendingItemId(anyLong(), anyString()))
                    .thenThrow(new RuntimeException("Database error"));

            ResponseEntity<?> response = controller.getInvoices(RC_ID, FY_ID, SPENDING_ITEM_ID, authentication);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("getInvoice Tests")
    class GetInvoiceTests {

        @Test
        @DisplayName("Returns invoice by ID")
        void returnsInvoiceById() {
            when(invoiceService.getInvoiceById(eq(INVOICE_ID), eq(USERNAME)))
                    .thenReturn(Optional.of(invoiceDTO));

            ResponseEntity<?> response = controller.getInvoice(RC_ID, FY_ID, SPENDING_ITEM_ID, INVOICE_ID, authentication);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            SpendingInvoiceDTO body = (SpendingInvoiceDTO) response.getBody();
            assertNotNull(body);
            assertEquals(INVOICE_ID, body.getId());
        }

        @Test
        @DisplayName("Returns 404 when invoice not found")
        void returnsNotFoundWhenMissing() {
            when(invoiceService.getInvoiceById(eq(INVOICE_ID), eq(USERNAME)))
                    .thenReturn(Optional.empty());

            ResponseEntity<?> response = controller.getInvoice(RC_ID, FY_ID, SPENDING_ITEM_ID, INVOICE_ID, authentication);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        @DisplayName("Returns 403 when access denied")
        void returnsForbiddenOnAccessDenied() {
            when(invoiceService.getInvoiceById(anyLong(), anyString()))
                    .thenThrow(new IllegalArgumentException("Access denied"));

            ResponseEntity<?> response = controller.getInvoice(RC_ID, FY_ID, SPENDING_ITEM_ID, INVOICE_ID, authentication);

            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        @Test
        @DisplayName("Returns 500 on unexpected error")
        void returnsInternalServerError() {
            when(invoiceService.getInvoiceById(anyLong(), anyString()))
                    .thenThrow(new RuntimeException("Database error"));

            ResponseEntity<?> response = controller.getInvoice(RC_ID, FY_ID, SPENDING_ITEM_ID, INVOICE_ID, authentication);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("createInvoice Tests")
    class CreateInvoiceTests {

        @Test
        @DisplayName("Creates an invoice successfully")
        void createsInvoice() {
            when(invoiceService.createInvoice(eq(SPENDING_ITEM_ID), any(SpendingInvoiceDTO.class), eq(USERNAME)))
                    .thenReturn(invoiceDTO);

            ResponseEntity<?> response = controller.createInvoice(
                    RC_ID, FY_ID, SPENDING_ITEM_ID, invoiceDTO, authentication);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            SpendingInvoiceDTO body = (SpendingInvoiceDTO) response.getBody();
            assertNotNull(body);
            assertEquals(INVOICE_ID, body.getId());
        }

        @Test
        @DisplayName("Returns 400 on validation error")
        void returnsBadRequestOnValidationError() {
            when(invoiceService.createInvoice(anyLong(), any(SpendingInvoiceDTO.class), anyString()))
                    .thenThrow(new IllegalArgumentException("Amount is required"));

            ResponseEntity<?> response = controller.createInvoice(
                    RC_ID, FY_ID, SPENDING_ITEM_ID, invoiceDTO, authentication);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("Returns 500 on unexpected error")
        void returnsInternalServerError() {
            when(invoiceService.createInvoice(anyLong(), any(SpendingInvoiceDTO.class), anyString()))
                    .thenThrow(new RuntimeException("Database error"));

            ResponseEntity<?> response = controller.createInvoice(
                    RC_ID, FY_ID, SPENDING_ITEM_ID, invoiceDTO, authentication);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("updateInvoice Tests")
    class UpdateInvoiceTests {

        @Test
        @DisplayName("Updates an invoice successfully")
        void updatesInvoice() {
            SpendingInvoiceDTO updated = new SpendingInvoiceDTO();
            updated.setId(INVOICE_ID);
            updated.setComments("Updated comment");
            when(invoiceService.updateInvoice(eq(INVOICE_ID), any(SpendingInvoiceDTO.class), eq(USERNAME)))
                    .thenReturn(updated);

            ResponseEntity<?> response = controller.updateInvoice(
                    RC_ID, FY_ID, SPENDING_ITEM_ID, INVOICE_ID, invoiceDTO, authentication);

            assertEquals(HttpStatus.OK, response.getStatusCode());
        }

        @Test
        @DisplayName("Returns 404 when invoice not found on update")
        void returnsNotFoundOnUpdate() {
            when(invoiceService.updateInvoice(anyLong(), any(SpendingInvoiceDTO.class), anyString()))
                    .thenThrow(new IllegalArgumentException("Invoice not found"));

            ResponseEntity<?> response = controller.updateInvoice(
                    RC_ID, FY_ID, SPENDING_ITEM_ID, INVOICE_ID, invoiceDTO, authentication);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        @DisplayName("Returns 400 on validation error during update")
        void returnsBadRequestOnValidationError() {
            when(invoiceService.updateInvoice(anyLong(), any(SpendingInvoiceDTO.class), anyString()))
                    .thenThrow(new IllegalArgumentException("Invalid amount"));

            ResponseEntity<?> response = controller.updateInvoice(
                    RC_ID, FY_ID, SPENDING_ITEM_ID, INVOICE_ID, invoiceDTO, authentication);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("Returns 500 on unexpected error during update")
        void returnsInternalServerError() {
            when(invoiceService.updateInvoice(anyLong(), any(SpendingInvoiceDTO.class), anyString()))
                    .thenThrow(new RuntimeException("Database error"));

            ResponseEntity<?> response = controller.updateInvoice(
                    RC_ID, FY_ID, SPENDING_ITEM_ID, INVOICE_ID, invoiceDTO, authentication);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("deleteInvoice Tests")
    class DeleteInvoiceTests {

        @Test
        @DisplayName("Deletes an invoice successfully")
        void deletesInvoice() {
            doNothing().when(invoiceService).deleteInvoice(eq(INVOICE_ID), eq(USERNAME));

            ResponseEntity<?> response = controller.deleteInvoice(
                    RC_ID, FY_ID, SPENDING_ITEM_ID, INVOICE_ID, authentication);

            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        }

        @Test
        @DisplayName("Returns 404 when invoice not found on delete")
        void returnsNotFoundOnDelete() {
            doThrow(new IllegalArgumentException("Invoice not found"))
                    .when(invoiceService).deleteInvoice(anyLong(), anyString());

            ResponseEntity<?> response = controller.deleteInvoice(
                    RC_ID, FY_ID, SPENDING_ITEM_ID, INVOICE_ID, authentication);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        @DisplayName("Returns 400 on validation error during delete")
        void returnsBadRequestOnDelete() {
            doThrow(new IllegalArgumentException("Cannot delete active invoice"))
                    .when(invoiceService).deleteInvoice(anyLong(), anyString());

            ResponseEntity<?> response = controller.deleteInvoice(
                    RC_ID, FY_ID, SPENDING_ITEM_ID, INVOICE_ID, authentication);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("Returns 500 on unexpected error during delete")
        void returnsInternalServerError() {
            doThrow(new RuntimeException("Database error"))
                    .when(invoiceService).deleteInvoice(anyLong(), anyString());

            ResponseEntity<?> response = controller.deleteInvoice(
                    RC_ID, FY_ID, SPENDING_ITEM_ID, INVOICE_ID, authentication);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        }
    }

    // ==========================
    // File Endpoint Tests
    // ==========================

    @Nested
    @DisplayName("getFiles Tests")
    class GetFilesTests {

        @Test
        @DisplayName("Returns all files for an invoice")
        void returnsAllFiles() {
            List<SpendingInvoiceFileDTO> files = Arrays.asList(fileDTO);
            when(invoiceService.getFiles(eq(INVOICE_ID), eq(USERNAME))).thenReturn(files);

            ResponseEntity<?> response = controller.getFiles(
                    RC_ID, FY_ID, SPENDING_ITEM_ID, INVOICE_ID, authentication);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            @SuppressWarnings("unchecked")
            List<SpendingInvoiceFileDTO> body = (List<SpendingInvoiceFileDTO>) response.getBody();
            assertNotNull(body);
            assertEquals(1, body.size());
            assertEquals("receipt.pdf", body.get(0).getFileName());
        }

        @Test
        @DisplayName("Returns empty list when no files exist")
        void returnsEmptyList() {
            when(invoiceService.getFiles(eq(INVOICE_ID), eq(USERNAME)))
                    .thenReturn(Collections.emptyList());

            ResponseEntity<?> response = controller.getFiles(
                    RC_ID, FY_ID, SPENDING_ITEM_ID, INVOICE_ID, authentication);

            assertEquals(HttpStatus.OK, response.getStatusCode());
        }

        @Test
        @DisplayName("Returns 403 when access denied")
        void returnsForbiddenOnAccessDenied() {
            when(invoiceService.getFiles(anyLong(), anyString()))
                    .thenThrow(new IllegalArgumentException("Access denied"));

            ResponseEntity<?> response = controller.getFiles(
                    RC_ID, FY_ID, SPENDING_ITEM_ID, INVOICE_ID, authentication);

            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        @Test
        @DisplayName("Returns 500 on unexpected error")
        void returnsInternalServerError() {
            when(invoiceService.getFiles(anyLong(), anyString()))
                    .thenThrow(new RuntimeException("Database error"));

            ResponseEntity<?> response = controller.getFiles(
                    RC_ID, FY_ID, SPENDING_ITEM_ID, INVOICE_ID, authentication);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("downloadFile Tests")
    class DownloadFileTests {

        @Test
        @DisplayName("Downloads a file successfully with attachment disposition")
        void downloadsFileSuccessfully() {
            byte[] content = "PDF file content".getBytes();
            when(invoiceService.getFileMetadata(eq(FILE_ID), eq(USERNAME)))
                    .thenReturn(Optional.of(fileDTO));
            when(invoiceService.getFileContent(eq(FILE_ID), eq(USERNAME)))
                    .thenReturn(content);

            ResponseEntity<Resource> response = controller.downloadFile(
                    RC_ID, FY_ID, SPENDING_ITEM_ID, INVOICE_ID, FILE_ID, authentication);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(MediaType.APPLICATION_PDF, response.getHeaders().getContentType());
            String disposition = response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
            assertNotNull(disposition);
            assertEquals("attachment; filename=\"receipt.pdf\"", disposition);
            assertEquals(12345L, response.getHeaders().getContentLength());
        }

        @Test
        @DisplayName("Returns 404 when file metadata not found on download")
        void returnsNotFoundWhenMetadataMissing() {
            when(invoiceService.getFileMetadata(eq(FILE_ID), eq(USERNAME)))
                    .thenReturn(Optional.empty());

            ResponseEntity<Resource> response = controller.downloadFile(
                    RC_ID, FY_ID, SPENDING_ITEM_ID, INVOICE_ID, FILE_ID, authentication);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        @DisplayName("Returns 404 on IllegalArgumentException during download")
        void returnsNotFoundOnIllegalArgument() {
            when(invoiceService.getFileMetadata(anyLong(), anyString()))
                    .thenThrow(new IllegalArgumentException("File not found"));

            ResponseEntity<Resource> response = controller.downloadFile(
                    RC_ID, FY_ID, SPENDING_ITEM_ID, INVOICE_ID, FILE_ID, authentication);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        @DisplayName("Returns 500 on unexpected error during download")
        void returnsInternalServerError() {
            when(invoiceService.getFileMetadata(anyLong(), anyString()))
                    .thenThrow(new RuntimeException("Storage error"));

            ResponseEntity<Resource> response = controller.downloadFile(
                    RC_ID, FY_ID, SPENDING_ITEM_ID, INVOICE_ID, FILE_ID, authentication);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("viewFile Tests")
    class ViewFileTests {

        @Test
        @DisplayName("Views a file successfully with inline disposition")
        void viewsFileSuccessfully() {
            byte[] content = "PDF file content".getBytes();
            when(invoiceService.getFileMetadata(eq(FILE_ID), eq(USERNAME)))
                    .thenReturn(Optional.of(fileDTO));
            when(invoiceService.getFileContent(eq(FILE_ID), eq(USERNAME)))
                    .thenReturn(content);

            ResponseEntity<Resource> response = controller.viewFile(
                    RC_ID, FY_ID, SPENDING_ITEM_ID, INVOICE_ID, FILE_ID, authentication);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(MediaType.APPLICATION_PDF, response.getHeaders().getContentType());
            String disposition = response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
            assertNotNull(disposition);
            assertEquals("inline; filename=\"receipt.pdf\"", disposition);
            assertEquals(12345L, response.getHeaders().getContentLength());
        }

        @Test
        @DisplayName("Returns 404 when file metadata not found on view")
        void returnsNotFoundWhenMetadataMissing() {
            when(invoiceService.getFileMetadata(eq(FILE_ID), eq(USERNAME)))
                    .thenReturn(Optional.empty());

            ResponseEntity<Resource> response = controller.viewFile(
                    RC_ID, FY_ID, SPENDING_ITEM_ID, INVOICE_ID, FILE_ID, authentication);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        @DisplayName("Returns 404 on IllegalArgumentException during view")
        void returnsNotFoundOnIllegalArgument() {
            when(invoiceService.getFileMetadata(anyLong(), anyString()))
                    .thenThrow(new IllegalArgumentException("File not found"));

            ResponseEntity<Resource> response = controller.viewFile(
                    RC_ID, FY_ID, SPENDING_ITEM_ID, INVOICE_ID, FILE_ID, authentication);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        @DisplayName("Returns 500 on unexpected error during view")
        void returnsInternalServerError() {
            when(invoiceService.getFileMetadata(anyLong(), anyString()))
                    .thenThrow(new RuntimeException("Storage error"));

            ResponseEntity<Resource> response = controller.viewFile(
                    RC_ID, FY_ID, SPENDING_ITEM_ID, INVOICE_ID, FILE_ID, authentication);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("uploadFile Tests")
    class UploadFileTests {

        @Test
        @DisplayName("Uploads a file successfully")
        void uploadsFile() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "receipt.pdf", "application/pdf", "test content".getBytes());
            when(invoiceService.uploadFile(eq(INVOICE_ID), any(), eq("Receipt scan"), eq(USERNAME)))
                    .thenReturn(fileDTO);

            ResponseEntity<?> response = controller.uploadFile(
                    RC_ID, FY_ID, SPENDING_ITEM_ID, INVOICE_ID, file, "Receipt scan", authentication);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            SpendingInvoiceFileDTO body = (SpendingInvoiceFileDTO) response.getBody();
            assertNotNull(body);
            assertEquals("receipt.pdf", body.getFileName());
        }

        @Test
        @DisplayName("Returns 400 on validation error during upload")
        void returnsBadRequestOnValidationError() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "malware.exe", "application/octet-stream", "bad content".getBytes());
            when(invoiceService.uploadFile(anyLong(), any(), anyString(), anyString()))
                    .thenThrow(new IllegalArgumentException("File type not allowed"));

            ResponseEntity<?> response = controller.uploadFile(
                    RC_ID, FY_ID, SPENDING_ITEM_ID, INVOICE_ID, file, "Bad file", authentication);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("Returns 500 on unexpected error during upload")
        void returnsInternalServerError() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "receipt.pdf", "application/pdf", "test content".getBytes());
            when(invoiceService.uploadFile(anyLong(), any(), anyString(), anyString()))
                    .thenThrow(new RuntimeException("Storage error"));

            ResponseEntity<?> response = controller.uploadFile(
                    RC_ID, FY_ID, SPENDING_ITEM_ID, INVOICE_ID, file, "Receipt", authentication);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("deleteFile Tests")
    class DeleteFileTests {

        @Test
        @DisplayName("Deletes a file successfully")
        void deletesFile() {
            doNothing().when(invoiceService).deleteFile(eq(FILE_ID), eq(USERNAME));

            ResponseEntity<?> response = controller.deleteFile(
                    RC_ID, FY_ID, SPENDING_ITEM_ID, INVOICE_ID, FILE_ID, authentication);

            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        }

        @Test
        @DisplayName("Returns 404 when file not found on delete")
        void returnsNotFoundOnDelete() {
            doThrow(new IllegalArgumentException("File not found"))
                    .when(invoiceService).deleteFile(anyLong(), anyString());

            ResponseEntity<?> response = controller.deleteFile(
                    RC_ID, FY_ID, SPENDING_ITEM_ID, INVOICE_ID, FILE_ID, authentication);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        @DisplayName("Returns 403 when access denied on file delete")
        void returnsForbiddenOnDelete() {
            doThrow(new IllegalArgumentException("Access denied"))
                    .when(invoiceService).deleteFile(anyLong(), anyString());

            ResponseEntity<?> response = controller.deleteFile(
                    RC_ID, FY_ID, SPENDING_ITEM_ID, INVOICE_ID, FILE_ID, authentication);

            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        @Test
        @DisplayName("Returns 500 on unexpected error during file delete")
        void returnsInternalServerError() {
            doThrow(new RuntimeException("Storage error"))
                    .when(invoiceService).deleteFile(anyLong(), anyString());

            ResponseEntity<?> response = controller.deleteFile(
                    RC_ID, FY_ID, SPENDING_ITEM_ID, INVOICE_ID, FILE_ID, authentication);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("replaceFile Tests")
    class ReplaceFileTests {

        @Test
        @DisplayName("Replaces a file successfully")
        void replacesFile() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "updated-receipt.pdf", "application/pdf", "new content".getBytes());
            SpendingInvoiceFileDTO replacedDTO = new SpendingInvoiceFileDTO(
                    FILE_ID, "updated-receipt.pdf", "application/pdf", 11111L,
                    "11 KB", "Updated receipt", INVOICE_ID,
                    LocalDateTime.now(), LocalDateTime.now(), true
            );
            when(invoiceService.replaceFile(eq(FILE_ID), any(), eq("Updated receipt"), eq(USERNAME)))
                    .thenReturn(replacedDTO);

            ResponseEntity<?> response = controller.replaceFile(
                    RC_ID, FY_ID, SPENDING_ITEM_ID, INVOICE_ID, FILE_ID, file, "Updated receipt", authentication);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            SpendingInvoiceFileDTO body = (SpendingInvoiceFileDTO) response.getBody();
            assertNotNull(body);
            assertEquals("updated-receipt.pdf", body.getFileName());
        }

        @Test
        @DisplayName("Returns 404 when file not found on replace")
        void returnsNotFoundOnReplace() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "receipt.pdf", "application/pdf", "content".getBytes());
            when(invoiceService.replaceFile(anyLong(), any(), anyString(), anyString()))
                    .thenThrow(new IllegalArgumentException("File not found"));

            ResponseEntity<?> response = controller.replaceFile(
                    RC_ID, FY_ID, SPENDING_ITEM_ID, INVOICE_ID, FILE_ID, file, "desc", authentication);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        @DisplayName("Returns 400 on validation error during replace")
        void returnsBadRequestOnReplace() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "bad.exe", "application/octet-stream", "content".getBytes());
            when(invoiceService.replaceFile(anyLong(), any(), anyString(), anyString()))
                    .thenThrow(new IllegalArgumentException("Invalid file type"));

            ResponseEntity<?> response = controller.replaceFile(
                    RC_ID, FY_ID, SPENDING_ITEM_ID, INVOICE_ID, FILE_ID, file, "desc", authentication);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("Returns 500 on unexpected error during replace")
        void returnsInternalServerError() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "receipt.pdf", "application/pdf", "content".getBytes());
            when(invoiceService.replaceFile(anyLong(), any(), anyString(), anyString()))
                    .thenThrow(new RuntimeException("Storage error"));

            ResponseEntity<?> response = controller.replaceFile(
                    RC_ID, FY_ID, SPENDING_ITEM_ID, INVOICE_ID, FILE_ID, file, "desc", authentication);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        }
    }

    // ==========================
    // Authentication Helper Tests
    // ==========================

    @Nested
    @DisplayName("Authentication Helper Tests")
    class AuthenticationHelperTests {

        @Test
        @DisplayName("Uses 'anonymous' when authentication is null")
        void usesAnonymousWhenAuthIsNull() {
            when(invoiceService.getInvoicesBySpendingItemId(eq(SPENDING_ITEM_ID), eq("anonymous")))
                    .thenReturn(Collections.emptyList());

            ResponseEntity<?> response = controller.getInvoices(RC_ID, FY_ID, SPENDING_ITEM_ID, null);

            assertEquals(HttpStatus.OK, response.getStatusCode());
        }

        @Test
        @DisplayName("Uses 'anonymous' when authentication name is null")
        void usesAnonymousWhenNameIsNull() {
            Authentication nullNameAuth = new TestAuthentication(null);
            when(invoiceService.getInvoicesBySpendingItemId(eq(SPENDING_ITEM_ID), eq("anonymous")))
                    .thenReturn(Collections.emptyList());

            ResponseEntity<?> response = controller.getInvoices(RC_ID, FY_ID, SPENDING_ITEM_ID, nullNameAuth);

            assertEquals(HttpStatus.OK, response.getStatusCode());
        }
    }
}
