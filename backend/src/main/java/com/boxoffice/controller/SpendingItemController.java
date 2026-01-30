/*
 * myRC - Spending Item REST Controller
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-26
 * Version: 1.0.0
 *
 * Description:
 * REST Controller for Spending Item management.
 */
package com.boxoffice.controller;

import com.boxoffice.dto.ErrorResponse;
import com.boxoffice.dto.SpendingItemDTO;
import com.boxoffice.dto.SpendingMoneyAllocationDTO;
import com.boxoffice.service.SpendingItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Spending Item management.
 * Spending items represent individual expenditures within a fiscal year.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-26
 */
@RestController
@RequestMapping("/responsibility-centres/{rcId}/fiscal-years/{fyId}/spending-items")
@Tag(name = "Spending Item Management", description = "APIs for managing spending items within fiscal years")
public class SpendingItemController {

  private static final Logger logger = Logger.getLogger(SpendingItemController.class.getName());
  private final SpendingItemService spendingItemService;

  public SpendingItemController(SpendingItemService spendingItemService) {
    this.spendingItemService = spendingItemService;
  }

  /**
   * Get all spending items for a fiscal year.
   *
   * @param rcId the responsibility centre ID
   * @param fyId the fiscal year ID
   * @param categoryId optional category ID to filter by
   * @param authentication the authentication principal
   * @return list of spending items
   */
  @GetMapping
  @Operation(summary = "Get all spending items for a fiscal year",
      description = "Retrieves all spending items for a fiscal year, optionally filtered by category.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Spending items retrieved successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Access denied to this RC"),
      @ApiResponse(responseCode = "404", description = "Fiscal year not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<List<SpendingItemDTO>> getSpendingItems(
      @PathVariable Long rcId,
      @PathVariable Long fyId,
      @RequestParam(required = false) Long categoryId,
      Authentication authentication) {
    String username = getUsername(authentication);
    logger.info("GET /responsibility-centres/" + rcId + "/fiscal-years/" + fyId + 
        "/spending-items - Fetching spending items for user: " + username);

    try {
      List<SpendingItemDTO> items;
      if (categoryId != null) {
        items = spendingItemService.getSpendingItemsByFiscalYearIdAndCategoryId(fyId, categoryId, username);
      } else {
        items = spendingItemService.getSpendingItemsByFiscalYearId(fyId, username);
      }
      return ResponseEntity.ok(items);
    } catch (IllegalArgumentException e) {
      logger.warning("Access denied for spending items: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    } catch (Exception e) {
      logger.severe("Failed to fetch spending items: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Get a specific spending item by ID.
   *
   * @param rcId the responsibility centre ID
   * @param fyId the fiscal year ID
   * @param spendingItemId the spending item ID
   * @param authentication the authentication principal
   * @return the spending item
   */
  @GetMapping("/{spendingItemId}")
  @Operation(summary = "Get a specific spending item",
      description = "Retrieves a specific spending item by ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Spending item retrieved successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "404", description = "Spending item not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<SpendingItemDTO> getSpendingItem(
      @PathVariable Long rcId,
      @PathVariable Long fyId,
      @PathVariable Long spendingItemId,
      Authentication authentication) {
    String username = getUsername(authentication);
    logger.info("GET /responsibility-centres/" + rcId + "/fiscal-years/" + fyId + 
        "/spending-items/" + spendingItemId + " - Fetching spending item for user: " + username);

    try {
      Optional<SpendingItemDTO> itemOpt = spendingItemService.getSpendingItemById(spendingItemId, username);
      return itemOpt.map(ResponseEntity::ok)
          .orElseGet(() -> ResponseEntity.notFound().build());
    } catch (Exception e) {
      logger.severe("Failed to fetch spending item: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Create a new spending item for a fiscal year.
   *
   * @param rcId the responsibility centre ID
   * @param fyId the fiscal year ID
   * @param authentication the authentication principal
   * @param request the spending item creation request
   * @return the created spending item
   */
  @PostMapping
  @Operation(summary = "Create a new spending item",
      description = "Creates a new spending item for a fiscal year")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Spending item created successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request data"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Access denied"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<?> createSpendingItem(
      @PathVariable Long rcId,
      @PathVariable Long fyId,
      Authentication authentication,
      @RequestBody SpendingItemDTO request) {
    String username = getUsername(authentication);
    logger.info("POST /responsibility-centres/" + rcId + "/fiscal-years/" + fyId + 
        "/spending-items - Creating spending item for user: " + username);

    try {
      // Set the fiscal year ID from path
      request.setFiscalYearId(fyId);
      SpendingItemDTO created = spendingItemService.createSpendingItem(request, username);
      return ResponseEntity.status(HttpStatus.CREATED).body(created);
    } catch (IllegalArgumentException e) {
      logger.warning("Failed to create spending item: " + e.getMessage());
      return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    } catch (Exception e) {
      logger.severe("Failed to create spending item: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ErrorResponse("Failed to create spending item"));
    }
  }

  /**
   * Update an existing spending item.
   *
   * @param rcId the responsibility centre ID
   * @param fyId the fiscal year ID
   * @param spendingItemId the spending item ID
   * @param authentication the authentication principal
   * @param request the spending item update request
   * @return the updated spending item
   */
  @PutMapping("/{spendingItemId}")
  @Operation(summary = "Update a spending item",
      description = "Updates an existing spending item")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Spending item updated successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request data"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Access denied"),
      @ApiResponse(responseCode = "404", description = "Spending item not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<?> updateSpendingItem(
      @PathVariable Long rcId,
      @PathVariable Long fyId,
      @PathVariable Long spendingItemId,
      Authentication authentication,
      @RequestBody SpendingItemDTO request) {
    String username = getUsername(authentication);
    logger.info("PUT /responsibility-centres/" + rcId + "/fiscal-years/" + fyId + 
        "/spending-items/" + spendingItemId + " - Updating spending item for user: " + username);

    try {
      SpendingItemDTO updated = spendingItemService.updateSpendingItem(spendingItemId, request, username);
      return ResponseEntity.ok(updated);
    } catch (IllegalArgumentException e) {
      logger.warning("Failed to update spending item: " + e.getMessage());
      if (e.getMessage().contains("not found")) {
        return ResponseEntity.notFound().build();
      }
      return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    } catch (Exception e) {
      logger.severe("Failed to update spending item: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ErrorResponse("Failed to update spending item"));
    }
  }

  /**
   * Delete a spending item.
   *
   * @param rcId the responsibility centre ID
   * @param fyId the fiscal year ID
   * @param spendingItemId the spending item ID
   * @param authentication the authentication principal
   * @return no content on success
   */
  @DeleteMapping("/{spendingItemId}")
  @Operation(summary = "Delete a spending item",
      description = "Deletes a spending item")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Spending item deleted successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Access denied"),
      @ApiResponse(responseCode = "404", description = "Spending item not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<?> deleteSpendingItem(
      @PathVariable Long rcId,
      @PathVariable Long fyId,
      @PathVariable Long spendingItemId,
      Authentication authentication) {
    String username = getUsername(authentication);
    logger.info("DELETE /responsibility-centres/" + rcId + "/fiscal-years/" + fyId + 
        "/spending-items/" + spendingItemId + " - Deleting spending item for user: " + username);

    try {
      spendingItemService.deleteSpendingItem(spendingItemId, username);
      return ResponseEntity.noContent().build();
    } catch (IllegalArgumentException e) {
      logger.warning("Failed to delete spending item: " + e.getMessage());
      if (e.getMessage().contains("not found")) {
        return ResponseEntity.notFound().build();
      }
      return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    } catch (Exception e) {
      logger.severe("Failed to delete spending item: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ErrorResponse("Failed to delete spending item"));
    }
  }

  /**
   * Update the status of a spending item.
   *
   * @param rcId the responsibility centre ID
   * @param fyId the fiscal year ID
   * @param spendingItemId the spending item ID
   * @param authentication the authentication principal
   * @param request the status update request
   * @return the updated spending item
   */
  @PutMapping("/{spendingItemId}/status")
  @Operation(summary = "Update spending item status",
      description = "Updates the status of a spending item")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Status updated successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid status"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Access denied"),
      @ApiResponse(responseCode = "404", description = "Spending item not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<?> updateStatus(
      @PathVariable Long rcId,
      @PathVariable Long fyId,
      @PathVariable Long spendingItemId,
      Authentication authentication,
      @RequestBody StatusUpdateRequest request) {
    String username = getUsername(authentication);
    logger.info("PUT /responsibility-centres/" + rcId + "/fiscal-years/" + fyId + 
        "/spending-items/" + spendingItemId + "/status - Updating status for user: " + username);

    try {
      SpendingItemDTO updated = spendingItemService.updateSpendingItemStatus(spendingItemId, request.status, username);
      return ResponseEntity.ok(updated);
    } catch (IllegalArgumentException e) {
      logger.warning("Failed to update status: " + e.getMessage());
      if (e.getMessage().contains("not found")) {
        return ResponseEntity.notFound().build();
      }
      return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    } catch (Exception e) {
      logger.severe("Failed to update status: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ErrorResponse("Failed to update status"));
    }
  }

  /**
   * Get money allocations for a spending item.
   *
   * @param rcId the responsibility centre ID
   * @param fyId the fiscal year ID
   * @param spendingItemId the spending item ID
   * @param authentication the authentication principal
   * @return list of money allocations
   */
  @GetMapping("/{spendingItemId}/allocations")
  @Operation(summary = "Get money allocations",
      description = "Retrieves money allocations for a spending item")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Allocations retrieved successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "404", description = "Spending item not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<?> getAllocations(
      @PathVariable Long rcId,
      @PathVariable Long fyId,
      @PathVariable Long spendingItemId,
      Authentication authentication) {
    String username = getUsername(authentication);
    logger.info("GET /responsibility-centres/" + rcId + "/fiscal-years/" + fyId + 
        "/spending-items/" + spendingItemId + "/allocations - Fetching allocations for user: " + username);

    try {
      List<SpendingMoneyAllocationDTO> allocations = spendingItemService.getMoneyAllocations(spendingItemId, username);
      return ResponseEntity.ok(allocations);
    } catch (IllegalArgumentException e) {
      logger.warning("Failed to fetch allocations: " + e.getMessage());
      if (e.getMessage().contains("not found")) {
        return ResponseEntity.notFound().build();
      }
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(e.getMessage()));
    } catch (Exception e) {
      logger.severe("Failed to fetch allocations: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ErrorResponse("Failed to fetch allocations"));
    }
  }

  /**
   * Update money allocations for a spending item.
   *
   * @param rcId the responsibility centre ID
   * @param fyId the fiscal year ID
   * @param spendingItemId the spending item ID
   * @param authentication the authentication principal
   * @param allocations the new allocations
   * @return the updated spending item
   */
  @PutMapping("/{spendingItemId}/allocations")
  @Operation(summary = "Update money allocations",
      description = "Updates money allocations for a spending item")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Allocations updated successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid allocation data"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Access denied"),
      @ApiResponse(responseCode = "404", description = "Spending item not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<?> updateAllocations(
      @PathVariable Long rcId,
      @PathVariable Long fyId,
      @PathVariable Long spendingItemId,
      Authentication authentication,
      @RequestBody List<SpendingMoneyAllocationDTO> allocations) {
    String username = getUsername(authentication);
    logger.info("PUT /responsibility-centres/" + rcId + "/fiscal-years/" + fyId + 
        "/spending-items/" + spendingItemId + "/allocations - Updating allocations for user: " + username);

    try {
      SpendingItemDTO updated = spendingItemService.updateMoneyAllocations(spendingItemId, allocations, username);
      return ResponseEntity.ok(updated);
    } catch (IllegalArgumentException e) {
      logger.warning("Failed to update allocations: " + e.getMessage());
      if (e.getMessage().contains("not found")) {
        return ResponseEntity.notFound().build();
      }
      return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    } catch (Exception e) {
      logger.severe("Failed to update allocations: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ErrorResponse("Failed to update allocations"));
    }
  }

  /**
   * Extract username from authentication.
   */
  private String getUsername(Authentication authentication) {
    if (authentication != null && authentication.getName() != null) {
      return authentication.getName();
    }
    return "anonymous";
  }

  // Request DTOs
  public static class StatusUpdateRequest {
    public String status;
  }
}
