/*
 * myRC - Spending Invoice Repository
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Description:
 * Repository for Spending Invoice entities.
 */
package com.myrc.repository;

import com.myrc.model.SpendingInvoice;
import com.myrc.model.SpendingItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Spending Invoice entities.
 */
@Repository
public interface SpendingInvoiceRepository extends JpaRepository<SpendingInvoice, Long> {

    /**
     * Find all active invoices for a spending item, ordered by creation date.
     */
    @Query("SELECT i FROM SpendingInvoice i WHERE i.spendingItem.id = :spendingItemId AND i.active = true ORDER BY i.createdAt DESC")
    List<SpendingInvoice> findBySpendingItemId(@Param("spendingItemId") Long spendingItemId);

    /**
     * Find an invoice by ID with its files eagerly loaded.
     */
    @Query("SELECT i FROM SpendingInvoice i LEFT JOIN FETCH i.files WHERE i.id = :id AND i.active = true")
    Optional<SpendingInvoice> findByIdWithFiles(@Param("id") Long id);

    /**
     * Find all active invoices for a spending item with files eagerly loaded.
     */
    @Query("SELECT DISTINCT i FROM SpendingInvoice i LEFT JOIN FETCH i.files WHERE i.spendingItem.id = :spendingItemId AND i.active = true ORDER BY i.createdAt DESC")
    List<SpendingInvoice> findBySpendingItemIdWithFiles(@Param("spendingItemId") Long spendingItemId);

    /**
     * Count active invoices for a spending item.
     */
    @Query("SELECT COUNT(i) FROM SpendingInvoice i WHERE i.spendingItem.id = :spendingItemId AND i.active = true")
    long countBySpendingItemId(@Param("spendingItemId") Long spendingItemId);

    /**
     * Find all invoices for a spending item (including inactive).
     */
    List<SpendingInvoice> findBySpendingItem(SpendingItem spendingItem);
}
