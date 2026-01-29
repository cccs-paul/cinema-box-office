/*
 * myRC - Procurement Quote File Repository
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-28
 * Version: 1.0.0
 *
 * Description:
 * JPA Repository for ProcurementQuoteFile entity operations.
 */
package com.boxoffice.repository;

import com.boxoffice.model.ProcurementQuoteFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for ProcurementQuoteFile entity operations.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-28
 */
@Repository
public interface ProcurementQuoteFileRepository extends JpaRepository<ProcurementQuoteFile, Long> {

    /**
     * Find all files for a quote ordered by filename.
     *
     * @param quoteId the quote ID
     * @return list of files
     */
    List<ProcurementQuoteFile> findByQuoteIdAndActiveTrueOrderByFileNameAsc(Long quoteId);

    /**
     * Find file metadata only (without content) for a quote.
     * Returns all fields except the large content blob.
     *
     * @param quoteId the quote ID
     * @return list of file metadata
     */
    @Query("SELECT f FROM ProcurementQuoteFile f " +
           "WHERE f.quote.id = :quoteId AND f.active = true ORDER BY f.fileName ASC")
    List<ProcurementQuoteFile> findMetadataByQuoteId(@Param("quoteId") Long quoteId);

    /**
     * Find file by ID without loading content.
     *
     * @param id the file ID
     * @return optional file without content
     */
    @Query("SELECT f FROM ProcurementQuoteFile f WHERE f.id = :id AND f.active = true")
    Optional<ProcurementQuoteFile> findByIdWithoutContent(@Param("id") Long id);

    /**
     * Count files for a quote.
     *
     * @param quoteId the quote ID
     * @return count of files
     */
    @Query("SELECT COUNT(f) FROM ProcurementQuoteFile f WHERE f.quote.id = :quoteId AND f.active = true")
    long countByQuoteId(@Param("quoteId") Long quoteId);

    /**
     * Get total file size for a quote.
     *
     * @param quoteId the quote ID
     * @return total size in bytes
     */
    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM ProcurementQuoteFile f WHERE f.quote.id = :quoteId AND f.active = true")
    long getTotalFileSizeByQuoteId(@Param("quoteId") Long quoteId);

    /**
     * Check if a file exists by filename and quote.
     *
     * @param fileName the file name
     * @param quoteId the quote ID
     * @return true if exists
     */
    boolean existsByFileNameAndQuoteIdAndActiveTrue(String fileName, Long quoteId);

    /**
     * Delete all files for a quote (soft delete).
     *
     * @param quoteId the quote ID
     */
    @Modifying
    @Query("UPDATE ProcurementQuoteFile f SET f.active = false WHERE f.quote.id = :quoteId")
    void softDeleteByQuoteId(@Param("quoteId") Long quoteId);

    /**
     * Hard delete all files for a quote.
     *
     * @param quoteId the quote ID
     */
    void deleteByQuoteId(Long quoteId);
}
