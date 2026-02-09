/*
 * myRC - Spending Invoice File Repository
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.repository;

import com.myrc.model.SpendingInvoiceFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Spending Invoice File entities.
 */
@Repository
public interface SpendingInvoiceFileRepository extends JpaRepository<SpendingInvoiceFile, Long> {

    /**
     * Find all active files for an invoice.
     */
    @Query("SELECT f FROM SpendingInvoiceFile f WHERE f.invoice.id = :invoiceId AND f.active = true ORDER BY f.createdAt DESC")
    List<SpendingInvoiceFile> findActiveByInvoiceId(@Param("invoiceId") Long invoiceId);

    /**
     * Find an active file by ID with its invoice loaded.
     */
    @Query("SELECT f FROM SpendingInvoiceFile f JOIN FETCH f.invoice WHERE f.id = :id AND f.active = true")
    Optional<SpendingInvoiceFile> findActiveByIdWithInvoice(@Param("id") Long id);
}
