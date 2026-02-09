/*
 * myRC - Spending Invoice Service Implementation
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.myrc.dto.SpendingInvoiceDTO;
import com.myrc.dto.SpendingInvoiceFileDTO;
import com.myrc.model.Currency;
import com.myrc.model.SpendingInvoice;
import com.myrc.model.SpendingInvoiceFile;
import com.myrc.model.SpendingItem;
import com.myrc.repository.SpendingInvoiceFileRepository;
import com.myrc.repository.SpendingInvoiceRepository;
import com.myrc.repository.SpendingItemRepository;

/**
 * Implementation of SpendingInvoiceService.
 * Handles CRUD for invoices and file operations.
 */
@Service
@Transactional
public class SpendingInvoiceServiceImpl implements SpendingInvoiceService {

    private static final Logger logger = Logger.getLogger(SpendingInvoiceServiceImpl.class.getName());
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50 MB

    private final SpendingInvoiceRepository invoiceRepository;
    private final SpendingInvoiceFileRepository fileRepository;
    private final SpendingItemRepository spendingItemRepository;
    private final RCPermissionService permissionService;

    public SpendingInvoiceServiceImpl(SpendingInvoiceRepository invoiceRepository,
                                      SpendingInvoiceFileRepository fileRepository,
                                      SpendingItemRepository spendingItemRepository,
                                      RCPermissionService permissionService) {
        this.invoiceRepository = invoiceRepository;
        this.fileRepository = fileRepository;
        this.spendingItemRepository = spendingItemRepository;
        this.permissionService = permissionService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SpendingInvoiceDTO> getInvoicesBySpendingItemId(Long spendingItemId, String username) {
        SpendingItem spendingItem = findSpendingItemOrThrow(spendingItemId);
        checkReadAccess(spendingItem, username);

        List<SpendingInvoice> invoices = invoiceRepository.findBySpendingItemIdWithFiles(spendingItemId);
        return invoices.stream()
                .map(SpendingInvoiceDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SpendingInvoiceDTO> getInvoiceById(Long invoiceId, String username) {
        Optional<SpendingInvoice> invoiceOpt = invoiceRepository.findByIdWithFiles(invoiceId);
        if (invoiceOpt.isEmpty()) {
            return Optional.empty();
        }
        SpendingInvoice invoice = invoiceOpt.get();
        checkReadAccess(invoice.getSpendingItem(), username);
        return Optional.of(SpendingInvoiceDTO.fromEntity(invoice));
    }

    @Override
    public SpendingInvoiceDTO createInvoice(Long spendingItemId, SpendingInvoiceDTO invoiceDTO, String username) {
        SpendingItem spendingItem = findSpendingItemOrThrow(spendingItemId);
        checkWriteAccess(spendingItem, username);

        if (invoiceDTO.getAmount() == null) {
            throw new IllegalArgumentException("Invoice amount is required");
        }

        SpendingInvoice invoice = new SpendingInvoice();
        invoice.setSpendingItem(spendingItem);
        invoice.setDateReceived(invoiceDTO.getDateReceived());
        invoice.setDateProcessed(invoiceDTO.getDateProcessed());
        invoice.setComments(invoiceDTO.getComments());
        invoice.setAmount(invoiceDTO.getAmount());

        // Currency
        Currency currency = Currency.CAD;
        if (invoiceDTO.getCurrency() != null) {
            try {
                currency = Currency.valueOf(invoiceDTO.getCurrency());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid currency: " + invoiceDTO.getCurrency());
            }
        }
        invoice.setCurrency(currency);
        invoice.setExchangeRate(invoiceDTO.getExchangeRate());

        // Calculate CAD amount
        if (currency == Currency.CAD) {
            invoice.setAmountCad(invoiceDTO.getAmount());
        } else if (invoiceDTO.getExchangeRate() != null) {
            invoice.setAmountCad(invoiceDTO.getAmount().multiply(invoiceDTO.getExchangeRate()));
        }

        invoice.setCreatedBy(username);
        invoice.setModifiedBy(username);

        SpendingInvoice saved = invoiceRepository.save(invoice);
        logger.info("Created invoice " + saved.getId() + " for spending item " + spendingItemId + " by " + username);
        return SpendingInvoiceDTO.fromEntity(saved);
    }

    @Override
    public SpendingInvoiceDTO updateInvoice(Long invoiceId, SpendingInvoiceDTO invoiceDTO, String username) {
        SpendingInvoice invoice = findInvoiceOrThrow(invoiceId);
        checkWriteAccess(invoice.getSpendingItem(), username);

        if (invoiceDTO.getDateReceived() != null || invoiceDTO.getDateReceived() == null) {
            invoice.setDateReceived(invoiceDTO.getDateReceived());
        }
        if (invoiceDTO.getDateProcessed() != null || invoiceDTO.getDateProcessed() == null) {
            invoice.setDateProcessed(invoiceDTO.getDateProcessed());
        }
        invoice.setComments(invoiceDTO.getComments());

        if (invoiceDTO.getAmount() != null) {
            invoice.setAmount(invoiceDTO.getAmount());
        }

        // Currency
        if (invoiceDTO.getCurrency() != null) {
            try {
                Currency currency = Currency.valueOf(invoiceDTO.getCurrency());
                invoice.setCurrency(currency);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid currency: " + invoiceDTO.getCurrency());
            }
        }
        invoice.setExchangeRate(invoiceDTO.getExchangeRate());

        // Recalculate CAD amount
        if (invoice.getCurrency() == Currency.CAD) {
            invoice.setAmountCad(invoice.getAmount());
        } else if (invoice.getExchangeRate() != null) {
            invoice.setAmountCad(invoice.getAmount().multiply(invoice.getExchangeRate()));
        } else {
            invoice.setAmountCad(null);
        }

        invoice.setModifiedBy(username);

        SpendingInvoice saved = invoiceRepository.save(invoice);
        logger.info("Updated invoice " + invoiceId + " by " + username);
        return SpendingInvoiceDTO.fromEntity(saved);
    }

    @Override
    public void deleteInvoice(Long invoiceId, String username) {
        SpendingInvoice invoice = findInvoiceOrThrow(invoiceId);
        checkWriteAccess(invoice.getSpendingItem(), username);

        invoice.setActive(false);
        invoice.setModifiedBy(username);
        invoiceRepository.save(invoice);
        logger.info("Soft-deleted invoice " + invoiceId + " by " + username);
    }

    // === File Operations ===

    @Override
    @Transactional(readOnly = true)
    public List<SpendingInvoiceFileDTO> getFiles(Long invoiceId, String username) {
        SpendingInvoice invoice = findInvoiceOrThrow(invoiceId);
        checkReadAccess(invoice.getSpendingItem(), username);

        return invoice.getFiles().stream()
                .filter(f -> f.getActive())
                .map(SpendingInvoiceFileDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SpendingInvoiceFileDTO> getFileMetadata(Long fileId, String username) {
        Optional<SpendingInvoiceFile> fileOpt = fileRepository.findActiveByIdWithInvoice(fileId);
        if (fileOpt.isEmpty()) {
            return Optional.empty();
        }
        SpendingInvoiceFile file = fileOpt.get();
        checkReadAccess(file.getInvoice().getSpendingItem(), username);
        return Optional.of(SpendingInvoiceFileDTO.fromEntity(file));
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getFileContent(Long fileId, String username) {
        SpendingInvoiceFile file = findFileOrThrow(fileId);
        checkReadAccess(file.getInvoice().getSpendingItem(), username);
        return file.getContent();
    }

    @Override
    public SpendingInvoiceFileDTO uploadFile(Long invoiceId, MultipartFile file, String description, String username) {
        SpendingInvoice invoice = findInvoiceOrThrow(invoiceId);
        checkWriteAccess(invoice.getSpendingItem(), username);

        validateFile(file);

        try {
            SpendingInvoiceFile invoiceFile = new SpendingInvoiceFile(
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getSize(),
                    file.getBytes(),
                    invoice
            );
            if (description != null && !description.trim().isEmpty()) {
                invoiceFile.setDescription(description.trim());
            }
            invoice.addFile(invoiceFile);
            invoiceRepository.save(invoice);

            logger.info("Uploaded file '" + file.getOriginalFilename() + "' to invoice " + invoiceId + " by " + username);
            return SpendingInvoiceFileDTO.fromEntity(invoiceFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file content", e);
        }
    }

    @Override
    public void deleteFile(Long fileId, String username) {
        SpendingInvoiceFile file = findFileOrThrow(fileId);
        checkWriteAccess(file.getInvoice().getSpendingItem(), username);

        file.setActive(false);
        invoiceRepository.save(file.getInvoice());
        logger.info("Soft-deleted file " + fileId + " by " + username);
    }

    @Override
    public SpendingInvoiceFileDTO replaceFile(Long fileId, MultipartFile newFile, String description, String username) {
        SpendingInvoiceFile file = findFileOrThrow(fileId);
        checkWriteAccess(file.getInvoice().getSpendingItem(), username);

        validateFile(newFile);

        try {
            file.setFileName(newFile.getOriginalFilename());
            file.setContentType(newFile.getContentType());
            file.setFileSize(newFile.getSize());
            file.setContent(newFile.getBytes());
            if (description != null) {
                file.setDescription(description.trim().isEmpty() ? null : description.trim());
            }
            invoiceRepository.save(file.getInvoice());

            logger.info("Replaced file " + fileId + " with '" + newFile.getOriginalFilename() + "' by " + username);
            return SpendingInvoiceFileDTO.fromEntity(file);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file content", e);
        }
    }

    // === Helper Methods ===

    private SpendingItem findSpendingItemOrThrow(Long spendingItemId) {
        return spendingItemRepository.findById(spendingItemId)
                .orElseThrow(() -> new IllegalArgumentException("Spending item not found: " + spendingItemId));
    }

    private SpendingInvoice findInvoiceOrThrow(Long invoiceId) {
        SpendingInvoice invoice = invoiceRepository.findByIdWithFiles(invoiceId)
                .orElse(null);
        if (invoice == null) {
            // Try without files
            invoice = invoiceRepository.findById(invoiceId)
                    .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));
        }
        if (!invoice.getActive()) {
            throw new IllegalArgumentException("Invoice not found: " + invoiceId);
        }
        return invoice;
    }

    private SpendingInvoiceFile findFileOrThrow(Long fileId) {
        return fileRepository.findActiveByIdWithInvoice(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found: " + fileId));
    }

    private void checkReadAccess(SpendingItem spendingItem, String username) {
        Long rcId = spendingItem.getFiscalYear().getResponsibilityCentre().getId();
        if (!permissionService.hasAccess(rcId, username)) {
            throw new IllegalArgumentException("User does not have access to this Responsibility Centre");
        }
    }

    private void checkWriteAccess(SpendingItem spendingItem, String username) {
        Long rcId = spendingItem.getFiscalYear().getResponsibilityCentre().getId();
        if (!permissionService.hasWriteAccess(rcId, username)) {
            throw new IllegalArgumentException("User does not have write access to this Responsibility Centre");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File exceeds maximum size of 50 MB");
        }
    }
}
