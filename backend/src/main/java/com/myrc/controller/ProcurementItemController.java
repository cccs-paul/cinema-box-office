/*
 * myRC - Procurement Item REST Controller
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-28
 * Version: 1.0.0
 *
 * Description:
 * REST Controller for Procurement Item management.
 */
package com.myrc.controller;

import com.myrc.audit.Audited;
import com.myrc.dto.ErrorResponse;
import com.myrc.dto.ProcurementItemDTO;
import com.myrc.dto.ProcurementQuoteDTO;
import com.myrc.dto.ProcurementQuoteFileDTO;
import com.myrc.service.ProcurementItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST Controller for Procurement Item management.
 * Procurement items track purchase requisitions and purchase orders.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-28
 */
@RestController
@RequestMapping("/responsibility-centres/{rcId}/fiscal-years/{fyId}/procurement-items")
@Tag(name = "Procurement Management", description = "APIs for managing procurement items, quotes, and files")
public class ProcurementItemController {

    private static final Logger logger = Logger.getLogger(ProcurementItemController.class.getName());
    private final ProcurementItemService procurementItemService;

    public ProcurementItemController(ProcurementItemService procurementItemService) {
        this.procurementItemService = procurementItemService;
    }

    // ==========================
    // Procurement Item Endpoints
    // ==========================

    /**
     * Get all procurement items for a fiscal year.
     *
     * @param rcId the responsibility centre ID
     * @param fyId the fiscal year ID
     * @param status optional status to filter by
     * @param search optional search term
     * @param authentication the authentication principal
     * @return list of procurement items
     */
    @GetMapping
    @Operation(summary = "Get all procurement items for a fiscal year",
            description = "Retrieves all procurement items for a fiscal year, optionally filtered by status or search term.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Procurement items retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied to this RC"),
            @ApiResponse(responseCode = "404", description = "Fiscal year not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<ProcurementItemDTO>> getProcurementItems(
            @PathVariable Long rcId,
            @PathVariable Long fyId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            Authentication authentication) {
        String username = getUsername(authentication);
        logger.info("GET /responsibility-centres/" + rcId + "/fiscal-years/" + fyId +
                "/procurement-items - Fetching procurement items for user: " + username);

        try {
            List<ProcurementItemDTO> items;
            if (search != null && !search.trim().isEmpty()) {
                items = procurementItemService.searchProcurementItems(fyId, search.trim(), username);
            } else if (status != null && !status.trim().isEmpty()) {
                items = procurementItemService.getProcurementItemsByFiscalYearIdAndStatus(fyId, status, username);
            } else {
                items = procurementItemService.getProcurementItemsByFiscalYearId(fyId, username);
            }
            return ResponseEntity.ok(items);
        } catch (IllegalArgumentException e) {
            logger.warning("Access denied for procurement items: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            logger.severe("Failed to fetch procurement items: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get a specific procurement item by ID.
     *
     * @param rcId the responsibility centre ID
     * @param fyId the fiscal year ID
     * @param procurementItemId the procurement item ID
     * @param includeQuotes whether to include quotes
     * @param authentication the authentication principal
     * @return the procurement item
     */
    @GetMapping("/{procurementItemId}")
    @Operation(summary = "Get a specific procurement item",
            description = "Retrieves a specific procurement item by ID, optionally with quotes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Procurement item retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Procurement item not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ProcurementItemDTO> getProcurementItem(
            @PathVariable Long rcId,
            @PathVariable Long fyId,
            @PathVariable Long procurementItemId,
            @RequestParam(required = false, defaultValue = "false") boolean includeQuotes,
            Authentication authentication) {
        String username = getUsername(authentication);
        logger.info("GET /responsibility-centres/" + rcId + "/fiscal-years/" + fyId +
                "/procurement-items/" + procurementItemId + " - Fetching procurement item for user: " + username);

        try {
            Optional<ProcurementItemDTO> itemOpt = includeQuotes
                    ? procurementItemService.getProcurementItemWithQuotes(procurementItemId, username)
                    : procurementItemService.getProcurementItemById(procurementItemId, username);
            return itemOpt.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.severe("Failed to fetch procurement item: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Create a new procurement item for a fiscal year.
     *
     * @param rcId the responsibility centre ID
     * @param fyId the fiscal year ID
     * @param request the procurement item creation request
     * @param authentication the authentication principal
     * @return the created procurement item
     */
    @PostMapping
    @Audited(action = "CREATE_PROCUREMENT_ITEM", entityType = "PROCUREMENT_ITEM")
    @Operation(summary = "Create a new procurement item",
            description = "Creates a new procurement item for a fiscal year")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Procurement item created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> createProcurementItem(
            @PathVariable Long rcId,
            @PathVariable Long fyId,
            @RequestBody ProcurementItemDTO request,
            Authentication authentication) {
        String username = getUsername(authentication);
        logger.info("POST /responsibility-centres/" + rcId + "/fiscal-years/" + fyId +
                "/procurement-items - Creating procurement item for user: " + username);

        try {
            request.setFiscalYearId(fyId);
            ProcurementItemDTO created = procurementItemService.createProcurementItem(request, username);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            logger.warning("Failed to create procurement item: " + e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.severe("Failed to create procurement item: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An unexpected error occurred"));
        }
    }

    /**
     * Update an existing procurement item.
     *
     * @param rcId the responsibility centre ID
     * @param fyId the fiscal year ID
     * @param procurementItemId the procurement item ID
     * @param request the update request
     * @param authentication the authentication principal
     * @return the updated procurement item
     */
    @PutMapping("/{procurementItemId}")
    @Audited(action = "UPDATE_PROCUREMENT_ITEM", entityType = "PROCUREMENT_ITEM")
    @Operation(summary = "Update a procurement item",
            description = "Updates an existing procurement item")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Procurement item updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Procurement item not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ProcurementItemDTO> updateProcurementItem(
            @PathVariable Long rcId,
            @PathVariable Long fyId,
            @PathVariable Long procurementItemId,
            @RequestBody ProcurementItemDTO request,
            Authentication authentication) {
        String username = getUsername(authentication);
        logger.info("PUT /responsibility-centres/" + rcId + "/fiscal-years/" + fyId +
                "/procurement-items/" + procurementItemId + " - Updating procurement item for user: " + username);

        try {
            ProcurementItemDTO updated = procurementItemService.updateProcurementItem(procurementItemId, request, username);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            logger.warning("Failed to update procurement item: " + e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.severe("Failed to update procurement item: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update the status of a procurement item.
     *
     * @param rcId the responsibility centre ID
     * @param fyId the fiscal year ID
     * @param procurementItemId the procurement item ID
     * @param request the status update request
     * @param authentication the authentication principal
     * @return the updated procurement item
     */
    @PutMapping("/{procurementItemId}/status")
    @Audited(action = "UPDATE_PROCUREMENT_STATUS", entityType = "PROCUREMENT_ITEM")
    @Operation(summary = "Update procurement item status",
            description = "Updates the status of a procurement item")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Procurement item not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ProcurementItemDTO> updateProcurementItemStatus(
            @PathVariable Long rcId,
            @PathVariable Long fyId,
            @PathVariable Long procurementItemId,
            @RequestBody StatusUpdateRequest request,
            Authentication authentication) {
        String username = getUsername(authentication);
        logger.info("PUT /responsibility-centres/" + rcId + "/fiscal-years/" + fyId +
                "/procurement-items/" + procurementItemId + "/status - Updating status for user: " + username);

        try {
            ProcurementItemDTO updated = procurementItemService.updateProcurementItemStatus(procurementItemId, request.status(), username);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            logger.warning("Failed to update status: " + e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.severe("Failed to update status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete a procurement item.
     *
     * @param rcId the responsibility centre ID
     * @param fyId the fiscal year ID
     * @param procurementItemId the procurement item ID
     * @param authentication the authentication principal
     * @return no content response
     */
    @DeleteMapping("/{procurementItemId}")
    @Audited(action = "DELETE_PROCUREMENT_ITEM", entityType = "PROCUREMENT_ITEM")
    @Operation(summary = "Delete a procurement item",
            description = "Deletes a procurement item (soft delete)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Procurement item deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Procurement item not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteProcurementItem(
            @PathVariable Long rcId,
            @PathVariable Long fyId,
            @PathVariable Long procurementItemId,
            Authentication authentication) {
        String username = getUsername(authentication);
        logger.info("DELETE /responsibility-centres/" + rcId + "/fiscal-years/" + fyId +
                "/procurement-items/" + procurementItemId + " - Deleting procurement item for user: " + username);

        try {
            procurementItemService.deleteProcurementItem(procurementItemId, username);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.warning("Failed to delete procurement item: " + e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            logger.severe("Failed to delete procurement item: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Toggle creating a spending item from this procurement item.
     * If no spending item is linked, creates a new one.
     * If a spending item is linked and has not been modified, removes the link.
     * Returns a warning if the spending item has been modified after creation.
     *
     * @param rcId the responsibility centre ID
     * @param fyId the fiscal year ID
     * @param procurementItemId the procurement item ID
     * @param request the toggle request
     * @param authentication the authentication principal
     * @return the updated procurement item or warning response
     */
    @PostMapping("/{procurementItemId}/toggle-spending-link")
    @Audited(action = "TOGGLE_SPENDING_LINK", entityType = "PROCUREMENT_ITEM")
    @Operation(summary = "Toggle spending item link",
            description = "Creates or removes a spending item linked to this procurement item")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Spending link toggled successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request - spending item has been modified"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Procurement item not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> toggleSpendingLink(
            @PathVariable Long rcId,
            @PathVariable Long fyId,
            @PathVariable Long procurementItemId,
            @RequestBody(required = false) ToggleSpendingLinkRequest request,
            Authentication authentication) {
        String username = getUsername(authentication);
        logger.info("POST /responsibility-centres/" + rcId + "/fiscal-years/" + fyId +
                "/procurement-items/" + procurementItemId + "/toggle-spending-link - Toggling spending link for user: " + username);

        try {
            boolean forceUnlink = request != null && request.forceUnlink() != null && request.forceUnlink();
            ProcurementItemService.ToggleSpendingLinkResult result = 
                    procurementItemService.toggleSpendingLink(procurementItemId, username, forceUnlink);
            
            ToggleSpendingLinkResponse response = new ToggleSpendingLinkResponse(
                    result.procurementItem(),
                    result.spendingLinked(),
                    result.hasWarning(),
                    result.warningMessage());
            
            if (response.hasWarning() && !forceUnlink) {
                // Return warning to ask for confirmation
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warning("Failed to toggle spending link: " + e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.severe("Failed to toggle spending link: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An unexpected error occurred"));
        }
    }

    // ==========================
    // Quote Endpoints
    // ==========================

    /**
     * Get all quotes for a procurement item.
     *
     * @param rcId the responsibility centre ID
     * @param fyId the fiscal year ID
     * @param procurementItemId the procurement item ID
     * @param authentication the authentication principal
     * @return list of quotes
     */
    @GetMapping("/{procurementItemId}/quotes")
    @Operation(summary = "Get all quotes for a procurement item",
            description = "Retrieves all quotes for a procurement item")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quotes retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Procurement item not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<ProcurementQuoteDTO>> getQuotes(
            @PathVariable Long rcId,
            @PathVariable Long fyId,
            @PathVariable Long procurementItemId,
            Authentication authentication) {
        String username = getUsername(authentication);
        logger.info("GET quotes for procurement item " + procurementItemId + " by user: " + username);

        try {
            List<ProcurementQuoteDTO> quotes = procurementItemService.getQuotesByProcurementItemId(procurementItemId, username);
            return ResponseEntity.ok(quotes);
        } catch (IllegalArgumentException e) {
            logger.warning("Failed to get quotes: " + e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.severe("Failed to get quotes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get a specific quote by ID.
     *
     * @param rcId the responsibility centre ID
     * @param fyId the fiscal year ID
     * @param procurementItemId the procurement item ID
     * @param quoteId the quote ID
     * @param includeFiles whether to include files
     * @param authentication the authentication principal
     * @return the quote
     */
    @GetMapping("/{procurementItemId}/quotes/{quoteId}")
    @Operation(summary = "Get a specific quote",
            description = "Retrieves a specific quote by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quote retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Quote not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ProcurementQuoteDTO> getQuote(
            @PathVariable Long rcId,
            @PathVariable Long fyId,
            @PathVariable Long procurementItemId,
            @PathVariable Long quoteId,
            @RequestParam(required = false, defaultValue = "false") boolean includeFiles,
            Authentication authentication) {
        String username = getUsername(authentication);
        logger.info("GET quote " + quoteId + " by user: " + username);

        try {
            Optional<ProcurementQuoteDTO> quoteOpt = includeFiles
                    ? procurementItemService.getQuoteWithFiles(quoteId, username)
                    : procurementItemService.getQuoteById(quoteId, username);
            return quoteOpt.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.severe("Failed to get quote: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Create a new quote for a procurement item.
     *
     * @param rcId the responsibility centre ID
     * @param fyId the fiscal year ID
     * @param procurementItemId the procurement item ID
     * @param request the quote creation request
     * @param authentication the authentication principal
     * @return the created quote
     */
    @PostMapping("/{procurementItemId}/quotes")
    @Audited(action = "CREATE_PROCUREMENT_QUOTE", entityType = "PROCUREMENT_QUOTE")
    @Operation(summary = "Create a new quote",
            description = "Creates a new quote for a procurement item")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Quote created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Procurement item not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ProcurementQuoteDTO> createQuote(
            @PathVariable Long rcId,
            @PathVariable Long fyId,
            @PathVariable Long procurementItemId,
            @RequestBody ProcurementQuoteDTO request,
            Authentication authentication) {
        String username = getUsername(authentication);
        logger.info("POST quote for procurement item " + procurementItemId + " by user: " + username);

        try {
            ProcurementQuoteDTO created = procurementItemService.createQuote(procurementItemId, request, username);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            logger.warning("Failed to create quote: " + e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.severe("Failed to create quote: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update an existing quote.
     *
     * @param rcId the responsibility centre ID
     * @param fyId the fiscal year ID
     * @param procurementItemId the procurement item ID
     * @param quoteId the quote ID
     * @param request the update request
     * @param authentication the authentication principal
     * @return the updated quote
     */
    @PutMapping("/{procurementItemId}/quotes/{quoteId}")
    @Audited(action = "UPDATE_PROCUREMENT_QUOTE", entityType = "PROCUREMENT_QUOTE")
    @Operation(summary = "Update a quote",
            description = "Updates an existing quote")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quote updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Quote not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ProcurementQuoteDTO> updateQuote(
            @PathVariable Long rcId,
            @PathVariable Long fyId,
            @PathVariable Long procurementItemId,
            @PathVariable Long quoteId,
            @RequestBody ProcurementQuoteDTO request,
            Authentication authentication) {
        String username = getUsername(authentication);
        logger.info("PUT quote " + quoteId + " by user: " + username);

        try {
            ProcurementQuoteDTO updated = procurementItemService.updateQuote(quoteId, request, username);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            logger.warning("Failed to update quote: " + e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.severe("Failed to update quote: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete a quote.
     *
     * @param rcId the responsibility centre ID
     * @param fyId the fiscal year ID
     * @param procurementItemId the procurement item ID
     * @param quoteId the quote ID
     * @param authentication the authentication principal
     * @return no content response
     */
    @DeleteMapping("/{procurementItemId}/quotes/{quoteId}")
    @Audited(action = "DELETE_PROCUREMENT_QUOTE", entityType = "PROCUREMENT_QUOTE")
    @Operation(summary = "Delete a quote",
            description = "Deletes a quote (soft delete)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Quote deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Quote not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteQuote(
            @PathVariable Long rcId,
            @PathVariable Long fyId,
            @PathVariable Long procurementItemId,
            @PathVariable Long quoteId,
            Authentication authentication) {
        String username = getUsername(authentication);
        logger.info("DELETE quote " + quoteId + " by user: " + username);

        try {
            procurementItemService.deleteQuote(quoteId, username);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.warning("Failed to delete quote: " + e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            logger.severe("Failed to delete quote: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Select a quote for the procurement item.
     *
     * @param rcId the responsibility centre ID
     * @param fyId the fiscal year ID
     * @param procurementItemId the procurement item ID
     * @param quoteId the quote ID
     * @param authentication the authentication principal
     * @return the updated quote
     */
    @PostMapping("/{procurementItemId}/quotes/{quoteId}/select")
    @Audited(action = "SELECT_PROCUREMENT_QUOTE", entityType = "PROCUREMENT_QUOTE")
    @Operation(summary = "Select a quote",
            description = "Selects a quote for the procurement item")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quote selected successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Quote not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ProcurementQuoteDTO> selectQuote(
            @PathVariable Long rcId,
            @PathVariable Long fyId,
            @PathVariable Long procurementItemId,
            @PathVariable Long quoteId,
            Authentication authentication) {
        String username = getUsername(authentication);
        logger.info("POST select quote " + quoteId + " by user: " + username);

        try {
            ProcurementQuoteDTO selected = procurementItemService.selectQuote(quoteId, username);
            return ResponseEntity.ok(selected);
        } catch (IllegalArgumentException e) {
            logger.warning("Failed to select quote: " + e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            logger.severe("Failed to select quote: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==========================
    // File Endpoints
    // ==========================

    /**
     * Get all files for a quote.
     *
     * @param rcId the responsibility centre ID
     * @param fyId the fiscal year ID
     * @param procurementItemId the procurement item ID
     * @param quoteId the quote ID
     * @param authentication the authentication principal
     * @return list of file metadata
     */
    @GetMapping("/{procurementItemId}/quotes/{quoteId}/files")
    @Operation(summary = "Get all files for a quote",
            description = "Retrieves all file metadata for a quote")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Files retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Quote not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<ProcurementQuoteFileDTO>> getFiles(
            @PathVariable Long rcId,
            @PathVariable Long fyId,
            @PathVariable Long procurementItemId,
            @PathVariable Long quoteId,
            Authentication authentication) {
        String username = getUsername(authentication);
        logger.info("GET files for quote " + quoteId + " by user: " + username);

        try {
            List<ProcurementQuoteFileDTO> files = procurementItemService.getFilesByQuoteId(quoteId, username);
            return ResponseEntity.ok(files);
        } catch (IllegalArgumentException e) {
            logger.warning("Failed to get files: " + e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.severe("Failed to get files: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Download a file.
     *
     * @param rcId the responsibility centre ID
     * @param fyId the fiscal year ID
     * @param procurementItemId the procurement item ID
     * @param quoteId the quote ID
     * @param fileId the file ID
     * @param authentication the authentication principal
     * @return the file content
     */
    @GetMapping("/{procurementItemId}/quotes/{quoteId}/files/{fileId}/download")
    @Operation(summary = "Download a file",
            description = "Downloads a file attachment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File downloaded successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "File not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long rcId,
            @PathVariable Long fyId,
            @PathVariable Long procurementItemId,
            @PathVariable Long quoteId,
            @PathVariable Long fileId,
            Authentication authentication) {
        String username = getUsername(authentication);
        logger.info("GET download file " + fileId + " by user: " + username);

        try {
            Optional<ProcurementQuoteFileDTO> metadataOpt = procurementItemService.getFileMetadataById(fileId, username);
            if (metadataOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            ProcurementQuoteFileDTO metadata = metadataOpt.get();
            byte[] content = procurementItemService.getFileContent(fileId, username);

            ByteArrayResource resource = new ByteArrayResource(content);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(metadata.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + metadata.getFileName() + "\"")
                    .contentLength(content.length)
                    .body(resource);
        } catch (IllegalArgumentException e) {
            logger.warning("Failed to download file: " + e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.severe("Failed to download file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * View a file inline (for images and PDFs).
     *
     * @param rcId the responsibility centre ID
     * @param fyId the fiscal year ID
     * @param procurementItemId the procurement item ID
     * @param quoteId the quote ID
     * @param fileId the file ID
     * @param authentication the authentication principal
     * @return the file content for inline viewing
     */
    @GetMapping("/{procurementItemId}/quotes/{quoteId}/files/{fileId}/view")
    @Operation(summary = "View a file inline",
            description = "Views a file inline in the browser (for images and PDFs)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "File not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Resource> viewFile(
            @PathVariable Long rcId,
            @PathVariable Long fyId,
            @PathVariable Long procurementItemId,
            @PathVariable Long quoteId,
            @PathVariable Long fileId,
            Authentication authentication) {
        String username = getUsername(authentication);
        logger.info("GET view file " + fileId + " by user: " + username);

        try {
            Optional<ProcurementQuoteFileDTO> metadataOpt = procurementItemService.getFileMetadataById(fileId, username);
            if (metadataOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            ProcurementQuoteFileDTO metadata = metadataOpt.get();
            byte[] content = procurementItemService.getFileContent(fileId, username);

            ByteArrayResource resource = new ByteArrayResource(content);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(metadata.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + metadata.getFileName() + "\"")
                    .contentLength(content.length)
                    .body(resource);
        } catch (IllegalArgumentException e) {
            logger.warning("Failed to view file: " + e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.severe("Failed to view file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Upload a file to a quote.
     *
     * @param rcId the responsibility centre ID
     * @param fyId the fiscal year ID
     * @param procurementItemId the procurement item ID
     * @param quoteId the quote ID
     * @param file the file to upload
     * @param description optional file description
     * @param authentication the authentication principal
     * @return the created file metadata
     */
    @PostMapping("/{procurementItemId}/quotes/{quoteId}/files")
    @Audited(action = "UPLOAD_QUOTE_FILE", entityType = "PROCUREMENT_QUOTE")
    @Operation(summary = "Upload a file",
            description = "Uploads a file to a quote")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "File uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Quote not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ProcurementQuoteFileDTO> uploadFile(
            @PathVariable Long rcId,
            @PathVariable Long fyId,
            @PathVariable Long procurementItemId,
            @PathVariable Long quoteId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            Authentication authentication) {
        String username = getUsername(authentication);
        logger.info("POST upload file to quote " + quoteId + " by user: " + username);

        try {
            ProcurementQuoteFileDTO uploaded = procurementItemService.uploadFile(quoteId, file, description, username);
            return ResponseEntity.status(HttpStatus.CREATED).body(uploaded);
        } catch (IllegalArgumentException e) {
            logger.warning("Failed to upload file: " + e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.severe("Failed to upload file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete a file.
     *
     * @param rcId the responsibility centre ID
     * @param fyId the fiscal year ID
     * @param procurementItemId the procurement item ID
     * @param quoteId the quote ID
     * @param fileId the file ID
     * @param authentication the authentication principal
     * @return no content response
     */
    @DeleteMapping("/{procurementItemId}/quotes/{quoteId}/files/{fileId}")
    @Audited(action = "DELETE_QUOTE_FILE", entityType = "PROCUREMENT_QUOTE")
    @Operation(summary = "Delete a file",
            description = "Deletes a file (soft delete)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "File deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "File not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteFile(
            @PathVariable Long rcId,
            @PathVariable Long fyId,
            @PathVariable Long procurementItemId,
            @PathVariable Long quoteId,
            @PathVariable Long fileId,
            Authentication authentication) {
        String username = getUsername(authentication);
        logger.info("DELETE file " + fileId + " by user: " + username);

        try {
            procurementItemService.deleteFile(fileId, username);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.warning("Failed to delete file: " + e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            logger.severe("Failed to delete file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Replace a file.
     *
     * @param rcId the responsibility centre ID
     * @param fyId the fiscal year ID
     * @param procurementItemId the procurement item ID
     * @param quoteId the quote ID
     * @param fileId the file ID to replace
     * @param file the new file
     * @param description optional file description
     * @param authentication the authentication principal
     * @return the updated file DTO
     */
    @PutMapping("/{procurementItemId}/quotes/{quoteId}/files/{fileId}")
    @Audited(action = "REPLACE_QUOTE_FILE", entityType = "PROCUREMENT_QUOTE")
    @Operation(summary = "Replace a file",
            description = "Replaces an existing file with a new one")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File replaced successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "File not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ProcurementQuoteFileDTO> replaceFile(
            @PathVariable Long rcId,
            @PathVariable Long fyId,
            @PathVariable Long procurementItemId,
            @PathVariable Long quoteId,
            @PathVariable Long fileId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            Authentication authentication) {
        String username = getUsername(authentication);
        logger.info("PUT replace file " + fileId + " on quote " + quoteId + " by user: " + username);

        try {
            ProcurementQuoteFileDTO replaced = procurementItemService.replaceFile(fileId, file, description, username);
            return ResponseEntity.ok(replaced);
        } catch (IllegalArgumentException e) {
            logger.warning("Failed to replace file: " + e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.severe("Failed to replace file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==========================
    // Helper Methods & Records
    // ==========================

    /**
     * Get the username from the authentication principal.
     *
     * @param authentication the authentication object
     * @return the username
     */
    private String getUsername(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalArgumentException("User not authenticated");
        }
        return authentication.getName();
    }

    /**
     * Record for status update requests.
     */
    public record StatusUpdateRequest(String status) {}

    /**
     * Record for toggle spending link requests.
     */
    public record ToggleSpendingLinkRequest(Boolean forceUnlink) {}

    /**
     * Response for toggle spending link operation.
     */
    public record ToggleSpendingLinkResponse(
            ProcurementItemDTO procurementItem,
            boolean spendingLinked,
            boolean hasWarning,
            String warningMessage) {
        
        public static ToggleSpendingLinkResponse success(ProcurementItemDTO item, boolean linked) {
            return new ToggleSpendingLinkResponse(item, linked, false, null);
        }
        
        public static ToggleSpendingLinkResponse warning(ProcurementItemDTO item, String message) {
            return new ToggleSpendingLinkResponse(item, true, true, message);
        }
    }
}
