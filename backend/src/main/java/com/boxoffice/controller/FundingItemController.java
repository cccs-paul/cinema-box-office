/*
 * myRC - Funding Item REST Controller
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-23
 * Version: 1.0.0
 *
 * Description:
 * REST Controller for Funding Item management.
 */
package com.boxoffice.controller;

import com.boxoffice.dto.ErrorResponse;
import com.boxoffice.dto.FundingItemDTO;
import com.boxoffice.dto.MoneyAllocationDTO;
import com.boxoffice.service.FundingItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Funding Item management.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-23
 */
@RestController
@RequestMapping("/fiscal-years/{fyId}/funding-items")
@Tag(name = "Funding Item Management", description = "APIs for managing funding items within fiscal years")
public class FundingItemController {

  private static final Logger logger = Logger.getLogger(FundingItemController.class.getName());
  private final FundingItemService fundingItemService;

  public FundingItemController(FundingItemService fundingItemService) {
    this.fundingItemService = fundingItemService;
  }

  /**
   * Get all funding items for a fiscal year.
   *
   * @param fyId the fiscal year ID
   * @param authentication the authentication principal
   * @return list of funding items
   */
  @GetMapping
  @Operation(summary = "Get all funding items for a fiscal year",
      description = "Retrieves all funding items associated with a fiscal year")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Funding items retrieved successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Access denied to this fiscal year"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<List<FundingItemDTO>> getFundingItems(
      @PathVariable Long fyId,
      Authentication authentication) {
    // Use default user for unauthenticated access (development mode)
    String username = "default-user";
    if (authentication != null && authentication.getName() != null && !authentication.getName().isEmpty()) {
      username = authentication.getName();
    }
    logger.info("GET /fiscal-years/" + fyId + "/funding-items - Fetching funding items for user: " + username);
    
    try {
      List<FundingItemDTO> fundingItems = fundingItemService.getFundingItemsByFiscalYearId(fyId, username);
      return ResponseEntity.ok(fundingItems);
    } catch (IllegalArgumentException e) {
      logger.warning("Access denied for funding items: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    } catch (Exception e) {
      logger.severe("Failed to fetch funding items: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Get a specific funding item by ID.
   *
   * @param fyId the fiscal year ID
   * @param fiId the funding item ID
   * @param authentication the authentication principal
   * @return the funding item
   */
  @GetMapping("/{fiId}")
  @Operation(summary = "Get a specific funding item",
      description = "Retrieves a specific funding item by ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Funding item retrieved successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "404", description = "Funding item not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<FundingItemDTO> getFundingItem(
      @PathVariable Long fyId,
      @PathVariable Long fiId,
      Authentication authentication) {
    // Use default user for unauthenticated access (development mode)
    String username = "default-user";
    if (authentication != null && authentication.getName() != null && !authentication.getName().isEmpty()) {
      username = authentication.getName();
    }
    logger.info("GET /fiscal-years/" + fyId + "/funding-items/" + fiId + " - Fetching funding item for user: " + username);
    
    try {
      Optional<FundingItemDTO> fiOpt = fundingItemService.getFundingItemById(fiId, username);
      return fiOpt.map(ResponseEntity::ok)
          .orElseGet(() -> ResponseEntity.notFound().build());
    } catch (Exception e) {
      logger.severe("Failed to fetch funding item: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Create a new funding item for a fiscal year.
   *
   * @param fyId the fiscal year ID
   * @param authentication the authentication principal
   * @param request the funding item creation request
   * @return the created funding item
   */
  @PostMapping
  @Operation(summary = "Create a new funding item",
      description = "Creates a new funding item for a fiscal year")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Funding item created successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request data"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Access denied"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<?> createFundingItem(
      @PathVariable Long fyId,
      Authentication authentication,
      @RequestBody FundingItemCreateRequest request) {
    // Use default user for unauthenticated access (development mode)
    String username = "default-user";
    if (authentication != null && authentication.getName() != null && !authentication.getName().isEmpty()) {
      username = authentication.getName();
    }
    logger.info("POST /fiscal-years/" + fyId + "/funding-items - Creating funding item: " + request.getName() + " for user: " + username);
    
    try {
      if (request.getName() == null || request.getName().trim().isEmpty()) {
        logger.warning("Funding item creation failed: Name is required");
        return ResponseEntity.badRequest().body(new ErrorResponse("Funding item name is required"));
      }

      FundingItemDTO createdFI = fundingItemService.createFundingItem(
          fyId,
          username,
          request.getName(),
          request.getDescription() != null ? request.getDescription() : "",
          request.getSource(),
          request.getComments(),
          request.getCurrency(),
          request.getExchangeRate(),
          request.getCategoryId(),
          request.getMoneyAllocations()
      );
      return ResponseEntity.status(HttpStatus.CREATED).body(createdFI);
    } catch (IllegalArgumentException e) {
      logger.warning("Funding item creation failed: " + e.getMessage());
      return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    } catch (Exception e) {
      logger.severe("Funding item creation failed: " + e.getMessage());
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("An unexpected error occurred"));
    }
  }

  /**
   * Update an existing funding item.
   *
   * @param fyId the fiscal year ID
   * @param fiId the funding item ID
   * @param authentication the authentication principal
   * @param request the update request
   * @return the updated funding item
   */
  @PutMapping("/{fiId}")
  @Operation(summary = "Update a funding item",
      description = "Updates an existing funding item")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Funding item updated successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request data"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "404", description = "Funding item not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<?> updateFundingItem(
      @PathVariable Long fyId,
      @PathVariable Long fiId,
      Authentication authentication,
      @RequestBody FundingItemCreateRequest request) {
    // Use default user for unauthenticated access (development mode)
    String username = "default-user";
    if (authentication != null && authentication.getName() != null && !authentication.getName().isEmpty()) {
      username = authentication.getName();
    }
    logger.info("PUT /fiscal-years/" + fyId + "/funding-items/" + fiId + " - Updating funding item for user: " + username);
    
    try {
      Optional<FundingItemDTO> updatedFI = fundingItemService.updateFundingItem(
          fiId,
          username,
          request.getName(),
          request.getDescription(),
          request.getSource(),
          request.getComments(),
          request.getCurrency(),
          request.getExchangeRate(),
          request.getCategoryId(),
          request.getMoneyAllocations()
      );
      return updatedFI.map(ResponseEntity::ok)
          .orElseGet(() -> ResponseEntity.notFound().build());
    } catch (IllegalArgumentException e) {
      logger.warning("Funding item update failed: " + e.getMessage());
      return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    } catch (Exception e) {
      logger.severe("Funding item update failed: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("An unexpected error occurred"));
    }
  }

  /**
   * Delete a funding item.
   *
   * @param fyId the fiscal year ID
   * @param fiId the funding item ID
   * @param authentication the authentication principal
   * @return no content
   */
  @DeleteMapping("/{fiId}")
  @Operation(summary = "Delete a funding item",
      description = "Deletes a funding item")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Funding item deleted successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "404", description = "Funding item not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<Void> deleteFundingItem(
      @PathVariable Long fyId,
      @PathVariable Long fiId,
      Authentication authentication) {
    // Use default user for unauthenticated access (development mode)
    String username = "default-user";
    if (authentication != null && authentication.getName() != null && !authentication.getName().isEmpty()) {
      username = authentication.getName();
    }
    logger.info("DELETE /fiscal-years/" + fyId + "/funding-items/" + fiId + " - Deleting funding item for user: " + username);
    
    try {
      fundingItemService.deleteFundingItem(fiId, username);
      return ResponseEntity.noContent().build();
    } catch (IllegalArgumentException e) {
      logger.warning("Funding item deletion failed: " + e.getMessage());
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      logger.severe("Funding item deletion failed: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  // Request DTOs
  public static class FundingItemCreateRequest {
    private String name;
    private String description;
    private String source;
    private String comments;
    private String currency;
    private BigDecimal exchangeRate;
    private Long categoryId;
    private List<MoneyAllocationDTO> moneyAllocations;

    public FundingItemCreateRequest() {}

    public FundingItemCreateRequest(String name, String description,
        String source, String comments, String currency, BigDecimal exchangeRate, Long categoryId,
        List<MoneyAllocationDTO> moneyAllocations) {
      this.name = name;
      this.description = description;
      this.source = source;
      this.comments = comments;
      this.currency = currency;
      this.exchangeRate = exchangeRate;
      this.categoryId = categoryId;
      this.moneyAllocations = moneyAllocations;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getDescription() {
      return description;
    }

    public void setDescription(String description) {
      this.description = description;
    }

    public String getSource() {
      return source;
    }

    public void setSource(String source) {
      this.source = source;
    }

    public String getComments() {
      return comments;
    }

    public void setComments(String comments) {
      this.comments = comments;
    }

    public String getCurrency() {
      return currency;
    }

    public void setCurrency(String currency) {
      this.currency = currency;
    }

    public BigDecimal getExchangeRate() {
      return exchangeRate;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
      this.exchangeRate = exchangeRate;
    }

    public Long getCategoryId() {
      return categoryId;
    }

    public void setCategoryId(Long categoryId) {
      this.categoryId = categoryId;
    }

    public List<MoneyAllocationDTO> getMoneyAllocations() {
      return moneyAllocations;
    }

    public void setMoneyAllocations(List<MoneyAllocationDTO> moneyAllocations) {
      this.moneyAllocations = moneyAllocations;
    }
  }
}
